import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Timer;

public class NetworkServer {
    String IP;
    ObjectInputStream semaphoreIn;
    ObjectInputStream queryIn;
    ObjectOutputStream semaphoreOut;
    ObjectOutputStream queryOut;

    Thread semaphoreListener;
    Thread queryListener;
    Timer timer;

    // !! --- Soft-Link Serialized Data --- !!

    boolean queuing;
    boolean awaitingConfirmation;
    boolean inQuery;
    boolean displayingAnswer;
    boolean awaitingQuery;
    boolean finishedQueries;
    boolean resetting;
    int placement;
    Query currentQuery;

    //               ----                   //



    public NetworkServer(ObjectInputStream semaphoreIn, ObjectInputStream queryIn, ObjectOutputStream semaphoreOut, ObjectOutputStream queryOut) {
        this.semaphoreIn = semaphoreIn;
        this.queryIn = queryIn;
        this.semaphoreOut = semaphoreOut;
        this.queryOut = queryOut;
        this.placement = 0;
        timer = new Timer();
        listen();
        speak();
    }

    public void listen(){
        semaphoreListener = new Thread(() -> {
            while(true){
                try {
                    parseSemaphore((ServerSemaphore) semaphoreIn.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Unable to interpret server semaphore.");
                    e.printStackTrace();
                }
            }
        });

        queryListener = new Thread(() -> {
            while(true){
                try {
                    parseQuery((Query) queryIn.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Unable to interpret server query.");
                    e.printStackTrace();
                }
            }
        });
        semaphoreListener.start();
        queryListener.start();
    }

    public void speak(){
        timer.schedule(new ScheduledClientSemaphore(), 0, 1000);
    }

    private void parseSemaphore(ServerSemaphore semaphore){
        boolean[] state = semaphore.getState();
        this.queuing = state[0];
        this.awaitingConfirmation = state[1];
        this.inQuery = state[2];
        this.displayingAnswer = state[3];
        this.awaitingQuery = state[4];
        this.finishedQueries = state[5];
        this.resetting = state[6];
        this.placement = semaphore.getPlacement();
        if(semaphore.hasCommand()) {
            boolean[] commands = semaphore.getCommands();
            if(commands[0]){
                //TODO: Send Client Semaphore immediately
            } if(commands[1]){
                //TODO: Display accompanying message to client
            } if(commands[2]){
                //TODO: Request Client's attention to a relevant event
            } if(commands[3]){
                //TODO: Reset Host's presence
            }
        }
    }

    public void sendSemaphore(ClientSemaphore semaphore){
        //TODO: Make threadsafe with a lock object
        new Thread(()->{
            try{
                semaphoreOut.writeObject(semaphore);
                semaphoreOut.flush();
            } catch (IOException e) {
                System.err.println("Unable to send client semaphore.");
                e.printStackTrace();
            }
        }).start();
    }

    private void parseQuery(Query query){
        this.currentQuery = query;
    }

    private void clearQuery(){
        this.currentQuery = null;
    }

    public boolean hasQuery(){
        return (!(currentQuery == null));
    }
}
