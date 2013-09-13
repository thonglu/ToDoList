package controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import model.Group;
import model.ModelManager;
import model.Task;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import com.example.taskmanagement.R;

public class MainActivity extends Activity {

	private static final int DIALOG_ACCOUNTS = 1;
	private static final int DIALOG_CONPLETE = 2;
	private static final String AUTH_TOKEN_TYPE = "Manage your tasks";
	private AccountManager accountManager;
	private EditText sText;
	private Spinner sSpin;
	private int compIndex;
	private CheckedTextView singleTaskView;
	private ModelManager db;
	private Cursor tCursor;
	private Cursor gCursor;
	private ArrayList<Task> taskList = new ArrayList<Task>();
	private ArrayList<Task> syncList = new ArrayList<Task>();
	private ArrayList<Task> searchList;
	private ArrayList<Group> groups = new ArrayList<Group>();
	private static String order = "_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// logIn();
		setContentView(R.layout.task_main_screen);

		db = new ModelManager(this);
		db.open();
		Log.i("test", "create");

		sText = (EditText) findViewById(R.id.searchText);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		searchFunction();
		db.initGroup();
	}

	private void searchFunction() {
		sSpin = (Spinner) findViewById(R.id.searchSpin);

		sSpin.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View v,
					int position, long arg3) {
				switch (position) {
				case 0:
					loadList("", "");
					sText.setEnabled(true);
					sText.addTextChangedListener(new TextWatcher() {

						@Override
						public void onTextChanged(CharSequence arg0, int arg1,
								int arg2, int arg3) {
							loadList(sText.getText().toString(), "title");

						}

						@Override
						public void beforeTextChanged(CharSequence arg0,
								int arg1, int arg2, int arg3) {
						}

						@Override
						public void afterTextChanged(Editable arg0) {
						}
					});
					sText.setOnFocusChangeListener(new OnFocusChangeListener() {

						@Override
						public void onFocusChange(View arg0, boolean arg1) {
							sText.setText("");
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									findViewById(R.id.searchSpin)
											.getApplicationWindowToken(), 0);
						}
					});
					break;
				case 1:
					sText.setEnabled(false);
					loadList(groups.get(0).getTitle(), "group");
					break;
				case 2:
					sText.setEnabled(false);
					loadList(groups.get(1).getTitle(), "group");
					break;
				case 3:
					sText.setEnabled(false);
					loadList(groups.get(2).getTitle(), "group");
					break;
				case 4:
					sText.setEnabled(false);
					loadList(groups.get(3).getTitle(), "group");
					break;
				case 5:
					sText.setEnabled(false);
					loadList(groups.get(4).getTitle(), "group");
					break;
				default:
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
		sSpin.setSelection(0);
		getData();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Task newTask = (Task) intent.getSerializableExtra("add new task");
		if (newTask != null) {
			db.addTask(newTask);
			Log.i("test", "add");
		}
		Task editTask = (Task) intent.getSerializableExtra("save current task");
		if (editTask != null) {
			db.editTask(editTask);
			Log.i("test", "edit");
		}
		Task delTask = (Task) intent
				.getSerializableExtra("delete current task");
		if (delTask != null) {
			delTask.setDelete(true);
			db.editTask(delTask);
			Log.i("test", "del");
		}

	}

	private void getData() {
		tCursor = db.getAllUsers(order);
		gCursor = db.getAllGroup();
		taskList.clear();
		syncList.clear();
		while (gCursor.moveToNext()) {
			Group group = new Group(gCursor.getString(2));
			group.setDID(gCursor.getInt(0));
			group.setGID(gCursor.getString(1));
			groups.add(group);
		}

		if (tCursor.moveToFirst()) {
			do {

				String title = tCursor.getString(2);
				String pri = tCursor.getString(3);
				String priority = "";
				if (pri.equals("1")) {
					priority = "High";
				} else if (pri.equals("2")) {
					priority = "Medium";
				} else if (pri.equals("3")) {
					priority = "Low";
				}

				Group group = null;
				for (int i = 0; i < groups.size(); i++) {
					if (tCursor.getInt(4) == groups.get(i).getDID()) {
						group = groups.get(i);
						break;
					}
				}

				Date date = new Date();
				try {
					date = new SimpleDateFormat("ddMMyyyyHHmmss").parse(tCursor
							.getString(5));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String[] emails = tCursor.getString(6).split("\\,");

				boolean isComplete = tCursor.getString(7).equalsIgnoreCase("1");

				boolean isDelete = tCursor.getString(8).equalsIgnoreCase("1");

				String des = tCursor.getString(9);
				Task task = new Task(title, priority, group, date, emails,
						isComplete, des);

				task.setGID(tCursor.getString(1));
				task.setDID(tCursor.getInt(0));
				task.setDelete(isDelete);

				if (!task.isDelete()) {
					taskList.add(task);
				}
				syncList.add(task);
			} while (tCursor.moveToNext());
		}
		loadList("", "");
	}

	private void loadList(String filter, String identity) {
		searchList = new ArrayList<Task>();
		if (identity.equals("title")) {
			for (int i = 0; i < taskList.size(); i++) {
				if (taskList.get(i).getTitle().contains(filter)) {
					searchList.add(taskList.get(i));
				}
			}
		} else if (identity.equals("group")) {
			for (int i = 0; i < taskList.size(); i++) {
				Task task = taskList.get(i);
				String group = task.getGroup().getTitle();
				if (group.equals(filter)) {
					searchList.add(taskList.get(i));
				}
			}
		} else {
			searchList = taskList;
		}

		mAdapter listAdapter = new mAdapter(this,
				R.id.main_task_list,searchList);

		ListView taskListView = (ListView) findViewById(R.id.main_task_list);
		listAdapter.notifyDataSetChanged();
		taskListView.setAdapter(listAdapter);

		taskListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long id) {
				Intent intent = new Intent(MainActivity.this,
						TaskManipulation.class);
				Task tempTask = searchList.get(position);
				intent.putExtra("view task", tempTask);
				startActivity(intent);
			}

		});

		taskListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int position, long arg3) {
				compIndex = position;
				singleTaskView = (CheckedTextView) v;
				showDialog(DIALOG_CONPLETE);
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent menuIntent;
		switch (item.getItemId()) {
		case R.id.new_task:
			menuIntent = new Intent(this, TaskManipulation.class);
			startActivity(menuIntent);
			return true;
		case R.id.sync:
			logIn();
			return true;
		case R.id.byAdd:
			order = "_id";
			getData();
			return true;
		case R.id.byDate:
			order = "duedate";
			getData();
			return true;
		case R.id.byPri:
			order = "priority";
			getData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void logIn() {

		accountManager = AccountManager.get(this);
		accountManager.invalidateAuthToken(AUTH_TOKEN_TYPE, null);
		Account[] accounts = accountManager.getAccountsByType("com.google");

		if (accounts.length > 1) {
			showDialog(DIALOG_ACCOUNTS);
		} else {
			getAccount(accounts[0]);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setTitle("Select a Google account");
			final Account[] accounts = accountManager
					.getAccountsByType("com.google");
			final int size = accounts.length;
			String[] names = new String[size];
			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Stuff to do when the account is selected by the user
					getAccount(accounts[which]);
					Log.i("test", "get account");
				}
			});
			return builder.create();
		case DIALOG_CONPLETE:
			return new AlertDialog.Builder(MainActivity.this)
					.setTitle(R.string.complete)
					.setPositiveButton(R.string.yesConfirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Drawable d = getResources().getDrawable(
											android.R.drawable.star_big_on);
									singleTaskView.setPaintFlags(singleTaskView
											.getPaintFlags()
											| Paint.STRIKE_THRU_TEXT_FLAG);
									singleTaskView.setCheckMarkDrawable(d);
									taskList.get(compIndex).setComplete(true);
									Log.i("test", taskList.get(compIndex).getTitle() +" " + taskList.get(compIndex).getPriority());
									db.editTask(taskList.get(compIndex));
									
									getData();
								}
							})
					.setNegativeButton(R.string.noConfirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Drawable d = getResources().getDrawable(
											android.R.drawable.star_big_off);
									singleTaskView.setPaintFlags(0);
									singleTaskView.setCheckMarkDrawable(d);
									taskList.get(compIndex).setComplete(false);
									db.editTask(taskList.get(compIndex));
									getData();
								}
							}).create();
		}
		return null;
	}

	private void getAccount(Account curAccount) {
		accountManager.getAuthToken(curAccount, AUTH_TOKEN_TYPE, null, this,
				new AccountManagerCallback<Bundle>() {
					@Override
					public void run(AccountManagerFuture<Bundle> future) {
						try {
							String tempToken = future.getResult().getString(
									AccountManager.KEY_AUTHTOKEN);

							accountManager.invalidateAuthToken("com.google",
									tempToken);

							String token = future.getResult().getString(
									AccountManager.KEY_AUTHTOKEN);
							Log.i("test", token);
							JSONHandler handler = new JSONHandler(
									MainActivity.this, token);

							handler.setTasks(syncList);
							handler.execute();
							if(JSONHandler.isFinish){
								syncList = handler.get();
								
								updateDB();
								
							}

							

							
						} catch (OperationCanceledException e) {

						} catch (Exception e) {

						}
					}
				}, null);
	}

	private void updateDB() {
		db.flushTask();
		for (int i = 0; i < syncList.size(); i++) {

			db.addTask(syncList.get(i));
		}

		getData();
	}

	private class mAdapter extends ArrayAdapter<Task> {

		private ArrayList<Task> adapterTasks;
		private Context context;

		public mAdapter(Context context, int textViewResourceId,
				ArrayList<Task> items) {
			super(context, textViewResourceId, items);
			this.context = context;
			this.adapterTasks = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.list_view, null);

			}
			CheckedTextView v = (CheckedTextView) view
					.findViewById(R.id.checkView);
			Task task = adapterTasks.get(position);
			if (task.isComplete()) {

				v.setText(task.toString());
				Drawable d = getResources().getDrawable(
						android.R.drawable.star_big_on);
				v.setPaintFlags(v.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				v.setCheckMarkDrawable(d);
			} else {

				v.setText(task.toString());
				Drawable d = getResources().getDrawable(
						android.R.drawable.star_big_off);
				v.setPaintFlags(0);
				v.setCheckMarkDrawable(d);
			}

			return view;
		}
	}

}
