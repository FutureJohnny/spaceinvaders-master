package com.example.android.spaceinvadersactivity;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class SpaceInvadersView extends SurfaceView implements Runnable {

    Context context;

    Thread gameThread = null;

    SurfaceHolder ourHolder;

    boolean playing;
    boolean paused = true;


    Canvas canvas;

    Paint paint;

    long fps;

    long timeThisFrame;

    int screenX;
    int screenY;

    PlayerShip playerShip;
    Bullet bullet;
    Bulletleft bulletleft;
    Bulletright bulletright;

    Bullet[] invadersBullets = new Bullet[200];
    int nextBullet;
    int maxInvaderBullets = 10;

    Invader[] invaders = new Invader[60];
    int numInvaders = 0;

    DefenceBrick[] bricks = new DefenceBrick[400];
    int numBricks;

    Powerup powerup;

    SoundPool soundPool;
    int playerExplodeID = -1;
    int invaderExplodeID = -1;
    int shootID = -1;
    int damageShelterID = -1;
    int uhID = -1;
    int ohID = -1;
    int powerupID = -1;

    int score = 0;
    int lives = 5;
    int tripleShots = 0;

    long menaceInterval = 1000;
    boolean uhOrOh;
    long lastMenaceTime = System.currentTimeMillis();

    int playerBulletColour = Color.BLUE;
    int invaderBulletColour = Color.RED;
    int powerupColour = Color.YELLOW;


    public SpaceInvadersView(Context context, int x, int y) {

        super(context);

        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

    /*
        Ignore the code dealing with sound as the objects used have been removed from Android Studio
     */

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("powerup.wav");
            powerupID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        } catch(IOException e) {
            // Print an error message to the console
            Log.e("Error", "Failed to load sound files");
        }

        prepareLevel();

    }

    private void prepareLevel() {


        // Make a new player space ship
        playerShip = new PlayerShip(context, screenX, screenY);
        // Prepare the players bullet
        bullet = new Bullet(screenY);
        bulletleft = new Bulletleft(screenY);
        bulletright = new Bulletright(screenY);

        powerup = new Powerup(context, screenX, screenY);


        // Initialize the invadersBullets array
        for(int i = 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Bullet(screenY);
        }
        // Build an army of invaders
        numInvaders = 0;
        for(int column = 0; column < 6; column ++ ){
            for(int row = 0; row < 5; row ++ ){
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders ++;
            }
        }
        // Build the shelters
        numBricks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++){
            for(int column = 0; column < 10; column ++ ) {
                for (int row = 0; row < 5; row++) {
                    bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }
        // Reset the menace level
        menaceInterval = 1000;
    }


    @Override
    public void run() {
        while (playing) {

            long startFrameTime = System.currentTimeMillis();

            if (!paused) {
                update();
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
            // We will do something new here towards the end of the project
            // Play a sound based on the menace level
            if(!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        // Play Uh
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        // Play Oh
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // Reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // Alter value of uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }

            }
        }



    private void update() {
        boolean bumped = false;
        boolean lost = false;

        // Move the player's ship
        playerShip.update(fps, screenX);

        // Update powerup
        powerup.update(fps);

        // Update the invaders if visible
        for(int i = 0; i < numInvaders; i++){

            if(invaders[i].getVisibility()) {
                // Move the next invader
                invaders[i].update(fps);

                // Does he want to take a shot?
                if(invaders[i].takeAim(playerShip.getX(),
                        playerShip.getLength())){

                    // If so try and spawn a bullet
                    if(invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getLength() / 2,
                            invaders[i].getY(), bullet.DOWN)) {

                        // Shot fired
                        // Prepare for the next shot
                        nextBullet++;

                        // Loop back to the first one if we have reached the last
                        if (nextBullet == maxInvaderBullets) {
                            // This stops the firing of another bullet until one completes its journey
                            // Because if bullet 0 is still active shoot returns false.
                            nextBullet = 0;
                        }
                    }
                }

                // If that move caused them to bump the screen change bumped to true
                if (invaders[i].getX() > screenX - invaders[i].getLength()
                        || invaders[i].getX() < 0){

                    bumped = true;

                }
            }

        }
        // Update all the invaders bullets if active
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }
        // Did an invader bump into the edge of the screen
        if(bumped){

            // Move all the invaders down and change direction
            for(int i = 0; i < numInvaders; i++){
                invaders[i].dropDownAndReverse();
                // Have the invaders landed
                if(invaders[i].getY() > screenY - screenY / 10){
                    lost = true;
                }
            }

            // Increase the menace level
            // By making the sounds more frequent
            menaceInterval = menaceInterval - 80;
        }
        if (lost) {
            prepareLevel();
        }

        // Update the players bullets
        if(bullet.getStatus()){
            bullet.update(fps);
        }
        if(bulletleft.getStatus()){
            bulletleft.update(fps);
        }
        if(bulletright.getStatus()){
            bulletright.update(fps);
        }
        // Has the player's bullet hit the top of the screen
        if(bullet.getImpactPointY() < 0){
            bullet.setInactive();
        }
        if(bulletleft.getImpactPointY() < 0){
            bulletleft.setInactive();
        }
        if(bulletright.getImpactPointY() < 0){
            bulletright.setInactive();
        }

        // Has an invaders bullet hit the bottom of the screen
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getImpactPointY() > screenY){
                invadersBullets[i].setInactive();
            }
        }


        // Has the player's bullet hit an invader
        if(bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }
        // Has the player's bulletleft hit an invader
        if(bulletleft.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bulletleft.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bulletleft.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }
        // Has the player's bulletright hit an invader
        if(bulletright.getStatus()) {
            for (int i = 0; i < numInvaders; i++) {
                if (invaders[i].getVisibility()) {
                    if (RectF.intersects(bulletright.getRect(), invaders[i].getRect())) {
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bulletright.setInactive();
                        score = score + 10;

                        // Has the player won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }
        // Has an alien bullet hit a shelter brick
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                for(int j = 0; j < numBricks; j++){
                    if(bricks[j].getVisibility()){
                        if(RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())){
                            // A collision has occurred
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }
        // Has a player bullet hit a shelter brick
        if(bullet.getStatus()){
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bullet.getRect(), bricks[i].getRect())){
                        // A collision has occurred
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }
        // Has a player bulletleft hit a shelter brick
        if(bulletleft.getStatus()){
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bulletleft.getRect(), bricks[i].getRect())){
                        // A collision has occurred
                        bulletleft.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }
        // Has a player bulletright hit a shelter brick
        if(bulletright.getStatus()){
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()){
                    if(RectF.intersects(bulletright.getRect(), bricks[i].getRect())){
                        // A collision has occurred
                        bulletright.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // Has an invader bullet hit the player ship
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()){
                if(RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())){
                    invadersBullets[i].setInactive();
                    lives --;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // Is it game over?
                    if(lives == 0){
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();

                    }
                }
            }
        }

        // Has the player touched the powerup
        if(powerup.getStatus()) {
            if(RectF.intersects(playerShip.getRect(), powerup.getRect())){
                powerup.setInactive();
                bulletleft.hasPowerup();
                bulletright.hasPowerup();
                tripleShots = 3;
                soundPool.play(powerupID,100,100,0,0,1);
            }
        }

        // If no powerup active, create one
        if(!powerup.getStatus()) {
            powerup = new Powerup(context,screenX,screenY);
        }

        // If powerup has went off screen, set inactive
        if(powerup.getImpactPointY() > screenY){
            powerup.setInactive();
        }
    }

    private void draw() {

        if (ourHolder.getSurface().isValid()) {

            canvas = ourHolder.lockCanvas();

            canvas.drawColor(Color.argb(255,0,0,0));


            paint.setColor(Color.argb(255,0,204,0));

            // Draw the player spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), screenY - 120, paint);

            // Draw the invaders
            for(int i = 0; i < numInvaders; i++){
                if(invaders[i].getVisibility()) {
                    if(uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    }else{
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }
            // Draw the bricks if visible
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }
            // Draw the players bullet if active
            if(bullet.getStatus()){
                paint.setColor(playerBulletColour);
                canvas.drawRect(bullet.getRect(), paint);
            }
            // Draw the players bulletleft if active
            if(bulletleft.getStatus()){
                paint.setColor(playerBulletColour);
                canvas.drawRect(bulletleft.getRect(), paint);
            }
            // Draw the players bulletright if active
            if(bulletright.getStatus()){
                paint.setColor(playerBulletColour);
                canvas.drawRect(bulletright.getRect(), paint);
            }

            // Draw PowerUp if active
            if(powerup.getStatus()){
                //paint.setColor(powerupColour);
                //canvas.drawRect(powerup.getRect(), paint);
                canvas.drawBitmap(powerup.getBitmap(), powerup.getX(), powerup.getY(), paint);
            }



            // Draw the invaders bullets if active
            // Update all the invader's bullets if active
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()) {
                    paint.setColor(invaderBulletColour);
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            paint.setColor(Color.argb(255,249,129,0));
            paint.setTextSize(40);
            canvas.drawText("Score: " + score + "     Lives: " + lives + "     Tripleshots: " + tripleShots, 10, 50, paint);

            ourHolder.unlockCanvasAndPost(canvas);
        }
    }


    public void pause() {
        playing = false;

        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error", "Joining thread");
        }
    }


    public void resume() {
        playing = true;

        gameThread = new Thread(this);
        gameThread.start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                paused = false;

                if(motionEvent.getY() > screenY - screenY / 4) {
                    if (motionEvent.getX() > screenX / 2) {
                        playerShip.setMovementState(playerShip.RIGHT);
                    } else {
                        playerShip.setMovementState(playerShip.LEFT);
                    }

                }

                if(motionEvent.getY() < screenY - screenY / 4) {
                    // Shots fired
                    if(bullet.shoot(playerShip.getX()+
                            playerShip.getLength()/2,screenY,bullet.UP)){
                        soundPool.play(shootID, 1, 1, 0, 0, 1);
                    }
                    if(bulletleft.shoot(playerShip.getX()+
                            playerShip.getLength()/2,screenY,bullet.UP)){
                            tripleShots --;
                            if (tripleShots < 0) {
                                tripleShots = 0;
                            }
                    }
                    if(bulletright.shoot(playerShip.getX()+
                            playerShip.getLength()/2,screenY,bullet.UP)){

                    }
                }
                break;

            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                if(motionEvent.getY() > screenY - screenY / 4) {
                    playerShip.setMovementState(playerShip.STOPPED);
                }

                break;

        }

        return true;
    }


}