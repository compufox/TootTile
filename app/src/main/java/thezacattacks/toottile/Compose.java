package thezacattacks.toottile;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ToggleButton;

public class Compose extends AppCompatActivity {

    private String postPrivacy;
    private boolean cw = false;

    private ImageButton privacyBtn;
    private ToggleButton cwBtn;
    private View cw_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        privacyBtn = (ImageButton) findViewById(R.id.privacy);
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
                                break;
                            case R.id.vis_unlisted:
                                postPrivacy = "unlisted";
                                break;
                            case R.id.vis_private:
                                postPrivacy = "private";
                                break;
                            case R.id.vis_dm:
                                postPrivacy = "direct";
                                break;
                        }
                        return false;
                    }
                });

                menu.show();
            }
        });

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
    }
}
