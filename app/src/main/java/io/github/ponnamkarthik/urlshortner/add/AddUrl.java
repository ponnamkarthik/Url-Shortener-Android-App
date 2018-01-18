package io.github.ponnamkarthik.urlshortner.add;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.ponnamkarthik.urlshortner.R;
import io.github.ponnamkarthik.urlshortner.auth.Login;
import io.github.ponnamkarthik.urlshortner.dashboard.Dashboard;
import io.github.ponnamkarthik.urlshortner.util.Api;
import io.github.ponnamkarthik.urlshortner.util.Network;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AddUrl extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.input_short_code)
    EditText inputShortCode;
    @BindView(R.id.input_long_url)
    EditText inputLongUrl;
    @BindView(R.id.button_create)
    Button buttonCreate;
    @BindView(R.id.progress)
    ProgressBar progress;

    private Api api;

    FirebaseAuth mAuth;
    FirebaseUser user;

    AddModel model;

    //Ads
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_url);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        Uri uri = i.getData();
        if(uri != null) {
            String url = uri.toString();
            inputLongUrl.setText(url);
        }

        //call LoadAds
        loadAds();

    }

    private void startDataService() {
        prepareNetwork();
        if (Network.hasConnectivity(this, true)) {
            createShortLink();
        } else {
            showError("No Internet Connection.");
        }
    }

    private void createShortLink() {

        String code = inputShortCode.getText().toString().trim();
        String long_url = inputLongUrl.getText().toString().trim();
        boolean auto = false;

        if(!Patterns.WEB_URL.matcher(long_url).matches()) {
            inputLongUrl.setError("Invalid Url");
            return;
        }

        if(code.isEmpty()) {
            auto = true;
        }

        user = mAuth.getCurrentUser();

        if (user == null) {
            invalidLogin();
            return;
        }

        showProgress();

        Call<AddModel> call = api.shortUrl(user.getUid(), code, long_url, auto);

        call.enqueue(new Callback<AddModel>() {
            @Override
            public void onResponse(Call<AddModel> call, final Response<AddModel> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        if (!response.isSuccessful()) {
                            showError("Unknown Error");
                        } else {
                            if(!response.body().getShort_url().isEmpty()) {
                                setClipboardText(response.body().getShort_url());
                            }
                        }
                    }
                });
                try {
                    model = response.body();
                    if(!model.isError()) {
                        createdSuccess();
                    } else {
                        showError(model.getMsg());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<AddModel> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        showError("Unknown Error");
                    }
                });
            }
        });


    }

    private void showError(String msg) {
        Snackbar.make(buttonCreate, msg,
                Snackbar.LENGTH_SHORT).show();
    }

    private void createdSuccess() {
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @OnClick(R.id.button_create)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_create:
                startDataService();
                break;
        }
    }

    private void invalidLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void prepareNetwork() {

        OkHttpClient client = new OkHttpClient();

        Retrofit signUp = new Retrofit.Builder()
                .client(client)
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = signUp.create(Api.class);

    }


    private void showProgress() {
        buttonCreate.setEnabled(false);
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        buttonCreate.setEnabled(true);
        progress.setVisibility(View.GONE);
    }

    private void loadAds() {
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private void setClipboardText(String text) {
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }

        Toast.makeText(this, "Short Url Copied !",
                Toast.LENGTH_SHORT).show();
    }

}
