package cz.andsolutiondroid.utilities;

import java.io.File;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;

public class SingleMediaScanner implements MediaScannerConnectionClient {

private MediaScannerConnection mMs;
private File mFile;

public SingleMediaScanner(Context context, File f) {
    mFile = f;
    mMs = new MediaScannerConnection(context, this);
    mMs.connect();
}

public void onMediaScannerConnected() {
    mMs.scanFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Reality-ASD/").getAbsolutePath(), null);
}

public void onScanCompleted(String path, Uri uri) {
    mMs.disconnect();
    // TODO mozna dodelat, aby se aktualizoval adapter v realityFormActivity
}

}