package com.fuzzylabs.questest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity {

    private static final String PASSWORD_PATTERN = "^.{8,20}$";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private UserLoginTask mAuthTask = null;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private ProgressBar progressBar;
    private TextView forgotPassword;

    private static QuestestDB questestDB = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        Button mEmailSignInButton = (Button) findViewById(R.id.btn_login);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setPaintFlags(forgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), ForgotPasswordActivity.class);
                startActivity(intent);
                finish();
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        mLoginFormView = findViewById(R.id.login_form);
        questestDB = new QuestestDB(getApplicationContext());
    }

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }
        mEmailView.setError(null);
        mPasswordView.setError(null);
        String email = mEmailView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();
        boolean cancel = false;
        View focusView = null;
        if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.matches(EMAIL_PATTERN);
    }

    private boolean isPasswordValid(String password) {
        return password.matches(PASSWORD_PATTERN);
    }

    private void showProgress(final boolean show) {
        if(show) {
            mEmailView.setEnabled(false);
            mPasswordView.setEnabled(false);
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            mEmailView.setEnabled(true);
            mPasswordView.setEnabled(true);
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Response> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Response doInBackground(Void... params) {
            RestConnection rest = new RestConnection();
            Map<String, String> postMap = new HashMap<String, String>();
            postMap.put("email", mEmail);
            postMap.put("password", mPassword);
            postMap.put("ClientType", "android");
            String resp = null;
            try {
                resp =  rest.sendPostForm(getString(R.string.questest_home) + "/api/login", postMap);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            mAuthTask = null;
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(mLoginFormView, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mPasswordView.setText("");
                mPasswordView.requestFocus();
            }
            if (respCode == 0) {
                questestDB.open();
                long row = questestDB.saveUser(response.getUser());
                questestDB.close();
                if(row > -1) {
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Snackbar.make(mLoginFormView, "DB Connection Error", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    mPasswordView.setText("");
                    mPasswordView.requestFocus();
                }
            } else {
                Snackbar.make(mLoginFormView, respMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                mPasswordView.setText("");
                mPasswordView.requestFocus();
            }
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

