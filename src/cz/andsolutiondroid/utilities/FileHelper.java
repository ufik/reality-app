package cz.andsolutiondroid.utilities;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileHelper {

	public FileHelper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static String getThumbPathFromUri(ContentResolver c, Uri u){

		Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(
                c, u,
                MediaStore.Images.Thumbnails.MINI_KIND,
                null );
		
		if( cursor != null && cursor.getCount() > 0 ) {
		     cursor.moveToFirst();
		     
		     return cursor.getString( cursor.getColumnIndex( MediaStore.Images.Thumbnails._ID ) );
		}
		
		return null;
	}
	
}
