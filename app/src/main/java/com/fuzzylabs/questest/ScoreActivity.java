package com.fuzzylabs.questest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class ScoreActivity extends AppCompatActivity {

    private static TextView subject;
    private static TextView totalQuestions;
    private static TextView attempted;
    private static TextView correct;
    private static TextView incorrect;
    private static TextView score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        AdView mAdView = (AdView) findViewById(R.id.adQuestest);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        subject = (TextView) findViewById(R.id.subject);
        totalQuestions = (TextView) findViewById(R.id.totalQuestions);
        attempted = (TextView) findViewById(R.id.attempted);
        correct = (TextView) findViewById(R.id.correct);
        incorrect = (TextView) findViewById(R.id.incorrect);
        score = (TextView) findViewById(R.id.score);

        Intent intent = getIntent();
        subject.setText(intent.getStringExtra("subject"));
        totalQuestions.setText(intent.getStringExtra("total"));
        attempted.setText(intent.getStringExtra("attempted"));
        correct.setText(intent.getStringExtra("correct"));
        incorrect.setText(intent.getStringExtra("incorrect"));
        score.setText(intent.getStringExtra("score"));
    }

    public void close(View view) {
        onBackPressed();
    }
}
