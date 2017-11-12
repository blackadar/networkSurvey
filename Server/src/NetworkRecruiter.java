import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkRecruiter {
    private ServerSocket semaphoreListener;
    private ServerSocket queryListener;

    private NetworkManager manager;
    private Thread listener;

    public NetworkRecruiter(NetworkManager superior) throws IOException {
        this.manager = superior;
        this.semaphoreListener = new ServerSocket(Constants.SEMAPHORE_PORT);
        this.queryListener = new ServerSocket(Constants.QUERY_PORT);
        listen();
    }

    private void listen(){
        listener = new Thread(() ->{
            Socket clientSemaphoreSocket;
            Socket clientQuerySocket;
            ObjectInputStream semaphoreIn;
            ObjectOutputStream semaphoreOut;
            ObjectOutputStream queryOut;
            ObjectInputStream queryIn;
            while(true){
                try{
                    clientSemaphoreSocket = semaphoreListener.accept();
                    clientQuerySocket = queryListener.accept();
                } catch (IOException e) {
                    System.err.println("Error docking with a client: ");
                    e.printStackTrace();
                    continue;
                }
                try{
                    semaphoreIn = new ObjectInputStream(clientSemaphoreSocket.getInputStream());
                    semaphoreOut = new ObjectOutputStream(clientSemaphoreSocket.getOutputStream());
                    queryOut = new ObjectOutputStream(clientQuerySocket.getOutputStream());
                    queryIn = new ObjectInputStream(clientQuerySocket.getInputStream());
                } catch (IOException e) {
                    System.err.println("Error docking with a client: ");
                    e.printStackTrace();
                    continue;
                }
                manager.addClient(new NetworkClient(manager, clientSemaphoreSocket, clientQuerySocket, semaphoreIn, semaphoreOut, queryOut, queryIn));
            }});
        listener.start();
    }
}