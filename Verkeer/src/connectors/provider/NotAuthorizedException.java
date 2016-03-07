package connectors.provider;

import java.io.IOException;
import java.util.logging.Level;
import verkeer.MyLogger;
import verkeer.Verkeer;

public class NotAuthorizedException extends Exception implements MyLogger {

    public NotAuthorizedException() {
        super();
        notAuthorizedLog();
    }

    public NotAuthorizedException(String message) {
        super(message);
        notAuthorizedLog();
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
        notAuthorizedLog();
    }

    public NotAuthorizedException(Throwable cause) {
        super(cause);
        notAuthorizedLog();
    }

    public NotAuthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        notAuthorizedLog();
    }

    public void notAuthorizedLog(){
        doLog(Level.WARNING, "Onbevoegde aanvraag.");
    }
    
    @Override
    public void doLog(Level lvl, String log) {
        try{
            Verkeer.getLogger(NotAuthorizedException.class.getName()).log(lvl, log);
        }
        catch(IOException ie){
            System.err.println("logbestand niet gevonden.");
        }
    }
    
}
