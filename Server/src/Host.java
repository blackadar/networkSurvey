import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Host extends Application {
    Identity identity = new Identity("Alpha Survey Server");
    boolean[] state = new boolean[7];
    private NetworkManager manager;
    private NetworkRecruiter recruiter;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            initServer();
        } catch (IOException e) {
            //TODO: Alert user of fatal Network error
            e.printStackTrace();
        }
    }

    private void initServer() throws IOException {
        manager = new NetworkManager(this);
        recruiter = new NetworkRecruiter(manager);
    }
}