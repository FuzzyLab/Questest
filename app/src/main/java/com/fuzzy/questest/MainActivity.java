package com.fuzzy.questest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, View.OnTouchListener {

    private static QuestestDB questestDB = null;
    private static User user;
    private static ViewFlipper contentFlipper;

    private static GetQuestionTask getQuestionTask = null;
    private static TextView questionView;
    private static Button option1;
    private static Button option2;
    private static Button option3;
    private static Button option4;
    private static TextView solutionView;

    private static TextView userName;
    private static TextView userEmail;

    private static Question question;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Add New Question", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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
        option1.setEnabled(false);
        option2.setEnabled(false);
        option3.setEnabled(false);
        option4.setEnabled(false);
        String answerSelected = (String) button.getText();
        int isCorrect = 0;
        if (answerSelected.equals(question.getAnswer())) {
            isCorrect = 1;
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
        solutionView.setText(question.getSolution());
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            contentFlipper.setDisplayedChild(0);
        } else if (id == R.id.nav_english) {
            contentFlipper.setDisplayedChild(1);
            fetchQuestion("ENGLISH");
        } else if (id == R.id.nav_aptitude) {
            contentFlipper.setDisplayedChild(1);
            fetchQuestion("APTITUDE");
        } else if (id == R.id.nav_gk) {
            contentFlipper.setDisplayedChild(1);
            fetchQuestion("GK");
        } else if (id == R.id.nav_computer) {
            contentFlipper.setDisplayedChild(1);
            fetchQuestion("COMPUTER");
        } else if (id == R.id.nav_reasoning) {
            contentFlipper.setDisplayedChild(1);
            fetchQuestion("REASONING");
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_rate) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchQuestion(String subject) {
        questionView = (TextView) findViewById(R.id.question);
        questionView.setText("");

        option1 = (Button) findViewById(R.id.option1);
        option1.setText("");
        option1.setOnClickListener(this);
        option1.setBackgroundResource(R.drawable.bnormal);
        option1.setEnabled(false);
        option1.setOnTouchListener(this);

        option2 = (Button) findViewById(R.id.option2);
        option2.setText("");
        option2.setOnClickListener(this);
        option2.setBackgroundResource(R.drawable.bnormal);
        option2.setEnabled(false);
        option2.setOnTouchListener(this);

        option3 = (Button) findViewById(R.id.option3);
        option3.setText("");
        option3.setOnClickListener(this);
        option3.setBackgroundResource(R.drawable.bnormal);
        option3.setEnabled(false);
        option3.setOnTouchListener(this);

        option4 = (Button) findViewById(R.id.option4);
        option4.setText("");
        option4.setOnClickListener(this);
        option4.setBackgroundResource(R.drawable.bnormal);
        option4.setEnabled(false);
        option4.setOnTouchListener(this);

        solutionView = (TextView) findViewById(R.id.solution);
        solutionView.setText("");

        GetQuestionRequest request = new GetQuestionRequest();
        request.setUser(user);
        Question question = new Question();
        question.setSubject(subject);
        request.setQuestion(question);
        String requestStr = new Gson().toJson(request);
        if(getQuestionTask == null) {
            getQuestionTask = new GetQuestionTask();
            getQuestionTask.execute(requestStr);
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                v.setBackgroundResource(R.drawable.bpressed);
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.setBackgroundResource(R.drawable.bnormal);
                v.invalidate();
                break;
            }
        }
        return false;
    }

    public class GetQuestionTask extends AsyncTask<String, Void, Response> {

        @Override
        protected Response doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/getquestion", request);
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
                Snackbar.make(contentFlipper, ex.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            if (respCode == 0) {
                question = response.getData();
                setScreen();
            } else {
                Snackbar.make(contentFlipper, respMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }

        private void setScreen() {
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

            option1.setEnabled(true);
            option2.setEnabled(true);
            option3.setEnabled(true);
            option4.setEnabled(true);
        }

        private void setButton(Button button, int option) {
            switch (option) {
                case 0:
                    button.setText(question.getAnswer());
                    break;
                case 1:
                    button.setText(question.getOptionA());
                    break;
                case 2:
                    button.setText(question.getOptionB());
                    break;
                case 3:
                    button.setText(question.getOptionC());
                    break;
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
