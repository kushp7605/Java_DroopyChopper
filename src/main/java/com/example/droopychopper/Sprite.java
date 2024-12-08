package com.example.droopychopper;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Sprite {
    // Image representing the sprite
    private Image image;

    // Position of the sprite
    private double positionX;
    private double positionY;

    // Velocity of the sprite
    private double velocityX;
    private double velocityY;

    // Dimensions of the sprite
    private double width;
    private double height;

    // Default constructor initializing position and velocity to 0
    public Sprite() {
        this.positionX = 0;
        this.positionY = 0;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    // Sets the image for the sprite and updates its dimensions
    public void setImage(Image image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
    }

    // Resizes the image and sets it for the sprite
    public void resizeImage(String filepath, int width, int height) {
        Image resizedImage = new Image(filepath, width, height, false, false);
        setImage(resizedImage);
    }

    // Sets the position of the sprite
    public void setPositionXY(double positionX, double positionY) {
        this.positionX = positionX;
        this.positionY = positionY;
    }

    // Returns the X position of the sprite
    public double getPositionX() {
        return positionX;
    }

    // Returns the Y position of the sprite
    public double getPositionY() {
        return positionY;
    }

    // Sets the velocity of the sprite
    public void setVelocity(double velocityX, double velocityY) {
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    // Adds to the current velocity of the sprite
    public void addVelocity(double x, double y) {
        this.velocityX += x;
        this.velocityY += y;
    }

    // Returns the X velocity of the sprite
    public double getVelocityX() {
        return velocityX;
    }

    // Returns the Y velocity of the sprite
    public double getVelocityY() {
        return velocityY;
    }

    // Returns the width of the sprite
    public double getWidth() {
        return width;
    }

    // Renders the sprite on the given GraphicsContext
    public void render(GraphicsContext gc) {
        gc.drawImage(image, positionX, positionY);
    }

    // Returns the boundary of the sprite as a Rectangle2D
    public Rectangle2D getBoundary() {
        return new Rectangle2D(positionX, positionY, width, height);
    }

    // Checks if this sprite intersects with another sprite
    public boolean intersectsSprite(Sprite otherSprite) {
        return otherSprite.getBoundary().intersects(this.getBoundary());
    }

    // Updates the position of the sprite based on its velocity and the given time
    public void update(double time) {
        positionX += velocityX * time;
        positionY += velocityY * time;
    }
}
