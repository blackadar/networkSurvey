import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
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
import java.util.TimerTask;
import java.util.Arrays;

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
    ArrayList<String> options;

    BarChart chart;
    CategoryAxis xAxis;
    NumberAxis yAxis;
    XYChart.Series dataSeries = new XYChart.Series();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.getIcons().add(new Image(Host.class.getResourceAsStream("server.png")));
        try {
            initServer();
        } catch (IOException e) {
            //TODO: Alert user of fatal Network error
            e.printStackTrace();
        }


        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        querySet = null;
        for(int i = 0; i < 4; i++){
            voteCount.add(0);
        }

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
        graphHolder.setAlignment(Pos.BOTTOM_CENTER);
        graphHolder.setPadding(new Insets(0, 0, 0, 50));

        HBox labelHolder = new HBox(450);

        labelHolder.setPrefHeight(screen.getBounds().getHeight()*.1);
        labelHolder.setAlignment(Pos.CENTER);

        inPane.setCenter(graphHolder);
        inPane.setBottom(labelHolder);
        ArrayList<Label> answers = new ArrayList<>();
        for(int i = 0; i < 4; i++) {
            answers.add(new Label());
            answers.get(i).setAlignment(Pos.CENTER);
            answers.get(i).setFont(new Font(20));
            labelHolder.getChildren().add(answers.get(i));
        }
        root.setBottom(timeLeft);

        /*
        for(int i = 0; i < 4; i++) {
            rectangles.add(new Rectangle((inPane.getWidth() - (5*50))/4, i*100, Color.BLACK));
            graphHolder.getChildren().add(rectangles.get(i));
        }
        */
        Runnable r = () -> {

            final QuerySet querySet1 = querySet;

            if (querySet1.hasNext()) {
                Platform.runLater(() -> {
                    Query query1 = querySet1.getNext();

                    manager.queryAll(query1);

                    // Answer Labels
                    options = query1.getOptions();

                    root.getChildren().remove(timeLeft);
                    timeLeft = new Rectangle(1, scene.getHeight() / 10, Color.GRAY);
                    root.setBottom(timeLeft);

                    query.setText(query1.getQuery());
                    queryNum.setText("Question " + counter);

                    /*
                    for (int z = 0; z < answers.size(); z++) {
                        answers.get(z).setText(options.get(z));
                    }
                    */
                    // Rectangle Timer section
                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(queryTime + 850), timeLeft);
                    scaleTransition.setToX(scene.getWidth() * 2);

                    createBarGraph(options);
                    inPane.setCenter(chart);

                    chart.getData().add(new XYChart.Data("Green", 58d));


                    /*
                    //Reset Graph
                    totalVotes = 0;
                    voteCount = new ArrayList<>();
                    updatePercentages();
                    */

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
        recruiter.close();
        manager.closeAll();
        System.exit(0);
    }

    private void initServer() throws IOException {
        manager = new NetworkManager(this);
        this.recruiter = manager.recruiter;
    }

    public ServerSemaphore getState(){
        return new ServerSemaphore(identity, state);
    }

    public void createBarGraph(ArrayList<String> options) {
        xAxis =  new CategoryAxis();
        xAxis.setCategories(FXCollections.<String>observableArrayList(options));
        xAxis.setLabel("Answers");
        yAxis = new NumberAxis("Percent Votes", 0.0d, 100d, 10.0d);

        dataSeries.setName("Answer Percentage");

        chart = new BarChart(xAxis, yAxis);
    }

    @Override
    public void update(Response r) {
        System.out.println("Got a response " + r.optionSelection);
        totalVotes++;
        System.out.println(voteCount.get(r.optionSelection));
        voteCount.set(r.optionSelection, voteCount.get(r.optionSelection) +1);
        System.out.println(voteCount.get(r.optionSelection));
        updatePercentages();
    }

    public void updatePercentages() {
        if(totalVotes == 0) return;

        for(int i = 0; i < options.size(); i++) {
            dataSeries.getData().add(new XYChart.Data(options.get(i), voteCount.get(i)/totalVotes));
        }

        chart.getData().add(dataSeries);
    }

}