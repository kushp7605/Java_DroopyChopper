package com.example.droopychopper;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.geometry.Pos;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/*
  The MainApp class represents the main entry point for the Droopy Chopper game application.
  It initializes the game menu, handles user input, and starts the game.
*/
public class MainApp extends Application {
    // Boolean Flag to indicate if the game is set
    public static boolean GAME_SET;
    // MediaPlayer for background music
    private MediaPlayer mediaPlayer;
    // Primary stage for the application
    private Stage primaryStage;

    /*
      Creates the content for the game menu.
      Return the root node containing all the menu elements
    */
    private Parent createContent() {
        Pane root = new Pane();

        // Background rectangle
        Rectangle bg = new Rectangle(700, 500);
        bg.setFill(Color.rgb(139, 0, 0));

        // Title image with fade-in animation
        ImageView title = new ImageView(new Image(getClass().getResource("/images/title.png").toExternalForm()));
        title.setFitWidth(350);
        title.setFitHeight(100);
        title.setOpacity(0);

        // Droopy Chopper image with fade-in animation
        ImageView droopyChopper = new ImageView(new Image(getClass().getResource("/images/chopper2.gif").toExternalForm()));
        droopyChopper.setFitWidth(150);
        droopyChopper.setFitHeight(50);
        droopyChopper.setOpacity(0);

        // Start button image
        ImageView startButton = new ImageView(new Image(getClass().getResource("/images/start.png").toExternalForm()));
        startButton.setFitWidth(100);
        startButton.setFitHeight(50);
        startButton.setOnMouseClicked(e -> startGame());

        // Exit button image
        ImageView exitButton = new ImageView(new Image(getClass().getResource("/images/exit.png").toExternalForm()));
        exitButton.setFitWidth(100);
        exitButton.setFitHeight(50);
        exitButton.setOnMouseClicked(e -> System.exit(0));

        // HBox to hold start and exit buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, exitButton);

        // Text instruction
        Text instructionText = new Text("Press Space bar to start, Press `ESC` to exit");
        instructionText.setFont(new Font(20));
        instructionText.setFill(Color.LIGHTBLUE);

        // Display high score
        int highScore = getHighScore();
        Text highScoreText = new Text("High Score: " + highScore);
        highScoreText.setFont(new Font(20));
        highScoreText.setFill(Color.LIGHTBLUE);

        // VBox to hold the buttons, instruction text, and high score text
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(buttonBox, instructionText, highScoreText);
        vbox.setOpacity(0);

        // VBox to hold the title, droopy chopper, and the vbox with buttons and instruction
        VBox mainBox = new VBox(30);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setLayoutX(140);
        mainBox.setLayoutY(50);
        mainBox.getChildren().addAll(title, droopyChopper, vbox);

        // Adding all elements to the root pane
        root.getChildren().addAll(bg, mainBox);

        // Background music with the file path
        String musicFile = getClass().getResource("/sounds/wing.mp3").toExternalForm();
        Media media = new Media(musicFile);
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();

        // Blink animation for background
        Timeline blinkBg = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(bg.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(bg.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(bg.opacityProperty(), 1))
        );
        blinkBg.setCycleCount(4);

        // Blink animation for title, droopyChopper, and vbox
        Timeline blinkElements = new Timeline(
                new KeyFrame(Duration.seconds(0), new KeyValue(title.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(title.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(title.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0), new KeyValue(droopyChopper.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(droopyChopper.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(droopyChopper.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0), new KeyValue(vbox.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.5), new KeyValue(vbox.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(1), new KeyValue(vbox.opacityProperty(), 1))
        );
        blinkElements.setCycleCount(4);

        // Play background blink animation first, then play elements blink animation
        blinkBg.setOnFinished(event -> blinkElements.play());
        blinkBg.play();

        return root;
    }


    /*
      Retrieves the high score from a log file.
      Return the highest score found in the log file
    */
    private int getHighScore() {
        int highScore = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("logs.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Score:")) {
                    String[] parts = line.split("\\|");
                    int score = Integer.parseInt(parts[0].trim().split(":")[1].trim());
                    if (score > highScore) {
                        highScore = score;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return highScore;
    }

    /*
      Starts the game by creating a new game stage and closing the menu stage.
    */
    public void startGame() {
        if (!GAME_SET) {
            GAME_SET = true;
            mediaPlayer.stop();
            DroopyChopper game = new DroopyChopper();
            Stage gameStage = new Stage();
            primaryStage.close();
            try {
                game.start(gameStage);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    /*
     Starts the application by initializing the primary stage and setting up the scene.
    */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Scene scene = new Scene(createContent());

        // Add key event handler to the scene
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                startGame();
            }
        });

        primaryStage.setTitle("Droopy Chopper");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    // The main method to launch the application.
    public static void main(String[] args) {
        launch(args);
    }
}
