package com.example.deepd.minesweeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Calendar;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import tyrantgit.explosionfield.ExplosionField;

public class FirstActivity extends AppCompatActivity {

    ExplosionField ef;
    LinearLayout bombLayout;
    View.OnTouchListener animate = new View.OnTouchListener() {
        private static final int MAX_CLICK_DURATION = 200;
        private long startClickTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startClickTime = Calendar.getInstance().getTimeInMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    if (clickDuration < MAX_CLICK_DURATION)
                        v.performClick();
                    return true;
            }

            AdditiveAnimator.animate(v).x(event.getX()).y(event.getY()).setDuration(1000).start();
            return true;
        }
    };
    View.OnClickListener explodeImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ef.explode(v);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addBombImage();
                }
            }, 1500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ef = ExplosionField.attach2Window(this);
        bombLayout = findViewById(R.id.bombLayout);

        addBombImage();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_first, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.instructions) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog);
            builder.setTitle("Instructions");
            builder.setView(R.layout.instructions);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addBombImage() {
        bombLayout.removeAllViews();

        ImageView imageView = new ImageView(FirstActivity.this);
        imageView.setBackgroundResource(R.drawable.ic_bomb);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(500, 380);
        layoutParams.setMargins(0, 70, 0, 0);
        imageView.setLayoutParams(layoutParams);


        imageView.setOnClickListener(explodeImage);
        imageView.setOnTouchListener(animate);

        bombLayout.addView(imageView);
    }

    public void beginEasy(View view) {
        startGame('e', 10, 8, 4);
    }

    public void beginIntermediate(View view) {
        startGame('i', 15, 10, 25);
    }

    public void beginHard(View view) {
        startGame('h', 23, 16, 70);
    }

    public void startGame(char difficulty, int r, int c, int flags) {
        Intent i = new Intent(FirstActivity.this, MainActivity.class);
        i.putExtra("difficulty", difficulty);
        i.putExtra("r", r);
        i.putExtra("c", c);
        i.putExtra("flags", flags);

        startActivity(i);
    }
}
