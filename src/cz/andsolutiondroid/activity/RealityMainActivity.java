package cz.andsolutiondroid.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import cz.andsolutiondroid.R;
import cz.andsolutiondroid.adapters.RealityAdapter;
import cz.andsolutiondroid.model.Reality;
import cz.andsolutiondroid.model.RealityDataSource;
import cz.andsolutiondroid.services.RealitySynchronize;
import cz.andsolutiondroid.utilities.FileHelper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class RealityMainActivity extends Activity {
	private RealityDataSource model = new RealityDataSource(this);
	private ListView lv;
	private static List<Reality> realities;
	private ArrayAdapter<Reality> aa;
	private ProgressDialog waitDlg;
	private static final int DELETE_ID = 1;
	private static final int SYNC_ID = 2;
	
	private SharedPreferences prefs;
	
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // ziskame reality prihlaseneho uzivatele
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // pokud neni uzivatel prihlasen, spusti se aktivita s loginem
        if(!isUserLogged()){
        	
        	Intent i = new Intent(RealityMainActivity.this, RealityLoginActivity.class);
    		startActivity(i);
    		
    		finish();
        }
        
		
        // initialization of variables
        Button btnForm = (Button)this.findViewById(R.id.btn_add);
        Button btnSynchronize = (Button)this.findViewById(R.id.btn_sync);
        lv = (ListView)findViewById(R.id.list);
        
        realities = reloadRealities();
        
        // set adapter
		aa = new RealityAdapter(RealityMainActivity.this, realities);
		lv.setAdapter(aa);
        
		// registering context menu for ListView
		registerForContextMenu(lv);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> av, View v, int index,
					long arg3) {

				Intent i = new Intent(RealityMainActivity.this, RealityFormActivity.class);
				i.putExtra("id_reality", realities.get(index).getId().toString());
				startActivity(i);
				
			}
		});
		
        btnForm.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				Intent i = new Intent(RealityMainActivity.this, RealityFormActivity.class);
				startActivity(i);
	
			}
		});
        
        btnSynchronize.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(isNetworkAvailable())
					new RealitySynchronizeTask().execute(realities);
				else
					Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
	
			}
		});
    }
    
    private boolean isUserLogged(){
		
    	if(prefs.getBoolean("login", false)){
    		return true;
    	}
    	
    	return false;
    }
    
    private List<Reality> reloadRealities(){
    	
    	List<Reality> r = null;
    	
    	if(isUserLogged()){
    	
	    	model.open();
	    	r = model.getRealities(prefs.getString("username", ""));
	        model.close();
        
    	}else{
    		
    		//Toast.makeText(getApplicationContext(), "Je nutné se nejdříve přihlásit!", Toast.LENGTH_LONG).show();

			r = new ArrayList<Reality>();
    	}
        
        return r;
    }
    
    protected void onResume() {
        super.onResume();
        
        if(!isUserLogged()){
        	
        	Intent i = new Intent(RealityMainActivity.this, RealityLoginActivity.class);
    		startActivity(i);
    		
    		finish();
        }
        
        realities = reloadRealities();
        
        aa = new RealityAdapter(RealityMainActivity.this, realities);
		lv.setAdapter(aa);
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        
        inflater.inflate(R.menu.menu, menu);
        
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                
            	Intent i = new Intent(RealityMainActivity.this, RealityPreferencesActivity.class);
				startActivity(i);
            	
                return true;
            case R.id.exit:
            	
            	finish();
            	
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    public void updateRealities(){
    	
    	aa.notifyDataSetChanged();
    }
    
    public void deleteReality(Long id){
    	
    	model.open();
    	
    	Reality r = realities.get(Integer.parseInt(id.toString()));
    	realities.remove(Integer.parseInt(id.toString()));
    	
    	model.deleteReality(r.getId());
    	
    	model.close();
    	
    	// smazeme celou slozku
    	File realityDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Reality-ASD/" + r.getId()); 
    	for (File file : realityDir.listFiles()) {
    		// nutne jeste nalezt miniatury a smazat
    		String path = FileHelper.getThumbPathFromUri(getContentResolver(), Uri.fromFile(file));
	    	if(path != null){
	    		new File(path).delete();
	    	}
    		// a nasledne smazat samotny soubor
    		file.delete();
		}
    	
    	realityDir.delete();
    	
    	// update adapteru
    	this.updateRealities();
    }
    
	private void syncReality(Long id) {

		Reality r = realities.get(Integer.parseInt(id.toString()));
		List<Reality> list = new ArrayList<Reality>();
		list.add(r);
		
		new RealitySynchronizeTask().execute(list);

	}
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
    	menu.add(Menu.NONE, DELETE_ID, Menu.NONE, "Smazat");
    	menu.add(Menu.NONE, SYNC_ID, Menu.NONE, "Synchronizovat");
    }

	// TODO: predelat tak aby bylo mozne skryt v menu synchronizaci, pokud uz je polozka synchronizovana
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case DELETE_ID:

	        	deleteReality(info.id);
	        	
	            return true;
	        case SYNC_ID:
	        	
	        	if(isNetworkAvailable())
	        		syncReality(info.id);
				else
					Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();

	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	public class RealitySynchronizeTask extends AsyncTask<List<Reality>, Integer, HashMap> {
		
		@Override
		protected void onPostExecute(HashMap result) {
			
			if(waitDlg != null)
				waitDlg.dismiss();
			
			if(result != null){
			
				if(result.containsKey("hashes") == true){
				
					model.open();
					
					JSONArray hashes = (JSONArray) result.get("hashes");
					
					for (int i = 0; i < hashes.length(); i++) {
						try {
							
							int ret = model.setSynchronizeByHash(hashes.getString(i).toString());
							
							Log.d("sync", hashes.getString(i).toString());
						} catch (JSONException e) {
							
							e.printStackTrace();
						}
					}
					
					model.close();
					
					realities = reloadRealities();
			        
			        aa = new RealityAdapter(RealityMainActivity.this, realities);
					lv.setAdapter(aa);
					
					aa.notifyDataSetChanged();
					
				}
			
			}
			
			if(result != null){ 
				Toast.makeText(RealityMainActivity.this, result.get("message").toString(), 3000).show();
			}else{
				Toast.makeText(RealityMainActivity.this, "Nastala chyba při přijímání dat ze serveru!", 3000).show();
			}
			
		}

		@Override
		protected void onPreExecute() {
			
			waitDlg = new ProgressDialog(RealityMainActivity.this);
			//waitDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			waitDlg.setTitle("Počkejte prosím.");
			waitDlg.setMessage("Probíhá synchronizace...");
			waitDlg.setIndeterminate(false);
			
			if(waitDlg != null)
				waitDlg.show();
			
		}
		
		protected void onProgressUpdate(Integer... progress) {
	        waitDlg.setProgress(progress[0]);
	     }
		
		@Override
		protected HashMap<String, Object> doInBackground(List<Reality>... realities) {
			
			RealitySynchronize sync = new RealitySynchronize();
				
			List<Reality> params = realities[0];
			
			List<Reality> rs = new ArrayList<Reality>();
			
			 int count = params.size();
	         for (int i = 0; i < count; i++) {
				 
	        	 rs.add(params.get(i));
	        	 //publishProgress((int) ((i / (float) count) * 100));
			}
			
			HashMap<String, Object> response = sync.upload(rs);
	         
			return response;
		}
		
	}
	
}