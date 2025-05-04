package ro.iss.utils;

public class NetworkingException extends Exception {
    public NetworkingException() {
      super();
    }

    public NetworkingException(String message) {
        super(message);
    }

    public NetworkingException(String message, Throwable cause) {
      super(message, cause);
    }
}
