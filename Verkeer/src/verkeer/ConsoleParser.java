/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 *
 * @author Robin
 */
public class ConsoleParser {
    private final Console console;
    private final PollThread pollThread;
    private static final Logger log = Logger.getLogger(ConsoleParser.class);
    
    public ConsoleParser(PollThread pollThread){
        console = System.console();
        this.pollThread=pollThread;
    }
    
    public void processCommandLineInput(){
        if(console == null){
            log.error("No console found. Don't try to start this in an IDE ;)");
            System.exit(1);
        }
        boolean keepRunning = true;
        while(keepRunning){
            String command = console.readLine(" $ ");
            if(command.equals("exit")){
                System.exit(2);
            }else if(command.equals("status")){
                printStatus();
            }else if(command.equals("properties")){
                printProperties();
            }else if(command.equals("reload")){
                pollThread.start();
            }
            else{
                String words[] = command.split(" ");
                if(words[0].equals("properties")){
                    if(words.length >= 2)
                        properties(words);
                    else{
                        System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
                    }
                }
            }
            //To be continued..
        }
    }

    private void printStatus() {
        System.out.println("  Status of the polling thread: "+ pollThread.getState().name());
        System.out.println("  Number of updates since launch: "+ pollThread.getUpdateCounter());
    }

    private void properties(String[] command) {
        //index 0 is al zeker properties.
        //doLog(Level.INFO, command[0] + " " + command[1] + " " + command[2] + " " + command[3] + " " + command[4] + " uitgevoerd.");
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
    
    private void printProperties(){
        System.out.println("\n\n--- Database ---");
        printKeysPropertiesFile("connectors/database/database.properties");
        System.out.println("\n\n--- Applicatie ---");
        printKeysPropertiesFile("verkeer/app.properties");
        System.out.println("\n\n--- Providers ---");
        printKeysPropertiesFile("connectors/provider/providers.properties");
        System.out.println("\n\n");
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
                    System.out.println("  No such property. (URL,IP,PORT,DATABASE,USER,PASSWORD)");
                }    
            }
        } catch (IOException ex) {
            //doLog(Level.SEVERE, "propertiesbestand van database niet gevonden.");
            System.err.println(ex.getMessage());
        }
    }
        
    private void printKeysPropertiesFile(String filename) {
        Properties prop = new Properties();
        try{
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream(filename);
            if(propsFile == null){
                System.err.println(filename+" kon niet geladen worden.");
            }else{
                prop.load(propsFile);
            }
            Set<Object> keys = prop.keySet();
            for(Object key : keys)
                System.out.println(key.toString()+"="+prop.getProperty(key.toString())); 
        }catch(FileNotFoundException e){
            System.out.println("printDatabaseProperties() kon "+filename+" niet vinden. FileNotFoundException");
        }catch(IOException e){
            System.out.println("printDatabaseProperties() kon "+filename+" niet inlezen. IOException");
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
                    System.out.println("  No such property. (providerCount, pollinterval)");
                }
            }
        } catch (IOException ex) {
            //doLog(Level.SEVERE, "propertiesbestand van app niet gevonden.");
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
}
