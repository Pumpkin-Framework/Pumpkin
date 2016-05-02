package nl.jk5.pumpkin.server.map;

public class MapLoadingException extends RuntimeException {

    public MapLoadingException(String message) {
        super(message);
    }

    public MapLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
