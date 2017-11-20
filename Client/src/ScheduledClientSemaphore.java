import java.util.TimerTask;

public class ScheduledClientSemaphore extends TimerTask{
    NetworkServer server;
    Client client;
    @Override
    public void run() {
        server.sendSemaphore(client.getState());
    }
}
