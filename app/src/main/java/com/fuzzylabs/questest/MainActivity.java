package com.fuzzylabs.questest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, View.OnTouchListener {

    private static ViewFlipper contentFlipper;
    private static TextView questionNo;
    private static TextView questionView;
    private static Button option1;
    private static Button option2;
    private static Button option3;
    private static Button option4;
    private static TextView solutionView;
    private static TextView userName;
    private static TextView userEmail;
    private static Spinner subjectSpinner;
    private static ScrollView scrollView;

    private static Button next;
    private static Button back;

    private static GetQuestionTask getQuestionTask = null;
    private static PostQuestionTask postQuestionTask = null;
    private static QuestestDB questestDB = null;
    private static User user;
    private static Question question;
    private static UserAttempt userAttempt;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContextThemeWrapper themedContext = new ContextThemeWrapper( MainActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar );
                AlertDialog.Builder builder = new AlertDialog.Builder(themedContext);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.add_question_dialog, null));
                final AlertDialog ad = builder.create();
                ad.setTitle("Add a Question");
                ad.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "Add",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                EditText postQuestion = (EditText) ad
                                        .findViewById(R.id.post_question);
                                boolean allIsWell = true;
                                String errorMessage = "Error";
                                if(TextUtils.isEmpty(postQuestion.getText().toString().trim())) {
                                    errorMessage = "Enter a question";
                                    allIsWell = false;
                                }
                                EditText postAnswer = (EditText) ad
                                        .findViewById(R.id.post_answer);
                                if(TextUtils.isEmpty(postAnswer.getText().toString().trim())) {
                                    errorMessage = "Enter correct answer";
                                    allIsWell = false;
                                }
                                EditText postOptionA = (EditText) ad
                                        .findViewById(R.id.post_optionA);
                                if(TextUtils.isEmpty(postOptionA.getText().toString().trim())) {
                                    errorMessage = "Enter valid option";
                                    allIsWell = false;
                                }
                                EditText postOptionB = (EditText) ad
                                        .findViewById(R.id.post_optionB);
                                if(TextUtils.isEmpty(postOptionB.getText().toString().trim())) {
                                    errorMessage = "Enter valid option";
                                    allIsWell = false;
                                }
                                EditText postOptionC = (EditText) ad
                                        .findViewById(R.id.post_optionC);
                                if(TextUtils.isEmpty(postOptionC.getText().toString().trim())) {
                                    errorMessage = "Enter valid option";
                                    allIsWell = false;
                                }
                                EditText postSolution = (EditText) ad
                                        .findViewById(R.id.post_solution);
                                if(allIsWell) {
                                    Question postQuest = new Question();
                                    postQuest.setUserId(user.getId());
                                    postQuest.setQuestion(postQuestion.getText().toString().trim());
                                    postQuest.setSubject(subjectSpinner.getSelectedItem().toString().trim());
                                    postQuest.setAnswer(postAnswer.getText().toString().trim());
                                    postQuest.setOptionA(postOptionA.getText().toString().trim());
                                    postQuest.setOptionB(postOptionB.getText().toString().trim());
                                    postQuest.setOptionC(postOptionC.getText().toString().trim());
                                    postQuest.setSolution(postSolution.getText().toString().trim());
                                    PostQuestionRequest request = new PostQuestionRequest();
                                    request.setQuestion(postQuest);
                                    request.setUser(user);
                                    if (postQuestionTask == null) {
                                        String requestStr = new Gson().toJson(request);
                                        postQuestionTask = new PostQuestionTask();
                                        postQuestionTask.execute(requestStr);
                                    }
                                    dialog.dismiss();
                                } else {
                                    Snackbar.make(contentFlipper, errorMessage, Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            }
                        });
                ad.show();
                subjectSpinner = (Spinner) ad.findViewById(R.id.post_subject);
                String[] regionsArray = getResources().getStringArray(R.array.subjects);
                ArrayList<String> regions = new ArrayList<String>(Arrays.asList(regionsArray));
                ArrayAdapter spinnerAdapter = new ArrayAdapter(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        regions);
                subjectSpinner.setAdapter(spinnerAdapter);
            }
        });
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
        userAttempt = new UserAttempt();
        userAttempt.setQuestionId(question.getId());
        userAttempt.setUserId(user.getId());
        userAttempt.setSubject(question.getSubject());
        Button button = (Button) view;
        next.setEnabled(true);
        option1.setEnabled(false);
        option2.setEnabled(false);
        option3.setEnabled(false);
        option4.setEnabled(false);
        String answerSelected = (String) button.getText();
        userAttempt.setMarked(answerSelected);
        question.setMarked(answerSelected);
        questestDB.open();
        questestDB.saveQuestion(question);
        questestDB.close();
        questions.add(question);
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
            subject = null;
            setTitle("Questest");
        } else if (id == R.id.nav_english) {
            contentFlipper.setDisplayedChild(1);
            setTitle("English");
            userAttempt = null;
            question = null;
            subject = "ENGLISH";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_aptitude) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Aptitude");
            userAttempt = null;
            question = null;
            subject = "APTITUDE";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_gk) {
            contentFlipper.setDisplayedChild(1);
            setTitle("GK");
            userAttempt = null;
            question = null;
            subject = "GK";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_computer) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Computer");
            userAttempt = null;
            question = null;
            subject = "COMPUTER";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_reasoning) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Reasoning");
            userAttempt = null;
            question = null;
            subject = "REASONING";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_banking) {
            contentFlipper.setDisplayedChild(1);
            setTitle("Banking");
            userAttempt = null;
            question = null;
            subject = "BANKING";
            fetchSavedQuestions();
            blankScreen();
            fetchQuestion();
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_rate) {
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
            if(position > -1) {
                question = questions.get(position);
                userAttempt = new UserAttempt();
                userAttempt.setQuestionId(question.getId());
                userAttempt.setUserId(user.getId());
                userAttempt.setSubject(question.getSubject());
                userAttempt.setMarked(question.getMarked());
            }
        }
    }

    private void fetchQuestion() {
        GetQuestionRequest request = new GetQuestionRequest();
        request.setUser(user);
        request.setUserAttempt(userAttempt);
        Question question = new Question();
        question.setSubject(subject);
        request.setQuestion(question);
        String requestStr = new Gson().toJson(request);
        if(getQuestionTask == null) {
            position++;
            getQuestionTask = new GetQuestionTask();
            getQuestionTask.execute(requestStr);
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public class PostQuestionTask extends AsyncTask<String, Void, Response> {

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
            postQuestionTask = null;
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
        }
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
                Snackbar.make(contentFlipper, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            if (respCode == 0) {
                question = response.getData();
            } else {
                question = null;
                Snackbar.make(contentFlipper, respMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            setScreen();
        }

        @Override
        protected void onCancelled() {
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
            }
        }
    }

    private void setButton(Button button, int option) {
        switch (option) {
            case 0:
                button.setText(question.getAnswer());
                if(!TextUtils.isEmpty(question.getMarked())) {
                    button.setBackgroundResource(R.drawable.bgreen);
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

        solutionView = (TextView) findViewById(R.id.solution);
        solutionView.setText("");

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
