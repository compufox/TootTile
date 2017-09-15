package thezacattacks.toottile;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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

import com.sys1yagi.mastodon4j.MastodonClient;

public class MainActivity extends AppCompatActivity {

    private MastodonClient mastoClient;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpActionBar();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                //  to using the oauth url
                                String inst = txtInst.getText().toString();
                                String user = txtEmail.getText().toString();
                                String pass = txtPass.getText().toString();

                                // TODO check for client/secret codes that already exist in prefs for this instance
                                //  then we register/auth

                                

                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.create();
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

}
