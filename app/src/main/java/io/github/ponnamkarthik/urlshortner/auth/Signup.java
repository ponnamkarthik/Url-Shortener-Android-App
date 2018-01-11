package io.github.ponnamkarthik.urlshortner.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.ponnamkarthik.urlshortner.dashboard.Dashboard;
import io.github.ponnamkarthik.urlshortner.R;
import io.github.ponnamkarthik.urlshortner.util.EmailValidator;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Signup extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText inputEmail;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.input_confirm_password)
    EditText inputConfirmPassword;
    @BindView(R.id.button_register)
    Button buttonRegister;
    @BindView(R.id.button_back)
    Button buttonBack;
    @BindView(R.id.progress)
    ProgressBar progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick({R.id.button_register, R.id.button_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_register:
                createUser();
                break;
            case R.id.button_back:
                this.onBackPressed();
                break;
        }
    }

    private void createUser() {

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirm_password = inputConfirmPassword.getText().toString().trim();
        EmailValidator emailValidator = new EmailValidator();

        if (!emailValidator.validate(email)) {
            inputEmail.setError("Invalid Email");
            return;
        }

        if (!password.equals(confirm_password)) {
            inputPassword.setError("Password not matched");
            inputConfirmPassword.setError("Password not matched");
            return;
        }

        //show Progress
        showProgress();

        //start Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        //hide Progress
                        hideProgress();

                        //check if task is successful
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            //sginup Success
                            signupSuccess();
                        } else {
                            //signup error
                            Snackbar.make(buttonRegister, "Registration Failed",
                                    Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    private void signupSuccess() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
