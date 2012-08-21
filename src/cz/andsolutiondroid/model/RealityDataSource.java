package cz.andsolutiondroid.model;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class RealityDataSource {
	// Database fields
	private SQLiteDatabase db;
	private RealityOpenHelper dbHelper;	
	
	public RealityDataSource(Context context) {
		this.dbHelper = new RealityOpenHelper(context);
	}

	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public List<Reality> getRealities(String user){
		
		List<Reality> realities = null;
		
		try {
			
			Cursor result = db.rawQuery("SELECT * FROM reality WHERE user = ?", new String[] {user});
			result.moveToFirst();

			realities = new ArrayList<Reality>();
			while (!result.isAfterLast()) {
				
				Reality r = cursorToReality(result);
				realities.add(r);
				
				result.moveToNext();
			}
			result.close();
	
		} catch (Exception e) {

			Log.d("getRealities", "a", e);
		}
		
		return realities;
	}
	
	public Reality getRealityById(String id){
		
		Cursor result = db.query(RealityOpenHelper.REALITY_TABLE_NAME, null, "_id = ?", new String[] {id}, null, null, null);
		result.moveToFirst();
		
		Reality r = cursorToReality(result);
		
		result.close();
		
		return r;
	}
	
	public int setSynchronizeByHash(String hash){
		
		int affected = 0;
		
		ContentValues cv = new ContentValues();
		cv.put(RealityOpenHelper.COLUMN_SYNC, "1");
		
		affected = db.update(RealityOpenHelper.REALITY_TABLE_NAME, cv, RealityOpenHelper.COLUMN_HASH + " = '" + hash + "'", null);
		
		return affected;
	}
	
	private Reality cursorToReality(Cursor result){
		
		Reality r = new Reality(result.getString(0), result.getString(1), result.getString(2), result.getString(4), Integer.parseInt(result.getString(3)), result.getString(5));

		return r;
	}
	
	public long addReality(Reality reality){
		
		ContentValues cv = realityToCV(reality);
		
		Long rt = db.insert(RealityOpenHelper.REALITY_TABLE_NAME, null, cv);
		
		return rt;
	}
	
	public int updateReality(Reality reality){
		
		int affected = 0;
		
		try {
			
			ContentValues cv = realityToCV(reality);
			
			affected = db.update(RealityOpenHelper.REALITY_TABLE_NAME, cv, RealityOpenHelper.COLUMN_ID + " = " + reality.getId(), null);

		} catch (Exception e) {

			Log.d("updateReality", "", e);
		}
		
		return affected;
	}
	
	public long deleteReality(String id) {
		
		return db.delete(RealityOpenHelper.REALITY_TABLE_NAME, RealityOpenHelper.COLUMN_ID + " = " + id, null);
	}
	
	public ContentValues realityToCV(Reality reality){
		
		ContentValues cv = new ContentValues();
		
		cv.put(RealityOpenHelper.COLUMN_NAME, reality.getName());
		cv.put(RealityOpenHelper.COLUMN_AREA, reality.getArea());
		cv.put(RealityOpenHelper.COLUMN_PRICE, reality.getPrice());
		cv.put(RealityOpenHelper.COLUMN_SYNC, reality.getSynchronize());
		cv.put(RealityOpenHelper.COLUMN_HASH, reality.getHash());
		cv.put(RealityOpenHelper.COLUMN_USER, reality.getUser());
		
		return cv;
	}
	
	public SQLiteDatabase getDb() {
		return db;
	}

}
