package thezacattacks.toottile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Apps;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    private ProgressBar accntProg;
    private MastoRegistrationTask regTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActionBar();
        if (UtilityHelp.accountPrefs == null)
            UtilityHelp.accountPrefs = getSharedPreferences("thezacattacks.toottile.accounts",
                    Context.MODE_PRIVATE);
        if (UtilityHelp.secretPrefs == null)
            UtilityHelp.secretPrefs = getSharedPreferences("thezacattacks.tootile.instance_secrets",
                    Context.MODE_PRIVATE);

        accntProg = (ProgressBar) findViewById(R.id.account_progress);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inf = LayoutInflater.from(getApplicationContext());

                View dialog = inf.inflate(R.layout.dialog_instance, null);
                builder.setView(dialog);

                final EditText txtInst = (EditText) dialog.findViewById(R.id.txt_instance);
                final EditText txtEmail = (EditText) dialog.findViewById(R.id.txt_email);
                final EditText txtPass = (EditText) dialog.findViewById(R.id.txt_passwd);

                builder.setPositiveButton("Authenticate", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO change out using just the pure instance url
                                String inst = txtInst.getText().toString();
                                String user = txtEmail.getText().toString();
                                String pass = txtPass.getText().toString();

                                UtilityHelp.cacheCreds(user, pass);

                                // TODO check for client/secret codes that already exist in prefs for this instance
                                //  then we register/auth
                                UtilityHelp.client = new MastodonClient.Builder(inst, new OkHttpClient.Builder(), new Gson()).build();
                                Apps app = new Apps(UtilityHelp.client);

                                // if we don't already have keys for this instance, we get them
                                if (!UtilityHelp.checkPrefsForInstance(inst)) {
                                    regTask = new MastoRegistrationTask();
                                    regTask.execute(app.createApp(
                                            "TootTile",
                                            "urn:ietf:wg:oauth:2.0:oob",
                                            new Scope(Scope.Name.WRITE),
                                            "https://github.com/theZacAttacks/TootTile"
                                    ));
                                    accntProg.setVisibility(View.VISIBLE);
                                }

                                MastoGetTokenTask getToken = new MastoGetTokenTask();
                                getToken.execute(app);

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.main_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    private void setUpActionBar() {
        ActionBar ab = getSupportActionBar();
        if (ab != null)
            ab.setDisplayHomeAsUpEnabled(true);
    }

    private void addClientKeys(AppRegistration reg) {
        if (reg != null) {
            SharedPreferences.Editor prefWriter = UtilityHelp.secretPrefs.edit();
            prefWriter.putString(reg.getInstanceName() + "-secrets",
                    reg.getClientId() + "||" + reg.getClientSecret());

            prefWriter.apply();
        } else {
            UtilityHelp.displayError(findViewById(android.R.id.content), "Couldn't get client keys :(");
        }
    }

    private void addAccessToken(AccessToken token, String accountName) {
        accntProg.setVisibility(View.GONE);

        if (token != null) {
            SharedPreferences.Editor accessWriter = UtilityHelp.accountPrefs.edit();
            accessWriter.putString(accountName, token.getAccessToken());
            accessWriter.apply();
        } else {
            UtilityHelp.displayError(findViewById(android.R.id.content), "Couldn't get access token :(");
        }
    }

    private class MastoRegistrationTask extends AsyncTask<MastodonRequest, Void, AppRegistration> {
        protected AppRegistration doInBackground(MastodonRequest... req) {
            MastodonRequest r = req[0];
            try {
                return (AppRegistration) r.execute();
            } catch (Mastodon4jRequestException mE) {
                return null;
            }
        }

        protected void onPostExecute(AppRegistration reg) {
            addClientKeys(reg);
        }
    }

    private class MastoGetTokenTask extends AsyncTask<Apps, Void, AccessToken> {

        private String acctName;
        private String id, secret, inst, user, pass;

        protected AccessToken doInBackground(Apps... req) {
            do {
                try {
                    inst = UtilityHelp.client.getInstanceName();

                    String[] tmp;
                    tmp = UtilityHelp.secretPrefs.getString(inst + "-secrets", null).split("||");
                    id = tmp[0];
                    secret = tmp[1];

                    tmp = UtilityHelp.getCache();
                    user = tmp[0];
                    pass = tmp[1];
                } catch (Exception e) {
                    SystemClock.sleep(500);
                }
            } while (regTask != null && regTask.getStatus() == Status.FINISHED);

            // TODO write function to run request and get/save token
            MastodonRequest r = req[0].postUserNameAndPassword(id,
                    secret,
                    new Scope(Scope.Name.WRITE),
                    user,
                    pass);
            try {
                AccessToken t = (AccessToken) r.execute();

                String inst = UtilityHelp.client.getInstanceName();

                UtilityHelp.client = new MastodonClient.Builder(inst,
                        new OkHttpClient.Builder(),
                        new Gson())
                        .accessToken(t.getAccessToken())
                        .build();

                Accounts acct = new Accounts(UtilityHelp.client);

                acctName = acct.getVerifyCredentials().execute().getAcct() + "@" + inst;

                return t;
            } catch (Mastodon4jRequestException e) {
                System.out.println(e.toString());
                return null;
            }

        }

        protected void onPostExecute(AccessToken t) {
            addAccessToken(t, acctName);
        }
    }

}
