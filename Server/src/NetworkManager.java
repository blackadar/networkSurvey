import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class NetworkManager implements ResponseUpdateListener{
    ArrayList<NetworkClient> clients = new ArrayList<>();
    Host host;
    Query lastQuery;
    NetworkRecruiter recruiter;
    Writer writer;
    boolean fileHeaderWritten;
    ArrayList<ResponseUpdateListener> listeners = new ArrayList<>();

    public NetworkManager(Host host) throws IOException {
        this.host = host;
        this.recruiter = new NetworkRecruiter(this);
        this.writer = new Writer("results.csv");
        listeners.add(host);
    }

    public void addClient(NetworkClient c){
        clients.add(c);
    }

    public void removeClient(NetworkClient c){
        c.close();
        clients.remove(c);
    }

    public void queryAll(Query query){
        lastQuery = query;
        for(NetworkClient c : clients){
            c.sendQuery(query);
        }
    }

    public ArrayList<Response> getResponses(){
        ArrayList<Response> responses = new ArrayList<>();
        for(NetworkClient c : clients){
            responses.add(c.currentResponse);
        }
        return responses;
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

    public void writeResponsesToFile(){
        if(!fileHeaderWritten){
            writer.write("Query");
            for(NetworkClient c : clients){
                writer.write(c.identity.getName());
            }
            writer.newLine();
            fileHeaderWritten = true;
        }
        writer.write(lastQuery.getQuery());
        for(NetworkClient c : clients){
            if(c.hasResponse()){
                writer.write(c.currentResponse.responseTo.getOptions().get(c.currentResponse.optionSelection) + "");
            } else {
                writer.write("x");
            }
        }
        writer.newLine();
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

    public void closeAll(){
        recruiter.close();
        writer.close();
        /*
        for(NetworkClient c : clients){
            c.close();
        }
        */
    }

    @Override
    public void update(Response r) {
        for(ResponseUpdateListener res : listeners){
            res.update(r);
        }
    }
}
