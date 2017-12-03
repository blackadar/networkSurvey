import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;

public class Client extends Application implements QueryUpdateListener{

    Identity identity = new Identity("Client");
    NetworkServer server;
    NetworkRecruit responder;
    Query currentQuery;
    boolean[] state = new boolean[3];
    boolean hasResponded = false;

    Label status = new Label("Queued");
    ArrayList<Button> buttons = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image(Client.class.getResourceAsStream("client.png")));
        primaryStage.setTitle("Survey Client");
        //Welcome Scene
        BorderPane init = new BorderPane();
        init.setPrefSize(500,300);
        init.setPadding(new Insets(30,10,10,10));
        Label initStatus = new Label("Welcome");
        initStatus.setFont(new Font(40));
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
        root.setPadding(new Insets(10,30,10,30));

        buttons.add(new Button("  ...   "));
        buttons.add(new Button("  ...   "));
        buttons.add(new Button("  ...   "));
        buttons.add(new Button("  ...   "));

        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        ButtonBar barOne = new ButtonBar(10, buttons.get(0), buttons.get(2));
        ButtonBar barTwo = new ButtonBar(10, buttons.get(1), buttons.get(3));
        buttonBox.getChildren().addAll(barOne, barTwo);

        BorderPane.setAlignment(buttonBox, Pos.CENTER);

        root.setCenter(buttonBox);

        status.setFont(new Font(20));
        BorderPane.setAlignment(status, Pos.CENTER);

        root.setTop(status);

        Scene operate = new Scene(root);

        //Handlers
        userName.setOnAction(event -> hostAddress.requestFocus());
        hostAddress.setOnAction(event -> connectButton.fire());
        connectButton.setOnAction(event -> {
            boolean success = false;
            try{
                this.identity = new Identity(userName.getText());
                this.responder = new NetworkRecruit(this, hostAddress.getText());
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
        for(int i = 0; i < buttons.size(); i++){
            final int iToUse = i;
            buttons.get(i).setOnAction(event -> respond(new Response(currentQuery, iToUse)));
        }


        //Functionality
        primaryStage.setScene(initialize);
        primaryStage.show();
        init.requestFocus();
    }

    private void respond(Response r){
        if(!hasResponded){
            server.sendResponse(r);
            hasResponded = true;
        }
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
        Platform.runLater(() -> {
            status.setText(query.getQuery());
            buttons.get(0).setText(query.getOptions().get(0));
            buttons.get(1).setText(query.getOptions().get(1));
            buttons.get(2).setText(query.getOptions().get(2));
            buttons.get(3).setText(query.getOptions().get(3));
            hasResponded = false;
        });
    }

    /**
     * Code sourced from Stack Overflow
     * @see "https://stackoverflow.com/questions/12830402/javafx-2-buttons-size-fill-width-and-are-each-same-width"
     */
    class ButtonBar extends HBox {
        ButtonBar(double spacing, Button... buttons) {
            super(spacing);
            getChildren().addAll(buttons);
            for (Button b: buttons) {
                HBox.setHgrow(b, Priority.ALWAYS);
                b.setMaxWidth(Double.MAX_VALUE);
            }
        }

        public void addButton(Button button) {
            HBox.setHgrow(button, Priority.ALWAYS);
            button.setMaxWidth(Double.MAX_VALUE);
            ObservableList<Node> buttons = getChildren();
            if (!buttons.contains(button)) {
                buttons.add(button);
            }
        }

        public void removeButton(Button button) {
            getChildren().remove(button);
        }

        @Override protected void layoutChildren() {
            double minPrefWidth = calculatePrefChildWidth();
            for (Node n: getChildren()) {
                if (n instanceof Button) {
                    ((Button) n).setMinWidth(minPrefWidth);
                }
            }
            super.layoutChildren();
        }

        private double calculatePrefChildWidth() {
            double minPrefWidth = 0;
            for (Node n: getChildren()) {
                minPrefWidth = Math.max(minPrefWidth, n.prefWidth(-1));
            }
            return minPrefWidth;
        }
    }
}
