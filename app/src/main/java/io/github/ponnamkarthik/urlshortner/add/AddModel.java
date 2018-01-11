package io.github.ponnamkarthik.urlshortner.add;

/**
 * Created by ponna on 11-01-2018.
 */

public class AddModel {

    private String msg;
    private boolean error;
    private String short_url;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getShort_url() {
        return short_url;
    }

    public void setShort_url(String short_url) {
        this.short_url = short_url;
    }
}
