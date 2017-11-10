import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkRecruiter {
    int queryPort = Constants.QUERY_PORT;
    int semaphorePort = Constants.SEMAPHORE_PORT;
    ServerSocket semaphoreListener;
    ServerSocket queryListener;

    NetworkManager superior;
    Thread listener;

    public NetworkRecruiter(NetworkManager superior) throws IOException {
        this.superior = superior;
        this.semaphoreListener = new ServerSocket(semaphorePort);
        this.queryListener = new ServerSocket(queryPort);
        listen();
    }

    private void listen(){
        listener = new Thread(() ->{
            Socket clientSemaphoreSocket;
            Socket clientQuerySocket;
            ObjectInputStream semaphoreIn;
            ObjectOutputStream semaphoreOut;
            ObjectOutputStream queryOut;
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
                } catch (IOException e) {
                    System.err.println("Error docking with a client: ");
                    e.printStackTrace();
                    continue;
                }
                superior.addClient(new NetworkClient(clientSemaphoreSocket, clientQuerySocket, semaphoreIn, semaphoreOut, queryOut));
            }});
        listener.start();
    }
}
