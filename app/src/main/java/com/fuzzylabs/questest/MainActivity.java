package com.fuzzylabs.questest;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, View.OnTouchListener {

    private static ViewFlipper contentFlipper;
    private static ViewFlipper solutionFlipper;
    private static TextView questionNo;
    private static TextView questionView;
    private static Button option1;
    private static Button option2;
    private static Button option3;
    private static Button option4;
    private static TextView solutionView;
    private static TextView userName;
    private static TextView userEmail;
    private static ScrollView scrollView;
    private static ProgressBar progressBar;

    private static Button next;
    private static Button back;
    private static Button postSolutionBtn;
    private static EditText postSolution;
    private static Button reportQuestionFab;
    private static Button postQuestionFab;

    private static GetQuestionTask getQuestionTask = null;
    private static PostSolutionTask postSolutionTask = null;
    private static ReportQuestionTask reportQuestionTask = null;
    private static QuestestDB questestDB = null;
    private static User user;
    private static Question question;
    private static String subject = null;
    private static List<Question> questions;
    private static int position;

    private static Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.include_main);
        contentFlipper = (ViewFlipper) view.findViewById(R.id.content_flipper);
        contentFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        contentFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        setTitle("Questest");

        questestDB = new QuestestDB(getApplicationContext());
        questestDB.open();
        user = questestDB.getUser();
        questestDB.close();
        user.setClientType("android");

        View headerView = navigationView.getHeaderView(0);
        userName = (TextView) headerView.findViewById(R.id.user_name);
        userEmail = (TextView) headerView.findViewById(R.id.user_email);
        userName.setText("".equals(user.getName())?"User":user.getName());
        userEmail.setText(user.getEmail());

        AdView mAdView = (AdView) findViewById(R.id.adQuestest);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void postQuestion(View view) {
        Intent intent = new Intent(getApplication(), PostQuestionActivity.class);
        startActivity(intent);
    }

    public void report(View view) {
        Snackbar.make(contentFlipper, "Report this question?", Snackbar.LENGTH_LONG)
            .setAction("Yes", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(reportQuestionTask == null && question != null) {
                    reportQuestionTask= new ReportQuestionTask();
                    String requestStr = new Gson().toJson(user);
                    reportQuestionTask.execute(requestStr);
                }
            }
        }).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        questestDB = new QuestestDB(getApplicationContext());
        questestDB.open();
        user = questestDB.getUser();
        questestDB.close();
        user.setClientType("android");
        userName.setText("".equals(user.getName())?"User":user.getName());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        next.setEnabled(true);
        option1.setEnabled(false);
        option2.setEnabled(false);
        option3.setEnabled(false);
        option4.setEnabled(false);
        String answerSelected = (String) button.getText();
        question.setMarked(answerSelected);
        questestDB.open();
        questestDB.saveQuestion(question);
        questestDB.close();
        questions.add(question);
        if (answerSelected.equals(question.getAnswer())) {
            view.setBackgroundResource(R.drawable.bgreen);
        } else {
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
        solutionFlipper.setVisibility(View.VISIBLE);
        if(TextUtils.isEmpty(question.getSolution())) {
            solutionFlipper.setDisplayedChild(1);
            postSolution = (EditText) findViewById(R.id.post_solution);
            postSolutionBtn = (Button) findViewById(R.id.post_solution_btn);
            postSolutionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String solution = postSolution.getText().toString().trim();
                    question.setSolution(solution);
                    PostQuestionRequest request = new PostQuestionRequest();
                    request.setQuestion(question);
                    request.setUser(user);
                    if(postSolutionTask == null) {
                        progressBar.setVisibility(View.VISIBLE);
                        String requestStr = new Gson().toJson(request);
                        postSolutionTask = new PostSolutionTask();
                        postSolutionTask.execute(requestStr);
                    }
                    solutionFlipper.setDisplayedChild(0);
                    solutionView = (TextView) findViewById(R.id.solution);
                    solutionView.setText(solution);
                    questestDB.open();
                    questestDB.saveQuestion(question);
                    questestDB.close();
                }
            });
        } else {
            solutionFlipper.setDisplayedChild(0);
            solutionView = (TextView) findViewById(R.id.solution);
            solutionView.setText(question.getSolution());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            questestDB.open();
            questestDB.ourDatabase.delete(QuestestDB.USER_TABLE,
                    null, null);
            questestDB.close();
            Intent intent = new Intent(getApplication(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void viewAccount(View view) {
        Intent intent = new Intent(getApplication(), AccountActivity.class);
        startActivity(intent);
    }

    public void takeTest(View view) {
        Intent intent = new Intent(getApplication(), TestActivity.class);
        Button btn = (Button) view;
        intent.putExtra("subject", btn.getText().toString().trim());
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            contentFlipper.setDisplayedChild(0);
            subject = null;
            setTitle("Questest");
        } else if (id == R.id.nav_english) {
            contentFlipper.setDisplayedChild(1);
            setTitle("English");
            question = null;
            subject = "ENGLISH";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_aptitude) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Aptitude");
            question = null;
            subject = "APTITUDE";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_gk) {
            contentFlipper.setDisplayedChild(1);
            setTitle("GK");
            question = null;
            subject = "GK";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_computer) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Computer");
            question = null;
            subject = "COMPUTER";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_reasoning) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Reasoning");
            question = null;
            subject = "REASONING";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_banking) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Banking");
            question = null;
            subject = "BANKING";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_share) {
            String thisApp = "https://play.google.com/store/apps/details?id=com.fuzzylabs.questest";
            String body = "---Questest---\n --Fuzzy Labs--\n "
                    + "\n\n Download this app: "
                    + thisApp;
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, body);
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Fuzzy Labs | Questest"));
            return true;
        } else if (id == R.id.nav_rate) {
            String url = "https://play.google.com/store/apps/details?id=com.fuzzylabs.questest";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            return true;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void nextClick(View view) {
        if(position == questions.size() - 1) {
            fetchQuestion();
        } else {
            position++;
            question = questions.get(position);
            setScreen();
        }
    }

    public void backClick(View view) {
        position--;
        question = questions.get(position);
        setScreen();
    }

    private void fetchSavedQuestions() {
        if(subject != null) {
            questestDB.open();
            questions = questestDB.getQuestions(subject);
            questestDB.close();
            position = questions.size() - 1;
        }
    }

    private void fetchQuestion() {
        if(getQuestionTask == null) {
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            String requestStr = new Gson().toJson(user);
            position++;
            getQuestionTask = new GetQuestionTask();
            getQuestionTask.execute(requestStr);
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public class GetQuestionTask extends AsyncTask<String, Void, Response> {

        @Override
        protected Response doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/getquestion/" + subject + "/" + position, request);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            getQuestionTask = null;
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
                question = response.getData().get(0);
            } else {
                question = null;
                Snackbar.make(contentFlipper, respMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            setScreen();
        }

        @Override
        protected void onCancelled() {
            getQuestionTask = null;
        }
    }

    public class ReportQuestionTask extends AsyncTask<String, Void, Response> {

        @Override
        protected Response doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/report/" + question.getId(), request);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            reportQuestionTask = null;
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(contentFlipper, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            Snackbar.make(contentFlipper, respMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            setScreen();
        }

        @Override
        protected void onCancelled() {
            getQuestionTask = null;
        }
    }

    public class PostSolutionTask extends AsyncTask<String, Void, Response> {

        @Override
        protected Response doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/postquestion", request);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            progressBar.setVisibility(View.GONE);
            postSolutionTask = null;
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(contentFlipper, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            Snackbar.make(contentFlipper, respMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        @Override
        protected void onCancelled() {
            postSolutionTask = null;
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setScreen() {
        blankScreen();
        if(question != null) {
            questionNo.setText("Question# " + (position+1));
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
                next.setEnabled(false);
            } else {
                option1.setEnabled(false);
                option2.setEnabled(false);
                option3.setEnabled(false);
                option4.setEnabled(false);
                next.setEnabled(true);
                solutionFlipper.setVisibility(View.VISIBLE);
                if(TextUtils.isEmpty(question.getSolution())) {
                    solutionFlipper.setDisplayedChild(1);
                    postSolution = (EditText) findViewById(R.id.post_solution);
                    postSolutionBtn = (Button) findViewById(R.id.post_solution_btn);
                    postSolutionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String solution = postSolution.getText().toString().trim();
                            question.setSolution(solution);
                            PostQuestionRequest request = new PostQuestionRequest();
                            request.setQuestion(question);
                            request.setUser(user);
                            if(postSolutionTask == null) {
                                progressBar.setVisibility(View.VISIBLE);
                                String requestStr = new Gson().toJson(request);
                                postSolutionTask = new PostSolutionTask();
                                postSolutionTask.execute(requestStr);
                            }
                            solutionFlipper.setDisplayedChild(0);
                            solutionView = (TextView) findViewById(R.id.solution);
                            solutionView.setText(solution);
                            questestDB.open();
                            questestDB.saveQuestion(question);
                            questestDB.close();
                        }
                    });
                } else {
                    solutionFlipper.setDisplayedChild(0);
                    solutionView = (TextView) findViewById(R.id.solution);
                    solutionView.setText(question.getSolution());
                }
            }
        }
        progressBar.setVisibility(View.GONE);
    }

    private void setButton(Button button, int option) {
        switch (option) {
            case 0:
                button.setText(question.getAnswer());
                if(!TextUtils.isEmpty(question.getMarked())) {
                    button.setBackgroundResource(R.drawable.bgreen);
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
        scrollView = (ScrollView) findViewById(R.id.questionScroll);

        questionNo = (TextView) findViewById(R.id.questionNo);
        questionNo.setText("Question#");

        questionView = (TextView) findViewById(R.id.question);
        questionView.setText("");

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

        solutionFlipper = (ViewFlipper)findViewById(R.id.solution_flipper);
        solutionFlipper.setVisibility(View.GONE);

        next = (Button) findViewById(R.id.next);
        next.setEnabled(false);
        back = (Button) findViewById(R.id.back);
        back.setEnabled(false);
        if (position > 0) {
            back.setEnabled(true);
        }
        questionView.setMovementMethod(new ScrollingMovementMethod());
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                questionView.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });
        questionView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                questionView.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
    }

}
