package cz.andsolutiondroid.model;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class Reality {
	private String id;
	private String name;
	private String area; 
	private String price;
	private int synchronize;
	private String user;
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public Reality(String name, String area, String price, String user) {
		this.name = name;
		this.area = area;
		this.price = price;
		this.user = user;
	}
	
	public Reality(String id, String name, String area, String price, int sync, String user) {
		this.id = id;
		this.name = name;
		this.area = area;
		this.price = price;
		this.synchronize = sync;
		this.user = user;
	}
	
	public Reality() {
		// TODO Auto-generated constructor stub
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString(){
		
		return getName();
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public void setSynchronize(int synchronize) {
		this.synchronize = synchronize;
	}

	public int getSynchronize() {
		return synchronize;
	}
	
	public String getHash(){
		
		String string = this.getName() + this.getUser();
		
		return getMd5Hash(string);
	}
	
	public static String getMd5Hash(String input) {
		  try {
		  MessageDigest md = MessageDigest.getInstance("MD5");
		  byte[] messageDigest = md.digest(input.getBytes());
		  BigInteger number = new BigInteger(1, messageDigest);
		  String md5 = number.toString(16);
		 
		  while (md5.length() < 32)
		  md5 = "0" + md5;
		 
		  return md5;
		  } catch (NoSuchAlgorithmException e) {
		  Log.e("MD5", e.getLocalizedMessage());
		  return null;
		  }
		  }
	
	public Object toJsonString() {
		
		JSONObject obj = new JSONObject();
	    try {
			obj.put("name", getName());
			obj.put("area", getArea());
			obj.put("price", getPrice());
			obj.put("hash", getHash());
			obj.put("user", getUser());
			
			return obj.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return null;
	}

}
