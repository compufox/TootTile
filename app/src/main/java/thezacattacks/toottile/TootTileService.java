package thezacattacks.toottile;

import android.content.Intent;
import android.service.quicksettings.TileService;

//import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by zepps on 9/11/17.
 */

public class TootTileService extends TileService {

    public TootTileService() {
        super();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        startActivityAndCollapse(new Intent(this, Compose.class));
    }

}
