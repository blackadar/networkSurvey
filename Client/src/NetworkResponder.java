import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class NetworkResponder {

    Socket semaphoreListener;
    Socket queryListener;

    Client client;
    ArrayList<QueryUpdateListener> listeners = new ArrayList<>();

    public NetworkResponder(Client client, String host) throws IOException {
        this.semaphoreListener = new Socket(host, Constants.SEMAPHORE_PORT);
        this.queryListener = new Socket(host, Constants.QUERY_PORT);
        this.client = client;
        listeners.add(client);
    }

    public NetworkServer connect() throws IOException, ClassNotFoundException {
        ObjectInputStream semaphoreIn;
        ObjectOutputStream semaphoreOut;
        ObjectOutputStream queryOut;
        ObjectInputStream queryIn;
        Identity identity;

            semaphoreOut = new ObjectOutputStream(semaphoreListener.getOutputStream());
            semaphoreIn = new ObjectInputStream(semaphoreListener.getInputStream());
            queryIn = new ObjectInputStream(queryListener.getInputStream());
            queryOut = new ObjectOutputStream(queryListener.getOutputStream());
            semaphoreOut.writeObject(client.identity);
            semaphoreOut.flush();
            identity = (Identity)semaphoreIn.readObject();

        return new NetworkServer(identity, client, this, semaphoreIn, queryIn, semaphoreOut, queryOut);
    }

    public void close() throws IOException {
        semaphoreListener.close();
        queryListener.close();
    }

}
