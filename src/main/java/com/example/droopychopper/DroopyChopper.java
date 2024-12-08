package com.example.droopychopper;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class DroopyChopper extends Application {
    // Constants for application dimensions
    private int APP_HEIGHT = 700;
    private int APP_WIDTH = 400;

    // Variables to track the score and game state
    private int TOTAL_SCORE = 0;
    private long spaceClickA;
    private double motionTime, elapsedTime;
    private boolean CLICKED, GAME_START, HIT_PIPE, GAME_OVER, MUSIC_MUTED = false;

    // Used to manage the start time of the game
    private LongValue startNanoTime;

    // Sprites for game elements
    private Sprite firstFloor, secondFloor, chopperSprite;

    // Instance of the Chopper class (representing the player character)
    private Chopper chopper;

    // Label to display the score
    private Text scoreLabel;

    // Graphics context for rendering
    private GraphicsContext gc, chopperGC;

    // Timer for game animation
    private AnimationTimer timer;

    // List to hold pipe objects in the game
    private ArrayList<Pipe> pipes;

    // Sound effects for various actions in the game
    private Sound coin, hit, wing, swoosh, die, credit, gunOne, gunTwo, rain;

    // ImageViews for game over screen and other UI elements
    private ImageView gameOver, startGame, creditsIcon;

    // Root group for the scene
    private Group root;

    // ImageView for mute button
    private ImageView muteButtonImage;


    /*
      Setting up the scene and stage
    */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Droopy Chopper");
        primaryStage.setResizable(false);

        Parent root = getContent();
        Scene main = new Scene(root, APP_WIDTH, APP_HEIGHT);
        setKeyFunctions(main);
        primaryStage.setScene(main);
        primaryStage.show();

        startGame();
    }

    /*
      Method to set up the key event handling
    */
    private void setKeyFunctions(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // Check if the SPACE key is pressed
            if (e.getCode() == KeyCode.SPACE) {
                setOnUserInput();
            }
            // Check if the ESCAPE key is pressed
            else if (e.getCode() == KeyCode.ESCAPE) {
                Platform.exit(); // Exit the application
            }
        });
    }

    /*
      Method to handle user input that is spacebar press
    */
    private void setOnUserInput() {
        // Check if the chopper has not hit a pipe
        if (!HIT_PIPE) {
            CLICKED = true; // Mark that the user has clicked using boolean flag

            // Check if the game has not started yet
            if (!GAME_START) {
                root.getChildren().remove(startGame); // Remove the start game image from the scene
                swoosh.playClip(); // Play swoosh sound effect
                GAME_START = true; // Set the game start boolean flag to true
            } else {
                wing.playClip(); // Play wing flapping sound effect
                spaceClickA = System.currentTimeMillis(); // Record the time of the spacebar click
                chopperSprite.setVelocity(0, -350); // Set the upward velocity of the chopper
            }
        }

        // Check if the game is over
        if (GAME_OVER) {
            startNewGame(); // Start a new game
        }
    }

    /*
      Method to create the main content of the game scene
    */
    private Parent getContent() {
        // Create the root group for the scene
        root = new Group();

        // Create canvas for drawing the game elements
        Canvas canvas = new Canvas(APP_WIDTH, APP_HEIGHT);
        Canvas chopperCanvas = new Canvas(APP_WIDTH, APP_HEIGHT);

        // Get the graphics context for drawing on the canvases
        gc = canvas.getGraphicsContext2D();
        chopperGC = chopperCanvas.getGraphicsContext2D();

        ImageView bg = setBackground(); // Set the background image
        setFloor(); // Initializes the floor elements
        pipes = new ArrayList<>(); // Initializes the list of pipes
        setPipes(); // Initialize and position the pipes
        setChopper(); // Initialize and position the chopper sprite
        setLabels(); // Set the score and other labels
        setSounds(); // Initialize the sound effects

        // Create and position the mute button image with the unmute button file path
        muteButtonImage = new ImageView(new Image(getClass().getResource("/images/unmute.png").toExternalForm()));
        muteButtonImage.setFitWidth(30);
        muteButtonImage.setFitHeight(30);
        muteButtonImage.setLayoutX(APP_WIDTH - 40);
        muteButtonImage.setLayoutY(50);
        // Set the action for mute button click
        muteButtonImage.setOnMouseClicked(e -> toggleMuteMusic());

        // Add all the game elements to the root group and return the root group as the content of the scene
        root.getChildren().addAll(bg, canvas, chopperCanvas, scoreLabel, startGame, creditsIcon, muteButtonImage);
        return root;
    }

    /*
       Method to toggle mute/unmute state of the music and sound effects
    */
    private void toggleMuteMusic() {
        // Toggle the MUSIC_MUTED boolean value
        MUSIC_MUTED = !MUSIC_MUTED;
        // Check if the music is muted
        if (MUSIC_MUTED) {
            // Change the mute button image to show the "mute" icon
            muteButtonImage.setImage(new Image(getClass().getResource("/images/mute.png").toExternalForm()));
            // Mute all sound effects
            coin.mute();
            hit.mute();
            wing.mute();
            swoosh.mute();
            die.mute();
            credit.mute();
            gunOne.mute();
            gunTwo.mute();
        } else {
            // Change the mute button image to show the "unmute" icon
            muteButtonImage.setImage(new Image(getClass().getResource("/images/unmute.png").toExternalForm()));
            // Unmute all sound effects
            coin.unmute();
            hit.unmute();
            wing.unmute();
            swoosh.unmute();
            die.unmute();
            credit.unmute();
            gunOne.unmute();
            gunTwo.unmute();
        }
    }

    /*
      Method to set the background image
    */
    private ImageView setBackground() {
        Random random = new Random();
        // Generate a random number (0 or 1)
        int bg = random.nextInt(2);
        // Choose the file path based on the random number
        String filePath = bg > 0 ? "/images/background.gif" : "/images/background_night.png";

        // Create an ImageView with the chosen background image
        ImageView imageView = new ImageView(new Image(getClass().getResource(filePath).toExternalForm()));
        imageView.setFitWidth(APP_WIDTH);
        imageView.setFitHeight(APP_HEIGHT);
        return imageView; // Return the imageView
    }

    /*
       Method to set up the game labels and UI elements
    */
    private void setLabels() {
        // Initialization of Score label
        scoreLabel = new Text("0");
        scoreLabel.setFont(Font.font("Courier", FontWeight.EXTRA_BOLD, 50));
        scoreLabel.setStroke(Color.BLACK); // Border color of the text
        scoreLabel.setFill(Color.WHITE); // Fill color of the text
        scoreLabel.setLayoutX(20); // X-axis position within the scene
        scoreLabel.setLayoutY(40); // Y-axis position within the scene

        // Initialization of game over image
        gameOver = new ImageView(new Image(getClass().getResource("/images/game_over.png").toExternalForm()));
        gameOver.setFitWidth(178); // Set width of the game over image
        gameOver.setFitHeight(50); // Set height of the game over image
        gameOver.setLayoutX(110); // X-axis position within the scene
        gameOver.setLayoutY(100); // Y-axis position within the scene

        // Initialization of ready/start game image
        startGame = new ImageView(new Image(getClass().getResource("/images/ready.png").toExternalForm()));
        startGame.setFitWidth(178); // Set width of the start game image
        startGame.setFitHeight(50); // Set height of the start game image
        startGame.setLayoutX(100); // X-axis position within the scene
        startGame.setLayoutY(100); // Y-axis position within the scene


        // Initialization of credits icon
        creditsIcon = new ImageView(new Image(getClass().getResource("/images/credit.png").toExternalForm()));
        creditsIcon.setFitWidth(30); // Set width of the credits icon
        creditsIcon.setFitHeight(30); // Set height of the credits icon
        creditsIcon.setLayoutX(APP_WIDTH - 40); // X-axis position near the right edge of the window
        creditsIcon.setLayoutY(10); // Y-axis position near the top edge of the window

        // Set action for mouse click on credits icon
        creditsIcon.setOnMouseClicked(e -> showCredits());
    }

    /*
       Method to display the credits window
    */
    private void showCredits() {
        // Create a new stage for the credits window
        Stage creditsStage = new Stage();
        // Set modality to block input events to other windows
        creditsStage.initModality(Modality.APPLICATION_MODAL);
        // Set stage style to utility (no decorations)
        creditsStage.initStyle(StageStyle.UTILITY);
        // Set title of the credits window
        creditsStage.setTitle("Credits");

        // Play background music and loop rain sound effect
        credit.playClip(); // Play credits background music
        rain.loopClip(); // Loop rain sound effect

        // Stop the music and rain sound effect when the credits window is closed
        creditsStage.setOnCloseRequest(event -> {
            credit.stop(); // Stop credits background music
            rain.stop(); // Stop looping rain sound effect
        });

        // Heading text for credits screen
        Text heading = new Text("Kingslayers");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        heading.setFill(Color.YELLOWGREEN);
        heading.setLayoutX(100);
        heading.setLayoutY(50);
        heading.setEffect(new DropShadow(20, Color.WHITE)); // Apply a glow effect with radius and color

        // Credits text for developers
        gunTwo.playClip(); // Play a sound effect
        Text developedBy = new Text("Developed by Sirjandeep Singh, Kush Patel and Nil Dankhara");
        developedBy.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        developedBy.setFill(Color.RED);
        developedBy.setLayoutX(50);
        developedBy.setLayoutY(100);

        // Credits for external assets - Chopper Image
        Text helicopterCredits = new Text("Chopper: https://opengameart.org/content/helicopter-2");
        helicopterCredits.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        helicopterCredits.setFill(Color.DARKKHAKI);
        helicopterCredits.setLayoutX(50);
        helicopterCredits.setLayoutY(130);

        // Credits for start screen buttons
        Text startScreenCredits = new Text("Start Screen Buttons: https://opengameart.org/content/a-pack-of-games-buttons-2");
        startScreenCredits.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        startScreenCredits.setFill(Color.DARKKHAKI);
        startScreenCredits.setLayoutX(50);
        startScreenCredits.setLayoutY(160);

        // Credits for mute/unmute icons
        Text muteCredits = new Text("Mute: https://icons8.com/icon/set/no-volume/family-material--static--white \n Unmute: https://icons8.com/icons/set/volume--static--white");
        muteCredits.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        muteCredits.setFill(Color.DARKKHAKI);
        muteCredits.setLayoutX(50);
        muteCredits.setLayoutY(190);

        // Credits for profile button icon
        Text profileButtonCredits = new Text("Profile Button: https://www.flaticon.com/free-icon/coding_3242257?term=developer&page=1&position=6&origin=search&related_id=3242257");
        profileButtonCredits.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        profileButtonCredits.setFill(Color.DARKKHAKI);
        profileButtonCredits.setLayoutX(50);
        profileButtonCredits.setLayoutY(250);

        // Credits for background images and various assets created with the help of Canva
        Text canvaCredits = new Text("Background Images: Made with the help of Canva\n Up Pipe and Down Pipe: Made with the help of Canva \n Title Image: Made with the help of Canva\nGame Over Image: Made with the help of Canva \n Floor Image: Made with the help of Canva\nGet Ready Image: Made with the help of Canva ");
        canvaCredits.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        canvaCredits.setFill(Color.DARKKHAKI);
        canvaCredits.setLayoutX(50);
        canvaCredits.setLayoutY(550);

        // Credits for sound and music sources
        Text wingCredits = new Text("Wing.mp3: https://pixabay.com/sound-effects/military-rotor-loop-106105/");
        wingCredits.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        wingCredits.setFill(Color.DARKKHAKI);
        wingCredits.setLayoutX(50);
        wingCredits.setLayoutY(280);

        Text musicCredits1 = new Text("Music1: https://pixabay.com/sound-effects/9mm-pistol-shoot-short-reverb-7152/");
        musicCredits1.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        musicCredits1.setFill(Color.DARKKHAKI);
        musicCredits1.setLayoutX(50);
        musicCredits1.setLayoutY(320);

        Text musicCredits3 = new Text("Music3: https://pixabay.com/sound-effects/realistic-gun-fire-100696/ \n Rain: https://pixabay.com/sound-effects/pouring-water-100696/ \n  die: https://pixabay.com/sound-effects/game-over-38511/ \n hit: https://pixabay.com/sound-effects/glass-cinematic-hit-161212/ \n score:https://pixabay.com/sound-effects/8-bit-video-game-points-version-1-145826/ \n swoosh:https://pixabay.com/sound-effects/slow-wind-sound-effect-108401/ ");
        musicCredits3.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        musicCredits3.setFill(Color.DARKKHAKI);
        musicCredits3.setLayoutX(50);
        musicCredits3.setLayoutY(360);

        // Initial opacity set to 0 for fade-in effect when displayed
        heading.setOpacity(0);
        developedBy.setOpacity(0);
        helicopterCredits.setOpacity(0);
        startScreenCredits.setOpacity(0);
        muteCredits.setOpacity(0);
        profileButtonCredits.setOpacity(0);
        canvaCredits.setOpacity(0);
        wingCredits.setOpacity(0);
        musicCredits1.setOpacity(0);
        musicCredits3.setOpacity(0);

        /*
          Fade transitions for each credit text
        */
        FadeTransition fadeHeading = new FadeTransition(Duration.seconds(1), heading);
        fadeHeading.setToValue(1);

        FadeTransition fadeDevelopedBy = new FadeTransition(Duration.seconds(1), developedBy);
        fadeDevelopedBy.setToValue(1);
        fadeDevelopedBy.setDelay(Duration.seconds(1));

        FadeTransition fadeHelicopterCredits = new FadeTransition(Duration.seconds(1), helicopterCredits);
        fadeHelicopterCredits.setToValue(1);
        fadeHelicopterCredits.setDelay(Duration.seconds(2));

        FadeTransition fadeStartScreenCredits = new FadeTransition(Duration.seconds(1), startScreenCredits);
        fadeStartScreenCredits.setToValue(1);
        fadeStartScreenCredits.setDelay(Duration.seconds(3));

        FadeTransition fadeMuteCredits = new FadeTransition(Duration.seconds(1), muteCredits);
        fadeMuteCredits.setToValue(1);
        fadeMuteCredits.setDelay(Duration.seconds(4));

        FadeTransition fadeProfileButtonCredits = new FadeTransition(Duration.seconds(1), profileButtonCredits);
        fadeProfileButtonCredits.setToValue(1);
        fadeProfileButtonCredits.setDelay(Duration.seconds(5));

        FadeTransition fadeCanvaCredits = new FadeTransition(Duration.seconds(1), canvaCredits);
        fadeCanvaCredits.setToValue(1);
        fadeCanvaCredits.setDelay(Duration.seconds(6));

        FadeTransition fadeWingCredits = new FadeTransition(Duration.seconds(1), wingCredits);
        fadeWingCredits.setToValue(1);
        fadeWingCredits.setDelay(Duration.seconds(7));

        FadeTransition fadeMusicCredits1 = new FadeTransition(Duration.seconds(1), musicCredits1);
        fadeMusicCredits1.setToValue(1);
        fadeMusicCredits1.setDelay(Duration.seconds(8));

        FadeTransition fadeMusicCredits3 = new FadeTransition(Duration.seconds(1), musicCredits3);
        fadeMusicCredits3.setToValue(1);
        fadeMusicCredits3.setDelay(Duration.seconds(9));

        /*
          Parallel transitions for texts that can fade in simultaneously
        */
        ParallelTransition parallelDevelopedBy = new ParallelTransition(fadeDevelopedBy);
        ParallelTransition parallelHelicopterCredits = new ParallelTransition(fadeHelicopterCredits);
        ParallelTransition parallelStartScreenCredits = new ParallelTransition(fadeStartScreenCredits);
        ParallelTransition parallelMuteCredits = new ParallelTransition(fadeMuteCredits);
        ParallelTransition parallelProfileButtonCredits = new ParallelTransition(fadeProfileButtonCredits);
        ParallelTransition parallelCanvaCredits = new ParallelTransition(fadeCanvaCredits);
        ParallelTransition parallelWingCredits = new ParallelTransition(fadeWingCredits);
        ParallelTransition parallelMusicCredits1 = new ParallelTransition(fadeMusicCredits1);
        ParallelTransition parallelMusicCredits3 = new ParallelTransition(fadeMusicCredits3);

        /*
           Sequential transition to play animations one after another
        */
        SequentialTransition sequentialTransition = new SequentialTransition(
                fadeHeading, parallelDevelopedBy, parallelHelicopterCredits, parallelStartScreenCredits,
                parallelMuteCredits, parallelProfileButtonCredits, parallelCanvaCredits,
                parallelWingCredits, parallelMusicCredits1, parallelMusicCredits3
        );

        // Action to perform after all animations finish
        sequentialTransition.setOnFinished(event -> {
            // Play additional sounds after animations
            gunOne.playClip();
        });

        /*
          Group for all credits text elements
        */
        Group creditsRoot = new Group(heading, developedBy, helicopterCredits, startScreenCredits, muteCredits,
                profileButtonCredits, canvaCredits, wingCredits, musicCredits1, musicCredits3);

        // Create scene with black background
        Scene creditsScene = new Scene(creditsRoot, 1100, 800, Color.BLACK);

        // Set the scene in the stage
        creditsStage.setScene(creditsScene);
        // Show the stage with credits
        creditsStage.show();

        // Play the sequential transition to start the fade-in animations
        sequentialTransition.play();
    }

    /*
       Method for the Sound Effects
    */
    private void setSounds() {
        // Initialize sound objects with respective sound files
        coin = new Sound("/sounds/score.mp3");
        hit = new Sound("/sounds/hit.mp3");
        wing = new Sound("/sounds/wing.mp3");
        swoosh = new Sound("/sounds/swoosh.mp3");
        die = new Sound("/sounds/die.mp3");
        credit = new Sound("/sounds/music3.mp3");
        gunOne = new Sound("/sounds/music2.mp3");
        gunTwo = new Sound("/sounds/music2.mp3");
        rain = new Sound("/sounds/rain.mp3");
    }

    /*
       Method for rendering the chopper
    */
    private void setChopper() {
        // Initialize chopper object and its sprite for rendering
        chopper = new Chopper();
        chopperSprite = chopper.getChopper(); // Get the sprite object of the chopper
        chopperSprite.render(gc); // Render the chopper sprite on the main graphics context
    }

    /*
       Method for initializing the floor sprites
    */
    private void setFloor() {
        // Initialize two floor sprites for scrolling effect
        firstFloor = new Sprite();
        // Resize and set image for first floor
        firstFloor.resizeImage("/images/floor.png", 400, 140);
        // Set initial position for first floor
        firstFloor.setPositionXY(0, APP_HEIGHT - 100);
        // Set velocity for horizontal scrolling
        firstFloor.setVelocity(-.4, 0);
        // Render first floor on dedicated chopper graphics context
        firstFloor.render(chopperGC);

        secondFloor = new Sprite();
        // Resize and set image for second floor
        secondFloor.resizeImage("/images/floor.png", 400, 140);
        // Position second floor next to first floor
        secondFloor.setPositionXY(firstFloor.getWidth(), APP_HEIGHT - 100);
        // Set velocity for horizontal scrolling
        secondFloor.setVelocity(-.4, 0);
        // Render second floor on main graphics context
        secondFloor.render(gc);
    }

    /*
       Method for the starting the game
    */
    private void startGame() {
        // Stop the die sound if it was playing
        die.stop();
        // Record the starting time in nanoseconds
        startNanoTime = new LongValue(System.nanoTime());

        timer = new AnimationTimer() {
            public void handle(long now) {
                // Calculate elapsed time since the last frame in seconds
                elapsedTime = (now - startNanoTime.value) / 1000000000.0;
                // Update startNanoTime for the next frame
                startNanoTime.value = now;

                // Clear both graphics contexts
                gc.clearRect(0, 0, APP_WIDTH, APP_HEIGHT);
                chopperGC.clearRect(0, 0, APP_WIDTH, APP_HEIGHT);

                // Move the floor sprites horizontally
                moveFloor();
                // Check the time between space hits for user input
                checkTimeBetweenSpaceHits();

                if (GAME_START) {
                    // Render pipes on the screen
                    renderPipes();
                    // Check and handle scrolling of pipes
                    checkPipeScroll();
                    // Update the total score based on game progress
                    updateTotalScore();

                    // Check if chopper hits a pipe
                    if (chopperHitPipe()) {
                        // Display game over image
                        root.getChildren().add(gameOver);
                        stopScroll(); // Stop scrolling
                        playHitSound(); // Play hit sound effect
                        motionTime += 0.18;
                        if (motionTime > 0.5) {
                            // Add a sudden upward motion to chopper sprite when hit
                            chopperSprite.addVelocity(-200, 400);
                            chopperSprite.render(gc); // Render chopper sprite
                            chopperSprite.update(elapsedTime); // Update chopper position
                            motionTime = 0; // Reset motion time
                        }
                    }

                    // Check if chopper hits the floor
                    if (chopperHitFloor()) {
                        if (!root.getChildren().contains(gameOver)) {
                            root.getChildren().add(gameOver); // Display game over image
                            playHitSound(); // Play hit sound effect
                            showHitEffect(); // Show hit effect
                        }
                        timer.stop(); // Stop the game timer
                        GAME_OVER = true; // Set game over using boolean flag
                        die.playClip(); // Play die sound effect
                        writeScoreToFile(); // Write current score to file
                    }
                }
            }
        };
        // Start the animation timer
        timer.start();
    }

    /*
       Method for the restarting the game
    */
    private void startNewGame() {
        // Remove game over image from root
        root.getChildren().remove(gameOver);
        // Add start game image to root
        root.getChildren().add(startGame);
        pipes.clear(); // Clear existing pipes
        setFloor(); // Reset floor sprites
        setPipes(); // Reset pipes
        setChopper(); // Reset chopper
        resetVariables(); // Reset game variables
        startGame(); // Start the game again
    }

    /*
      Reset game variables to their initial state
    */
    private void resetVariables() {
        // Reset score label
        updateScoreLabel(0);
        // Reset total score
        TOTAL_SCORE = 0;
        HIT_PIPE = false; // Reset pipe hit using boolean flag
        CLICKED = false; // Reset click  using boolean flag
        GAME_OVER = false; // Reset game over using boolean flag
        GAME_START = false; // Reset game start using boolean flag
    }

    /*
       Check the time elapsed between space key hits for chopper movement
    */
    private void checkTimeBetweenSpaceHits() {
        // Calculate time difference
        long difference = (System.currentTimeMillis() - spaceClickA) / 300;

        if (difference >= .001 && CLICKED) {
            // Reset click using boolean flag
            CLICKED = false;
            chopperSprite.addVelocity(0, 800); // Add upward velocity to chopper
            chopperSprite.render(chopperGC); // Render chopper on dedicated graphics context
            chopperSprite.update(elapsedTime); // Update chopper position
        } else {
            animateChopper(); // Animate chopper movement
        }
    }

    /*
        Update the total score based on chopper passing through pipes
    */
    private void updateTotalScore() {
        // Check if hit the pipe
        if (!HIT_PIPE) {
            for (Pipe pipe : pipes) {
                if (pipe.getPipe().getPositionX() == chopperSprite.getPositionX()) {
                    updateScoreLabel(++TOTAL_SCORE); // Update total score
                    coin.playClip(); // Play coin sound effect
                    break;
                }
            }
        }
    }

    /*
       Update the score label text with the given score
    */
    private void updateScoreLabel(int score) {
        scoreLabel.setText(Integer.toString(score)); // Set text of score label
    }

    /*
       Method to move the floor sprites horizontally across the screen.
       Handles rendering and updating positions of firstFloor and secondFloor sprites.
    */
    private void moveFloor() {
        // Render the floor sprites on the graphics context
        firstFloor.render(gc);
        secondFloor.render(gc);
        // Update their positions based on velocity
        firstFloor.update(5);
        secondFloor.update(5);

        // Check if either floor sprite has moved off-screen, and reposition them accordingly
        if (firstFloor.getPositionX() <= -APP_WIDTH) {
            firstFloor.setPositionXY(secondFloor.getPositionX() + secondFloor.getWidth(),
                    APP_HEIGHT - 100);
        } else if (secondFloor.getPositionX() <= -APP_WIDTH) {
            secondFloor.setPositionXY(firstFloor.getPositionX() + firstFloor.getWidth(),
                    APP_HEIGHT - 100);
        }
    }

    /*
      Method to animate the chopper sprite based on user input or game logic.
      Handles rendering and updating the chopperSprite, and switching frames for animation.
    */
    private void animateChopper() {
        // Render the chopper sprite on its dedicated graphics context
        chopperSprite.render(chopperGC);
        // Update the chopper's position and velocity over time
        chopperSprite.update(elapsedTime);

        // Control the animation frame switching based on user input or game state
        motionTime += 0.18;
        if (motionTime > 0.5 && CLICKED) {
            // Store the current chopperSprite to preserve its state during animation
            Sprite temp = chopperSprite;
            // Obtain the next animated frame of the chopper and update its state
            chopperSprite = chopper.animate();

            // Preserve the position and velocity of the chopper during animation switch
            chopperSprite.setPositionXY(temp.getPositionX(), temp.getPositionY());
            chopperSprite.setVelocity(temp.getVelocityX(), temp.getVelocityY());

            // Reset motionTime for next animation cycle
            motionTime = 0;
        }
    }


    /*
       Method to check if the chopper sprite intersects with any of the pipes.
       Returns true if there is a collision (chopperHitPipe), otherwise false.
    */
    private boolean chopperHitPipe() {
        for (Pipe pipe : pipes) {
            // Check collision between chopperSprite and each pipe's sprite
            if (!HIT_PIPE && chopperSprite.intersectsSprite(pipe.getPipe())) {
                HIT_PIPE = true; // Set using boolean flag to indicate chopper has hit a pipe
                showHitEffect(); // Trigger visual effect for collision
                return true; // Return true value as there is a collision
            }
        }
        return false; // Return false value if no collision detected
    }

    /*
       Method to show a visual hit effect using a fade transition on the root node.
       Creates a fading effect to simulate impact or collision feedback.
    */
    private void showHitEffect() {
        // Create a fade transition effect on the root node (game screen)
        ParallelTransition parallelTransition = new ParallelTransition();
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.10), root);
        fadeTransition.setToValue(0);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true); // Automatically reverse the fade effect

        // Add the fade transition to the parallel transition
        parallelTransition.getChildren().add(fadeTransition);
        // Play the parallel transition to apply the fade effect
        parallelTransition.play();
    }

    /*
      Method to play a sound effect indicating a hit or collision.
      Plays the 'hit' sound effect using the appropriate audio clip.
    */
    private void playHitSound() {
        hit.playClip(); // Play the 'hit' sound effect
    }


    /*
       Method to check if the chopper sprite has hit the floor or gone off-screen.
       Returns true if the chopper intersects with either firstFloor, secondFloor, or moves too far left.
    */
    private boolean chopperHitFloor() {
        return chopperSprite.intersectsSprite(firstFloor) ||
                chopperSprite.intersectsSprite(secondFloor) ||
                chopperSprite.getPositionX() < 0;
    }


    /*
       Method to stop the scrolling animation of pipes and floors.
       Sets velocities of all pipes and floors to zero to freeze their movement.
    */
    private void stopScroll() {
        // Stop all pipe movements
        for (Pipe pipe : pipes) {
            pipe.getPipe().setVelocity(0, 0);
        }
        // Stop first and second floor movements
        firstFloor.setVelocity(0, 0);
        secondFloor.setVelocity(0, 0);
    }


    /*
        Method to check and manage scrolling of pipes.
        Creates new pipes when necessary based on screen position, and removes pipes that have scrolled off-screen.
    */
    private void checkPipeScroll() {
        if (pipes.size() > 0) {
            Sprite p = pipes.get(pipes.size() - 1).getPipe();

            // Create new set of pipes when the last pipe reaches a specific position
            if (p.getPositionX() == APP_WIDTH / 2 - 80) {
                setPipes();
            } else if (p.getPositionX() <= -p.getWidth()) {
                // Remove pipes that have scrolled off-screen
                pipes.remove(0);
                pipes.remove(0); // Remove corresponding down pipe
            }
        }
    }

    /*
        Method to initialize and render a new set of pipes on the screen.
        Creates pairs of pipes with random heights and velocities, and renders them on the graphics context.
    */
    private void setPipes() {
        int height = getRandomPipeHeight();

        // Create upper and lower pipes
        Pipe pipe = new Pipe(true, height);
        Pipe downPipe = new Pipe(false, 425 - height);

        // Set velocities for both pipes
        pipe.getPipe().setVelocity(-.4, 0);
        downPipe.getPipe().setVelocity(-.4, 0);

        // Render both pipes on the graphics context and add them to the pipes list
        pipe.getPipe().render(gc);
        downPipe.getPipe().render(gc);

        pipes.addAll(Arrays.asList(pipe, downPipe));
    }

    /*
      Method to generate a random height for pipes.
      Returns an integer representing the height of the upper pipe.
    */
    private int getRandomPipeHeight() {
        return (int) (Math.random() * (410 - 25)) + 25; // Random height between 25 and 410
    }


    /*
      Method to render all pipes currently on the screen.
      Updates their positions and renders them on the graphics context.
    */
    private void renderPipes() {
        for (Pipe pipe : pipes) {
            Sprite p = pipe.getPipe();
            p.render(gc);
            p.update(5);
        }
    }


    /*
        Created a File/IO for reading the scores
       Method to write the current game score to a log file.
       Appends the score and timestamp to the end of the specified log file.
    */
    private void writeScoreToFile() {
        String filePath = "logs.txt";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "Score: " + TOTAL_SCORE + " | Time: " + timestamp + "\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(logEntry); // Write score entry to file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
        Helper class to hold a mutable long value.
        Used for tracking time and durations in the game.
    */
    public class LongValue {
        public long value;

        public LongValue(long i) {
            this.value = i;
        }
    }
}
