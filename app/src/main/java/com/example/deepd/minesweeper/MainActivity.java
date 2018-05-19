package com.example.deepd.minesweeper;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.waynell.library.DropAnimationView;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import tyrantgit.explosionfield.ExplosionField;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView flagsLeft, winLose;
    GridLayout mineField;
    int rows, cols, hr, min, sec;
    Button block[][];
    Chronometer timer;
    Typeface digital, digitalItalics;

    ExplosionField explosionField;

    int land[][], flags, minesDeactivated;
    boolean isCleared[][], flagPlaced[][];
    String mines[];

    DropAnimationView dropAnimationView;

    View.OnLongClickListener placeFlag = new View.OnLongClickListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public boolean onLongClick(View v) {
            String tag = v.getTag().toString();
            Button b = mineField.findViewWithTag(tag);
            String t[] = tag.split(" ");
            int x = Integer.parseInt(t[0]);
            int y = Integer.parseInt(t[1]);
            if (isCleared[x][y])
                return true;
            if (!flagPlaced[x][y]) {
                if (flags > 0) {
                    flagPlaced[x][y] = true;
                    b.setBackgroundResource(R.drawable.background_flag);
                    flags--;
                    flagsLeft.setText("" + flags);

                    if (land[x][y] == -1)
                        minesDeactivated++;
                } else
                    Toast.makeText(MainActivity.this, "You're out of flags!", Toast.LENGTH_SHORT).show();
            } else {
                b.setBackgroundResource(R.drawable.background_block);
                flagPlaced[x][y] = false;
                flags++;
                flagsLeft.setText("" + flags);

                if (land[x][y] == -1)
                    minesDeactivated--;
            }

            if (minesDeactivated == mines.length)
                gameWon();

            return true;
        }
    };
    View.OnClickListener clickOnBlock = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String tag = v.getTag().toString();
            String t[] = tag.split(" ");
            int x = Integer.parseInt(t[0]);
            int y = Integer.parseInt(t[1]);

            clicked(x, y);
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        explosionField = ExplosionField.attach2Window(this);
        dropAnimationView = findViewById(R.id.drop_animation_view);
        dropAnimationView.setDrawables(R.drawable.mine_red,
                R.drawable.landmine,
                R.drawable.skull);

        flagsLeft = findViewById(R.id.flagsLeft);
        mineField = findViewById(R.id.mineField);
        timer = findViewById(R.id.timer);
        winLose = findViewById(R.id.win_lose);

        hr = min = sec = 0;

        Intent i = getIntent();
        rows = i.getIntExtra("r", 0);
        cols = i.getIntExtra("c", 0);
        flags = i.getIntExtra("flags", 0);

        mineField.setColumnCount(cols);
        block = new Button[rows][cols];

        mines = new String[flags];
        isCleared = new boolean[rows][cols];
        land = new int[rows][cols];
        flagPlaced = new boolean[rows][cols];

        digital = Typeface.createFromAsset(getAssets(), "fonts/digital-7.ttf");
        digitalItalics = Typeface.createFromAsset(getAssets(), "fonts/digital-7 (italic).ttf");
        flagsLeft.setTypeface(digitalItalics);
        timer.setTypeface(digitalItalics);

        flagsLeft.setText("" + flags);

        placeMines();
        generateLandValues();
        createMineField();

        timer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.reset) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Restart");
            builder.setMessage("Are you sure you want to restart?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    recreate();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    public void gameOver(int x, int y) {
        int i, j;
        String s[];
        for (String mine : mines) {
            Button b = mineField.findViewWithTag(mine);
            s = mine.split(" ");
            i = Integer.parseInt(s[0]);
            j = Integer.parseInt(s[1]);
            if (!flagPlaced[i][j]) {
                if (mine.equals(x + " " + y))
                    b.setBackgroundResource(R.drawable.mine);
                else
                    b.setBackgroundResource(R.drawable.background_mine);
            } else
                flagPlaced[i][j] = false;
            b.setClickable(false);
            b.setEnabled(false);
            b.setOnClickListener(null);
            b.setOnLongClickListener(null);
        }
        timer.stop();
        wrongFlags();

        dropAnimationView.bringToFront();
        dropAnimationView.startAnimation();

        winLose.setText("Game Over");
        gameOverAlert(false);
    }

    public void gameOverAlert(boolean win) {
        long elapsedMillis = SystemClock.elapsedRealtime() - timer.getBase();

        @SuppressLint("DefaultLocale")
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % TimeUnit.MINUTES.toSeconds(1));

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        if (win)
            builder.setTitle("Congratulations");
        else
            builder.setTitle("Game over");
        builder.setMessage("Time: " + hms);
        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recreate();
            }
        });
        if (win)
            builder.setIcon(R.drawable.ic_insert_emoticon_black_24dp);
        else
            builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();
    }

    public void wrongFlags() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (flagPlaced[i][j]) {
                    Button b = mineField.findViewWithTag(i + " " + j);
                    b.setBackgroundResource(R.drawable.background_flag_wrong);
                    b.setClickable(false);
                    b.setEnabled(false);
                }
            }
        }
    }

    public void gameWon() {
        Toast.makeText(this, "You win!", Toast.LENGTH_SHORT).show();
        timer.stop();

        gameOverAlert(true);
    }

    public void placeMines() {
        for (int i = 0; i < flags; ) {
            int x = (int) (Math.random() * rows);
            int y = (int) (Math.random() * cols);

            if (land[x][y] == -1)
                continue;

            land[x][y] = -1;
            mines[i++] = x + " " + y;
        }
    }

    public void generateLandValues() {
        int m;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (land[i][j] == -1)
                    continue;

                m = 0;
                int a[] = new int[8];
                int b[] = new int[8];
                a[0] = i - 1;
                b[0] = j - 1;
                a[1] = i - 1;
                b[1] = j;
                a[2] = i - 1;
                b[2] = j + 1;
                a[3] = i;
                b[3] = j - 1;
                a[4] = i;
                b[4] = j + 1;
                a[5] = i + 1;
                b[5] = j - 1;
                a[6] = i + 1;
                b[6] = j;
                a[7] = i + 1;
                b[7] = j + 1;
                for (int k = 0; k < 8; k++) {
                    if (a[k] < 0 || a[k] > rows - 1 || b[k] < 0 || b[k] > cols - 1)
                        continue;
                    if (land[a[k]][b[k]] == -1)
                        m++;
                }

                land[i][j] = m;
            }
        }
    }

    public void createMineField() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                block[i][j] = new Button(this);
                block[i][j].setTag(i + " " + j);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(50, 50);
//                params.setMargins(10, 10, 10, 10);
                block[i][j].setLayoutParams(params);

                block[i][j].setPadding(2, 2, 2, 2);
                block[i][j].setTypeface(digital);
                block[i][j].setBackgroundResource(R.drawable.background_block);
//                block[i][j].setElevation(10);

                block[i][j].setOnClickListener(clickOnBlock);
                block[i][j].setOnLongClickListener(placeFlag);

                mineField.addView(block[i][j]);
            }
        }
    }

    public void clicked(int x, int y) {
        if (land[x][y] == 0) {
            clearLand(x, y);
        } else if (land[x][y] == -1) {
            gameOver(x, y);
        } else {
            revealNumber(x, y);
        }
    }

    public void revealNumber(int x, int y) {
        if (flagPlaced[x][y])
            return;

        Button b = mineField.findViewWithTag(x + " " + y);
        String st = "" + land[x][y];
        b.setText(st);
        b.setBackgroundResource(R.drawable.background_number);
        b.setTextColor(Color.parseColor(getNumberColor(land[x][y])));

        final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        // Use bounce interpolator with amplitude 0.2 and frequency 30
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 30);
        myAnim.setInterpolator(interpolator);
        b.startAnimation(myAnim);
    }

    private String getNumberColor(int n) {
        String color[] = {"#0000ff", "#008100", "#ff1300", "#000083", "#810500", "#2a9494", "#000000", "#808080"};
        return color[n - 1];
    }

    public void removeBlockWithTag(int x, int y) {
        String tag = x + " " + y;
        View v = mineField.findViewWithTag(tag);

        Random random = new Random();
        int nextInt = random.nextInt(256 * 256 * 256);
        String colorCode = String.format("#%06x", nextInt);
        v.setBackgroundColor(Color.parseColor(colorCode));

        explosionField.explode(v);
        v.setEnabled(false);
    }

    private void clearLand(int x, int y) {
        if (flagPlaced[x][y])
            return;
        isCleared[x][y] = true;
        if (land[x][y] == -2)
            return;
        else if (land[x][y] == 0) {
            land[x][y] = -2;
            removeBlockWithTag(x, y);
        } else {
            revealNumber(x, y);
            return;
        }

        int a[] = new int[8];
        int b[] = new int[8];
        a[0] = x - 1;
        b[0] = y - 1;
        a[1] = x - 1;
        b[1] = y;
        a[2] = x - 1;
        b[2] = y + 1;
        a[3] = x;
        b[3] = y - 1;
        a[4] = x;
        b[4] = y + 1;
        a[5] = x + 1;
        b[5] = y - 1;
        a[6] = x + 1;
        b[6] = y;
        a[7] = x + 1;
        b[7] = y + 1;
        for (int i = 0; i < 8; i++) {
            if (a[i] >= 0 && a[i] < rows && b[i] >= 0 && b[i] < cols) {
                final int ai = a[i], bi = b[i];
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clearLand(ai, bi);
                    }
                }, 500);
            }
        }
    }
}
// TODO: Add high scores feature