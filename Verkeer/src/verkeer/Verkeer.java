
package verkeer;

/**
 *
 * @author Simon
 */
public class Verkeer {
    public static void main(String[] args) {
        System.out.println("Launching application.");
        
        PollThread t = new PollThread();
        t.setDaemon(true);
        t.start();
        
        ConsoleParser cp = new ConsoleParser(t);
        cp.processCommandLineInput();
        
    }
}
