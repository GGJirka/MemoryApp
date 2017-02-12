package jimmy.gg.flashingnumbers.LevelManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import jimmy.gg.flashingnumbers.R;
import jimmy.gg.flashingnumbers.menu.FlashingNumbers;
import jimmy.gg.flashingnumbers.settings.NumbersSettings;

public class Numbers extends AppCompatActivity{
    private TextView timeRemain, numbers;
    private Button remember;
    private SharedPreferences sharedPreferences;
    private ProgressBar timer;
    private ArrayList<String> numbersInRow;
    private ArrayList<ImageView> images = new ArrayList<>();
    private CountDownTimer screenTimer=null, countDownTimer=null;
    private int actualIndex=0, countRow=0,timeRemain1,sec = 0;
    private StringBuilder numbersToSend = new StringBuilder();


    @Override
    protected void onCreate(Bundle savedInstanceState){
        setTheme(R.style.NumbersStyle);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numbers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setTitle(getIntent().getStringExtra(EXTRA_LEVEL));
        timer = (ProgressBar) findViewById(R.id.progressBar);
        timeRemain = (TextView) findViewById(R.id.time_remain);
        remember = (Button)findViewById(R.id.remember);

        remember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numbersDone();
            }
        });
        if (FlashingNumbers.sharedPreferences.getString(String.valueOf(getText(R.string.INFO)), "uncheck").equals("uncheck")) {
            showInfo();
        } else {
            gameStart();
        }
    }

    public void showInfo() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.checkbox, null);
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.check);
        new AlertDialog.Builder(Numbers.this)
                .setView(view)
                .setTitle("Info")
                .setMessage("Use arrows to move to another part of number.\n" +
                        "Select number arrangement in settings like below.\n" +
                        "from this: 555555 -> 55 55 55")
                .setPositiveButton(getText(R.string.quick_button_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkbox.isChecked()) {
                            SharedPreferences.Editor editor = FlashingNumbers.sharedPreferences.edit();
                            editor.putString(String.valueOf(getText(R.string.INFO)), "check");
                            editor.commit();
                        }
                        gameStart();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (checkbox.isChecked()) {
                            SharedPreferences.Editor editor = FlashingNumbers.sharedPreferences.edit();
                            editor.putString(String.valueOf(getText(R.string.INFO)), "check");
                            editor.commit();
                        }
                        gameStart();
                    }
                })
                .show();


    }
    public String timer(){
        String[] seconds = getIntent().getStringExtra(EXTRA_TIME).split(" ");
        StringBuilder builder = new StringBuilder();
        for(char c:seconds[1].toCharArray()){
            if(c!='s'){
                builder.append(c);
            }
        }
        return builder.toString();
    }

    public void gameStart(){
        screenTimer = new CountDownTimer(Integer.parseInt(sharedPreferences.getString(KEY_SETTINGS_TIMER,"3"))*1000-500,100){
            TextView countDown = (TextView)findViewById(R.id.count_down);
            @Override
            public void onTick(long millisUntilFinished) {
                if (Math.round((float)millisUntilFinished/ 1000.0f) != sec) {
                    sec = Math.round((float)millisUntilFinished / 1000.0f);
                    countDown.setText("" + (sec+1));
                }
            }
            @Override
            public void onFinish(){
                countDown.setVisibility(View.INVISIBLE);
                timeRemain.setVisibility(View.VISIBLE);
                timer.setVisibility(View.VISIBLE);
                remember.setVisibility(View.VISIBLE);
                revealNumbers();startTimer();
            }
        };
        screenTimer.start();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch(menuItem.getItemId()){
            case R.id.action_settings:
                Intent intent = new Intent(this,NumbersSettings.class);
                startActivity(intent);
                return true;
            case R.id.retry_icon:
                Intent retry = new Intent(this,Numbers.class)
                        .putExtra(EXTRA_LEVEL,getIntent().getStringExtra(EXTRA_LEVEL))
                        .putExtra(EXTRA_NUMBERS,getIntent().getStringExtra(EXTRA_NUMBERS))
                        .putExtra(EXTRA_TIME,getIntent().getStringExtra(EXTRA_TIME))
                        .setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                this.startActivity(retry);
                return true;
            default:
                this.finish();
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void revealNumbers(){
        LinearLayout layout = (LinearLayout) findViewById(R.id.numbers_numbers_config);
        int groupSize = Integer.parseInt(sharedPreferences.getString(KEY_SETTINGS_GROUP,"2"));
        int row = Integer.parseInt(sharedPreferences.getString(KEY_SETTINGS_ROW,"2"));
        String extraNumbers = getIntent().getStringExtra(EXTRA_NUMBERS);
        LinearLayout dots = (LinearLayout) findViewById(R.id.dots);
        numbers = (TextView)findViewById(R.id.revealed_numbers);
        StringBuilder build_number = new StringBuilder();
        layout.setVisibility(View.VISIBLE);
        String[] data = extraNumbers.split(" ");
        int number = Integer.parseInt(data[1]);
        numbersInRow = new ArrayList<>();
        countRow = number;
        int num = 0;

        for(int i = 1;i<=number;i++){
            int rand = new Random().nextInt(10);
            build_number.append(String.valueOf(rand));
            numbersToSend.append(String.valueOf(rand));

            if(i%groupSize==0 ) {
                build_number.append(" ");
            }
            if((row*groupSize)+groupSize-1>=12){
                for(int j=0;j<groupSize;j++){
                    if((j*groupSize)+j-1<=12){
                        num = j*groupSize;
                    }
                }
                if(i % (num) == 0 || i >= number) {
                    numbersInRow.add(build_number.toString());
                    build_number.delete(0, build_number.length());
                    ImageView dot = new ImageView(Numbers.this);
                    dot.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    dot.setImageResource(R.drawable.dot_unfill);
                    dots.addView(dot);
                    images.add(dot);
                }
            }else {
                if (i % (row * groupSize) == 0 || i >= number) {
                    numbersInRow.add(build_number.toString());
                    build_number.delete(0, build_number.length());
                    ImageView dot = new ImageView(Numbers.this);
                    dot.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    dot.setImageResource(R.drawable.dot_unfill);
                    dots.addView(dot);
                    images.add(dot);
                }
            }
        }
        numbers.setText(numbersInRow.get(actualIndex));
        images.get(0).setImageResource(R.drawable.dot_fill);
    }

    public void leftButton(View view){
        if(actualIndex!=0) {
            actualIndex--;
            numbers.setText(numbersInRow.get(actualIndex));
            images.get(actualIndex).setImageResource(R.drawable.dot_fill);
            images.get(actualIndex+1).setImageResource(R.drawable.dot_unfill);
        }
    }

    public void rightButton(View view){
        if(actualIndex!=numbersInRow.size()-1) {
            actualIndex++;
            numbers.setText(numbersInRow.get(actualIndex));
            images.get(actualIndex).setImageResource(R.drawable.dot_fill);
            images.get(actualIndex-1).setImageResource(R.drawable.dot_unfill);
        }
    }

    public void startTimer(){
        Runnable run = new Runnable() {
            @Override
            public void run() {
                final int time = Integer.parseInt(timer()) * 1000;
                countDownTimer = new CountDownTimer(Integer.parseInt(timer()) * 1000, 1) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        timer.setProgress((int) (millisUntilFinished / (Integer.parseInt(timer()) * 1000 / 100)));
                        timeRemain1 = (int) ((time - millisUntilFinished) / 10);
                    }

                    @Override
                    public void onFinish() {
                        numbersDone();
                    }
                };
                countDownTimer.start();
            }
        };
        runOnUiThread(run);
    }


    @Override
    public void onPause(){
        super.onPause();
        if(screenTimer != null) {
            screenTimer.cancel();
        }
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        if(screenTimer != null) {
            screenTimer.cancel();
        }
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
    @Override
    public void onResume(){
        super.onResume();
    }

    public void numbersDone(){
        Intent intent = new Intent(this,NumbersRemember.class);
        intent.putExtra(EXTRA_NUMBERS, numbersToSend.toString());
        intent.putExtra(EXTRA_NUMBERS_COUNT, countRow);
        intent.putExtra(EXTRA_LEVEL, getIntent().getStringExtra(EXTRA_LEVEL));
        intent.putExtra(EXTRA_TIME, getIntent().getStringExtra(EXTRA_TIME));
        intent.putExtra(EXTRA_TIME_REMAIN,timeRemain1);
        startActivity(intent);
        countDownTimer.cancel();
    }

    public final String EXTRA_NUMBERS = "jimmy.gg.flashingnumbers.NUMBERS";
    public final String EXTRA_NUMBERS_COUNT = "jimmy.gg.flashingnumbers.NUMBERS_COUNT";
    public final String EXTRA_LEVEL = "jimmy.gg.flashingnumbers.LEVEL";
    public final String EXTRA_TIME = "jimmy.gg.flashingnumbers.TIME";
    public final String EXTRA_TIME_REMAIN = "jimmy.gg.flashingnumbers.TIME_REMAIN";
    public final String KEY_SETTINGS_GROUP = "settings_group";
    public final String KEY_SETTINGS_ROW = "settings_row";
    public final String KEY_SETTINGS_TIMER = "settings_timer";
}