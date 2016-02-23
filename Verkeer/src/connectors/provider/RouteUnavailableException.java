package connectors.provider;

public class RouteUnavailableException extends Exception {

    public RouteUnavailableException() {
        super();
    }

    public RouteUnavailableException(String message) {
        super(message);
    }

    public RouteUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public RouteUnavailableException(Throwable cause) {
        super(cause);
    }

    public RouteUnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
