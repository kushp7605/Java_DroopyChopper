package com.example.droopychopper;

/* The Pipe Class represents a pipe in the game, which is either facing up and facing down
   It contains a Sprite for the pipe, its position, and its dimensions.
 */

public class Pipe {
    // Sprite object representing the pipe image
    private Sprite pipe;

    // X and Y coordinates of the pipe's position
    private double locationX;
    private double locationY;

    // Height and width of the pipe
    private double height;
    private double width;

    /*
       Constructor for the Pipe class.
       Initializes a pipe sprite, sets its dimensions, and positions it on the screen.
       isFaceUp - boolean indicating whether the pipe is facing up or down
       Height - the height of the pipe

     */
    public Pipe(boolean isFaceUp, int height) {
        this.pipe = new Sprite();

        // Resize the image based on the pipe orientation (up or down)
        this.pipe.resizeImage(isFaceUp ? "/images/up_jar.png" : "/images/down_jar.png", 60, height);
        this.width = 60;
        this.height = height;

        // Set the initial X and Y position of the pipe
        this.locationX = 650;
        this.locationY = isFaceUp? 600 - height : 0;

        // Set the pipe's position on the screen
        this.pipe.setPositionXY(locationX, locationY);
    }

    /*
        Getter for the pipe sprite.
        Return the Sprite object representing the pipe
     */
    public Sprite getPipe() {
        return pipe;
    }
}
