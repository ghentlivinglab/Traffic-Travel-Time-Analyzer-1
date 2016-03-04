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

/**
 *
 * @author Robin
 */
public class ConsoleParser {
    private final Console console;
    private final PollThread pollThread;
    public ConsoleParser(PollThread pollThread){
        console = System.console();
        this.pollThread=pollThread;
    }
    public void processCommandLineInput(){
        if(console == null){
            System.err.println("No console.");
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
            }else if(command.equals("start")){
                pollThread.start();
            }
            //To be continued..
        }
    }

    private void printStatus() {
        System.out.println("  Status of the polling thread: "+ pollThread.getState().name());
        System.out.println("  Number of updates since launch: "+ pollThread.updateCounter);
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
}
