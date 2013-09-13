package controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import model.Group;
import model.Task;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.example.taskmanagement.R;

public class TaskDetailInfo extends Activity {

	private static final int DATE_DIALOG_ID = 1;
	private static final int TIME_DIALOG_ID = 2;

	private Date time;
	private EditText titleText;
	private Button dateBtn;
	private Button timeBtn;
	private Spinner priSpinner;
	private Spinner groupSpinner;
	private EditText desText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manipulate_task_detail);
		loadTask(TaskManipulation.curTask);
		setListenner();
	}

	private boolean loadTask(Task curTask){
		titleText = (EditText) findViewById(R.id.titleText);
		dateBtn = (Button) findViewById(R.id.dateBtn);
		timeBtn = (Button) findViewById(R.id.timeBtn);
		priSpinner = (Spinner) findViewById(R.id.prioritySpinner);
		groupSpinner = (Spinner) findViewById(R.id.groupSpinner);
		desText = (EditText)findViewById(R.id.descriptionText);
		
		if(TaskManipulation.curTask == null){
			Log.i("test", "new task");
			time = new Date();
			
			TaskManipulation.curTask = new Task("", "",null,time,new String[]{},false, "");
			dateBtn.setText(new SimpleDateFormat("MMM dd yyyy").format(time));
			timeBtn.setText(new SimpleDateFormat("hh : mm a").format(time));
		}else{
			
			titleText.setText(TaskManipulation.curTask.getTitle());
			
			time= TaskManipulation.curTask.getDueDate();
			dateBtn.setText(new SimpleDateFormat("MMM dd yyyy").format(time));
			timeBtn.setText(new SimpleDateFormat("hh : mm a").format(time));
			
			ArrayAdapter<String> tempAdapter;
			
			String priority = TaskManipulation.curTask.getPriority();
			tempAdapter = (ArrayAdapter<String>)priSpinner.getAdapter();
			int priPosition = tempAdapter.getPosition(priority);
			priSpinner.setSelection(priPosition);
			String group = TaskManipulation.curTask.getGroup().getTitle();
			tempAdapter = (ArrayAdapter<String>)groupSpinner.getAdapter();
			int groupPosition = tempAdapter.getPosition(group);
			groupSpinner.setSelection(groupPosition);
			
			desText.setText(TaskManipulation.curTask.getNote());
		}
		loadDateTimePicker();
		
		return true;
	}

	private void setListenner(){

		
		titleText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				TaskManipulation.curTask.setTitle(titleText.getText().toString());
			}
		});
		
		desText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				TaskManipulation.curTask.setNote(desText.getText().toString());
			}
		});
	
		priSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String pri = priSpinner.getSelectedItem().toString();
				TaskManipulation.curTask.setPriority(pri);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
		
		groupSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Group group = new Group(groupSpinner.getSelectedItem().toString());
				TaskManipulation.curTask.setGroup(group);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
			
		});
	}
	
	private void loadDateTimePicker() {
		
		dateBtn.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				showDialog(DATE_DIALOG_ID);

			}
		});

		
		timeBtn.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				showDialog(TIME_DIALOG_ID);

			}
		});

	}

	private DatePickerDialog.OnDateSetListener dateSetListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker arg0, int year, int month, int day) {
			time = TaskManipulation.curTask.getDueDate();
			time.setDate(day);
			time.setMonth(month);
			time.setYear(year-1900);
			TaskManipulation.curTask.setDueDate(time);
			dateBtn.setText(new SimpleDateFormat("MMM dd yyyy").format(time));
			
			
		}
	};

	private TimePickerDialog.OnTimeSetListener timeSetListener = new OnTimeSetListener() {
		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// TODO Auto-generated method stub
			time = TaskManipulation.curTask.getDueDate();
			time.setHours(hourOfDay);
			time.setMinutes(minute);
			TaskManipulation.curTask.setDueDate(time);
			timeBtn.setText(new SimpleDateFormat("hh : mm a").format(time));
			
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {

		Date date;
		switch (id) {
		case DATE_DIALOG_ID:
			date = TaskManipulation.curTask.getDueDate();
			return new DatePickerDialog(this, dateSetListener, date.getYear() +1900,date.getMonth(),date.getDate());
		case TIME_DIALOG_ID:
			date = TaskManipulation.curTask.getDueDate();
			return new TimePickerDialog(this, timeSetListener, date.getHours(),
					date.getMinutes(), false);
		}
		return null;
	}	
}
