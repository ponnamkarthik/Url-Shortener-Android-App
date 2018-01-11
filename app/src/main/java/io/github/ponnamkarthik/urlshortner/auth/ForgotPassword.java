package io.github.ponnamkarthik.urlshortner.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.ponnamkarthik.urlshortner.R;
import io.github.ponnamkarthik.urlshortner.util.EmailValidator;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ForgotPassword extends AppCompatActivity {

    @BindView(R.id.input_email)
    EditText inputEmail;
    @BindView(R.id.button_reset_mail)
    Button buttonResetMail;
    @BindView(R.id.button_back)
    Button buttonBack;
    @BindView(R.id.progress)
    ProgressBar progress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();

    }

    @OnClick({R.id.button_reset_mail, R.id.button_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_reset_mail:
                sendRestMail();
                break;
            case R.id.button_back:
                this.onBackPressed();
                break;
        }
    }

    private void sendRestMail() {

        String email = inputEmail.getText().toString().trim();
        EmailValidator emailValidator = new EmailValidator();

        if(!emailValidator.validate(email)) {
            inputEmail.setError("Invalid Email");
            return;
        }

        showProgress();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgress();
                        if (task.isSuccessful()) {
                            Snackbar.make(buttonResetMail, "Password reset mail sent successful.\nPlease check your inbox.",
                                    Snackbar.LENGTH_SHORT).show();
                        } else {
                            if(task.getException().getLocalizedMessage().equals("There is no user record corresponding to this identifier. The user may have been deleted.")) {
                                Snackbar.make(buttonResetMail, "Email address not registered.",
                                        Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(buttonResetMail, "Unable to send mail.",
                                        Snackbar.LENGTH_SHORT).show();
                            }
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
