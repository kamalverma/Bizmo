package com.android.databaseaccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DatabaseHelper extends SQLiteOpenHelper 
{
	public static String DB_PATH;
	public static String DB_NAME = "Bizmo.sqlite";
	public static SQLiteDatabase _database;

	private final Context myContext;
	public static String apstorphe = "'";
	public static String sep = ",";

	public DatabaseHelper(Context context, String uid) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
		
		File mydir = myContext.getDir("usr_"+uid, Context.MODE_PRIVATE); //Creating an internal dir;
		DB_PATH = mydir.getAbsolutePath()+ "/";//myContext.getFilesDir().toString() + "/";
	
	}

	/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * */
	public void createDataBase() throws IOException {
		boolean dbExist = checkDataBase();
		if (!dbExist) 
		{
			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonn'a be able to overwrite that
			// database with our database.
			try {
				copyDataBase();
				Log.i("database created", "");
			} catch (IOException e) {
				Log.e("error", e.toString());
				throw new Error("Error copying database");
			}
		}
	}
	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase()
	{
		SQLiteDatabase checkDB = null;
		try 
		{
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READWRITE);
		} 
		catch (SQLiteException e) 
		{
			// database does't exist yet.
		}
		if (checkDB != null) 
		{
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	public void copyDataBase() throws IOException
	{
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);
		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		
		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[2048];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}
		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public static SQLiteDatabase openDataBase() throws SQLException {
		
		// Open the database
		if (_database == null) {
			_database = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
					SQLiteDatabase.OPEN_READWRITE
							| SQLiteDatabase.CREATE_IF_NECESSARY);
		} else if (!_database.isOpen()) {
			_database = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
					SQLiteDatabase.OPEN_READWRITE
							| SQLiteDatabase.CREATE_IF_NECESSARY);
		}
		return _database;
	}

	public static void closedatabase() {
		if (_database != null)
			_database.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	
	public static Cursor getData(String query_str) 
	{
		Cursor c;
		if (_database != null) 
		{
			try 
			{
				openDataBase();
				c = _database.rawQuery(query_str, null);
				return c;
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				return null;
			}
		}
		else
			return null;
	}
	
	public static DictionaryEntry[][] get(String query_str) {

		DictionaryEntry dir = null;
		String[] columns;
		int index;
		int rowIndex = 0;
		DictionaryEntry[] row_obj = null; // An array of columns and their
											// values
		DictionaryEntry[][] data_arr = null;
		Cursor c;

		if (_database != null) {
			try {
				openDataBase();
				c = _database.rawQuery(query_str, null);
				if (c.moveToFirst()) {
					rowIndex = 0;
					data_arr = new DictionaryEntry[c.getCount()][];
					do {
						columns = c.getColumnNames();
						row_obj = new DictionaryEntry[columns.length]; // (columns.length);
						for (int i = 0; i < columns.length; i++) {
							dir = new DictionaryEntry();
							dir.key = columns[i];
							index = c.getColumnIndex(dir.key);
							dir.value = c.getString(index);
							row_obj[i] = dir;
						}
						data_arr[rowIndex] = row_obj;
						rowIndex++;
					} while (c.moveToNext());
				}
				c.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return data_arr;
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it return dir.delete(); }
		return dir.delete();
	}

}
