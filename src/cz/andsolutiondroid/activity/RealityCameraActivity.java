package cz.andsolutiondroid.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import cz.andsolutiondroid.R;
import cz.andsolutiondroid.R.id;
import cz.andsolutiondroid.utilities.CameraPreview;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class RealityCameraActivity extends Activity {

    protected static final int MEDIA_TYPE_IMAGE = 1;
	protected static final String TAG = "CAMERA";
	private Camera mCamera;
    private CameraPreview mPreview;
    private String id_reality = null;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String size = prefs.getString("camera_size", "");
        
        int index = size.indexOf("x");
        
        if(index != -1){
        
	        int width = Integer.valueOf(size.substring(0, index));
	        int height = Integer.valueOf(size.substring(index + 1, size.length()));
	        
	        Camera.Parameters parameters = mCamera.getParameters();
	        parameters.setPictureSize(width, height);
	        mCamera.setParameters(parameters);
	        
        }
        
        id_reality = getIntent().getExtras().getString("id_reality");
        
        // Add a listener to the Capture button
    	Button captureButton = (Button) findViewById(id.button_capture);
    	captureButton.setOnClickListener(
    	    new View.OnClickListener() {
    	        
    	        public void onClick(View v) {
    	            // get an image from the camera
    	            mCamera.takePicture(null, null, mPicture);
    	        }
    	    }
    	);
    	
    	// Add a listener to the Capture button
    	Button backButton = (Button) findViewById(id.btn_back);
    	backButton.setOnClickListener(
    	    new View.OnClickListener() {
    	        
    	        public void onClick(View v) {
    	        	finish();
    	        }
    	    }
    	);
        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(id.camera_preview);
        preview.addView(mPreview);
    }
    
    protected void onResume(){
    	super.onResume();

    	getCameraInstance();
    }
    
    protected void onDestroy(){
    	super.onDestroy();
    	releaseCamera();
    }
    
    private PictureCallback mPicture = new PictureCallback() {

	    
	    public void onPictureTaken(byte[] data, Camera camera) {

	        File pictureFile = getOutputMediaFile();
	        if (pictureFile == null){
	            Log.d(TAG, "Error creating media file, check storage permissions: ");
	            return;
	        }

	        try {
	        	
	        	Display display = ((WindowManager) RealityCameraActivity.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	        	int rotation = display.getRotation();
	                        
	            int degrees = 0;
	            switch (rotation) {
	                case Surface.ROTATION_0: degrees = 0; break;
	                case Surface.ROTATION_90: degrees = 90; break;
	                case Surface.ROTATION_180: degrees = 180; break;
	                case Surface.ROTATION_270: degrees = 270; break;
	            }
	            
	            if(degrees == 0 || degrees == 180)
	            	rotation = 90;
	            else
	            	rotation = 0;
	        	
	        	Matrix mat = new Matrix();
	        	mat.postRotate(rotation);
	        	Bitmap bMap = BitmapFactory.decodeByteArray(data, 0, data.length);
	        	Bitmap bMapRotate = Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(), bMap.getHeight(), mat, true);
	        	
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            
	            bMapRotate.compress(Bitmap.CompressFormat.JPEG, 90, fos);
	            
	            MediaScannerConnection.scanFile(RealityCameraActivity.this, new String[] {pictureFile.toString()}, null, null);
	            
	            mCamera.startPreview();
	            
	            Toast.makeText(RealityCameraActivity.this, "Obrazek ulozen.", Toast.LENGTH_LONG).show();
	            
	        } catch (FileNotFoundException e) {
	            Log.d(TAG, "File not found: " + e.getMessage());
	        } catch (IOException e) {
	            Log.d(TAG, "Error accessing file: " + e.getMessage());
	        }
	        
	    }
	};
    
    /** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
	    Camera c = null;
	    try {
	        c = Camera.open(); // attempt to get a Camera instance
	    }
	    catch (Exception e){
	       
	    }
	    return c; // returns null if camera is unavailable
	}
	
	/** Create a File for saving an image or video */
	private File getOutputMediaFile(){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.
		
		if(id_reality == null){
			id_reality = "new_tmp";
		}
		
	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "Reality-ASD/" + id_reality);
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("Reality-ASD", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
	    File mediaFile;

       mediaFile = new File(mediaStorageDir.getPath() + File.separator +
       "IMG_"+ timeStamp + ".jpg");


	    return mediaFile;
	}
	
	 @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }
	 
	 private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
	
}