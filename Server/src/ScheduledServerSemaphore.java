import java.util.TimerTask;

public class ScheduledServerSemaphore extends TimerTask {
    Host host;
    NetworkClient client;

    public ScheduledServerSemaphore(Host host, NetworkClient client){
        this.host = host;
        this.client = client;
    }

    @Override
    public void run() {
        client.sendSemaphore(host.getState());
    }
}
