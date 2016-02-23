/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package verkeer;

import java.io.Console;

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
                System.out.println("  Command not found");
            }
            //To be continued..
        }
    }

    private void printStatus() {
        System.out.println("  Status of the polling thread: "+ pollThread.getState().name());
        System.out.println("  Number of updates since launch: "+ pollThread.updateCounter);
    }
}
