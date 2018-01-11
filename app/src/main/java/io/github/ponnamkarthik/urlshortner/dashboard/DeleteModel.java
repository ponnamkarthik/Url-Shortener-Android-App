package io.github.ponnamkarthik.urlshortner.dashboard;

/**
 * Created by ponna on 11-01-2018.
 */

public class DeleteModel {

    private String msg;
    private boolean error;

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
}
