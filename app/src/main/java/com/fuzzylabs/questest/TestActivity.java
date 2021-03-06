package com.fuzzylabs.questest;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnTouchListener {

    private static ViewFlipper contentFlipper;
    private static ViewFlipper solutionFlipper;
    private static TextView questionNo;
    private static TextView questionView;
    private static ImageView imageView;
    private static Button option1;
    private static Button option2;
    private static Button option3;
    private static Button option4;
    private static TextView solutionView;

    private static Button next;
    private static Button back;

    private static GetTestTask getTestTask = null;
    private static GetImageTask getImageTask = null;
    private static QuestestDB questestDB = null;
    private static User user;
    private static Question question;
    private static String subject = null;
    private static List<Question> questions;
    private static int position;
    private static Random random = new Random();
    private static final int testCount = 10;
    private CountDownTimer countDownTimer;
    private final long startTime = testCount * 1 * 1000 * 60;
    private final long interval = 1 * 1000;

    private static int attempted = 0;
    private static int correct = 0;
    private static int incorrect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        contentFlipper = (ViewFlipper) findViewById(R.id.content_flipper);
        contentFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        contentFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        contentFlipper.setDisplayedChild(0);

        subject = getIntent().getStringExtra("subject");
        position = 0;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(subject);

        questestDB = new QuestestDB(getApplicationContext());
        questestDB.open();
        user = questestDB.getUser();
        questestDB.close();
        user.setClientType("android");

        AdView mAdView = (AdView) findViewById(R.id.adQuestest);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        attempted = 0;
        correct = 0;
        incorrect = 0;

        fetchTest();
    }

    @Override
    public void onBackPressed() {
        Snackbar.make(contentFlipper, "Finish Test?", Snackbar.LENGTH_LONG)
            .setAction("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    countDownTimer.cancel();
                    Intent intent = new Intent(getApplication(), ScoreActivity.class);
                    intent.putExtra("subject", subject);
                    intent.putExtra("total", String.valueOf(testCount));
                    intent.putExtra("attempted", String.valueOf(attempted));
                    intent.putExtra("correct", String.valueOf(correct));
                    intent.putExtra("incorrect", String.valueOf(incorrect));
                    intent.putExtra("score", correct+"/"+testCount);
                    startActivity(intent);
                    finish();
                }
            }).show();
    }

    public void finishTest(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        attempted++;
        Button button = (Button) view;
        option1.setEnabled(false);
        option2.setEnabled(false);
        option3.setEnabled(false);
        option4.setEnabled(false);
        String answerSelected = (String) button.getText();
        question.setMarked(answerSelected);
        if (answerSelected.equals(question.getAnswer())) {
            correct++;
            view.setBackgroundResource(R.drawable.bgreen);
        } else {
            incorrect++;
            view.setBackgroundResource(R.drawable.bred);
            if (option1.getText().equals(question.getAnswer()))
                option1.setBackgroundResource(R.drawable.bgreen);
            else if (option2.getText().equals(question.getAnswer()))
                option2.setBackgroundResource(R.drawable.bgreen);
            else if (option3.getText().equals(question.getAnswer()))
                option3.setBackgroundResource(R.drawable.bgreen);
            else if (option4.getText().equals(question.getAnswer()))
                option4.setBackgroundResource(R.drawable.bgreen);
        }
        solutionFlipper.setDisplayedChild(0);
        solutionFlipper.setVisibility(View.VISIBLE);
        solutionView = (TextView) findViewById(R.id.solution);
        solutionView.setText(question.getSolution());
    }

    public void nextClick(View view) {
        position++;
        question = questions.get(position);
        setScreen();
    }

    public void backClick(View view) {
        position--;
        question = questions.get(position);
        setScreen();
    }

    private void fetchTest() {
        if(getTestTask == null) {
            GetTestRequest request = new GetTestRequest();
            request.setUser(user);
            request.setSubject(subject);
            request.setCount(testCount);
            String requestStr = new Gson().toJson(request);
            getTestTask = new GetTestTask();
            getTestTask.execute(requestStr);
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public class GetTestTask extends AsyncTask<String, Void, Response> {

        @Override
        protected Response doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/gettest", request);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            getTestTask = null;
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(contentFlipper, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            if (respCode == 0) {
                questions = response.getData();
                question = questions.get(position);
            } else {
                question = null;
                Snackbar.make(contentFlipper, respMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            contentFlipper.setDisplayedChild(1);
            setScreen();
            countDownTimer = new MyCountDownTimer(startTime, interval);
            countDownTimer.start();
        }

        @Override
        protected void onCancelled() {
            getTestTask = null;
        }
    }

    public class GetImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String quesid = params[0];
            Bitmap image = null;
            try {
                image =  rest.getImage(getString(R.string.questest_home) + "/api/getimage/" + quesid);
            } catch (Exception e) {
                return null;
            }
            return image;
        }

        @Override
        protected void onPostExecute(final Bitmap image) {
            getImageTask = null;
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(image);
            next.setEnabled(true);
            back.setEnabled(true);
            if(position < 1) {
                back.setEnabled(false);
            }
            if(position >= questions.size()-1) {
                next.setEnabled(false);
            }
        }

        @Override
        protected void onCancelled() {
            getImageTask = null;
            next.setEnabled(true);
            back.setEnabled(true);
            if(position < 1) {
                back.setEnabled(false);
            }
            if(position >= questions.size()-1) {
                next.setEnabled(false);
            }
        }
    }

    private void setScreen() {
        blankScreen();
        if(question != null) {
            if(question.isImage() && getImageTask == null) {
                next.setEnabled(false);
                back.setEnabled(false);
                getImageTask = new GetImageTask();
                getImageTask.execute(question.getId());
            } else {
                back.setEnabled(true);
                next.setEnabled(true);
                if(position < 1) {
                    back.setEnabled(false);
                }
                if(position >= questions.size()-1) {
                    next.setEnabled(false);
                }
            }
            Set<Integer> set = new HashSet<Integer>();
            boolean isDone = false;
            questionView.setText(question.getQuestion());
            questionView.scrollTo(0, 0);
            int option;
            do {
                option = random.nextInt(4);
                isDone = set.add(option);
            } while (!isDone);
            setButton(option1, option);
            do {
                option = random.nextInt(4);
                isDone = set.add(option);
            } while (!isDone);
            setButton(option2, option);
            do {
                option = random.nextInt(4);
                isDone = set.add(option);
            } while (!isDone);
            setButton(option3, option);
            do {
                option = random.nextInt(4);
                isDone = set.add(option);
            } while (!isDone);
            setButton(option4, option);
            if (TextUtils.isEmpty(question.getMarked())) {
                option1.setEnabled(true);
                option2.setEnabled(true);
                option3.setEnabled(true);
                option4.setEnabled(true);
            } else {
                option1.setEnabled(false);
                option2.setEnabled(false);
                option3.setEnabled(false);
                option4.setEnabled(false);
            }
        }
    }

    private void setButton(Button button, int option) {
        switch (option) {
            case 0:
                button.setText(question.getAnswer());
                if(!TextUtils.isEmpty(question.getMarked())) {
                    button.setBackgroundResource(R.drawable.bgreen);
                    solutionFlipper.setDisplayedChild(0);
                    solutionFlipper.setVisibility(View.VISIBLE);
                    solutionView = (TextView) findViewById(R.id.solution);
                    solutionView.setText(question.getSolution());
                }
                break;
            case 1:
                button.setText(question.getOptionA());
                if(question.getOptionA().equals(question.getMarked())) {
                    button.setBackgroundResource(R.drawable.bred);
                }
                break;
            case 2:
                button.setText(question.getOptionB());
                if(question.getOptionB().equals(question.getMarked())) {
                    button.setBackgroundResource(R.drawable.bred);
                }
                break;
            case 3:
                button.setText(question.getOptionC());
                if(question.getOptionC().equals(question.getMarked())) {
                    button.setBackgroundResource(R.drawable.bred);
                }
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    private void blankScreen() {
        FloatingActionButton raf = (FloatingActionButton) findViewById(R.id.reportQuestionFab);
        raf.setVisibility(View.GONE);

        questionNo = (TextView) findViewById(R.id.questionNo);
        questionView = (TextView) findViewById(R.id.question);
        questionView.setText("");

        imageView = (ImageView) findViewById(R.id.image);
        imageView.setVisibility(View.GONE);

        option1 = (Button) findViewById(R.id.option1);
        option1.setText("");
        option1.setOnClickListener(this);
        option1.setBackgroundResource(R.drawable.bnormal);
        option1.setEnabled(false);

        option2 = (Button) findViewById(R.id.option2);
        option2.setText("");
        option2.setOnClickListener(this);
        option2.setBackgroundResource(R.drawable.bnormal);
        option2.setEnabled(false);

        option3 = (Button) findViewById(R.id.option3);
        option3.setText("");
        option3.setOnClickListener(this);
        option3.setBackgroundResource(R.drawable.bnormal);
        option3.setEnabled(false);

        option4 = (Button) findViewById(R.id.option4);
        option4.setText("");
        option4.setOnClickListener(this);
        option4.setBackgroundResource(R.drawable.bnormal);
        option4.setEnabled(false);

        next = (Button) findViewById(R.id.next);
        next.setEnabled(false);
        back = (Button) findViewById(R.id.back);
        back.setEnabled(false);
        if (position > 0) {
            back.setEnabled(true);
        }
        solutionFlipper = (ViewFlipper)findViewById(R.id.solution_flipper);
        solutionFlipper.setVisibility(View.GONE);
    }

    public class MyCountDownTimer extends CountDownTimer {

        DecimalFormat df = new DecimalFormat("00");

        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
            setTime(startTime);
        }

        @Override
        public void onFinish() {
            next.setEnabled(false);
            next.setEnabled(false);
            option1.setEnabled(false);
            option2.setEnabled(false);
            option3.setEnabled(false);
            option4.setEnabled(false);
            questionNo.setText("Time's up!");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            Intent intent = new Intent(getApplication(), ScoreActivity.class);
            intent.putExtra("subject", subject);
            intent.putExtra("total", String.valueOf(testCount));
            intent.putExtra("attempted", String.valueOf(attempted));
            intent.putExtra("correct", String.valueOf(correct));
            intent.putExtra("incorrect", String.valueOf(incorrect));
            intent.putExtra("score", correct+"/"+testCount);
            startActivity(intent);
            finish();
        }

        @Override
        public void onTick(long milliSec) {
            setTime(milliSec);
        }

        private void setTime(long milliSec) {
            String min = String.valueOf(milliSec/60000);
            String sec = df.format((int)(milliSec/1000)%60);
            questionNo.setText(min+":"+sec);
        }
    }

}
