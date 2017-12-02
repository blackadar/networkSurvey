import javafx.application.Application;
import javafx.stage.Stage;

public class Client extends Application {

    Identity identity = new Identity("Client");
    boolean[] state = new boolean[3];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) { }

    public ClientSemaphore getState(){
        return new ClientSemaphore(identity, state);
    }
}
