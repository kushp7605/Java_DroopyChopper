package com.example.droopychopper;

import javafx.scene.media.AudioClip;


/*
  The Sound class represents a sound effect that can be played, looped, muted, or stopped.
  It uses the JavaFX AudioClip class to handle the sound playback.
 */
public class Sound {
    // AudioClip object representing the sound effect
    private AudioClip soundEffect;
    // Boolean flag to indicate whether the sound is muted
    private boolean isMuted = false;


    /*
      Constructor for the Sound class.
      Initializes the AudioClip with the given file path.
      FilePath - the path to the sound file
     */
    public Sound(String filePath) {
        soundEffect = new AudioClip(getClass().getResource(filePath).toExternalForm());
    }


    // Play the sound effect if it is not muted.
    public void playClip() {
        if (!isMuted) {
            soundEffect.play();
        }
    }

    // Loops the sound effect indefinitely if it is not muted
    public void loopClip() {
        if (!isMuted) {
            soundEffect.setCycleCount(AudioClip.INDEFINITE);
            soundEffect.play();
        }
    }

    // Mutes the sound effect and stops any currently playing sound.
    public void mute() {
        isMuted = true;
        soundEffect.stop();
    }

    // Unmutes the sound effect.
    public void unmute() {
        isMuted = false;
    }

    // Stops the sound effect if it is currently playing.
    public void stop() {
        soundEffect.stop();
    }
}
