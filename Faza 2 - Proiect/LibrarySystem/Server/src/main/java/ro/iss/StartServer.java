package ro.iss;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.iss.implementation.*;
import ro.iss.services.IService;
import ro.iss.utils.AbstractServer;
import ro.iss.utils.HibernateUtils;
import ro.iss.utils.NetworkingException;
import ro.iss.utils.RpcConcurrentServer;

import java.io.File;
import java.io.IOException;
import java.rmi.ServerException;
import java.util.Properties;

public class StartServer {
    private static int defaultPort=55555;
    private static Logger logger = LogManager.getLogger(StartServer.class);
    public static void main(String[] args) {

        Properties serverProps=new Properties();
        try {
            serverProps.load(StartServer.class.getResourceAsStream("/server.properties"));
            logger.info("Server properties set {}",serverProps);
        } catch (IOException e) {
            logger.error("Cannot find server.properties "+e);
            logger.debug("Looking for file in "+(new File(".")).getAbsolutePath());
            return;
        }

        ILibrarianRepository librarianRepository = new LibrarianRepository(HibernateUtils.getSessionFactory());
        IReaderRepository readerRepository = new ReaderRepository(HibernateUtils.getSessionFactory());
        IBookRepository bookRepository = new BookRepository(HibernateUtils.getSessionFactory());
        ILoanRepository loanRepository = new LoanRepository(HibernateUtils.getSessionFactory());

        IService service = new Service(readerRepository, librarianRepository, bookRepository, loanRepository);

        int serverPort = defaultPort;
        try {
            serverPort = Integer.parseInt(serverProps.getProperty("librarysys.server.port"));
        }catch (NumberFormatException nef){
            logger.error("Wrong Port Number"+nef.getMessage());
            logger.debug("Using default port "+defaultPort);
        }
        logger.debug("Starting server on port: "+serverPort);
        AbstractServer server = new RpcConcurrentServer(serverPort, service);

        try {
            server.start();
        } catch (NetworkingException e) {
            logger.error("Error starting the server" + e.getMessage());
        }finally {
            try {
                server.stop();
            }catch(NetworkingException e){
                logger.error("Error stopping server "+e.getMessage());
            }
        }

    }
}