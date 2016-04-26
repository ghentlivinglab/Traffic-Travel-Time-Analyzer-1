package verkeer;

import java.io.Console;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;

public class ConsoleParser {

    private final Console console;
    private final PollControl pollControl;
    private static final Logger log = Logger.getLogger(ConsoleParser.class);

    public ConsoleParser(PollControl pollControl) {
        console = System.console();
        this.pollControl = pollControl;
    }

    public void processCommandLineInput() {
        if (console == null) {
            log.info("No console found. No input allowed.");
        } else {
            boolean keepRunning = true;
            while (keepRunning) {
                String command = console.readLine(" $ ");
                if (command.equals("exit")) {
                    System.exit(2);
                } else if (command.equals("status")) {
                    printStatus();
                } else if (command.equals("properties")) {
                    printProperties();
                } else if (command.equals("poll")) {
                    pollControl.forcePoll();
                } else if (command.equals("stop")) {
                    pollControl.stopPolling();
                } else if (command.equals("restart")) {
                    pollControl.stopPolling();
                    pollControl.init();
                    pollControl.startPolling();
                } else {
                    String words[] = command.split(" ");
                    if (words[0].equals("properties")) {
                        if (words.length >= 2) {
                            properties(words);
                        } else {
                            System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
                        }
                    }
                }
                //To be continued..
            }
        }
    }

    private void printStatus() {
        System.out.println("  Number of updates since restart: " + pollControl.getUpdateCounter());
    }

    private void properties(String[] command) {
        if (command[1].equals("db")) {
            if (command.length >= 3 && command[2].equals("get")) {
                showPropertiesDatabase(command);
            } else {
                System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
            }
        } else if (command[1].equals("app")) {
            if (command.length >= 3 && command[2].equals("get")) {
                showPropertiesApp(command);
            } else if (command.length >= 3 && command[2].equals("set")) {
                System.out.println("  Nog niet geimplementeerd");
            } else {
                System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
            }
        } else {
            System.out.println("  Usage: properties db|app get|set propertyname propertyvalue");
        }
    }

    private void printProperties() {
        System.out.println("\n\n--- Database ---");
        printKeysPropertiesFile("./config/database.properties");
        System.out.println("\n\n--- Applicatie ---");
        printKeysPropertiesFile("./config/app.properties");
        System.out.println("\n\n--- Providers ---");
        printKeysPropertiesFile("./config/providers.properties");
        System.out.println("\n\n");
    }

    private void showPropertiesDatabase(String[] command) {
        try {
            Properties prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("connectors/database/database.properties");
            prop.load(is);
            int size = command.length;
            for (int i = 3; i < size; i++) {
                String value = prop.getProperty(command[i]);
                if (value != null) {
                    System.out.println(command[i] + "=" + value);
                } else {
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
        try {
            InputStream propsFile = getClass().getClassLoader().getResourceAsStream(filename);
            if (propsFile == null) {
                System.err.println(filename + " kon niet geladen worden.");
            } else {
                prop.load(propsFile);
            }
            Set<Object> keys = prop.keySet();
            for (Object key : keys) {
                System.out.println(key.toString() + "=" + prop.getProperty(key.toString()));
            }
        } catch (FileNotFoundException e) {
            System.out.println("printDatabaseProperties() kon " + filename + " niet vinden. FileNotFoundException");
        } catch (IOException e) {
            System.out.println("printDatabaseProperties() kon " + filename + " niet inlezen. IOException");
        }
    }

    private void showPropertiesApp(String[] command) {
        try {
            Properties prop = new Properties();
            InputStream is = getClass().getClassLoader().getResourceAsStream("verkeer/app.properties");
            prop.load(is);
            int size = command.length;
            for (int i = 3; i < size; i++) {
                String value = prop.getProperty(command[i]);
                if (value != null) {
                    System.out.println(command[i] + "=" + value);
                } else {
                    System.out.println("  No such property. (providerCount, pollinterval)");
                }
            }
        } catch (IOException ex) {
            //doLog(Level.SEVERE, "propertiesbestand van app niet gevonden.");
            System.err.println(ex.getMessage());
        }

    }

}
