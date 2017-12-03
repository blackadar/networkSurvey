import java.util.TimerTask;

public class ScheduledClientSemaphore extends TimerTask{
    NetworkServer server;
    Client client;

    public ScheduledClientSemaphore(NetworkServer server, Client client){
        this.server = server;
        this.client = client;
    }

    @Override
    public void run() {
        server.sendSemaphore(client.getState());
    }
}
