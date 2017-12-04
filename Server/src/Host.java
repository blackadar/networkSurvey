import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
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
import java.net.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Arrays;

public class Host extends Application implements ResponseUpdateListener{
    Identity identity = new Identity("Alpha Survey Server");
    ArrayList<String> addresses = new ArrayList<>();
    int locationCounter = 0;
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

    private static final Object updateLock = new Object();
    private static final Object percentageLock = new Object();

    BarChart<String, Number> chart;
    CategoryAxis xAxis;
    NumberAxis yAxis;
    XYChart.Series<String, Number> dataSeries = new XYChart.Series<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws UnknownHostException, SocketException {
        //Init
        java.util.Timer timer = new java.util.Timer();
        primaryStage.getIcons().add(new Image(Host.class.getResourceAsStream("server.png")));

        // @see "https://stackoverflow.com/questions/9481865/getting-the-ip-address-of-the-current-machine-using-java"
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements())
        {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements())
            {
                InetAddress i = (InetAddress) ee.nextElement();
                if(!((i.getHostAddress().trim().startsWith("fe") || (i.getHostAddress().trim().startsWith("0:") || (i.getHostAddress().trim().equals("127.0.0.1")))))) addresses.add(i.getHostAddress());
            }
        }

        try {
            initServer();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        for(int i = 0; i < 4; i++){
            voteCount.add(0);
        }
        int result = jfc.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile.exists()) {
                querySet = QuerySet.parseText(selectedFile.getPath());
                } else {
                    JOptionPane.showConfirmDialog(null, "File Read Error.", "Error", JOptionPane.DEFAULT_OPTION);
                    System.exit(1);
                }
        }

        //Queue Scene
        BorderPane init = new BorderPane();
        init.setPadding(new Insets(10,10,10,10));
        Scene initScene = new Scene(init, 300, 300);

        VBox labelHolder = new VBox(20);

        Label status = new Label("Queueing");
        Label location = new Label(addresses.get(0));
        Button start = new Button("Start");

        labelHolder.setAlignment(Pos.CENTER);

        status.setFont(new Font(30));
        location.setFont(new Font(30));
        start.setFont(new Font(20));

        labelHolder.getChildren().addAll(status, location);
        init.setTop(labelHolder);
        init.setCenter(start);




        //SOP Scene
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 0, 10));
        Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight());

        // Start section Question and Answer
        Label query = new Label("Question");
        Label queryNum = new Label("Quesetion Zero");


        query.setFont(new Font(40));
        queryNum.setFont(new Font(40));


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


        int queryTime = querySet.getTimePerQuery() * 1000;
        root.setBottom(timeLeft);


        Runnable r = () -> {
            final QuerySet querySet1 = querySet;

            if (querySet1.hasNext()) {
                Platform.runLater(() -> {
                    Query query1 = querySet1.getNext();
                    manager.queryAll(query1);
                    options = query1.getOptions();

                    createBarGraph(options);
                    inPane.setCenter(chart);

                    root.getChildren().remove(timeLeft);
                    timeLeft = new Rectangle(1, scene.getHeight() / 10, Color.GRAY);
                    root.setBottom(timeLeft);

                    query.setText(query1.getQuery());
                    queryNum.setText("Question " + counter);

                    ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(queryTime + 850), timeLeft);
                    scaleTransition.setToX(scene.getWidth() * 2);

                    scaleTransition.play();
                    counter++;
                });
            } else {
                manager.closeAll();
            }

        };

        //Handlers
        start.setOnAction(event -> {
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            timer.purge();
            r.run();
            timer.schedule(new TimerTask() {
                public void run() {
                    manager.writeResponsesToFile();
                    clearVoteCounts();
                    r.run();
                }
            }, queryTime, queryTime);
        });

        timer.schedule(new TimerTask() {
            public void run(){
                if(locationCounter + 1 < addresses.size()){
                    locationCounter++;
                } else {
                    locationCounter = 0;
                }
                Platform.runLater(() -> location.setText(addresses.get(locationCounter)));
            }
        }, 0, 3000);

        //Logic
        primaryStage.setScene(initScene);
        primaryStage.show();

    }

    @Override
    public void stop(){
        recruiter.close();
        manager.closeAll();
        System.exit(0);
    }

    private void clearVoteCounts(){
        dataSeries.getData().clear();
        for(int i = 0; i < voteCount.size(); i++){
            voteCount.set(i,0);
        }
        totalVotes = 0;
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
        xAxis.setCategories(FXCollections.observableArrayList(options));
        xAxis.tickLabelFontProperty().set(Font.font(20));
        yAxis = new NumberAxis("Percent Votes", 0.0d, 100d, 10.0d);
        yAxis.setAutoRanging(false);
        chart = new BarChart<>(xAxis, yAxis);
        chart.setAnimated(true);
        chart.setLegendVisible(false);
        chart.getData().add(dataSeries);
    }

    @Override
    public void update(Response r) {
        synchronized (updateLock) {
            System.out.println("Got a response " + r.optionSelection);
            totalVotes++;
            voteCount.set(r.optionSelection, voteCount.get(r.optionSelection) + 1);
            updatePercentages();
        }
    }

    public void updatePercentages() {
        synchronized (percentageLock) {
            System.out.println("Updating Percentages: \n" +
                    "Total Votes: " + totalVotes + "\n" +
                    "Vote Counts: " + voteCount + "\n");

            if (totalVotes == 0) return;
            Platform.runLater(() -> {
                dataSeries.getData().clear();

                for (int i = 0; i < options.size(); i++) {

                    dataSeries.getData().add(new XYChart.Data<>(options.get(i), (voteCount.get(i) / totalVotes) * 100.0));
                }

                System.out.println("Data Series: " + dataSeries.getData());
            });
        }
    }

}