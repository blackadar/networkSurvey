import java.util.ArrayList;

public class NetworkManager {
    ArrayList<NetworkClient> clients = new ArrayList<>();
    Host host;

    public NetworkManager(Host host){
        this.host = host;
    }

    public void addClient(NetworkClient c){
        clients.add(c);
    }

    public void queryAll(Query query){
        for(NetworkClient c : clients){
            c.sendQuery(query);
        }
    }

    public void displayToAll(String message){
        for(NetworkClient c : clients){
            c.sendMessage(message);
        }
    }

    public void requestAll(){
        for(NetworkClient c : clients){
            c.requestUserAttention();
        }
    }

    public void semaphoreAll(ServerSemaphore semaphore){
        for(NetworkClient c : clients){
            c.sendSemaphore(semaphore);
        }
    }

    public void requestUpdateAll() {
        for(NetworkClient c : clients){
            c.returnState();
        }
    }

    public void clearAllResponses(){
        for(NetworkClient c : clients){
            c.resetResponse();
        }
    }

    public void resetAll(){
        for(NetworkClient c : clients){
            c.reset();
        }
    }
}
