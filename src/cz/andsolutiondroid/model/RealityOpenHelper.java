package cz.andsolutiondroid.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RealityOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "reality.db";
    private static final int DATABASE_VERSION = 6;
    
    public static final String REALITY_TABLE_NAME = "reality";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AREA = "area";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_USER = "user";
    public static final String COLUMN_SYNC = "sync";
    public static final String COLUMN_HASH = "hash";
    
    private static final String REALITY_TABLE_CREATE =
        "CREATE TABLE " + REALITY_TABLE_NAME + " (" +
        		""+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT," +
        		" "+COLUMN_NAME+" TEXT," +
        		" "+COLUMN_AREA+" TEXT," +
        		" "+COLUMN_SYNC+" TEXT," +
        		" "+COLUMN_PRICE+" TEXT," +
        		" "+COLUMN_USER+" TEXT," +
        		" "+COLUMN_HASH+" TEXT);";
    
    public RealityOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
   
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(REALITY_TABLE_CREATE);
    }

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		db.execSQL("DROP TABLE IF EXISTS " + REALITY_TABLE_NAME);
		onCreate(db);
		
	}
	
}