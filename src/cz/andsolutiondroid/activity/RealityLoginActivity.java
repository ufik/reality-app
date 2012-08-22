package cz.andsolutiondroid.activity;

import cz.andsolutiondroid.R;
import cz.andsolutiondroid.services.RealitySynchronize;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RealityLoginActivity extends Activity {
	
	private Button btnLogin;
	private EditText editUsername;
	private EditText editPassword;
	private SharedPreferences prefs;
	private ProgressDialog waitDlg;
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_login);
        
        btnLogin = (Button)this.findViewById(R.id.login_button);
        editUsername = (EditText)this.findViewById(R.id.edit_username);
        editPassword = (EditText)this.findViewById(R.id.edit_password);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        btnLogin.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				
				if(isNetworkAvailable()){
					
					String[] login = {editUsername.getText().toString(), editPassword.getText().toString()};
					
					new RealityLoginTask().execute(login);
				
				}else{
					Toast.makeText(getApplicationContext(), R.string.no_internet, Toast.LENGTH_LONG).show();
				}
				
			}

        	
		});
        
    }
    
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager 
              = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    
    public class RealityLoginTask extends AsyncTask<String[], Integer, String[]> {
    	
    	@Override
    	protected void onPostExecute(String[] result) {
    		
    		if(waitDlg != null)
    			waitDlg.dismiss();
    		
    		if(result != null){
    		
    			if(result[0].equalsIgnoreCase("[LOGIN_OK]")){
					
					Toast.makeText(getApplicationContext(), result[1], Toast.LENGTH_LONG).show();
					
					Editor edit = prefs.edit();
					edit.putString("username", editUsername.getText().toString());
					edit.putString("password", editPassword.getText().toString());
					edit.commit();
					
					edit.putBoolean("login", true);
					edit.commit();
					
					Intent i = new Intent(RealityLoginActivity.this, RealityMainActivity.class);
					startActivity(i);
					
					finish();
					
				}else{

					Toast.makeText(getApplicationContext(), result[1], Toast.LENGTH_LONG).show();

				}
    		}
    	}

    	@Override
    	protected void onPreExecute() {
    		
    		waitDlg = new ProgressDialog(RealityLoginActivity.this);
    		//waitDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    		waitDlg.setTitle("Počkejte prosím.");
    		waitDlg.setMessage("Probíhá přihlašování.");
    		waitDlg.setIndeterminate(false);
    		
    		if(waitDlg != null)
    			waitDlg.show();
    		
    	}
    	
    	protected void onProgressUpdate(Integer... progress) {
            waitDlg.setProgress(progress[0]);
         }
    	
    	protected String[] doInBackground(String[]... params) {
    		
    		RealitySynchronize rs = new RealitySynchronize();
			
			String[] response = rs.login(params[0][0], params[0][1]);
 
    		return response;
    	}

    	
    }
    
}

