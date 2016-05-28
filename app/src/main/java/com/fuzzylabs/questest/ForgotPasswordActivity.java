package com.fuzzylabs.questest;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String PASSWORD_PATTERN = "^.{8,20}$";

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private View forgotView;
    private TextView forgotEmail;
    private TextView forgotPassword;
    private TextView forgotReenter;
    private ProgressBar progressBar;
    private static QuestestDB questestDB = null;

    private ChangePasswordTask changePasswordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        forgotView = findViewById(R.id.forgotView);
        forgotEmail = (TextView) findViewById(R.id.forgotEmail);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotReenter = (TextView) findViewById(R.id.forgotReenter);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        questestDB = new QuestestDB(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplication(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void changePassword(View view) {
        forgotEmail.setError(null);
        forgotPassword.setError(null);
        forgotReenter.setError(null);
        String email = forgotEmail.getText().toString().trim();
        String password = forgotPassword.getText().toString().trim();
        String reenter = forgotReenter.getText().toString().trim();
        if(password.equals(reenter)) {
            boolean cancel = false;
            View focusView = null;
            if (!isEmailValid(email)) {
                forgotEmail.setError(getString(R.string.error_invalid_email));
                focusView = forgotEmail;
                cancel = true;
            } else if (!isPasswordValid(password)) {
                forgotPassword.setError(getString(R.string.error_invalid_password));
                focusView = forgotPassword;
                cancel = true;
            } else if (!isPasswordValid(reenter)) {
                forgotReenter.setError(getString(R.string.error_invalid_password));
                focusView = forgotReenter;
                cancel = true;
            }
            if (cancel) {
                focusView.requestFocus();
            } else {
                if(changePasswordTask == null) {
                    showProgress(true);
                    changePasswordTask = new ChangePasswordTask(email, password);
                    changePasswordTask.execute((Void) null);
                }
            }
        } else {
            Snackbar.make(forgotView, "Passwords do not match", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            forgotPassword.setText("");
            forgotPassword.requestFocus();
            forgotReenter.setText("");
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
            forgotEmail.setEnabled(false);
            forgotPassword.setEnabled(false);
            forgotReenter.setEnabled(false);
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            forgotEmail.setEnabled(true);
            forgotPassword.setEnabled(true);
            forgotReenter.setEnabled(true);
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    public class ChangePasswordTask extends AsyncTask<Void, Void, Response> {

        private final String mEmail;
        private final String mPassword;

        ChangePasswordTask(String email, String password) {
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
                resp = rest.sendPostForm(getString(R.string.questest_home) + "/api/changepassword", postMap);
            } catch (Exception e) {
                return null;
            }
            return new Gson().fromJson(resp, Response.class);
        }

        @Override
        protected void onPostExecute(final Response response) {
            changePasswordTask = null;
            showProgress(false);
            int respCode = 1;
            String respMsg = "";
            try {
                respCode = response.getRespCode();
                respMsg = response.getRespMsg();
            } catch (Exception ex) {
                Snackbar.make(forgotView, "Internet Connection Error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                forgotPassword.setText("");
                forgotPassword.requestFocus();
                forgotReenter.setText("");
            }
            if (respCode == 0) {
                questestDB.open();
                long row = questestDB.saveUser(response.getUser());
                questestDB.close();
                if (row > -1) {
                    Intent intent = new Intent(getApplication(), MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Snackbar.make(forgotView, "DB Connection Error", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    forgotPassword.setText("");
                    forgotPassword.requestFocus();
                    forgotReenter.setText("");
                }
            } else {
                Snackbar.make(forgotView, respMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                forgotPassword.setText("");
                forgotPassword.requestFocus();
                forgotReenter.setText("");
            }
        }
    }
}
