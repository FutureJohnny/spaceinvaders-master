package com.example.android.spaceinvadersactivity;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SpaceInvadersActivity extends Activity {

    // spaceInvadersView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get a Display object to access screen details
        Display display = getWindowManager().getDefaultDisplay();
        // Load the resolution into a Point object
        Point size = new Point();
        display.getSize(size);

        // Initialize gameView and set it as the view
        spaceInvadersView = new SpaceInvadersView(this, size.x, size.y);
        setContentView(spaceInvadersView);

    }

    private void setContentView(SpaceInvadersActivity spaceInvadersView) {
    }


    @Override
    protected void onResume() {
        super.onResume();

        spaceInvadersView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        spaceInvadersView.pause();
    }

}
