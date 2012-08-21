package cz.andsolutiondroid.activity;

import java.util.ArrayList;
import java.util.List;
import cz.andsolutiondroid.R;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class RealityPreferencesActivity extends PreferenceActivity {
	
	// pokud je login true, je nutne overit prihlaseni
    private SharedPreferences prefs = null;
    
    private CharSequence[] entries = {};
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    
	    ListPreference cameraSize = (ListPreference) findPreference("camera_size");
	    
	    try {
	    	 Camera camera = Camera.open();
	         Camera.Parameters cameraParameters = camera.getParameters();
	         List<Camera.Size> listSupportedPictureSizes = cameraParameters.getSupportedPictureSizes();
	 	     
	         List<String> s = new ArrayList<String>();
	         for (Size size : listSupportedPictureSizes) {
	        	 s.add(size.width + "x" + size.height); 
			}
	         
	         entries = s.toArray(new CharSequence[listSupportedPictureSizes.size()]);
	         
	         camera.release();
	 	    
		} catch (Exception e) {
			Log.d("Preference", e.getMessage());
		}
	    
	    cameraSize.setEntries(entries);
 	    cameraSize.setEntryValues(entries);
	    
	    Preference logout = (Preference) findPreference("logout");
        logout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                                public boolean onPreferenceClick(Preference preference) {
                                    
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putBoolean("login", false);
                                        editor.commit();
                                        
                                        finish();
                                        
                                        return true;
                                }

                        });  				
	}

}
