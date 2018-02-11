package io.github.ponnamkarthik.urlshortner.dashboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.ponnamkarthik.urlshortner.R;
import io.github.ponnamkarthik.urlshortner.add.AddUrl;
import io.github.ponnamkarthik.urlshortner.auth.Login;
import io.github.ponnamkarthik.urlshortner.util.Api;
import io.github.ponnamkarthik.urlshortner.util.Network;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Dashboard extends AppCompatActivity {

    @BindView(R.id.button_retry)
    Button buttonRetry;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;
    @BindView(R.id.progress_layout)
    RelativeLayout progressLayout;
    @BindView(R.id.content)
    RecyclerView content;
    @BindView(R.id.fab_add)
    FloatingActionButton fabAdd;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Api api;

    private List<DashboardModel> models;
    private DashboardAdapter dashboardAdapter;

    private DeleteInterface deleteUrl;

    // ads
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);



        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        startDataService();

        // call loadAds
        loadAds();
        loadIntersitialAds();

        deleteUrl = new DeleteInterface() {
            @Override
            public void deleteUrl(final String uid, final String code) {
                new AlertDialog.Builder(Dashboard.this)
                        .setMessage("Do you want to delete \n\nhttps:\\\\urlst.ga\\" + code + "\n\nThis url will no longer be working")
                        .setTitle("DeleteInterface")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                startDelete(uid, code);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        };

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefresh.setRefreshing(true);
                startDataService();
                Log.d("Karthik", "IsSwipe");
            }
        });

    }

    private void startDelete(String uid, String code) {
        if(Network.hasConnectivity(this, true)) {
            deleteData(uid, code);
        } else {
            Snackbar.make(fabAdd, "Not Available in offilemode",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private void startDataService() {
        hideError();
        showProgress();
        prepareNetwork();
        getData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = mAuth.getCurrentUser();
        if (user == null) {
            invalidLogin();
        }
    }

    private void invalidLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void prepareNetwork() {

        File httpCacheDirectory = new File(getCacheDir(), "responses");
        int cacheSize = 10 * 1024 * 1024; // 10 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .cache(cache)
                .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
                .build();


        Retrofit signUp = new Retrofit.Builder()
                .client(client)
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = signUp.create(Api.class);

    }

    private void getData() {

        user = mAuth.getCurrentUser();
        if (user == null) {
            invalidLogin();
            return;
        }
        Call<List<DashboardModel>> call = api.getShortUrls(user.getUid());

        call.enqueue(new Callback<List<DashboardModel>>() {
            @Override
            public void onResponse(Call<List<DashboardModel>> call, final Response<List<DashboardModel>> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        if (!response.isSuccessful()) {
                            showError();
                        }
                    }
                });
                try {
                    models = response.body();

                    setListData();
                } catch (Exception e) {
                    showError();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<DashboardModel>> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showError();
                    }
                });
            }
        });
    }

    private void deleteData(String uid, String code) {

        Call<DeleteModel> call = api.deleteUrl(uid, code);

        call.enqueue(new Callback<DeleteModel>() {
            @Override
            public void onResponse(Call<DeleteModel> call, final Response<DeleteModel> response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isSuccessful()) {
                            deleteError();
                        }
                    }
                });
                try {
                    DeleteModel deleteModel = response.body();
                    if (deleteModel.isError()) {
                        deleteError(deleteModel.getMsg());
                    } else {
                        deleteError(deleteModel.getMsg());
                    }

                    startDataService();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<DeleteModel> call, Throwable t) {
                t.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleteError();
                    }
                });
            }
        });
    }

    private void deleteError() {
        Snackbar.make(fabAdd, "Unable to DeleteInterface Url.",
                Snackbar.LENGTH_SHORT).show();
    }

    private void deleteError(String msg) {
        Snackbar.make(fabAdd, msg,
                Snackbar.LENGTH_SHORT).show();
    }

    private void setListData() {
        content.setLayoutManager(new LinearLayoutManager(this));
        if (dashboardAdapter == null) {
            dashboardAdapter = new DashboardAdapter(this, models, deleteUrl);
            content.setAdapter(dashboardAdapter);
        } else {
            dashboardAdapter.updateListData(models);
        }
    }

    private void showProgress() {
        if(models == null || models.isEmpty()) {
            progressLayout.setVisibility(View.VISIBLE);
        } else {
            swipeRefresh.setRefreshing(true);
        }
    }

    private void hideProgress() {
        if(swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
        if(models == null || models.isEmpty()) {
            progressLayout.setVisibility(View.GONE);
        } else {
            swipeRefresh.setRefreshing(false);
        }
    }

    private void showError() {
        hideProgress();
        if(models == null || models.isEmpty()) {
            errorLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hideError() {
        errorLayout.setVisibility(View.GONE);
    }

    @OnClick({R.id.button_retry, R.id.fab_add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_retry:
                startDataService();
                break;
            case R.id.fab_add:
                Intent intent = new Intent(this, AddUrl.class);
                startActivity(intent);
                break;
        }
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

    private void loadIntersitialAds() {
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstial_ad));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mInterstitialAd.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logoutUser();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
        }

        mAuth.signOut();

        invalidLogin();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            okhttp3.Response originalResponse = chain.proceed(chain.request());
            if (Network.hasConnectivity(Dashboard.this, true)) {
                int maxAge = 5; // read from cache for 1 minute
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                return originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
        }
    };

}
