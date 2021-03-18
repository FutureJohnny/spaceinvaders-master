package com.example.android.spaceinvadersactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Powerup {
    private float x;
    private float y;

    private Bitmap bitmap;

    Random r = new Random();
    private RectF rect;


    private int length = 30;
    private int height = 30;

    float speed = 150;



    private boolean isActive = false;


    public float getImpactPointY(){

            return y + height;
    }

    public Powerup(Context context, int screenX, int screenY) {
        rect = new RectF();
        int randomNumber = -1;
        // Initialize the bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.powerup);
        // stretch the bitmap to a size appropriate for the screen resolution
        bitmap = Bitmap.createScaledBitmap(bitmap,
                (int) (length),
                (int) (height),
                false);
        // Spawn a powerup roughly every 20 seconds
        randomNumber = r.nextInt(1000);
        if(randomNumber == 0) {
                if (!isActive) {
                    //Spawn powerup at random position on x axis and at top of screen
                    x = r.nextInt(screenX);
                    y = 0;
                    isActive = true;
                } else {
                    isActive = false;
                }
        }
    }

    public RectF getRect(){
        return  rect;
    }
    public boolean getStatus(){
        return isActive;
    }

    public void setInactive(){
        isActive = false;
    }

    public Bitmap getBitmap(){
        return bitmap;
    }
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }


    public void update(long fps){
        y = y + speed / fps;

        // Update rect
        rect.left = x;
        rect.right = x + length;
        rect.top = y;
        rect.bottom = y + height;

    }

//    public boolean spawnPowerup() {
//        int randomNumber = -1;
//        randomNumber = r.nextInt(60);
//        if(randomNumber == 0) {
//            return true;
//        }
//        return false;
//    }
}
