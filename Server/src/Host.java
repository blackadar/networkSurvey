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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.Time;

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
       /* try {
            initServer();
        } catch (IOException e) {
            //TODO: Alert user of fatal Network error
            e.printStackTrace();
        }
        */

       Screen screen = Screen.getPrimary();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 10, 10, 10));
        Scene scene = new Scene(root, screen.getVisualBounds().getWidth(), screen.getVisualBounds().getHeight());
        primaryStage.setScene(scene);

        Label query = new Label("Question");
        Label queryNum = new Label("Quesetion Zero");
        Label answerOne = new Label("Same Answer One!!!!!");
        Label answerTwo = new Label("Same Answer Two!!!!!");
        Label answerThree = new Label("Same Answer Three!!!!!");
        Label answerFour = new Label("Same Answer Four!!!!!");
        query.setFont(new Font("Arial", 40));
        queryNum.setFont(new Font("Arial", 40));
        answerOne.setFont(new Font("Arial", 40));
        answerTwo.setFont(new Font("Arial", 40));
        answerThree.setFont(new Font("Arial", 40));
        answerFour.setFont(new Font("Arial", 100));

        BorderPane inPane = new BorderPane();
        inPane.prefHeightProperty().bind(scene.heightProperty());
        inPane.prefWidthProperty().bind(scene.widthProperty());

        VBox questionBox = new VBox(5);
        questionBox.getChildren().addAll(queryNum, query);
        questionBox.setAlignment(Pos.CENTER);

        VBox answerBox = new VBox(10);
        answerBox.getChildren().addAll(answerOne, answerTwo, answerThree, answerFour);
        answerBox.setAlignment(Pos.CENTER);

        Rectangle timeLeft = new Rectangle(1, scene.getHeight()/10, Color.RED);

        root.setBottom(timeLeft);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(10), timeLeft);
        scaleTransition.setByX(scene.getWidth() * 2);


        inPane.setTop(answerBox);
        root.setCenter(inPane);
        root.setTop(questionBox);

        scaleTransition.play();
        primaryStage.show();
    }

   /* public Timeline createTimeline(int time, Rectangle timeLeft, double sceneWidth) {
        Timeline progressTime = new Timeline();
        progressTime.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO),
                new KeyFrame(new Duration(time *10),
                        new KeyValue(timeLeft.set,timeLeft.getWidth() + (sceneWidth/100)))
                )
        );

        return progressTime;
    }
    */

    private void initServer() throws IOException {
        manager = new NetworkManager(this);
        recruiter = new NetworkRecruiter(manager);
    }

    public ServerSemaphore getState(){
        return new ServerSemaphore(identity, state);
    }
}