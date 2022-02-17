package com.example.p3l3byangcindycpscounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    boolean isBurst;
    boolean isGame;
    boolean isStart;
    long startTime, clicks;
    int lastProgress;

    TextView gameText;
    TextView recText;
    TextView counter;
    Button burst;
    Button endurance;
    SeekBar seekBar;
    ConstraintLayout layout;
    Button restart;

    String TAG = "com.yangcindy.lab03b.sharedprefs";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isBurst = false;
        isGame = false;
        gameText = findViewById(R.id.gametext);
        recText = findViewById(R.id.record);
        counter = findViewById(R.id.counter);
        burst = findViewById(R.id.burst);
        endurance = findViewById(R.id.endurance);
        seekBar = findViewById(R.id.size);
        layout = findViewById(R.id.activity_main_layout);
        restart = findViewById(R.id.restart);

        sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) counter.getLayoutParams();

        reset(restart);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                params.width = progress;
                params.height = progress;
                counter.setLayoutParams(params);

//                counter.setWidth(progress);
//                counter.setHeight(progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//record state
                lastProgress = seekBar.getProgress();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//pop snackbar
                Snackbar snackbar = Snackbar.make(layout,"blob size changed to " +  seekBar.getProgress()+"DP",Snackbar.LENGTH_LONG);
                snackbar.show();
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        seekBar.setProgress(lastProgress);
                        params.width = lastProgress;
                        params.height = lastProgress;
                        counter.setLayoutParams(params);

//                        counter.setWidth(lastProgress);
//                        counter.setHeight(lastProgress);
                        Snackbar.make(layout,"blob size reverted back to "+ lastProgress + "DP",Snackbar.LENGTH_LONG).show();
                    }
                });
                snackbar.setActionTextColor(Color.BLUE);
                View snackBarView = snackbar.getView();
                TextView textView = snackBarView.findViewById(R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                snackbar.show();
            }
        });
    }

    public void reset(View view) {
        counter.setVisibility(View.INVISIBLE);
        seekBar.setVisibility(View.INVISIBLE);
        gameText.setText("Welcome to CPS Counter!");
        burst.setVisibility(View.VISIBLE);
        endurance.setVisibility(View.VISIBLE);
        recText.setText("Burst Record: " + sharedPreferences.getString(burst.getTag().toString(),"N/A") + "\nEndurance Record: " + sharedPreferences.getString(endurance.getTag().toString(),"N/A"));
        restart.setVisibility(View.INVISIBLE);

        isGame = false;
        isStart = false;
        startTime = 0;
        clicks = 0;
    }

    public void start(View view) {
        isBurst = view.getTag().equals("burst");
        burst.setVisibility(View.INVISIBLE);
        endurance.setVisibility(View.INVISIBLE);
        gameText.setText("");
        recText.setText("drag to resize clicking blob :)");
        counter.setVisibility(View.VISIBLE);
        seekBar.setVisibility(View.VISIBLE);
    }

    public void clicked(View view) {
        if(isGame && !isStart) {
            return;
        }

        else if(!isGame) {
            isGame = true;
            isStart = true;
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            float curTime = ((System.currentTimeMillis()-startTime)/1000f);
                            gameText.setText(curTime + "");
                            if(curTime >= (isBurst ? 3 : 10)) {
                                timer.cancel();
                                isStart = false;
                                float cps = clicks/((System.currentTimeMillis()-startTime)/1000f);
                                cps = (float) Math.round(cps * 10)/10;
                                String curRecord = isBurst ? sharedPreferences.getString(burst.getTag().toString(),"N/A") : sharedPreferences.getString(endurance.getTag().toString(),"N/A");
                                try {
                                    if(Float.parseFloat(curRecord) < cps) {
                                        gameText.setText("New record! " + cps + " CPS");
                                        editor.putString(isBurst ? burst.getTag().toString() : endurance.getTag().toString(), cps + "");
                                        editor.apply();
                                    }
                                    else {
                                        gameText.setText(cps + " CPS");
                                    }
                                } catch(NumberFormatException e) {
                                    gameText.setText("New record! " + cps + " CPS");
                                    editor.putString(isBurst ? burst.getTag().toString() : endurance.getTag().toString(), cps + "");
                                    editor.apply();
                                }
                                restart.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            };
            startTime=System.currentTimeMillis();
            timer.scheduleAtFixedRate(task, 10, 10);
            clicks = 1;
            counter.setText(clicks+"");
        }

        else {
            clicks++;
            counter.setText(clicks+"");
        }
    }

//    class Reminder {
//        Timer timer;
//
//        public Reminder(int seconds) {
//            timer = new Timer();
//            timer.schedule(new RemindTask(), seconds*1000);
//        }
//
//        class RemindTask extends TimerTask {
//            public void run() {
//                timer.cancel(); //Terminate the timer thread
//                isStart = false;
//                float cps = clicks/((System.currentTimeMillis()-startTime)/1000f);
//                gameText.setText(cps + "CPS");
//            }
//        }
//    }
}

