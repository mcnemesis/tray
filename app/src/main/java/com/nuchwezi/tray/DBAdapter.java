package com.nuchwezi.tray;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author Nemesis Fixx
 *
 */
public class DBAdapter {

	private static final String Tag = MainActivity.TAG;
	private Context context;
	private SQLiteDatabase database;
	private DBHelper dbHelper;

	public void insertList(String KEY, String VALUE) {
		if(this.existsDictionaryKey(KEY)){
			try {
				JSONArray jsonArray = new JSONArray(fetchDictionaryEntry(KEY));
				jsonArray.put(VALUE);
				updateDictionaryEntry(new DBAdapter.DictionaryKeyValue(KEY, jsonArray.toString()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else{
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(VALUE);
			createDictionaryEntry(new DBAdapter.DictionaryKeyValue(KEY, jsonArray.toString()));
		}
	}

	public static class DictionaryKeyValue {
		public String key;
		public String value;

		public DictionaryKeyValue(String _key, String _value) {
			this.key = _key;
			this.value = _value;
		}
	}

	public DBAdapter(Context context) {
		this.context = context;
	}

	public DBAdapter open() throws SQLException {
		Log.i(Tag, "Opening Adapter to DB");
		dbHelper = new DBHelper(context);
		try{
		database = dbHelper.getWritableDatabase();
		}catch(Exception e){
			e.printStackTrace();
		}
		return this;
	}

	public void close() {
		Log.i(Tag, "Closing Adapter to DB");
		dbHelper.close();
	}

	/**
	 * Returns the Row Id of the new entry, -1 otherwise (on failure)
	 */
	public long createDictionaryEntry(DictionaryKeyValue kv) {

		ContentValues itemValues = this.create_Dictionary_ContentValues(kv);

		try {
			
			//this.deleteDictionaryEntry(kv.key);
			
			Log.i(Tag, "Creating a Dictionary Entry (" + kv.key
					+ ") in the DB...");
			return database.insert(DBHelper.DICTIONARY_DATABASE_TABLE, null,
					itemValues);

		} catch (Exception e) {
			Log.e(Tag, "Catalog DB Error : " + e.getMessage());
			return -1;
		}
	}

	/**
	 * Return a Dictionary Entry's Value for the given key, returns the null if
	 * the entry doesn't exist
	 */

	public String fetchDictionaryEntry(String key) throws SQLException {

		Log.d(Tag, "Fetching a Dictionary Entry (" + key + ") from the DB");

		Cursor mCursor = database.query(true,
				DBHelper.DICTIONARY_DATABASE_TABLE, new String[] {
						DBHelper.ROWID, DBHelper.COL_KEY, DBHelper.COL_VALUE

				}, DBHelper.COL_KEY + "= ?", new String[] { key }, null, null,
				null, "1");

		if (mCursor != null) {
			mCursor.moveToFirst();
			if (mCursor.getCount() > 0) {
				String val = mCursor.getString(mCursor
						.getColumnIndex(DBHelper.COL_VALUE));
				Log.d(Tag, String.format("DB VAL : %s", val));
				mCursor.close();
				return val;
			} else {
				mCursor.close();
				return null;
			}
		} else
			return null;
	}

	/**
	 * Update the Dictionary Entry by key
	 */

	public boolean updateDictionaryEntry(DictionaryKeyValue kv) {

		ContentValues updateValues = this.create_Dictionary_ContentValues(kv);
		Log.i(Tag, "Updating a Dictionary Entry (" + kv.key + ") in the DB");

		return database.update(DBHelper.DICTIONARY_DATABASE_TABLE,
				updateValues, DBHelper.COL_KEY + "= ?", new String[] { kv.key}) > 0;
	}

	/**
	 * Deletes Dictionary Item by key
	 */

	public boolean deleteDictionaryEntry(String key) {

		Log.w(Tag, "Deleting a Dictionary Item (KEY =" + key + ") from the DB");

		return database.delete(DBHelper.DICTIONARY_DATABASE_TABLE,
				DBHelper.COL_KEY + "= ?", new String[] { key}) > 0;
	};

	/**
	 * Return a Cursor over the list of all entries in the dictionary table
	 * 
	 * @return Cursor over all
	 */

	public Cursor fetchAllDictionaryEntries() {
		Log.i(Tag, "Fetching all Entries in the Dictionary in the DB");

		try {
			return database
					.query(DBHelper.DICTIONARY_DATABASE_TABLE, new String[] {
							DBHelper.ROWID, DBHelper.COL_KEY,
							DBHelper.COL_VALUE, }, null, null, null, null, null);

		} catch (Exception e) {
			Log.e(Tag, "ERROR While Fetching Entries : " + e.getMessage());
			return null;
		}
	}

	private ContentValues create_Dictionary_ContentValues(DictionaryKeyValue kv) {
		ContentValues values = new ContentValues();

		values.put(DBHelper.COL_KEY, kv.key);
		values.put(DBHelper.COL_VALUE, kv.value);

		return values;
	}

	public boolean isOpen() {
		if(database == null)
			return false;
		return database.isOpen();
	}
	
	/**
	 * Check whether given Dictionary Key exists
	 */
	public boolean existsDictionaryKey(String key) {
		String query = String.format("SELECT EXISTS(SELECT %s FROM %s WHERE %s = ?)",
				DBHelper.ROWID,DBHelper.DICTIONARY_DATABASE_TABLE,DBHelper.COL_KEY,key);
		
		Cursor mCursor = database.rawQuery(query, new String[] {key});

		if (mCursor != null) {
			mCursor.moveToFirst();
			if (mCursor.getCount() > 0) {
				int val = mCursor.getInt(0);
				
				Log.d(Tag, String.format("DB KEY EXISTS (%s) : %s", key, (val == 1)));
				
				mCursor.close();
				
				return (val == 1);
			} else {
				mCursor.close();
				return false;
			}
		} else
			return false;
	}

}
