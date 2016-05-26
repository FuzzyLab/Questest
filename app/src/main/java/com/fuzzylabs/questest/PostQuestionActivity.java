package com.fuzzylabs.questest;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class PostQuestionActivity extends AppCompatActivity {

    private static PostQuestionTask postQuestionTask = null;
    private static QuestestDB questestDb;
    private static User user;
    private static Spinner subjectSpinner;
    private static View postQuestionView;
    private static EditText postQuestion;
    private static EditText postAnswer;
    private static EditText postOptionA;
    private static EditText postOptionB;
    private static EditText postOptionC;
    private static EditText postSolution;
    private static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_question);

        postQuestionView = findViewById(R.id.postQuestionView);
        subjectSpinner = (Spinner) findViewById(R.id.post_subject);
        String[] regionsArray = getResources().getStringArray(R.array.subjects);
        ArrayList<String> regions = new ArrayList<String>(Arrays.asList(regionsArray));
        ArrayAdapter spinnerAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                regions);
        subjectSpinner.setAdapter(spinnerAdapter);

        Snackbar.make(postQuestionView, "Post a question", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

        AdView mAdView = (AdView) findViewById(R.id.adQuestest);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        postQuestion = (EditText) findViewById(R.id.post_question);
        postAnswer = (EditText) findViewById(R.id.post_answer);
        postOptionA = (EditText) findViewById(R.id.post_optionA);
        postOptionB = (EditText) findViewById(R.id.post_optionB);
        postOptionC = (EditText) findViewById(R.id.post_optionC);
        postSolution = (EditText) findViewById(R.id.post_solution);

        questestDb = new QuestestDB(getApplicationContext());
        questestDb.open();
        user = questestDb.getUser();
        questestDb.close();
        user.setClientType("android");
    }

    public void close(View view) {
        onBackPressed();
    }

    public void postQuestion(View view) {
        boolean allIsWell = true;
        String errorMessage = "Error";
        String question = postQuestion.getText().toString().trim();
        String answer = postAnswer.getText().toString().trim();
        String optionA = postOptionA.getText().toString().trim();
        String optionB = postOptionB.getText().toString().trim();
        String optionC = postOptionC.getText().toString().trim();
        if(TextUtils.isEmpty(question) && allIsWell) {
            errorMessage = "Enter a question";
            allIsWell = false;
        }
        if(TextUtils.isEmpty(answer) && allIsWell) {
            errorMessage = "Enter correct answer";
            allIsWell = false;
        }
        if(TextUtils.isEmpty(optionA)) {
            errorMessage = "Enter valid option";
            allIsWell = false;
        }
        if(TextUtils.isEmpty(optionB)) {
            errorMessage = "Enter valid option";
            allIsWell = false;
        }
        if(TextUtils.isEmpty(optionC)) {
            errorMessage = "Enter valid option";
            allIsWell = false;
        }
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
                progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
                String requestStr = new Gson().toJson(request);
                postQuestionTask = new PostQuestionTask();
                postQuestionTask.execute(requestStr);
            }
        } else {
            Snackbar.make(postQuestionView, errorMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
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
            progressBar.setVisibility(View.GONE);
            postQuestionTask = null;
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(postQuestionView, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            Snackbar.make(postQuestionView, respMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            postQuestion.setText("");
            postAnswer.setText("");
            postOptionA.setText("");
            postOptionB.setText("");
            postOptionC.setText("");
            postSolution.setText("");
        }

        @Override
        protected void onCancelled() {
            postQuestionTask = null;
            progressBar.setVisibility(View.GONE);
        }
    }
}
