package io.github.ponnamkarthik.urlshortner.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.ponnamkarthik.urlshortner.R;
import io.github.ponnamkarthik.urlshortner.dashboard.Dashboard;
import io.github.ponnamkarthik.urlshortner.util.EmailValidator;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Login extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText inputEmail;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.button_forgot_password)
    Button buttonForgotPassword;
    @BindView(R.id.button_login)
    Button buttonLogin;
    @BindView(R.id.button_register)
    Button buttonRegister;
    @BindView(R.id.progress)
    ProgressBar progress;
    @BindView(R.id.button_login_google)
    Button buttonLoginGoogle;


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();

    }

    private void loginUser() {

        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        EmailValidator emailValidator = new EmailValidator();

        if (!emailValidator.validate(email)) {
            inputEmail.setError("Invalid Email");
            return;
        }

        if (password.length() < 6) {
            inputPassword.setError("Password is too short");
            return;
        }

        showProgress();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgress();
                        if (task.isSuccessful()) {

                            FirebaseUser user = mAuth.getCurrentUser();

                            //Login Success
                            loginSuccess();

                        } else {
                            Snackbar.make(buttonRegister, "Login failed.",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, Dashboard.class);
            startActivity(intent);
            finish();
        }

    }

    @OnClick({R.id.button_forgot_password, R.id.button_login, R.id.button_register, R.id.button_login_google})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.button_forgot_password:
                intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
                break;
            case R.id.button_login:
                loginUser();
                break;
            case R.id.button_register:
                intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
                break;
            case R.id.button_login_google:
                performGoogleLogin();
                break;
        }
    }

    private void performGoogleLogin() {

        showProgress();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);


    }

    private void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    private void loginSuccess() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                hideProgress();
                Snackbar.make(buttonRegister, "Login failed.",
                        Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgress();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            //Login Success
                            loginSuccess();

                        } else {
                            Snackbar.make(buttonRegister, "Login failed.",
                                    Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
