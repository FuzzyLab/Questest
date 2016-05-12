package com.fuzzylabs.questest;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;

public class AccountActivity extends AppCompatActivity {

    private static Spinner sexSpinner;
    private static UpdateUserTask updateUserTask;
    private static View accountView;
    private static QuestestDB questestDb;
    private static User user;
    private static TextView emailId;
    private static EditText nameView;
    private static EditText ageView;
    private static ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        accountView = findViewById(R.id.accountView);
        sexSpinner = (Spinner) findViewById(R.id.sexSpinner);
        String[] regionsArray = getResources().getStringArray(R.array.sex);
        ArrayList<String> regions = new ArrayList<String>(Arrays.asList(regionsArray));
        ArrayAdapter spinnerAdapter = new ArrayAdapter(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                regions);
        sexSpinner.setAdapter(spinnerAdapter);

        AdView mAdView = (AdView) findViewById(R.id.adQuestest);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        questestDb = new QuestestDB(getApplicationContext());
        questestDb.open();
        user = questestDb.getUser();
        questestDb.close();
        user.setClientType("android");

        emailId = (TextView) findViewById(R.id.emailId);
        nameView = (EditText) findViewById(R.id.name);
        ageView = (EditText) findViewById(R.id.age);
        emailId.setText(user.getEmail());
        nameView.setText(user.getName());
        ageView.setText(user.getAge());
        if("Female".equalsIgnoreCase(user.getSex())) {
            sexSpinner.setSelection(1);
        }
    }

    public void close(View view) {
        onBackPressed();
    }

    public void save(View view) {
        String name = nameView.getText().toString().trim();
        String age = ageView.getText().toString().trim();
        String sex = sexSpinner.getSelectedItem().toString();
        if(updateUserTask == null && !TextUtils.isEmpty(name) && !TextUtils.isEmpty(age)) {
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
            user.setName(name);
            user.setAge(age);
            user.setSex(sex);
            updateUserTask = new UpdateUserTask();
            String requestStr = new Gson().toJson(user);
            updateUserTask.execute(requestStr);
        } else {
            Snackbar.make(accountView, "Invalid values", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    public class UpdateUserTask extends AsyncTask<String, Void, Response> {

        @Override
        protected Response doInBackground(String... params) {
            RestConnection rest = new RestConnection();
            String request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/update", request);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            progressBar.setVisibility(View.GONE);
            updateUserTask = null;
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(accountView, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
            if(respCode == 0) {
                questestDb.open();
                questestDb.saveUser(user);
                questestDb.close();
            }
            Snackbar.make(accountView, respMsg, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        @Override
        protected void onCancelled() {
            updateUserTask = null;
            progressBar.setVisibility(View.GONE);
        }
    }
}
