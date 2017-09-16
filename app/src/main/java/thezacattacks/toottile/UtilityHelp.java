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

final class UtilityHelp {

    static MastodonClient client;
    static SharedPreferences accountPrefs;
    static SharedPreferences secretPrefs;

    static private String[] credCache;

    static void displayError(View v, String msg) {
        Snackbar error = Snackbar.make(v, msg, Snackbar.LENGTH_SHORT);
        error.show();
    }

    static void loadAccount(String userName) {
        String[] tmp = userName.split("@");
        String token = accountPrefs.getString(userName, null);

        client = new MastodonClient.Builder(tmp[1],
                new OkHttpClient.Builder(),
                new Gson())
                .accessToken(token)
                .build();
    }

    static void cacheCreds(String... creds) {
        credCache = creds;
    }

    static String[] getCache() {
        String[] tmp = credCache;
        credCache = null;

        return tmp;
    }

    static boolean checkPrefsForInstance(String instance) {
        return secretPrefs.contains(instance + "-secrets");
    }

    static Map<String, ?> getAccounts() {
        return accountPrefs.getAll();
    }

}
