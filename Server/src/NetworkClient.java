import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;

public class NetworkClient {
    Identity identity;
    NetworkManager manager;
    String IP;
    Socket semaphore;
    Socket query;
    ObjectInputStream semaphoreIn;
    ObjectOutputStream semaphoreOut;
    ObjectOutputStream queryOut;
    ObjectInputStream queryIn;
    boolean threadStop = false;

    Thread semaphoreListener;
    Thread queryListener;
    Timer timer;

    ArrayList<ResponseUpdateListener> listeners = new ArrayList<>();

    // !! --- Soft-Link Serialized Data --- !!
    boolean awaitingReady;
    boolean awaitingUserResponse;
    boolean awaitingServerQuery;
    Response currentResponse;
    //                  ---                 //

    public NetworkClient(Identity identity, NetworkManager manager, Socket semaphore, Socket query, ObjectInputStream semaphoreIn, ObjectOutputStream semaphoreOut, ObjectOutputStream queryOut, ObjectInputStream queryIn){
        this.identity = identity;
        this.manager = manager;
        this.semaphore = semaphore;
        this.query = query;
        this.semaphoreIn = semaphoreIn;
        this.semaphoreOut = semaphoreOut;
        this.queryOut = queryOut;
        this.queryIn = queryIn;
        this.IP = semaphore.getInetAddress().toString();
        timer = new Timer();
        listeners.add(manager);
        listen();
        speak();
    }

    public void listen(){
        semaphoreListener = new Thread(() -> {
            while(!threadStop){
                try {
                    parseSemaphore((ClientSemaphore)semaphoreIn.readObject());
                } catch (SocketException | EOFException ex){
                    System.err.println("Server encountered " + ex.toString());
                    manager.removeClient(this);
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Server encountered " + e.toString());
                }
            }});
        queryListener = new Thread(() -> {
            while(!threadStop) {
                try {
                    parseQueryResponse((Response) queryIn.readObject());
                } catch (SocketException | EOFException ex){
                    System.err.println("Server encountered " + ex.toString());
                    manager.removeClient(this);
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Server encountered " + e.toString());
                }
            }
        });
        queryListener.start();
        semaphoreListener.start();
    }

    public void speak(){
        timer.schedule(new ScheduledServerSemaphore(manager.host, this), 0, 1000);
    }

    private void parseSemaphore(ClientSemaphore input){
        boolean[] state = input.getState();
        this.awaitingReady = state[0];
        this.awaitingUserResponse = state[1];
        this.awaitingServerQuery = state[2];
        if(input.hasCommand()){
            boolean[] commands = input.getCommands();
            if(commands[0]){
                //TODO: Send Server Semaphore immediately
            } if(commands[1]){
                //TODO: Display raised hand notification on Host
            } if(commands[2]){
                //TODO: Request Host's attention to a relevant event
            } if(commands[3]){
                //TODO: Reset Client's presence
            }
        }
    }

    private void parseQueryResponse(Response response){
        this.currentResponse = response;
        for(ResponseUpdateListener r : listeners){
            r.update(response);
        }
    }

    public void sendSemaphore(ServerSemaphore serverSemaphore){
        //TODO: Make threadsafe with a lock object
        new Thread(()->{
            try{
                semaphoreOut.writeObject(serverSemaphore);
                semaphoreOut.flush();
            } catch (IOException e) {
                System.err.println("Unable to send server semaphore.");
                e.printStackTrace();
            }
        }).start();
    }

    public void sendQuery(Query query){
        new Thread(()->{
            try{
                queryOut.writeObject(query);
                semaphoreOut.flush();
            } catch (IOException e) {
                System.err.println("Unable to send new query.");
                e.printStackTrace();
            }
        }).start();
    }

    public boolean hasResponse(){
        return (!(currentResponse == null));
    }

    public void resetResponse(){
        this.currentResponse = null;
    }

    public void returnState(){
        sendSemaphore(new ServerSemaphore(manager.host.identity, manager.host.state, new boolean[]{true,false,false,false}));
    }

    public void sendMessage(String message){
        sendSemaphore(new ServerSemaphore(manager.host.identity, manager.host.state, new boolean[]{false,true,false,false}, message));
    }

    public void requestUserAttention(){
        sendSemaphore(new ServerSemaphore(manager.host.identity, manager.host.state, new boolean[]{false,false,true,false}));
    }

    public void reset(){
        sendSemaphore(new ServerSemaphore(manager.host.identity, manager.host.state, new boolean[]{false,false,false,true}));
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
