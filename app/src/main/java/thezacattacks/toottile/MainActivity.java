package thezacattacks.toottile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.gson.Gson;
import com.sys1yagi.mastodon4j.MastodonClient;
import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.Scope;
import com.sys1yagi.mastodon4j.api.entity.auth.AccessToken;
import com.sys1yagi.mastodon4j.api.entity.auth.AppRegistration;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Accounts;
import com.sys1yagi.mastodon4j.api.method.Apps;

import java.util.Map;

import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

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

                                // TODO check for client/secret codes that already exist in prefs for this instance
                                //  then we register/auth
                                UtilityHelp.client = new MastodonClient.Builder(inst, new OkHttpClient.Builder(), new Gson()).build();
                                Apps app = new Apps(UtilityHelp.client);

                                // if we don't already have keys for this instance, we get them
                                if (!UtilityHelp.checkPrefsForInstance(inst)) {
                                    AppRegistration reg;

                                    MastoRegistrationTask regTask = new MastoRegistrationTask();
                                    regTask.execute(app.createApp(
                                            "TootTile",
                                            "urn:ietf:wg:oauth:2.0:oob",
                                            new Scope(Scope.Name.WRITE),
                                            "https://github.com/theZacAttacks/TootTile"
                                    ));
                                }
                                
                                try {
                                    String[] tmp;
                                    String clientID, clientSecret;
                                    tmp = UtilityHelp.secretPrefs.getString(inst + "-secrets", null).split("||");
                                    clientID = tmp[0];
                                    clientSecret = tmp[1];

                                    // TODO: move this shit into the async task
                                    AccessToken token = app.postUserNameAndPassword(clientID,
                                            clientSecret,
                                            new Scope(Scope.Name.WRITE),
                                            user,
                                            pass).execute();
                                    
                                    UtilityHelp.client = new MastodonClient.Builder(inst,
                                            new OkHttpClient.Builder(),
                                            new Gson())
                                            .accessToken(token.getAccessToken())
                                            .build();
                                    
                                    Accounts acct = new Accounts(UtilityHelp.client);
                                    
                                    String accountName = acct.getVerifyCredentials().execute().getAcct();
                                    accountName += "@" + inst;

                                    SharedPreferences.Editor accessWriter = UtilityHelp.accountPrefs.edit();
                                    accessWriter.putString(accountName, token.getAccessToken());
                                    accessWriter.commit();

                                } catch (Mastodon4jRequestException e) {
                                    UtilityHelp.displayError(view, "Couldn't get access token :(");
                                }

                                // mastoClient.postUserNameAndPassword(ID, SECRET, SCOPE, user, pass).authCode <- save

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

            prefWriter.commit();
        } else {
            UtilityHelp.displayError(findViewById(android.R.id.content), "Couldn't get client keys :(");
        }
    }

    private class MastoRegistrationTask extends AsyncTask<MastodonRequest, Void, AppRegistration> {
        private Exception e;

        protected AppRegistration doInBackground(MastodonRequest... req) {
            MastodonRequest r = req[0];
            try {
                return (AppRegistration) r.execute();
            } catch (Mastodon4jRequestException mE) {
                this.e = mE;
                return null;
            }
        }

        protected void onPostExecute(AppRegistration reg) {
            addClientKeys(reg);
        }
    }

    private class MastoGetTokenTask extends AsyncTask<MastodonRequest, Void, AccessToken> {
        protected AccessToken doInBackground(MastodonRequest... req) {
            // TODO write function to run request and get/save token
        }
    }

}
