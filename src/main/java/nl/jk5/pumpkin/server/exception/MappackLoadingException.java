package nl.jk5.pumpkin.server.exception;

public class MappackLoadingException extends Exception {

    public MappackLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public MappackLoadingException(String message) {
        super(message);
    }
}
