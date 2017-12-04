import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.util.Timer;

public class NetworkServer {
    String IP;
    Identity identity;
    Client client;
    NetworkRecruit responder;
    ObjectInputStream semaphoreIn;
    ObjectInputStream queryIn;
    ObjectOutputStream semaphoreOut;
    ObjectOutputStream queryOut;

    Thread semaphoreListener;
    Thread queryListener;
    Timer timer;

    boolean threadStop = false;

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



    public NetworkServer(Identity identity, Client client, NetworkRecruit responder, ObjectInputStream semaphoreIn, ObjectInputStream queryIn, ObjectOutputStream semaphoreOut, ObjectOutputStream queryOut) {
        this.identity = identity;
        this.semaphoreIn = semaphoreIn;
        this.queryIn = queryIn;
        this.semaphoreOut = semaphoreOut;
        this.queryOut = queryOut;
        this.responder = responder;
        this.client = client;
        this.placement = 0;
        timer = new Timer();
        listen();
        speak();
    }

    public void listen(){
        semaphoreListener = new Thread(() -> {
            while(!threadStop){
                try {
                    parseSemaphore((ServerSemaphore) semaphoreIn.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Client encountered " + e.toString());
                    close();
                }
            }
        });

        queryListener = new Thread(() -> {
            while(!threadStop){
                try {
                    parseQuery((Query) queryIn.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Client encountered " + e.toString());
                }
            }
        });
        semaphoreListener.start();
        queryListener.start();
    }

    public void speak(){
        timer.schedule(new ScheduledClientSemaphore(this, client), 0, 1000);
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

    public void sendResponse(Response r){
        //TODO: Make threadsafe with a lock object
        new Thread(()->{
            try{
                queryOut.writeObject(r);
                queryOut.flush();
            } catch (IOException e) {
                System.err.println("Unable to send client response.");
                e.printStackTrace();
            }
        }).start();
    }

    private void parseQuery(Query query){
        this.currentQuery = query;
        for(QueryUpdateListener q : responder.listeners){
            q.update(query);
        }
    }

    private void clearQuery(){
        this.currentQuery = null;
    }

    public boolean hasQuery(){
        return (!(currentQuery == null));
    }

    public void close(){
        threadStop = true;
        timer.cancel();
        timer.purge();
        try {
            semaphoreOut.close();
            semaphoreIn.close();
            queryOut.close();
            queryIn.close();
        } catch (IOException e) {
            System.err.println("Unable to close ports fully.");
            e.printStackTrace();
        }
    }
}
