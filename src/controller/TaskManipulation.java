package controller;

import java.util.ArrayList;

import model.Task;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.gsm.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.example.taskmanagement.R;

@SuppressWarnings("deprecation")
public class TaskManipulation extends TabActivity {

	static ArrayList<String[]> contact = new ArrayList<String[]>();
	static Task curTask;
	private TabHost tabHost;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_tab);

		Intent receiveIntent = getIntent();
		curTask = (Task) receiveIntent.getSerializableExtra("view task");
		tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent(this, TaskDetailInfo.class);
		spec = tabHost.newTabSpec("details").setIndicator("Detail Information")
				.setContent(intent);

		tabHost.addTab(spec);

		intent = new Intent(this, TaskCollaborators.class);
		spec = tabHost.newTabSpec("collaborators")
				.setIndicator("Collaborators").setContent(intent);

		tabHost.addTab(spec);
		tabHost.setCurrentTab(2);

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(
						tabHost.getApplicationWindowToken(), 0);

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tabs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Intent menuIntent;
		switch (item.getItemId()) {
		case R.id.saveMenu:
			if (validateTask(curTask)) {
				for (int i = 0; i < contact.size(); i++) {
					Log.i("email result", contact.get(i)[0]);
					Log.i("email result", contact.get(i)[1]);
				}
				
				showDialog(1);
			}
			return true;
		case R.id.deleteMenu:
			menuIntent = new Intent(this, MainActivity.class);
			menuIntent.putExtra("delete current task", curTask);
			menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(menuIntent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean validateTask(Task task) {
		if (curTask.getColEmail() == null) {
			curTask.setColEmail(new String[] {""});

		}
		return true;
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			
			return new AlertDialog.Builder(this)
					.setTitle(R.string.callConfirm)
					.setPositiveButton(R.string.yesConfirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String mesContent = "! You got a new to do task!!!\n"
											+ curTask.toSMSString();
									SmsManager smsManager = SmsManager
											.getDefault();
									for (int i = 0; i < contact.size(); i++) {
										String mes = "hi " + contact.get(i)[0]
												+ mesContent;

										smsManager.sendTextMessage(
												contact.get(i)[1], null, mes,
												null, null);
									}
									Intent menuIntent = new Intent(TaskManipulation.this, MainActivity.class);
									if (curTask.getDID() == -1) {
										menuIntent.putExtra("add new task", curTask);
									} else {
										menuIntent.putExtra("save current task", curTask);
									}
									menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(menuIntent);

								}
							})
					.setNegativeButton(R.string.noConfirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									Intent menuIntent = new Intent(TaskManipulation.this, MainActivity.class);
									if (curTask.getDID() == -1) {
										menuIntent.putExtra("add new task", curTask);
									} else {
										menuIntent.putExtra("save current task", curTask);
									}
									menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(menuIntent);
								}
							}).create();
		}
		return null;
	}
}
