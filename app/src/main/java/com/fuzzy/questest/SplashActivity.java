package com.fuzzy.questest;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    private static QuestestDB questestDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        questestDB = new QuestestDB(getApplicationContext());
        try {
            questestDB.open();
            if(questestDB.getUserCount() > 0) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                }, 2000);
            } else {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }
                }, 2000);
            }
        } catch (Exception e) {
        } finally {
            questestDB.close();
        }
    }
}
