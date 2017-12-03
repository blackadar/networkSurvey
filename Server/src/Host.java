import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;

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


        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        QuerySet querySet = null;

            int result = jfc.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = jfc.getSelectedFile();
                if (selectedFile.exists()) {
                    querySet = QuerySet.parseText(selectedFile.getPath());
                }
                else {
                    JOptionPane.showConfirmDialog(null, "Query Set does not exist there.", "Error", JOptionPane.DEFAULT_OPTION);
                    System.exit(1);
                }
            }


       Screen screen = Screen.getPrimary();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight());
        primaryStage.setScene(scene);


        // Start section Question and Answer
        Label query = new Label("Question");
        Label queryNum = new Label("Quesetion Zero");


        query.setFont(new Font(20));
        queryNum.setFont(new Font(20));



        BorderPane inPane = new BorderPane();
        inPane.prefHeightProperty().bind(scene.heightProperty());
        inPane.prefWidthProperty().bind(scene.widthProperty());

        VBox questionBox = new VBox(5);
        questionBox.getChildren().addAll(queryNum, query);
        questionBox.setAlignment(Pos.CENTER);

        GridPane answerPane = new GridPane();

        answerPane.setHgap(10);
        answerPane.setAlignment(Pos.CENTER);

        inPane.setTop(answerPane);
        root.setCenter(inPane);
        root.setTop(questionBox);

        primaryStage.show();

        int counter = 0;

        while(querySet.hasNext()) {
            Query query1 = querySet.getNext();

            ArrayList<Label> answers = new ArrayList<>();

            query.setText(query1.getQuery());
            queryNum.setText("Question " + counter);

            ArrayList<String> options = query1.getOptions();

            for(String answer : options) {
                Label l1 = new Label(answer);
                answers.add(l1);
                l1.setFont(new Font(20));
            }

            ArrayList<Label> leftColumnLabels = new ArrayList<>();
            ArrayList<Label> rightColumnLabels = new ArrayList<>();

            // CHANGE TO ONE HBOX WITH TWO VBOX IN IT
            // VBOX ON LEFT ALIGNS TO RIGHT AND VBOX ON RIGHT ALIGNS LEFT
            for(int i = 0; i < answers.size(); i++) {
                if((i%2) == 0) leftColumnLabels.add(answers.get(i));
                else rightColumnLabels.add(answers.get(i));
            }
            for(int i = 0; i < leftColumnLabels.size(); i++) {
                answerPane.add(leftColumnLabels.get(i), 0, i);
            }
            for(int i = 0; i < rightColumnLabels.size(); i++) {
                answerPane.add(rightColumnLabels.get(i), 0, i);
            }


            // Timer section
            Rectangle timeLeft = new Rectangle(1, scene.getHeight() / 10, Color.RED);

            root.setBottom(timeLeft);

            ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(10), timeLeft);
            scaleTransition.setToX(scene.getWidth() * 2);


            // Graph Section
            ArrayList<Rectangle> rectangles = new ArrayList<>();



            scaleTransition.play();
        }
    }

    @Override
    public void stop(){
        manager.closeAll();
    }

    private void initServer() throws IOException {
        manager = new NetworkManager(this);
        this.recruiter = manager.recruiter;
    }

    public ServerSemaphore getState(){
        return new ServerSemaphore(identity, state);
    }
}