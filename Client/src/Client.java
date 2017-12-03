import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Client extends Application implements QueryUpdateListener{

    Identity identity = new Identity("Client");
    NetworkServer server;
    NetworkResponder responder;
    Query currentQuery;
    boolean[] state = new boolean[3];

    Label status;
    ArrayList<Button> buttons = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //Welcome Scene
        BorderPane init = new BorderPane();
        init.setPrefSize(500,300);
        init.setPadding(new Insets(30,10,10,10));
        Label initStatus = new Label("Welcome");
        initStatus.setFont(new Font(20));
        BorderPane.setAlignment(initStatus, Pos.CENTER);
        init.setTop(initStatus);

        GridPane textFields = new GridPane();
        TextField userName = new TextField();
        userName.setPromptText("User Name or ID");
        TextField hostAddress = new TextField();
        hostAddress.setPromptText("Server IP");
        Button connectButton = new Button("Connect");

        textFields.addColumn(0, userName, hostAddress, connectButton);
        textFields.setAlignment(Pos.CENTER);
        textFields.setVgap(20);
        GridPane.setHalignment(connectButton, HPos.CENTER);
        init.setCenter(textFields);
        Scene initialize = new Scene(init);

        //Normal Operating Scene
        BorderPane root = new BorderPane();
        root.setPrefSize(500,300);
        root.setPadding(new Insets(10,10,10,10));

        GridPane buttonPane = new GridPane();
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setHgap(20);
        buttonPane.setVgap(20);
        buttons.add(new Button("  ...   "));
        buttons.add(new Button("  ...   "));
        buttons.add(new Button("  ...   "));
        buttons.add(new Button("  ...   "));

        buttonPane.addColumn(0, buttons.get(0), buttons.get(2));
        buttonPane.addColumn(1, buttons.get(1), buttons.get(3));

        BorderPane.setAlignment(buttonPane, Pos.CENTER);
        root.setCenter(buttonPane);

        Label status = new Label("Queued");
        status.setFont(new Font(20));
        BorderPane.setAlignment(status, Pos.CENTER);

        root.setTop(status);

        Scene operate = new Scene(root);

        //Handlers
        connectButton.setOnAction(event -> {
            boolean success = false;
            try{
                this.identity = new Identity(userName.getText());
                this.responder = new NetworkResponder(this, hostAddress.getText());
                this.server = responder.connect();
                success = true;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if(success){
                primaryStage.setScene(operate);
                primaryStage.show();
                root.requestFocus();
            } else {
                initStatus.setText("Connection Refused");
            }
        });


        //Functionality
        primaryStage.setScene(initialize);
        primaryStage.show();
        init.requestFocus();
    }

    @Override
    public void stop() throws IOException {
        server.close();
        responder.close();
    }

    public ClientSemaphore getState(){
        return new ClientSemaphore(identity, state);
    }

    @Override
    public void update(Query query) {
        this.currentQuery = query;
        status.setText(query.getQuery());
        buttons.get(0).setText(query.getOptions().get(0));
        buttons.get(1).setText(query.getOptions().get(1));
        buttons.get(2).setText(query.getOptions().get(2));
        buttons.get(3).setText(query.getOptions().get(3));
    }
}
