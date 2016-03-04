/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Robin
 */
public class ConsoleParser implements MyLogger{
    private final Console console;
    private final PollThread pollThread;
    public ConsoleParser(PollThread pollThread){
        console = System.console();
        this.pollThread=pollThread;
    }
    public void processCommandLineInput(){
        if(console == null){
            doLog(Level.WARNING, "No console.");
            System.exit(1);
        }
        boolean keepRunning = true;
        while(keepRunning){
            String command = console.readLine(" $ ");
            if(command.equals("exit")){
                System.exit(2);
            }else if(command.equals("status")){
                printStatus();
            }else{
                String words[] = command.split(" ");
                if(words[0].equals("properties")){
                    if(words.length >= 2)
                        properties(words);
                    else{
                        doLog(Level.INFO, "  Usage: properties db|app get|set propertyname propertyvalue");
                        System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
                    }
                }
            }
            //To be continued..
        }
    }

    private void printStatus() {
        doLog(Level.INFO, "  Status of the polling thread: "+ pollThread.getState().name());
        System.out.println("  Status of the polling thread: "+ pollThread.getState().name());
        doLog(Level.INFO, "  Number of updates since launch: "+ pollThread.updateCounter);
        System.out.println("  Number of updates since launch: "+ pollThread.updateCounter);
    }

    private void properties(String[] command) {
        //index 0 is al zeker properties.
        doLog(Level.INFO, command[0] + " " + command[1] + " " + command[2] + " " + command[3] + " " + command[4] + " uitgevoerd.");
        if(command[1].equals("db")){
            if(command.length>=3&&command[2].equals("get")){
                showPropertiesDatabase(command);
            }else if(command.length>=3&&command[2].equals("set")){
                changePropertiesDatabase(command);
            }else{
                System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
            }
        }else if(command[1].equals("app")){
            if(command.length>=3&&command[2].equals("get")){
                showPropertiesApp(command);
            }else if(command.length>=3&&command[2].equals("set")){
                System.out.println("  Nog niet geimplementeerd");
            }else{
                System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
            }
        }else{
            System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
        }
    }

    private void showPropertiesDatabase(String[] command) {
        try {
            Properties prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("connectors/database/database.properties");
            prop.load(is);
            int size = command.length;
            for(int i=3; i<size; i++){
                String value = prop.getProperty(command[i]);
                if(value  != null)
                    System.out.println(command[i]+"="+value);
                else{
                    doLog(Level.INFO, "No such property. (URL,IP,PORT,DATABASE,USER,PASSWORD)");
                    System.out.println("  No such property. (URL,IP,PORT,DATABASE,USER,PASSWORD)");
                }    
            }
        } catch (IOException ex) {
            doLog(Level.SEVERE, "propertiesbestand van database niet gevonden.");
            System.err.println(ex.getMessage());
        }
    }
    private void showPropertiesApp(String[] command){
        try {
            Properties prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("verkeer/app.properties");
            prop.load(is);
            int size = command.length;
            for(int i=3; i<size; i++){
                String value = prop.getProperty(command[i]);
                if(value  != null)
                    System.out.println(command[i]+"="+value);
                else{
                    doLog(Level.INFO, "No such property. (URL,IP,PORT,DATABASE,USER,PASSWORD)");
                    System.out.println("  No such property. (providerCount, pollinterval)");
                }
            }
        } catch (IOException ex) {
            doLog(Level.SEVERE, "propertiesbestand van app niet gevonden.");
            System.err.println(ex.getMessage());
        }
        
    }

    private void changePropertiesDatabase(String[] command) {
        /*try {
            
            Properties prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("connectors/database/database.properties");
            
            prop.load(is);
            is.close();
            int size = command.length;
            if(size != 5)
                System.out.println("Usage: props db set propertyname propertyvalue");
            else{
                File f = new File("connectors/database/database.properties");
                FileOutputStream out = new FileOutputStream(f);
                prop.setProperty(command[3], command[4]);
                prop.store(out, null);
                out.close();
            }*/
            System.out.println("  Nog niet geimplementeerd.");
        /*} catch (IOException ex) {
            Logger.getLogger(ConsoleParser.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    @Override
    public void doLog(Level lvl, String log) {
        try{
            Verkeer.getLogger(ConsoleParser.class.getName()).log(lvl, log);
        }
        catch(IOException ie){
            System.err.println("logbestand niet gevonden.");
        }
    }
}
