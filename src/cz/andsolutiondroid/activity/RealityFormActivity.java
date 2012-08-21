package cz.andsolutiondroid.activity;

import java.io.File;
import cz.andsolutiondroid.R;
import cz.andsolutiondroid.model.Reality;
import cz.andsolutiondroid.model.RealityDataSource;
import cz.andsolutiondroid.utilities.SingleMediaScanner;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;

public class RealityFormActivity extends Activity  {
	protected static final String MEDIA_TYPE_IMAGE = null;

	private RealityDataSource model = new RealityDataSource(this);
	
	private Reality updateReality;
	private String id = null;
	private EditText name;
	private EditText area;
	private EditText price;
	
	private Gallery sdcardImages;
	private ImageAdapter iAdapter = new ImageAdapter(this);
	
	private SharedPreferences prefs;
	
	/**
     * Cursor used to access the results from querying for images on the SD card.
     */
    private Cursor cursor;
    /*
     * Column index for the Thumbnails Image IDs.
     */
    private int columnIndex;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        name = (EditText) RealityFormActivity.this.findViewById(R.id.EditText01);
        area = (EditText) RealityFormActivity.this.findViewById(R.id.EditText02);
        price  = (EditText) RealityFormActivity.this.findViewById(R.id.EditText03);
        
        Button btnForm = (Button)this.findViewById(R.id.add_reality);
        Button btnCamera = (Button)this.findViewById(R.id.take_picture);
        
        if(getIntent().getExtras() != null){
        	id = getIntent().getExtras().getString("id_reality");
        }
        
        if(id != null){

            model.open();
            updateReality = model.getRealityById(id);
            model.close();
            
            name.setText(updateReality.getName());
            area.setText(updateReality.getArea());
            price.setText(updateReality.getPrice());
        }
        
        btnCamera.setOnClickListener(new OnClickListener() {

			
			public void onClick(View arg0) {
				Intent i = new Intent(RealityFormActivity.this, RealityCameraActivity.class);
				i.putExtra("id_reality", id);
				startActivity(i);
			}
        });
        
        btnForm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
	            
	            Reality r = new Reality(name.getText().toString(), area.getText().toString(), price.getText().toString(), prefs.getString("username", ""));

				try {
					
					model.open();
					Log.d("test", "testing");
					if(id == null){
						try {

						r.setSynchronize(0);
						Long ret_id = model.addReality(r);
						
						File dir = new File(Environment.getExternalStoragePublicDirectory(
					              Environment.DIRECTORY_PICTURES), "Reality-ASD/" + ret_id);
						// nutne vymazat adresar s fotkami new_tmp a prehrat fotky do adresare s ID reality
					    // vytvori adresar pro fotky
					    if (! dir.exists()){
					        if (! dir.mkdirs()){
					            Log.d("Reality-ASD", "failed to create directory");
					        }
					    }
					    Log.d("adding reality", dir.getAbsolutePath());
					    // najdeme si docasny adresar s fotkami
					    File tmp_dir = new File(Environment.getExternalStoragePublicDirectory(
					              Environment.DIRECTORY_PICTURES), "Reality-ASD/new_tmp");
					    
					    // a premistime fotky z docasneho adresare do noveho
					    for (File file : tmp_dir.listFiles()) {
					    	Log.d("removing", file.getPath());
					    	
					    	// je take nutne smazat nahledy z docasneho adresare
					    	//new File(FileHelper.getThumbPathFromUri(getContentResolver(), Uri.fromFile(file))).delete();
					    	
							file.renameTo(new File(Environment.getExternalStoragePublicDirectory(
						              Environment.DIRECTORY_PICTURES), "Reality-ASD/" + ret_id + "/" + file.getName()));
							
							// vytvori nahled
							MediaScannerConnection.scanFile(RealityFormActivity.this, new String[] {file.toString()}, null, null);
					    	
						}
					    
					} catch (Exception e) {
						
						Log.d("adding reality", "neco spatne! " + e.getMessage());
					}
					    
					}
					else{
						r.setId(updateReality.getId());
						model.updateReality(r);
					}
					
					model.close();
					
					sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
				            + Environment.getExternalStorageDirectory()))); 
					
				} catch (Exception e) {
					Log.d("Reality", "add", e);
				}
				
				finish();
	
			}
		});
        
        this.updateCursor();

        sdcardImages = (Gallery) findViewById(R.id.gallery1);
        sdcardImages.setAdapter(iAdapter);
        
    }
	
	private void updateCursor(){
		
		String[] projection = {MediaStore.Images.Thumbnails._ID};
		
		String idPath = null;
        if(id == null){
        	idPath = "new_tmp";
        }else{
        	idPath = id;
        }
		
		// Create the cursor pointing to the SDCard
        cursor = managedQuery( MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, 
                MediaStore.Images.Media.DATA + " like ? ",
                new String[] {"%Reality-ASD/"+ idPath +"%"},  
                null);
        // Get the column index of the Thumbnails Image ID
        if(cursor != null){
        	if(cursor.getColumnCount() > 0) columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID);
        }
		
	}
	
    protected void onResume() {
        super.onResume();
        
        this.updateCursor();
        iAdapter.notifyDataSetChanged();
        
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    } 
	
	/**
     * Adapter for our image files.
     */
    private class ImageAdapter extends BaseAdapter {

        private Context context;

        public ImageAdapter(Context localContext) {
            context = localContext;
        }

        public int getCount() {
            try {
            	return cursor.getCount();
			} catch (Exception e) {
				return 0;
			}
        	
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        	ImageView i = new ImageView(context);
            // Move cursor to current position
            cursor.moveToPosition(position);
            // Get the current value for the requested column
            int imageID = cursor.getInt(columnIndex);
            
            Bitmap b = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(),
            		imageID, MediaStore.Images.Thumbnails.MINI_KIND, null);

            i.setImageBitmap(b);
            i.setLayoutParams(new Gallery.LayoutParams(150, 100));
            i.setScaleType(ImageView.ScaleType.FIT_XY);
			i.setBackgroundResource(0);
            return i;
        }

    }
	
}
