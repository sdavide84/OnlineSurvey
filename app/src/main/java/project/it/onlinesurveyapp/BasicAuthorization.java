package project.it.onlinesurveyapp;

import android.util.Base64;

import com.bumptech.glide.load.model.LazyHeaderFactory;

public class BasicAuthorization implements LazyHeaderFactory {

    private final String username;
    private final String password;

    public BasicAuthorization(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String buildHeader() {
        return "Basic " + Base64.encodeToString((username + ":" + password).getBytes(), Base64.NO_WRAP);
    }
}
