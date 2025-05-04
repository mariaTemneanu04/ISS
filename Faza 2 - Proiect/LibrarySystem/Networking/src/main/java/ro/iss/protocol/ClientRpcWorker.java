package ro.iss.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.domain.Book;
import ro.iss.domain.Loan;
import ro.iss.domain.Reader;
import ro.iss.domain.User;
import ro.iss.protocol.request.Request;
import ro.iss.protocol.request.RequestType;
import ro.iss.protocol.response.Response;
import ro.iss.protocol.response.ResponseType;
import ro.iss.services.IObserver;
import ro.iss.services.IService;
import ro.iss.services.MyException;
import ro.iss.utils.FilterBooksDTO;
import ro.iss.utils.FilterLoansDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;

public class ClientRpcWorker implements Runnable, IObserver {

    private IService server;
    private Socket connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    private static Logger logger = LogManager.getLogger(ClientRpcWorker.class);

    public ClientRpcWorker(IService server, Socket connection) {
        logger.info("Creating worker");
        this.server = server;
        this.connection = connection;

        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();

            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
            logger.info("Worker created");

        } catch (IOException e) {
            logger.error(e);
            logger.error(e.getStackTrace());
        }
    }

    @Override
    public void run() {
        while(connected){
            try {
                Object request=input.readObject();
                Response response = handleRequest((Request)request);
                if (response!=null){
                    sendResponse(response);
                }

            } catch (IOException | ClassNotFoundException e) {
                logger.error("Error in worker (reading): "+e);
                logger.error(e.getStackTrace());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("Error in worker (sleeping): "+e);
                logger.error(e.getStackTrace());
            }
        }
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            logger.error("Error in worker (closing connection): "+e);
        }
    }

    private static final Response okResponse=new Response.Builder().type(ResponseType.OK).build();
    private static final Response managementResponse=new Response.Builder().type(ResponseType.MANAGEMENT).build();

    private Response handleRequest(Request request){
        System.out.println("sunt in HANDLEREQUEST");

        Response response=null;
        String handlerName="handle"+(request).type();

        logger.debug("HandlerName "+handlerName);
        try {
            System.out.println("try din handlreq");
            Method method=this.getClass().getDeclaredMethod(handlerName, Request.class);
            System.out.println("s-a luat metoda: " + method);

            response=(Response)method.invoke(this,request);
            logger.debug("Method "+handlerName+ " invoked");

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.error("Error in worker (invoking method handleRequest): "+e);
            logger.error(e.getStackTrace());
        }

        return response;
    }

    private Response handleFIND_USER_TYPE(Request request) {
        List<String> creds = (List<String>) request.data();
        String username = creds.get(0);
        String password = creds.get(1);

        try {
            String userType = server.findUserType(username, password);
            return new Response.Builder().type(ResponseType.OK).data(userType).build();
        } catch (MyException e) {
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        }
    }

    private Response handleLOGIN(Request request){
        logger.traceEntry("method entered: handleLOGIN with params " + request);
        User u = (User) request.data();

        try {
            User found = server.authenticate(u, this);
            logger.info("User logged in: {}", u);

            return new Response.Builder().type(ResponseType.OK).data(found).build();

        } catch (Exception er) {
            connected = false;
            logger.error("Error in worker (solving method handleLOGIN): "+er);

            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleLOGOUT(Request request) {
        logger.traceEntry("method entered: handleLOGOUT with params " + request);
        User u = (User) request.data();

        try {
            server.logout(u, this);
            connected = false;
            logger.info("User logged out!");

            return okResponse;

        } catch (MyException er) {
            logger.error("Error in worker (solving method handleLOGOUT): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleFINDALL_BOOKS(Request request) {
        logger.traceEntry("method entered: handleFINDALL_BOOKS with params " + request);

        try {
            List<Book> books = (List<Book>) server.findAllBooks();
            return new Response.Builder().type(ResponseType.FONDALL_BOOKS).data(books).build();
        } catch (MyException er) {
                logger.error("Error in worker (solving method handleFINDALL_BOOKS): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleFINDALL_READERS(Request request) {
        logger.traceEntry("method entered: handleFINDALL_READERS with params " + request);

        try {
            List<Reader> readers = (List<Reader>) server.findAllReaders();
            return new Response.Builder().type(ResponseType.FOUNDALL_READERS).data(readers).build();
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleFINDALL_READERS): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleREADERS_BY_NAME(Request request) {
        logger.traceEntry("method entered: handleREADERS_BY_NAME with params " + request);
        String name = (String)request.data();

        try {
            List<Reader> readers = (List<Reader>) server.getReadersByName(name);
            return new Response.Builder().type(ResponseType.OK).data(readers).build();

        } catch (MyException e) {
            logger.error("Error in worker (solving method handleREADERS_BY_NAME): "+e);
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        }
    }

    private Response handleFINDALL_LOANS(Request request) {
        logger.traceEntry("method entered: handleFINDALL_LOANS with params " + request);

        try {
            List<Loan> loans = (List<Loan>) server.findAllLoans();
            return new Response.Builder().type(ResponseType.FOUNDALL_LOANS).data(loans).build();
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleFINDALL_LOANS): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleFILTER_LOANS(Request request) {
        logger.traceEntry("method entered: handleFILTER_LOANS with params " + request);

        try {
            FilterLoansDTO filter = (FilterLoansDTO) request.data();
            Reader reader = filter.getReader();
            String title = filter.getTitle();

            List<Loan> loans = (List<Loan>) server.getLoansFiltered(reader, title);
            return new Response.Builder().type(ResponseType.OK).data(loans).build();
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleFILTER_LOANS): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleFILTER_BOOKS(Request request) {
        logger.traceEntry("method entered: handleFILTER_BOOKS with params " + request);

        try {
            FilterBooksDTO filter = (FilterBooksDTO) request.data();
            String title = filter.getTitle();
            String author = filter.getAuthor();

            List<Book> books = (List<Book>) server.getBooksFiltered(title, author);
            return new Response.Builder().type(ResponseType.OK).data(books).build();
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleFILTER_BOOKS): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleREADER_LOANS(Request request) {
        logger.traceEntry("method entered: handleREADER_LOANS with params " + request);
        Reader reader = (Reader) request.data();

        try {
            List<Loan> loans = (List<Loan>) server.getLoansForReader(reader);
            return new Response.Builder().type(ResponseType.OK).data(loans).build();
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleREADER_LOANS): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleADD_BOOK(Request request) {
        logger.traceEntry("method entered: handleADD_BOOK with params " + request);
        Book book = (Book) request.data();

        try {
            server.addBook(book);
            logger.info("Book added: " + book);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleADD_BOOK): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleADD_READER(Request request) {
        logger.traceEntry("method entered: handleADD_READER with params " + request);
        Reader reader = (Reader) request.data();

        try {
            server.addReader(reader);
            logger.info("Reader added: " + reader);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleADD_READER): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleADD_LOAN(Request request) {
        logger.traceEntry("method entered: handleADD_LOAN with params " + request);
        Loan loan = (Loan) request.data();

        try {
            server.addLoan(loan);
            logger.info("Loan added: " + loan);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleADD_LOAN): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleDELETE_BOOK(Request request) {
        logger.traceEntry("method entered: handleDELETE_BOOK with params " + request);
        Book book = (Book) request.data();

        try {
            server.deleteBook(book);
            logger.info("Book deleted: " + book);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleDELETE_BOOK): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }

    }

    private Response handleUPDATE_BOOK(Request request) {
        logger.traceEntry("method entered: handleUPDATE_BOOK with params " + request);
        Book book = (Book) request.data();

        try {
            server.updateBook(book);
            logger.info("Book updated: " + book);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleUPDATE_BOOK): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleDELETE_LOAN(Request request) {
        logger.traceEntry("method entered: handleDELETE_LOAN with params " + request);
        Loan loan = (Loan) request.data();

        try {
            server.deleteLoan(loan);
            logger.info("Loan deleted: " + loan);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleDELETE_LOAN): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    private Response handleUPDATE_LOAN(Request request) {
        logger.traceEntry("method entered: handleUPDATE_LOAN with params " + request);
        Loan loan = (Loan) request.data();

        try {
            server.updateLoan(loan);
            logger.info("Loan updated: " + loan);
            return okResponse;
        } catch (MyException er) {
            logger.error("Error in worker (solving method handleUPDATE_LOAN): "+er);
            return new Response.Builder().type(ResponseType.ERROR).data(er.getMessage()).build();
        }
    }

    @Override
    public void showBooks() {
        Response response = new Response.Builder().type(ResponseType.MANAGEMENT).build();
        logger.info("Book list was modified");

        try {
            sendResponse(response);
            logger.info("Response sent!");
        } catch (IOException e) {
            logger.error("Error in worker (solving method showBooks): "+e);
            logger.error(e.getStackTrace());
        }
    }


    private void sendResponse(Response response) throws IOException{
        logger.debug("sending response {}",response);
        synchronized (output) {
            output.writeObject(response);
            output.flush();
        }
    }
}
