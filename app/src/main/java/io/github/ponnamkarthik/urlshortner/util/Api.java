package io.github.ponnamkarthik.urlshortner.util;

import java.util.List;

import io.github.ponnamkarthik.urlshortner.add.AddModel;
import io.github.ponnamkarthik.urlshortner.dashboard.DashboardModel;
import io.github.ponnamkarthik.urlshortner.dashboard.DeleteModel;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ponna on 17-11-2017.
 */

public interface Api {

    @GET("data")
    Call<List<DashboardModel>> getShortUrls(@Query("uid") String uid);

    @GET("add")
    Call<AddModel> shortUrl(@Query("uid") String uid, @Query("code") String code, @Query("url") String url, @Query("auto") boolean auto);

    @GET("delete")
    Call<DeleteModel> deleteUrl(@Query("uid") String uid, @Query("code") String code);
}

