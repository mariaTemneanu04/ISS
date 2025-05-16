package ro.iss.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.domain.Book;
import ro.iss.domain.Loan;
import ro.iss.domain.Reader;
import ro.iss.domain.User;
import ro.iss.domain.validator.ValidationException;
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
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServiceRpcProxy implements IService {
    private final String host;
    private final int port;

    private IObserver client;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Socket connection;
    private static final Logger logger = LogManager.getLogger(ServiceRpcProxy.class);
    private final BlockingQueue<Response> qresponses;
    private volatile boolean finished;

    public ServiceRpcProxy(String host, int port) {
        logger.info("Creating proxy...");
        this.host = host;
        this.port = port;
        qresponses = new LinkedBlockingQueue<>();
    }

    @Override
    public synchronized String findUserType (String user, String password) throws MyException {
        initializeConnection();
        Request req = new Request.Builder().type(RequestType.FIND_USER_TYPE).data(List.of(user, password)).build();
        sendRequest(req);
        Response response = readResponse();

        if (response.type() == ResponseType.ERROR) {
            logger.error("Error finding user type " + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        }

        return (String) response.data();
    }


    @Override
    public User authenticate(User user, IObserver observer) throws MyException {
        System.out.println("Sunt in authenticate din SERVICEPROXY");
        // initializeConnection();
        System.out.println("s-a facut initconnection");

        Request req = new Request.Builder().type(RequestType.LOGIN).data(user).build();
        System.out.println("s-a facut request.");
        sendRequest(req);
        Response response = readResponse();
        System.out.println("s-a primit response: " + response);

        if (response.type() == ResponseType.OK) {
            this.client = observer;
            logger.info("Logged in");
            return (User) response.data();
        }
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error logging in" + response.data().toString());
            String err = response.data().toString();
            closeConnection();
            throw new MyException(err);
        }
        return null;
    }

    @Override
    public void logout(User user, IObserver observer) throws MyException {
        Request req = new Request.Builder().type(RequestType.LOGOUT).data(user).build();
        sendRequest(req);

        Response response = readResponse();
        closeConnection();

        if (response.type() == ResponseType.ERROR) {
            logger.error("Error logging out" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Logged out");
        }
    }

    @Override
    public synchronized Iterable<Book> findAllBooks() throws MyException {
        Request req = new Request.Builder().type(RequestType.FINDALL_BOOKS).data(null).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error finding all books" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found all books");
        }
        return (List<Book>) response.data();
    }


    @Override
    public synchronized Iterable<Reader> findAllReaders() throws MyException {
        Request req = new Request.Builder().type(RequestType.FINDALL_READERS).data(null).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error finding all readers" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found all readers");
        }

        return (List<Reader>) response.data();
    }

    @Override
    public synchronized List<Reader> getReadersByName(String name) throws MyException {
        Request req = new Request.Builder().type(RequestType.READERS_BY_NAME).data(name).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error finding readers by name" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found readers by name");
        }

        return (List<Reader>) response.data();
    }

    @Override
    public synchronized Iterable<Loan> findAllLoans() throws MyException {
        Request req = new Request.Builder().type(RequestType.FINDALL_LOANS).data(null).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error finding all loans" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found all loans");
        }

        return (List<Loan>) response.data();
    }

    @Override
    public synchronized List<Loan> getLoansFiltered(Reader reader, String title) throws MyException {
        FilterLoansDTO filter = new FilterLoansDTO(reader, title);
        Request req = new Request.Builder().type(RequestType.FILTER_LOANS).data(filter).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error filtering loans" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found filtered loans");
        }

        return (List<Loan>) response.data();
    }

    @Override
    public List<Book> getBooksFiltered(String title, String author) throws MyException {
        FilterBooksDTO filter = new FilterBooksDTO(title, author);
        Request request = new Request.Builder().type(RequestType.FILTER_BOOKS).data(filter).build();
        sendRequest(request);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error filtering books" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found filtered books");
        }

        return (List<Book>) response.data();
    }

    @Override
    public synchronized List<Loan> getLoansForReader(Reader reader) throws MyException {
        Request req = new Request.Builder().type(RequestType.READER_LOANS).data(reader).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error finding loans for reader: " + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Found loans for reader");
        }

        return (List<Loan>) response.data();
    }

    @Override
    public synchronized void addBook(Book b) throws MyException {
        Request req = new Request.Builder().type(RequestType.ADD_BOOK).data(b).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error adding a book" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Added a book");
        }
    }

    @Override
    public synchronized void deleteBook(Book b) throws MyException {
        Request req = new Request.Builder().type(RequestType.DELETE_BOOK).data(b).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error deleting a book" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Deleted a book");
        }
    }

    @Override
    public synchronized void updateBook(Book b) throws MyException {
        Request req = new Request.Builder().type(RequestType.UPDATE_BOOK).data(b).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error updating a book" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Updated a book");
        }
    }

    @Override
    public void addLoan(Loan l) throws MyException {
        Request req = new Request.Builder().type(RequestType.ADD_LOAN).data(l).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error adding a loan" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Added a loan");
        }
    }

    @Override
    public synchronized void deleteLoan(Loan l) throws MyException {
        Request req = new Request.Builder().type(RequestType.DELETE_LOAN).data(l).build();
        sendRequest(req);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error deleting a loan" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Deleted a loan");
        }
    }

    @Override
    public synchronized void updateLoan(Loan l) throws MyException {
        Request request = new Request.Builder().type(RequestType.UPDATE_LOAN).data(l).build();
        sendRequest(request);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error updating a loan" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Updated a loan");
        }
    }

    @Override
    public void addReader(Reader r) throws MyException, ValidationException {
        Request request = new Request.Builder().type(RequestType.ADD_READER).data(r).build();
        sendRequest(request);

        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            logger.error("Error adding reader" + response.data().toString());
            String err = response.data().toString();
            throw new MyException(err);
        } else {
            logger.info("Added a reader");
        }
    }

    private void initializeConnection() throws MyException {
        try {
            this.connection = new Socket(this.host, this.port);
            this.output = new ObjectOutputStream(this.connection.getOutputStream());
            this.output.flush();
            this.input = new ObjectInputStream(this.connection.getInputStream());
            this.finished = false;
            this.startReader();
            logger.info("Connection initialized");
            System.out.println("Connection initialized");

        } catch (IOException e) {
            logger.error("Error connecting to server " + e);
            throw new MyException("Error connecting to server " + e);
        }

    }

    private void startReader() {
        Thread tw = new Thread(new ReaderThread());
        tw.start();
    }

    private void sendRequest(Request request) throws MyException {
        try {
            this.output.writeObject(request);
            this.output.flush();

            logger.info("Request sent: " + request);

        } catch (IOException e) {
            logger.error("Error sending object " + e);
            throw new MyException("Error sending object " + e);
        }
    }

    private Response readResponse() throws MyException {
        Response response = null;

        try {
            response = this.qresponses.take();
            logger.info("Response received: " + response);

        } catch (InterruptedException e) {
            logger.error("Reading response error " + e);
            throw new MyException("Reading response error " + e);
        }

        return response;
    }

    private void closeConnection() {
        this.finished = true;

        try {
            this.input.close();
            this.output.close();
            this.connection.close();
            this.client = null;
            logger.info("Closed connection");

        } catch (IOException e) {
            logger.error("Error closing connection: " + e);;
        }

    }

    private class ReaderThread implements Runnable {
        public void run() {
            while (!finished) {
                try {
                    Object response = input.readObject();
                    if (!(response instanceof Response)) {
                        logger.error("Received invalid object: " + response);
                        continue;
                    }
                    if (isUpdate((Response) response)) {
                        handleUpdate((Response) response);
                    } else {
                        qresponses.put((Response) response);
                    }
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    if (e instanceof SocketException)
                        logger.info("Socket closed: " + e);
                    else
                        logger.error("Reading error: " + e);
                    break;
                }
            }
        }
    }

    private void handleUpdate(Response response) {
        client.showBooks();
    }

    private boolean isUpdate(Response response) {
        return response.type() == ResponseType.MANAGEMENT;
    }

}
