package com.example.android.spaceinvadersactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class PlayerShip {

    RectF rect;

    // The player ship will be represented by a Bitmap
    private Bitmap bitmap;

    // How long and high our ship will be
    private float length;
    private float height;

    // X is the far left of the rectangle which forms our ship
    private float x;

    // Y is the top coordinate
    private float y;

    // This will hold the pixels per second speed that the ship will move
    private float shipSpeed;

    // Which ways can the ship move
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // Is the ship moving and in which direction
    private int shipMoving = STOPPED;

    // This the the constructor method
    // When we create an object from this class we will pass
    // in the screen width and height
    public PlayerShip(Context context, int screenX, int screenY){

        // Initialize a blank RectF
        rect = new RectF();

        length = 90;
        height = 50;

        // Start ship in roughly the screen centre
        x = screenX / 2;
        y = screenY;

        // Initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ship);

        // stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);

        // How fast is the spaceship in pixels per second
        shipSpeed = 350;
    }


    public RectF getRect(){
        return rect;
    }

    // This is a getter method to make the rectangle that
    // defines our ship available in SpaceInvadersView class
    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getLength(){
        return length;
    }

    // This method will be used to change/set if the ship is going left, right or nowhere
    public void setMovementState(int state){
        shipMoving = state;
    }

    // This update method will be called from update in SpaceInvadersView
    // It determines if the player ship needs to move and changes the coordinates
    // contained in x if necessary
    public void update(long fps, int screenX){
        if(shipMoving == LEFT){
            // Is player ship at left edge of screen
            if (x < 0){
                x = 0;
            }
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT){
            // Is player ship at right edge of screen
            if (x > screenX - length) {
                x = screenX - length;
            }
            x = x + shipSpeed / fps;
        }

        // Update rect which is used to detect hits
        rect.top = y-100;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + length;

    }
}

