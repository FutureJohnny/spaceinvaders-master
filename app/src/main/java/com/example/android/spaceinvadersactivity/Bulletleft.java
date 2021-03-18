package com.example.android.spaceinvadersactivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class Bulletleft {
    private float x;
    private float y;

    private RectF rect;

    // Which way is it shooting
    public final int UP = 0;
    public final int DOWN = 1;
    public final int LEFT = 2;
    public final int RIGHT = 3;

    public final int bulletLeft = 1;
    public final int bulletRight = 1;
    // Going nowhere
    int heading = -1;
    float speed =  350;

    private int width = 3;
    private int height;

    private int powerUpShots;




    private boolean isActive;
    private boolean hasPowerup;

    public Bulletleft(int screenY) {

        height = screenY / 20;
        isActive = false;


        rect = new RectF();
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

    public void hasPowerup() {
        hasPowerup = true;
        powerUpShots = 3;
    }

    public float getImpactPointY(){
        if (heading == DOWN){
            return y + height;
        }else{
            return  y;
        }

    }

    public boolean shoot(float startX, float startY, int direction) {
        if (!isActive && hasPowerup && powerUpShots > 0) {

                x = startX;
                y = startY - 100;
                heading = direction;
                isActive = true;
                powerUpShots --;
                return true;

        }

        // Bullet already active
        return false;
    }

    public void update(long fps){

        // Just move up or down

            y = y - speed / fps;
            x = x - 1;


        // Update rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }
}
