package com.example.droopychopper;

import java.util.ArrayList;
import java.util.Arrays;


// Represents the chopper in the Droopy Chopper game.
public class Chopper {
    private Sprite chopper; // The main chopper sprite
    private ArrayList<Sprite> flight = new ArrayList<>(); // List to store flight animation sprites
    private int currentChopper = 0; // Index of the current chopper sprite in flight animation
    private double locationX = 70; // Initial X position of the chopper
    private double locationY = 200; // Initial Y position of the chopper
    private final int CHOPPER_WIDTH = 50; // Width of the chopper sprite
    private final int CHOPPER_HEIGHT = 45; // Height of the chopper sprite


    // Constructor to initialize the chopper and set up its initial properties.
    public Chopper() {
        chopper = new Sprite(); // Create a new Sprite object for the chopper

        // Resize and set the chopper's image
        chopper.resizeImage("/images/chopper1.gif", CHOPPER_WIDTH, CHOPPER_HEIGHT);

        // Set initial position of the chopper
        chopper.setPositionXY(locationX, locationY);
        // Initialize flight animation frames
        setFlightAnimation();
    }

    // Sets up the flight animation frames for the chopper
    public void setFlightAnimation() {
        // Create sprite objects for different frames of chopper animation
        Sprite chopper2 = new Sprite();
        chopper2.resizeImage("/images/chopper2.gif", CHOPPER_WIDTH, CHOPPER_HEIGHT);
        chopper2.setPositionXY(locationX, locationY);

        Sprite chopper3 = new Sprite();
        chopper3.resizeImage("/images/chopper1.gif", CHOPPER_WIDTH, CHOPPER_HEIGHT);
        chopper3.setPositionXY(locationX, locationY);

        Sprite chopper4 = new Sprite();
        chopper4.resizeImage("/images/chopper3.gif", CHOPPER_WIDTH, CHOPPER_HEIGHT);
        chopper4.setPositionXY(locationX, locationY);

        // Add all chopper animation frames to the flight ArrayList
        flight.addAll(Arrays.asList(chopper, chopper2, chopper3, chopper4));
    }

    /*
     Retrieves the main chopper sprite.
     Return The main chopper sprite.
    */
    public Sprite getChopper() {
        return chopper;
    }

    /*
     Animates the chopper by returning the current frame from the flight animation.
     Updates to the next frame for the next call.
     Return The current chopper sprite frame for animation.
    */
    public Sprite animate() {
        // Get the current chopper sprite from the flight animation list
        Sprite currentSprite = flight.get(currentChopper);
        // Update to the next chopper sprite frame in the animation cycle
        currentChopper = (currentChopper + 1) % flight.size();
        return currentSprite; // Return the current chopper sprite for rendering
    }
}
