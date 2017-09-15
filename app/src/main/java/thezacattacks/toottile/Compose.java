package thezacattacks.toottile;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ToggleButton;

import com.sys1yagi.mastodon4j.MastodonRequest;
import com.sys1yagi.mastodon4j.api.entity.Status;
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException;
import com.sys1yagi.mastodon4j.api.method.Statuses;

import java.util.Map;

public class Compose extends AppCompatActivity {

    private String postPrivacy;
    private String postTxt;
    private boolean cw = false;

    private ImageButton privacyBtn, nsfwBtn, acctBtn, mediaBtn;
    private Button sendBtn;
    private ToggleButton cwBtn;
    private View cw_text;

    private Map<String, String> accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        if (UtilityHelp.accountPrefs == null)
            UtilityHelp.accountPrefs = getSharedPreferences("thezacattacks.toottile.accounts",
                    Context.MODE_PRIVATE);
        if (UtilityHelp.secretPrefs == null)
            UtilityHelp.secretPrefs = getSharedPreferences("thezacattacks.tootile.instance_secrets",
                    Context.MODE_PRIVATE);

        privacyBtn = (ImageButton) findViewById(R.id.privacyBtn);
        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // make a new menu and anchor it on the button
                PopupMenu menu = new PopupMenu(Compose.this, privacyBtn);

                // inflate the xml file
                menu.getMenuInflater().inflate(R.menu.privacy_menu, menu.getMenu());

                // make a new listener for the menu clicks
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.vis_public:
                                postPrivacy = "public";
                                privacyBtn.setImageResource(R.drawable.ic_public_black_24dp);
                                break;
                            case R.id.vis_unlisted:
                                postPrivacy = "unlisted";
                                privacyBtn.setImageResource(R.drawable.ic_lock_open_black_24dp);
                                break;
                            case R.id.vis_private:
                                postPrivacy = "private";
                                privacyBtn.setImageResource(R.drawable.ic_lock_outline_black_24dp);
                                break;
                            case R.id.vis_dm:
                                postPrivacy = "direct";
                                privacyBtn.setImageResource(R.drawable.ic_mail_black_24dp);
                                break;
                        }
                        return false;
                    }
                });

                menu.show();
            }
        });

        sendBtn = (Button) findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Statuses s = new Statuses(UtilityHelp.client);
                EditText status = (EditText) findViewById(R.id.status_compose);
                postTxt = status.getText().toString();

                MastoPostTask post = new MastoPostTask();
                post.execute(s);
            }
        });

        mediaBtn = (ImageButton) findViewById(R.id.mediaBtn);

        cwBtn = (ToggleButton) findViewById(R.id.cwBtn);
        cwBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cw = cwBtn.isChecked();

                if (cw)
                    cw_text.setVisibility(View.VISIBLE);
                else
                    cw_text.setVisibility(View.GONE);
            }
        });

        cw_text = findViewById(R.id.status_cw);
        cw_text.setVisibility(View.GONE);

        nsfwBtn = (ImageButton) findViewById(R.id.nsfwBtn);
        nsfwBtn.setVisibility(View.GONE);

        acctBtn = (ImageButton) findViewById(R.id.accountBtn);

        accounts = (Map<String, String>) UtilityHelp.getAccounts();

        if (accounts.size() <= 1)
            acctBtn.setVisibility(View.GONE);
        if (accounts.isEmpty()) {
            disableButtons();
            UtilityHelp.displayError(findViewById(android.R.id.content),
                    "Please add an account in the main app");
        }

        // TODO
        //check to see if we have more than one instance saved
        // in prefs and if we do then we show the chooser button
        // maybe menu?
    }

    private void disableButtons() {
        sendBtn.setEnabled(false);
        mediaBtn.setEnabled(false);
    }

    private class MastoPostTask extends AsyncTask<Statuses, Void, Boolean> {
        protected Boolean doInBackground(Statuses... req) {
            Statuses r = req[0];
            try {

                r.postStatus(postTxt,
                        null,
                        null,
                        false,
                        null,
                        com.sys1yagi.mastodon4j.api.entity.Status.Visibility.valueOf(postPrivacy));

                return true;
            } catch (Mastodon4jRequestException mE) {
                return false;
            }
        }

        protected void onPostExecute(boolean status) {
            if (status) {
                // we close the compose activity
                Compose.this.finish();
            } else {
                UtilityHelp.displayError(findViewById(android.R.id.content), "Couldn't post status D:");
            }
        }
    }
}
