package connectors.provider;

import java.io.IOException;
import java.util.logging.Level;
import verkeer.MyLogger;
import verkeer.Verkeer;

public class RouteUnavailableException extends Exception implements MyLogger{

    public RouteUnavailableException() {
        super();
        routeUnavailabeLog();
    }

    public RouteUnavailableException(String message) {
        super(message);
        routeUnavailabeLog();
    }

    public RouteUnavailableException(String message, Throwable cause) {
        super(message, cause);
        routeUnavailabeLog();
    }

    public RouteUnavailableException(Throwable cause) {
        super(cause);
        routeUnavailabeLog();
    }

    public RouteUnavailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        routeUnavailabeLog();
    }

    public void routeUnavailabeLog(){
        doLog(Level.WARNING, "Route niet beschikbaar.");
    }
    
    @Override
    public void doLog(Level lvl, String log) {
        try{
            Verkeer.getLogger(RouteUnavailableException.class.getName()).log(lvl, log);
        }
        catch(IOException ie){
            System.err.println("logbestand niet gevonden.");
        }
    }

}
