import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
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
import java.util.TimerTask;

public class Host extends Application implements ResponseUpdateListener{
    Identity identity = new Identity("Alpha Survey Server");
    boolean[] state = new boolean[7];
    private NetworkManager manager;
    private NetworkRecruiter recruiter;
    private QuerySet querySet;
    ArrayList<Rectangle> rectangles = new ArrayList<>();
    ArrayList<Integer> voteCount = new ArrayList<>();
    Screen screen = Screen.getPrimary();
    double totalVotes = 0;
    int counter = 1;
    Rectangle timeLeft = new Rectangle(1, screen.getBounds().getHeight()/10, Color.GREY);

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
        querySet = null;

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




        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight());
        primaryStage.setScene(scene);
        root.setStyle("-fx-border-color: cyan");

        // Start section Question and Answer
        Label query = new Label("Question");
        Label queryNum = new Label("Quesetion Zero");


        query.setFont(new Font(40));
        queryNum.setFont(new Font(40));



        BorderPane inPane = new BorderPane();
        inPane.prefHeightProperty().bind(scene.heightProperty());
        inPane.prefWidthProperty().bind(scene.widthProperty());
        inPane.setStyle("-fx-border-color: springgreen");

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



        int queryTime = querySet.getTimePerQuery() * 1000;

        //while(querySet.hasNext()) {

        java.util.Timer timer = new java.util.Timer();


        HBox graphHolder = new HBox(50);
        graphHolder.setPrefHeight(screen.getBounds().getHeight()*0.6);

        HBox labelHolder = new HBox(450);
        labelHolder.setPrefHeight(screen.getBounds().getHeight()*.1);
        labelHolder.setAlignment(Pos.CENTER);

        inPane.setCenter(graphHolder);
        inPane.setBottom(labelHolder);
        ArrayList<Label> answers = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            answers.add(new Label());
            answers.get(i).setAlignment(Pos.CENTER);
            answers.get(i).setFont(new Font(100));
            labelHolder.getChildren().add(answers.get(i));
        }
        root.setBottom(timeLeft);
        Runnable r = () -> {



            final QuerySet querySet1 = querySet;
            if (querySet1.hasNext()) {



                Platform.runLater(() -> {
                    Query query1 = querySet1.getNext();

                    // Answer Labels
                    ArrayList<String> options = query1.getOptions();

                    root.getChildren().remove(timeLeft);
                    timeLeft = new Rectangle(1, scene.getHeight() / 10, Color.GRAY);
                    root.setBottom(timeLeft);

                    query.setText(query1.getQuery());
                    queryNum.setText("Question " + counter);

                    for (int z = 0; z < answers.size(); z++) {
                        answers.get(z).setText(options.get(z));
                    }
                    // Rectangle Timer section
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(queryTime + 850), timeLeft);
                    scaleTransition.setToX(scene.getWidth() * 2);


                    // Graph Section


                    scaleTransition.play();
                    counter++;
                });




            }

        };
        r.run();
        timer.schedule(new TimerTask() {
                public void run() {
                    r.run();
                }
            }, queryTime, queryTime);


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

    @Override
    public void update(Response r) {
        totalVotes++;
        voteCount.set(r.optionSelection, voteCount.get(r.optionSelection) +1);
        updatePercentages();
    }

    public void updatePercentages() {
        ArrayList<ScaleTransition> transitions = new ArrayList<>();
        ArrayList<Thread> threads = new ArrayList<>();
         for(int i = 0; i < rectangles.size(); i++) {
             final int iToUse = i;
             transitions.set(i, new ScaleTransition(Duration.seconds(0.25), rectangles.get(i)));
             transitions.get(i).setToY((screen.getBounds().getHeight()*.6) *
                             (voteCount.get(i)/totalVotes));
             threads.set(i, new Thread(() -> {
                 transitions.get(iToUse).play();
             }));
        }

        for(Thread t : threads) {
             t.start();
        }

    }

}