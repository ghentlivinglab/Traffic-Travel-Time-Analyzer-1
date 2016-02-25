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
            String command = console.readLine("Verkeer@localhost: $ ");
            if(command.equals("exit")){
                System.exit(2);
            }else if(command.equals("status")){
                printStatus();
            }else{
                String words[] = command.split(" ");
                if(words[0].equals("props")){
                    if(words.length >= 2)
                        properties(words);
                    else
                        System.out.println("  Usage: props db|app");
                }
            }
            //To be continued..
        }
    }

    private void printStatus() {
        System.out.println("  Status of the polling thread: "+ pollThread.getState().name());
        System.out.println("  Number of updates since launch: "+ pollThread.updateCounter);
    }

    private void properties(String[] command) {
        //index 0 is al zeker properties.
        if(command[1].equals("db")){
            if(command.length>=3&&command[2].equals("get")){
                showPropertiesDatabase(command);
            }else if(command.length>=3&&command[2].equals("set")){
                changePropertiesDatabase(command);
            }else{
                System.out.println("  Usage props db get|set");
            }
        }else if(command[1].equals("app")){
            System.out.println("app properties");
        }else{
            System.out.println("  Usage: properties db|app");
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
                else
                    System.out.println("  No such property. (URL,IP,PORT,DATABASE,USER,PASSWORD)");
            }
        } catch (IOException ex) {
            Logger.getLogger(ConsoleParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void changePropertiesDatabase(String[] command) {
        try {
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
            }
        } catch (IOException ex) {
            Logger.getLogger(ConsoleParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
