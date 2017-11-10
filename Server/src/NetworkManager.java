import java.util.ArrayList;

public class NetworkManager {
    ArrayList<NetworkClient> clients = new ArrayList<>();

    public void addClient(NetworkClient c){
        clients.add(c);
    }
}
