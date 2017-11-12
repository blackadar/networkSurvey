import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class NetworkServer {
    String IP;
    ObjectInputStream semaphoreIn;
    ObjectInputStream queryIn;
    ObjectOutputStream semaphoreOut;
    ObjectOutputStream queryOut;

    Thread semaphoreListener;
    Thread queryListener;

    // !! --- Soft-Link Serialized Data --- !!

    boolean queuing;
    boolean awaitingConfirmation;
    boolean inQuery;
    boolean displayingAnswer;
    boolean awaitingQuery;
    boolean finishedQueries;
    boolean resetting;
    Query currentQuery;

    //               ----                   //



    public NetworkServer(ObjectInputStream semaphoreIn, ObjectInputStream queryIn, ObjectOutputStream semaphoreOut, ObjectOutputStream queryOut) {
        this.semaphoreIn = semaphoreIn;
        this.queryIn = queryIn;
        this.semaphoreOut = semaphoreOut;
        this.queryOut = queryOut;
        listen();
    }

    public void listen(){
        semaphoreListener = new Thread(() -> {
            while(true){
                //TODO: Accept new Semaphore objects
            }
        });

        queryListener = new Thread(() -> {
            while(true){
                //TODO: Accept new Query objects
            }
        });
        semaphoreListener.start();
        queryListener.start();
    }

    private void parseSemaphore(Semaphore semaphore){
        //TODO: Fill data fields from object
    }

    public void sendSemaphore(ClientSemaphore semaphore){
        //TODO: Implement method
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
