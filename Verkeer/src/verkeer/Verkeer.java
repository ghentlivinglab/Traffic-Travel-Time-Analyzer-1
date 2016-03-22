
package verkeer;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Simon
 */
public class Verkeer {
    
    private static final String LOG_PARENT_DIRECTORY = "logs";
    private static FileHandler fh = null;
    private static Logger logger = null;
    
    public static void main(String[] args) throws IOException {
        
        Verkeer.getLogger(Verkeer.class.getName()).log(Level.INFO, "Launching Application");
        System.out.println("Launching application.");
        
        PollThread t = new PollThread();
        t.start();
        
        ConsoleParser cp = new ConsoleParser(t);
        cp.processCommandLineInput();
        
    }
    
    public static Logger getLogger(String loggerName) throws IOException{
        if ( fh == null ) {
            boolean append = true;
            fh = new FileHandler(getFileName(), append);
            fh.setFormatter(new SimpleFormatter());
        }
        
        logger = Logger.getLogger(loggerName);
        if(logger.getHandlers() != null){ // anders duplicate logging, want meerdere dezelfde handlers zorgt voor dezelfde output meerdere malen
            logger.removeHandler(fh);
        }
        logger.setLevel(Level.ALL);
        logger.addHandler(fh);
        logger.setUseParentHandlers(false);
        return logger;
    }
    
    public static String getFileName(){
        DateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        String logbestand = LOG_PARENT_DIRECTORY+"/"+dateformat.format(date)+".log";
        File yourFile = new File(logbestand);
        File parent = new File(LOG_PARENT_DIRECTORY);
        
        // Aanmaken als het nog niet bestaat
         if(!parent.exists()){
            System.err.println("Parentdirectory bestaat niet");
         }
                 
                 
        if(!parent.exists() && !parent.mkdirs()){
            System.err.println("Parentdirectory van logbestand kon niet aangemaakt worden");
        }

        if(!yourFile.exists()){
            try {
                yourFile.createNewFile();
            } catch (IOException ex) {
                //System.err.println("Logfile could not be created");
                ex.printStackTrace();
            }
        }
        return logbestand;
    }
    
}
