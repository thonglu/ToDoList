package model;

import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ModelManager {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");

	// database table
	private static final String DATABASE_TABLE = "tasks";
	// database columns
	private static final String KEY_ID = "_id";
	private static final String KEY_GID = "GID";
	private static final String KEY_TITLE = "title";
	private static final String KEY_PRIORITY = "priority";
	private static final String KEY_DES = "description";
	private static final String KEY_GROUP = "taskGroup";
	private static final String KEY_COLEMAIL = "emails";
	private static final String KEY_DUEDATE = "duedate";
	private static final String KEY_ISCOMPLETE = "iscomplete";
	private static final String KEY_ISDELETE = "isdelete";

	private SQLiteDatabase db;
	private DatabaseHanlder dbHelper;

	private Cursor gCursor;

	private final Context mContext;

	public ModelManager(Context context) {
		mContext = context;
	}

	public ModelManager open() {
		dbHelper = new DatabaseHanlder(mContext);
		db = dbHelper.getWritableDatabase();

		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public void initGroup() {
		if (!isDBOn()) {
			ContentValues c = new ContentValues();
			c.put("flag", "start");
			db.insert("flag", null, c);

			ContentValues group = new ContentValues();
			group.put("GID", "");
			group.put("title", "House Task");
			db.insert("taskGroups", null, group);

			group = new ContentValues();
			group.put("GID", "");
			group.put("title", "School Task");
			db.insert("taskGroups", null, group);

			group = new ContentValues();
			group.put("GID", "");
			group.put("title", "Daily Task");
			db.insert("taskGroups", null, group);

			group = new ContentValues();
			group.put("GID", "");
			group.put("title", "Other Task");
			db.insert("taskGroups", null, group);

			group = new ContentValues();
			group.put("GID", "");
			group.put("title", "Google Sync");

			db.insert("taskGroups", null, group);
		}
	}

	public long addTask(Task newTask) {
		gCursor = getAllGroup();
		ContentValues task = new ContentValues();

		// initialize the row
		task.put(KEY_GID, newTask.getGID());

		task.put(KEY_TITLE, newTask.getTitle());

		String pri = newTask.getPriority();
		String priority = "";
		if (pri.equals("High")) {
			priority = "1";
		} else if (pri.equals("Medium")) {
			priority = "2";
		} else if (pri.equals("Low")) {
			priority = "3";
		}

		task.put(KEY_PRIORITY, priority);

		while (gCursor.moveToNext()) {
			if (gCursor.getString(2).equals(newTask.getGroup().getTitle())) {
				task.put(KEY_GROUP, gCursor.getInt(0));
				break;
			}

		}

		task.put(KEY_DUEDATE, dateFormat.format(newTask.getDueDate()));

		String emails = "";
		for (int i = 0; i < newTask.getColEmail().length; i++) {
			emails += newTask.getColEmail()[i] + ",";
		}
		task.put(KEY_COLEMAIL, emails);

		task.put(KEY_ISCOMPLETE, newTask.isComplete());

		task.put(KEY_ISDELETE, newTask.isDelete());

		task.put(KEY_DES, newTask.getNote());

		return db.insert(DATABASE_TABLE, null, task);
	}

	public boolean flushTask() {
		return db.delete(DATABASE_TABLE, null, null) > 0;
	}

	public long editTask(Task editedTask) {
		gCursor = getAllGroup();
		ContentValues tempEditTask = new ContentValues();

		// initialize the row
		tempEditTask.put(KEY_GID, editedTask.getGID());

		tempEditTask.put(KEY_TITLE, editedTask.getTitle());

		String pri = editedTask.getPriority();
		String priority = "";
		if (pri.equals("High")) {
			priority = "1";
		} else if (pri.equals("Medium")) {
			priority = "2";
		} else if (pri.equals("Low")) {
			priority = "3";
		}

		tempEditTask.put(KEY_PRIORITY, priority);

		while (gCursor.moveToNext()) {
			if (gCursor.getString(2).equals(editedTask.getGroup().getTitle())) {
				tempEditTask.put(KEY_GROUP, gCursor.getInt(0));
				break;
			}

		}

		tempEditTask.put(KEY_DUEDATE,
				dateFormat.format(editedTask.getDueDate()));

		String emails = "";
		for (int i = 0; i < editedTask.getColEmail().length; i++) {
			emails += editedTask.getColEmail()[i] + ",";
		}
		tempEditTask.put(KEY_COLEMAIL, emails);

		tempEditTask.put(KEY_ISCOMPLETE, editedTask.isComplete());

		tempEditTask.put(KEY_ISDELETE, editedTask.isDelete());

		tempEditTask.put(KEY_DES, editedTask.getNote());

		return db.update(DATABASE_TABLE, tempEditTask, KEY_ID + "="
				+ editedTask.getDID(), null);
	}

	public long editGroup(Group group) {
		ContentValues tempEditGroup = new ContentValues();

		// initialize the row
		tempEditGroup.put(KEY_GID, group.getGID());

		tempEditGroup.put(KEY_TITLE, group.getTitle());

		return db.update(DATABASE_TABLE, tempEditGroup,
				KEY_ID + "=" + group.getDID(), null);
	}

	public Cursor getAllUsers(String order) {
		Log.i("result", order);
		return db.query(DATABASE_TABLE, new String[] { KEY_ID, KEY_GID,
				KEY_TITLE, KEY_PRIORITY, KEY_GROUP, KEY_DUEDATE, KEY_COLEMAIL,
				KEY_ISCOMPLETE, KEY_ISDELETE, KEY_DES }, null, null, null,
				null, order);

	}

	public Cursor getAllGroup() {
		return db.query("taskGroups", new String[] { "_id", "GID", "title" },
				null, null, null, null, null);
	}

	public boolean isDBOn() {
		Cursor c = db.query("flag", null, null, null, null, null, null);
		return c.moveToFirst();
	}

	private class DatabaseHanlder extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "ToDoList.db";
		private static final int DATABASE_VERSION = 1;

		private static final String TASK_TABLE = "create table tasks (_id integer primary key autoincrement, GID text not null, "
				+ "title text not null, priority text not null, taskGroup integer not null, duedate text not null, "
				+ "emails text not null, iscomplete text not null, isdelete text not null, description text not null);";

		private static final String GROUP_TABLE = "create table taskGroups (_id integer primary key autoincrement, "
				+ "GID text not null, title text not null);";

		private static final String FLAG_TABLE = "create table flag (_id integer primary key autoincrement, flag text not null);";

		public DatabaseHanlder(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(GROUP_TABLE);
			Log.i("test", "table 1");
			db.execSQL(TASK_TABLE);
			Log.i("test", "table 2");
			db.execSQL(FLAG_TABLE);
			db.execSQL("PRAGMA foreign_keys = ON;");
			Log.i("test", "DB Created");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS tasks ");
			onCreate(db);
			Log.i("test", "DB Upgrade");
		}

	}
}
