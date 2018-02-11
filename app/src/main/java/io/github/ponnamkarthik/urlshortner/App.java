package io.github.ponnamkarthik.urlshortner;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by ponna on 11-01-2018.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this,
                getString(R.string.admob_app_id));

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/rsregular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
