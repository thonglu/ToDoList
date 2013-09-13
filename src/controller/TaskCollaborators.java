package controller;

import java.util.ArrayList;

import android.R.id;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.taskmanagement.R;

public class TaskCollaborators extends ListActivity {

	private int deleteIndex;
	private ArrayAdapter<String> colListadapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manipulate_task_collaborators);
		
		colListadapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);

		colListadapter.setNotifyOnChange(true);
		setListAdapter(colListadapter);

		
		Button addContactBtn = (Button)findViewById(R.id.newContact);
		ListView list = (ListView)findViewById(id.list);
		addContactBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				ShowContact();
				
			}
		});
		
		
		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> adapter, View v,
					int position, long id) {
				deleteIndex = position;
				showDialog(1);
				return true;
			}
		});
		
		list.setAdapter(colListadapter);
	}

	@Override
	protected void onResume() {
		colListadapter.clear();
		if(TaskManipulation.curTask.getColEmail().length!=0) {
			for (int i = 0; i < TaskManipulation.curTask.getColEmail().length; i++) {
				colListadapter.add(TaskManipulation.curTask.getColEmail()[i]);
			}
		}
		colListadapter.notifyDataSetChanged();
		super.onResume();
	}

	
	public void ShowContact() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI);
		startActivityForResult(intent, 1001);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {

			switch (requestCode) {
			case 1001:
				Cursor cursor = null;
				Cursor pCur=null;
				Cursor cur = null;
				String email = "";
				String name = "";
				String phone = "";
				try {
					String[] contactTable = new String[] { BaseColumns._ID,
							Contacts.DISPLAY_NAME, Contacts.HAS_PHONE_NUMBER };
									
					Uri result = data.getData();
					// get the contact id from the Uri
					String id = result.getLastPathSegment();
					
					
					ContentResolver cr = getContentResolver();
//					Uri uri = Uri.parse(ContactsContract.Contacts.CONTENT_LOOKUP_URI+"/"+id);
					
					//query for phone number
					pCur = cr.query(Phone.CONTENT_URI, null,
							Phone.CONTACT_ID + "=?" , new String[]{id}, null);
					if (pCur.moveToFirst()) {
						phone = pCur.getString(pCur
								.getColumnIndex(Phone.NUMBER));
					}
					
					//query for contact name base on id
					Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
					         Uri.encode(phone));
					cur = cr.query(lookupUri,
							contactTable,null ,null, null);
					if (cur.moveToFirst()) {
						name = cur.getString(cur
								.getColumnIndex(Contacts.DISPLAY_NAME));
					}
					
					// query for everything email
					cursor = cr.query(Email.CONTENT_URI,
							null, Email.CONTACT_ID + "=?", new String[] { id },
							null);
					
					
					int emailIdx = cursor.getColumnIndex(Email.DATA);
					// let's just get the first email
					if (cursor.moveToFirst()) {
						email = cursor.getString(emailIdx);
						Log.i("email result", "Got email: " + email);
					} else {
						Log.i("emal result", "No results");
					}
				} catch (Exception e) {
					Log.e("email result", "Failed to get email data", e);
				} finally {
					if (cursor != null) {
						cursor.close();
					}
					
					if (pCur != null) {
						pCur.close();
					}
					
					if (cur != null) {
						cur.close();
					}
					if (email.length() == 0) {
						Toast.makeText(this, "No email found for contact.",
								Toast.LENGTH_LONG).show();
					} else {
						String[] tempContact = new String[] { name,
								phone };
						TaskManipulation.contact.add(tempContact);
						String[] tempEmails = TaskManipulation.curTask.getColEmail();
						ArrayList<String>newTempEmails = new ArrayList<String>();
						for (int i = 0; i < tempEmails.length; i++) {
							if(tempEmails[i] != null && !tempEmails[i].trim().equals("")){
								newTempEmails.add(tempEmails[i]);
							}
						}
						newTempEmails.add(email);
						TaskManipulation.curTask.setColEmail(newTempEmails.toArray(new String[newTempEmails.size()]));
					}
				}

				break;
			}
		}
	}
	
	 @Override
	protected Dialog onCreateDialog(int id) {
	        switch (id) {
	        case 1:
	            return new AlertDialog.Builder(this)
	                .setTitle(R.string.deleteConfirm)
	                .setPositiveButton(R.string.yesConfirm, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int whichButton) {

	                    	ArrayList<String> emails = new ArrayList<String>();
	        				for (int i = 0; i < TaskManipulation.curTask.getColEmail().length; i++) {
	        					emails.add(TaskManipulation.curTask.getColEmail()[i]);
	        				}
	        				emails.remove(deleteIndex);
	        				TaskManipulation.curTask.setColEmail(emails.toArray(new String[emails.size()]));
	        				onResume();
	        				
	                    }
	                })
	                .setNegativeButton(R.string.noConfirm, new DialogInterface.OnClickListener() {
	                    @Override
						public void onClick(DialogInterface dialog, int whichButton) {

	                        /* User clicked Cancel so do some stuff */
	                    }
	                })
	                .create();
	        }
	        return null;
	    }

}
