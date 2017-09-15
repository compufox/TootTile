package thezacattacks.toottile;

import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;

import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * Created by sgduser on 9/15/2017.
 */

public final class UtilityHelp {

    public static MastodonClient client;
    public static SharedPreferences accountPrefs;
    public static SharedPreferences secretPrefs;

    public static void displayError(View v, String msg) {
        Snackbar error = Snackbar.make(v, msg, Snackbar.LENGTH_SHORT);
        error.show();
    }

    public static void loadAccount(String userName) {
        String[] tmp = userName.split("@");
        String token = accountPrefs.getString(userName, null);

        client = new MastodonClient.Builder(tmp[1],
                new OkHttpClient.Builder(),
                new Gson())
                .accessToken(token)
                .build();
    }

    public static boolean checkPrefsForInstance(String instance) {
        return secretPrefs.contains(instance + "-secrets");
    }

    public static Map<String, ?> getAccounts() {
        return accountPrefs.getAll();
    }

}
