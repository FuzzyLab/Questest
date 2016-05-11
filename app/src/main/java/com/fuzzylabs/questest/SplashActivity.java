package com.fuzzylabs.questest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;

public class SplashActivity extends AppCompatActivity {

    private static QuestestDB questestDB = null;
    private static GetUserTask getUserTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        questestDB = new QuestestDB(getApplicationContext());
        getUserTask = new GetUserTask();
        getUserTask.execute();
    }

    public class GetUserTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            questestDB.open();
            boolean isUser = questestDB.getUserCount() > 0;
            questestDB.close();
            return isUser;
        }

        @Override
        protected void onPostExecute(final Boolean response) {
            getUserTask = null;
            if(response) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }, 1500);
            } else {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                }, 1500);
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
