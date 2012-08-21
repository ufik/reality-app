package cz.andsolutiondroid.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Environment;

import android.util.Log;
import cz.andsolutiondroid.model.Reality;

public class RealitySynchronize{
	
	private static final String URL = "http://www.zapabike.cz/reality/index.php?presenter=RealitySynchronize";
	private HttpClient client = new DefaultHttpClient();
	private HttpPost p = new HttpPost(URL);
	
	public RealitySynchronize() {
		
	}
	
	private String getResponse(List<NameValuePair> pairs, MultipartEntity files) throws ClientProtocolException, IOException{
		
		if(pairs != null) p.setEntity(new UrlEncodedFormEntity(pairs));
		if(files != null) p.setEntity(files);
		
		HttpResponse resp = client.execute(p);
		
		BufferedReader br = new BufferedReader(
					new InputStreamReader(
							resp.getEntity().getContent(), "utf-8"));
		
		String line;
		String response = "";
		
		while ( (line = br.readLine()) != null ) {
			
			response += line;
		}		
		
		return response = response.replaceAll("&quot;", "'");
		
	}
	
	public String[] login(String username, String password){
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);   
	    nameValuePairs.add(new BasicNameValuePair("username", username));
	    nameValuePairs.add(new BasicNameValuePair("password", Sha1.getHash(password)));
	    
	    try {	
			
			String response = getResponse(nameValuePairs, null);
	
			JSONObject res = new JSONObject(response);
			String status = (String) res.get("status").toString();
			String message = (String) res.get("message").toString();
			
			String[] ret = {status, message};
			
			return ret;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    String[] ret = {"n/a", "n/a"};
	    
		return ret;
	}
	
	public HashMap upload(List<Reality> r){
		
		
		try {

			JSONObject json = new JSONObject();
			JSONArray data = new JSONArray();
			
			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			
			for (Reality reality : r) {
				data.put(reality.toJsonString());
				String fileName = createZip(reality.getId(), reality.getHash());

				if(fileName != null){
					FileBody f = new FileBody(new File(fileName));
					reqEntity.addPart(reality.getHash(), f);
				}

			}
			
			json.put("data", data);
		    
		    reqEntity.addPart("data", new StringBody(json.toString()));
		    
			String response = getResponse(null, reqEntity);
			
			Log.d("Response: ", "asd" + response);
			
			
			JSONObject res = new JSONObject(response);
			String status = (String) res.get("status").toString();
			String message = (String) res.get("message").toString();
			
			// odpoved serveru
			if(status.contains("[OK]")){
				
				JSONArray hashes = res.getJSONArray("hashes"); 
				HashMap<String, Object> resp1 = new HashMap<String, Object>();
				
				resp1.put("message", message);
				resp1.put("hashes", hashes);
				
				return resp1;
			}else{
				Log.d("Response: ", status);
				HashMap<String, Object> resp1 = new HashMap<String, Object>();
				
				resp1.put("message", message);
				
				return resp1;
			}
			
		} catch (Exception e) {
			Log.e("SERVER", "Error retrieving data.", e);
		}
		
		return null;
	}
	
	private String createZip(String id, String hash){
		//These are the files to include in the ZIP file
		File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Reality-ASD/" + id + "/");
		
		//Create a buffer for reading the files
		byte[] buf = new byte[1024];
		
		try {
		 // Create the ZIP file
		 File fileZip = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Reality-ASD/" + hash + ".zip");
		 if(fileZip.exists()){
			 boolean resp = fileZip.delete();
			 Log.d("zip delete", Boolean.toString(resp));
		 }
		 
		 String outFilename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Reality-ASD/" + hash + ".zip";
		 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
		
		 // Compress the files
		 for (String name : dir.list()) {
			 FileInputStream in = new FileInputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Reality-ASD/" + id + "/" + name);
				
		     // Add ZIP entry to output stream.
		     out.putNextEntry(new ZipEntry(name));
		Log.d("zip", id + name);
		     // Transfer bytes from the file to the ZIP file
		     int len;
		     while ((len = in.read(buf)) > 0) {
		         out.write(buf, 0, len);
		     }
		
		     // Complete the entry
		     out.closeEntry();
		     in.close();
		}

		 // Complete the ZIP file
		 out.close();
		 
		 return outFilename;
		} catch (IOException e) {
			Log.d("zip", e.getMessage());
			return null;
		}
		
	}
	
}

class Sha1 {

	  public static String getHash(String str) {
	    MessageDigest digest = null;
	    byte[] input = null;

	    try {
	      digest = MessageDigest.getInstance("SHA-1");
	      digest.reset();
	      input = digest.digest(str.getBytes("UTF-8"));

	    } catch (NoSuchAlgorithmException e1) {
	      e1.printStackTrace();
	    } catch (UnsupportedEncodingException e) {
	      e.printStackTrace();
	    }
	    return convertToHex(input);
	  }
	  
	  public static String getHash(byte[] data) {
	    MessageDigest digest = null;
	    byte[] input = null;

	    try {
	      digest = MessageDigest.getInstance("SHA-1");
	      digest.reset();
	      input = digest.digest(data);

	    } catch (NoSuchAlgorithmException e1) {
	      e1.printStackTrace();
	    }
	    return convertToHex(input);
	  }
	  
	    private static String convertToHex(byte[] data) { 
	        StringBuffer buf = new StringBuffer();
	        for (int i = 0; i < data.length; i++) { 
	            int halfbyte = (data[i] >>> 4) & 0x0F;
	            int two_halfs = 0;
	            do { 
	                if ((0 <= halfbyte) && (halfbyte <= 9)) 
	                    buf.append((char) ('0' + halfbyte));
	                else 
	                    buf.append((char) ('a' + (halfbyte - 10)));
	                halfbyte = data[i] & 0x0F;
	            } while(two_halfs++ < 1);
	        } 
	        return buf.toString();
	    } 


	}
