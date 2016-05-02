package com.fuzzy.questest;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static QuestestDB questestDB = null;
    private static User user;
    private static ViewFlipper contentFlipper;

    private static GetQuestionTask getQuestionTask = null;
    private static TextView questionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View view = findViewById(R.id.include_main);
        contentFlipper = (ViewFlipper) view.findViewById(R.id.contentFlipper);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
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
            questionView = (TextView) findViewById(R.id.question);
            fetchQuestion("ENGLISH");
        } else if (id == R.id.nav_aptitude) {
            contentFlipper.setDisplayedChild(1);
            questionView = (TextView) findViewById(R.id.question);
            fetchQuestion("APTITUDE");
        } else if (id == R.id.nav_gk) {
            contentFlipper.setDisplayedChild(1);
            questionView = (TextView) findViewById(R.id.question);
            fetchQuestion("GK");
        } else if (id == R.id.nav_computer) {
            contentFlipper.setDisplayedChild(1);
            questionView = (TextView) findViewById(R.id.question);
            fetchQuestion("COMPUTER");
        } else if (id == R.id.nav_reasoning) {
            contentFlipper.setDisplayedChild(1);
            questionView = (TextView) findViewById(R.id.question);
            fetchQuestion("REASONING");
        } else if (id == R.id.nav_share) {
        } else if (id == R.id.nav_rate) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void fetchQuestion(String subject) {
        GetQuestionRequest request = new GetQuestionRequest();
        request.setUser(user);
        Question question = new Question();
        question.setSubject(subject);
        request.setQuestion(question);
        String requestStr = new Gson().toJson(request);
        if(getQuestionTask == null) {
            getQuestionTask = new GetQuestionTask();
            getQuestionTask.execute(request);
        }
    }

    public class GetQuestionTask extends AsyncTask<GetQuestionRequest, Void, Response> {

        @Override
        protected Response doInBackground(GetQuestionRequest... params) {
            RestConnection rest = new RestConnection();
            GetQuestionRequest request = params[0];
            String resp = null;
            try {
                resp =  rest.sendPostJson(getString(R.string.questest_home) + "/api/getquestion", new Gson().toJson(request));
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            getQuestionTask = null;
            questionView.setText("Hello");
            Toast.makeText(MainActivity.this, new Gson().toJson(response), Toast.LENGTH_LONG).show();
            int respCode;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                return;
            }
            if (respCode == 0) {
                Question question = (Question) response.getData();
                questionView.setText(question.getQuestion());
            } else {
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
