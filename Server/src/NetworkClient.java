import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkClient {
    String IP;
    Socket semaphore;
    Socket query;
    ObjectInputStream semaphoreIn;
    ObjectOutputStream semaphoreOut;
    ObjectOutputStream queryOut;

    Thread listener;

    public NetworkClient(Socket semaphore, Socket query, ObjectInputStream semaphoreIn, ObjectOutputStream semaphoreOut, ObjectOutputStream queryOut){
        this.semaphore = semaphore;
        this.query = query;
        this.semaphoreIn = semaphoreIn;
        this.semaphoreOut = semaphoreOut;
        this.queryOut = queryOut;
        this.IP = semaphore.getInetAddress().toString();
        listen();
    }

    public void listen(){
        listener = new Thread(() -> {
            while(true){
                try {
                    parseSemaphore((ClientSemaphore)semaphoreIn.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Unable to interpret client semaphore.");
                    e.printStackTrace();
                }
            }});
        listener.start();
    }

    private void parseSemaphore(ClientSemaphore input){
        //TODO: Parse ClientSemaphore into local data fields
    }

    public void sendSemaphore(ServerSemaphore serverSemaphore){
        //TODO: Make threadsafe with a lock object
        new Thread(()->{
            try{
                semaphoreOut.writeObject(serverSemaphore);
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
            } catch (IOException e) {
                System.err.println("Unable to send new query.");
                e.printStackTrace();
            }
        }).start();
    }
}
