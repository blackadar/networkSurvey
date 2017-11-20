import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkResponder {

    ServerSocket semaphoreListener;
    ServerSocket queryListener;

    Client client;

    public NetworkResponder(Client client) throws IOException {
        this.semaphoreListener = new ServerSocket(Constants.SEMAPHORE_PORT);
        this.queryListener = new ServerSocket(Constants.QUERY_PORT);
        this.client = client;
    }

    public NetworkServer connect() throws IOException, ClassNotFoundException {
        Socket serverSemaphoreSocket;
        Socket serverQuerySocket;
        ObjectInputStream semaphoreIn;
        ObjectOutputStream semaphoreOut;
        ObjectOutputStream queryOut;
        ObjectInputStream queryIn;
        Identity identity;

            serverSemaphoreSocket = semaphoreListener.accept();
            serverQuerySocket = queryListener.accept();

            semaphoreIn = new ObjectInputStream(serverSemaphoreSocket.getInputStream());
            semaphoreOut = new ObjectOutputStream(serverSemaphoreSocket.getOutputStream());
            queryOut = new ObjectOutputStream(serverQuerySocket.getOutputStream());
            queryIn = new ObjectInputStream(serverQuerySocket.getInputStream());
            semaphoreOut.writeObject(client.identity);
            semaphoreOut.flush();
            identity = (Identity)semaphoreIn.readObject();

        return new NetworkServer(identity, semaphoreIn, queryIn, semaphoreOut, queryOut);
    }

    public void close() throws IOException {
        semaphoreListener.close();
        queryListener.close();
    }

}
