package jvalhondo.android.CashControl.app;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
// Action Bar Sherlock imports
import com.actionbarsherlock.app.SherlockActivity;
// These 3 can be import hear and be used
// on any other class which extends SherlockActivity 
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class LoanEdit extends SherlockActivity {
	// for fields Person, Description, Amount
	private EditText mPersonText;
	private EditText mDescriptionText;
	private EditText mAmountText;
	// DataBase params
	private Long mRowId;
	private LoansDbAdapter mDbHelper;
	// for the date Check Box
	private CheckBox mAlarmCheckBox;
	// for the date button "Set Date"
	private TextView mDateDisplay;
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;
    // for the date button "Set Time"
    private TextView mTimeDisplay;
    private Button mPickTime;
    private int mHour;
    private int mMinute;
    static final int TIME_DIALOG_ID = 1;
    
    private Calendar dateTimeCalendar;
    private Calendar alarmCalendar;
    // for the correctAlarmSet() method
    private Calendar rightNow;
	private int yearRightNow;
	private int monthRightNow;
	private int dayRightNow;
	private int hourRightNow;
	private int minuteRightNow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new LoansDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.loan_edit);
		setTitle(R.string.edit_loan_title);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// getSupportActionBar().setDisplayUseLogoEnabled(false);
		
		// capture our Text elements
		mPersonText = (EditText) findViewById(R.id.person);
		mDescriptionText = (EditText) findViewById(R.id.description);
		mAmountText = (EditText) findViewById(R.id.amount);
		
		// capture our View elements Set Date
        mDateDisplay = (TextView) findViewById(R.id.dateDisplay);
        mPickDate = (Button) findViewById(R.id.pickDate);
        // add a click listener to the button
        mPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        // get the current Date and Time Calendar
        dateTimeCalendar = Calendar.getInstance();
        mYear = dateTimeCalendar.get(Calendar.YEAR);
        // Month is 0 based so add 1
        mMonth = dateTimeCalendar.get(Calendar.MONTH);
        mDay = dateTimeCalendar.get(Calendar.DAY_OF_MONTH);
        // display the current date
        updateDateDisplay();
        
        
        // capture our View elements Set Time
        mTimeDisplay = (TextView) findViewById(R.id.timeDisplay);
        mPickTime = (Button) findViewById(R.id.pickTime);
        // add a click listener to the button
        mPickTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID);
            }
        });
        mHour = dateTimeCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = dateTimeCalendar.get(Calendar.MINUTE);
        // display the current date
        updateTimeDisplay();
        // capture our View element Check Box and add a Listener
        mAlarmCheckBox = (CheckBox) findViewById(R.id.alarmCheckBox);
		mAlarmCheckBox.setOnClickListener(new View.OnClickListener() {

			  public void onClick(View view) {
				  	// Is alarmCheckBox checked?
					if (mAlarmCheckBox.isChecked()) {
						mPickDate.setEnabled(true);
						mPickTime.setEnabled(true);
						updateDateDisplay();
						updateTimeDisplay();
					}
					else {
						mPickDate.setEnabled(false);
						mPickTime.setEnabled(false);
					}
			  }
		});
		
		mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(LoansDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(LoansDbAdapter.KEY_ROWID)
                                    : null;
        }
        
        populateFields();
        
        // capture our View element Confirm Button and add a Listener
        Button confirmButton = (Button) findViewById(R.id.confirm);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
		    public void onClick(View view) {
	    		
		    	if( someFieldsEmpty() == true ){
		    		Toast.makeText(getBaseContext(), "Loan not saved! Some fields are empty.",Toast.LENGTH_SHORT).show();
		    	} else if(mAlarmCheckBox.isChecked() == true & correctAlarmSet() == false ) { 
		    		// Month is 0 based so add 1
		    		int mMonthError = mMonth + 1;
		    		// adding a 0 if minutes or hours are between 0 or 9 included
		    		String mHourError = pad(mHour);
		    		String mMinuteError = pad(mMinute);
		    		String hourRightNowError = pad(hourRightNow);
		    		String minuteRightNowError = pad(minuteRightNow);
		    		
		    		showErrorMessage("Error: Alarm set not right", "Alarm set: " + mDay	+ "-" + mMonthError
		    				+ "-" + mYear + " at " + mHourError + ":" + mMinuteError
		    				+ " and today is: " + dayRightNow	+ "-" + monthRightNow + "-"
		    				+ yearRightNow + " at " + hourRightNowError + ":" + minuteRightNowError );	
		    	} else{
		    		Toast.makeText(getBaseContext(), "Loan saved!",Toast.LENGTH_SHORT).show();
		    		if (mAlarmCheckBox.isChecked()) upDateNotifications();
		    		setResult(RESULT_OK);
		    		finish();
		    	}
		    }    
		});
	} // close onCreate method
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.action_bar_edit_menu, menu);
        return true;
    }
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    	
    		case R.id.save_action_bar:
    			if( someFieldsEmpty() == true ){
		    		Toast.makeText(getBaseContext(), "Loan not saved! Some fields are empty.",Toast.LENGTH_SHORT).show();
		    	} else if(mAlarmCheckBox.isChecked() == true & correctAlarmSet() == false ) { 
		    		// Month is 0 based so add 1
		    		int mMonthError = mMonth + 1;
		    		// adding a 0 if minutes or hours are between 0 or 9 included
		    		String mHourError = pad(mHour);
		    		String mMinuteError = pad(mMinute);
		    		String hourRightNowError = pad(hourRightNow);
		    		String minuteRightNowError = pad(minuteRightNow);
		    		
		    		showErrorMessage("Error: Alarm set not right", "Alarm set: " + mDay	+ "-" + mMonthError
		    				+ "-" + mYear + " at " + mHourError + ":" + mMinuteError
		    				+ " and today is: " + dayRightNow	+ "-" + monthRightNow + "-"
		    				+ yearRightNow + " at " + hourRightNowError + ":" + minuteRightNowError );	
		    	} else{
		    		Toast.makeText(getBaseContext(), "Loan saved!",Toast.LENGTH_SHORT).show();
		    		if (mAlarmCheckBox.isChecked()) upDateNotifications();
		    		setResult(RESULT_OK);
		    		finish();
		    	}
    			return true;
    			
    		case R.id.social_share_action_bar:
    			if (someFieldsEmpty()) {
		    		Toast.makeText(getBaseContext(), "Share option not able! Some fields are empty.",Toast.LENGTH_SHORT).show();
		    	} else {
		    		Intent shareViaIntent = new Intent(android.content.Intent.ACTION_SEND);
	            	shareViaIntent.setType("text/plain");
	            	String shareBody = "Remember " + mPersonText.getText().toString() + " that I lent you the amount of " +
	            			mAmountText.getText().toString() + "€ for " + mDescriptionText.getText().toString() + 
	            			". Could you pay me back when you can?";
	            	shareViaIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.shareSubject);
	            	shareViaIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
	            	startActivity(Intent.createChooser(shareViaIntent, "Share via"));
		    	}
    			return true;
    			
    		case R.id.delete_action_bar:
    			AlertDialog.Builder alertDialogDelete = new AlertDialog.Builder(LoanEdit.this);
            	alertDialogDelete.setMessage(getResources().getString(R.string.delete_confirm_question));
            	alertDialogDelete.setNegativeButton(getResources().getString(R.string.alert_dialog_cancel), null);
            	alertDialogDelete.setPositiveButton(getResources().getString(R.string.alert_dialog_confirm), new AlertDialog.OnClickListener() {

    				// Delete confirm button
    				public void onClick(DialogInterface dialog, int which) {
    					 if (mRowId != null) {
    						 mDbHelper.deleteLoan(mRowId);
    						 setResult(RESULT_OK);
    						 finish();
    					 } else {
    						 // set params to empty for not saving the loan
    						 mPersonText.setText("");
    						 mDescriptionText.setText("");
    						 mAmountText.setText("");
    						 setResult(RESULT_OK);
    						 finish();
    					 }
    				}});
            	alertDialogDelete.show();			
    			return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }

	// check the alarm is set right
	private boolean correctAlarmSet() {
		// Get the Date and Time right now
		rightNow = Calendar.getInstance();
		yearRightNow = rightNow.get(Calendar.YEAR);
		// Month is 0 based so add 1
		monthRightNow = rightNow.get(Calendar.MONTH) + 1;
		dayRightNow = rightNow.get(Calendar.DAY_OF_MONTH);
		hourRightNow = rightNow.get(Calendar.HOUR_OF_DAY);
		minuteRightNow = rightNow.get(Calendar.MINUTE);
		// check if it's not a future date and time
		if( mYear < yearRightNow) return false;
		else if ( mMonth+1 < monthRightNow & mYear == yearRightNow ) return false;
		else if ( mDay < dayRightNow & (mMonth+1 == monthRightNow & mYear == yearRightNow )) return false;
		else if ( mHour < hourRightNow & (mDay == dayRightNow & mMonth+1 == monthRightNow & mYear == yearRightNow )) return false;
		else if ( mMinute < minuteRightNow & (mHour == hourRightNow & mDay == dayRightNow & mMonth+1 == monthRightNow & mYear == yearRightNow )) return false;	
		return true;
	}
	
	// check fields are not empty before confirm
	private boolean someFieldsEmpty() {
		
		if( (mPersonText.getText().toString()).equals("") || (mPersonText.getText().toString()).equals(" ")  ||
    			(mDescriptionText.getText().toString()).equals("") || (mDescriptionText.getText().toString()).equals(" ") ||
    			(mAmountText.getText().toString()).equals("") || (mAmountText.getText().toString()).equals(" ") ){
			
			return true;
		}
		return false;
	}
	
	// Update Alarms that has been set
	private void upDateNotifications() {
		// Get the Date and Time right now
		rightNow = Calendar.getInstance();

		alarmCalendar = Calendar.getInstance();
		alarmCalendar.setTimeInMillis(System.currentTimeMillis());
		// setting alarm date
		alarmCalendar.set(Calendar.MONTH, mMonth);
		alarmCalendar.set(Calendar.DAY_OF_MONTH, mDay);
		alarmCalendar.set(Calendar.YEAR, mYear);
		// setting alarm time
		alarmCalendar.set(Calendar.HOUR_OF_DAY, mHour);
		alarmCalendar.set(Calendar.MINUTE, mMinute);
		alarmCalendar.set(Calendar.SECOND, 0);
		alarmCalendar.set(Calendar.MILLISECOND, 0);
		
		// int alarmTime = alarmCalendar.get(Calendar.MINUTE) - rightNow.get(Calendar.MINUTE);
		// Toast.makeText(getBaseContext(), alarmTime,Toast.LENGTH_SHORT).show();
		
		Intent alarmIntent = new Intent(this, OneShotAlarm.class);
		PendingIntent pendingIntentSender = PendingIntent.getBroadcast(this,
                0, alarmIntent, 0);
		AlarmManager alarmManager = (AlarmManager) LoanEdit.this.getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), pendingIntentSender);
	}
	
	// is call when an error should be alert
	private void showErrorMessage(String title, String content) {
		AlertDialog alertDialog;
		
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(content);
		alertDialog.show();
	}
	
	// updates the date we display in the TextView
	private void updateDateDisplay() {
		mDateDisplay.setText(
	            new StringBuilder()
	            		.append(mDay).append("-")
	            		// Month is 0 based so add 1
	                    .append(mMonth + 1).append("-")
	                    .append(mYear).append(" "));
	}
	
	// updates the time we display in the TextView
	private void updateTimeDisplay() {
	    mTimeDisplay.setText(
	       new StringBuilder()
	           .append(pad(mHour)).append(":")
	           .append(pad(mMinute)));
	}
	
	// introduce a "0" if hour and/or minute are less than 10
	private static String pad(int c) {
		   if (c >= 10)
		      return String.valueOf(c);
		   else
		      return "0" + String.valueOf(c);
	}
	
	//show the dialog of the time/date Picker
	protected Dialog onCreateDialog(int id) {
	 	switch (id) {
	    case DATE_DIALOG_ID:
	         return new DatePickerDialog(this,
	                     mDateSetListener, mYear, mMonth, mDay);
	    case TIME_DIALOG_ID:
	         return new TimePickerDialog(this,
	                 mTimeSetListener, mHour, mMinute, false);
	   }
	   return null;
	}
	
	// the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDateDisplay();
                }
            };
	
	
	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	            mHour = hourOfDay;
	            mMinute = minute;
	            updateTimeDisplay();
	        }
	    };
	    
	
	private void populateFields() {
	   if (mRowId != null) {
		  Cursor loanCursor = mDbHelper.fetchNote(mRowId);
		  startManagingCursor(loanCursor);
		  mPersonText.setText(loanCursor.getString(
		              loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_PERSON)));
		  mDescriptionText.setText(loanCursor.getString(
				  	  loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_DESCRIPTION)));
		  mAmountText.setText(loanCursor.getString(
				      loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_AMOUNT)));
		  mDateDisplay.setText(loanCursor.getString(
				      loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_DATE)));
		  mTimeDisplay.setText(loanCursor.getString(
				      loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_TIME)));
	   }
    }
		
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(LoansDbAdapter.KEY_ROWID, mRowId);
    }
	 
	private void saveState() {
		 String person = mPersonText.getText().toString();
	     String description = mDescriptionText.getText().toString();
	     String amount = mAmountText.getText().toString();
	     String date = mDateDisplay.getText().toString();
	     String time = mTimeDisplay.getText().toString();
	     
	     if( someFieldsEmpty() == true ) {
	    	 
	    	 Toast.makeText(getBaseContext(), "Loan not saved!",Toast.LENGTH_SHORT).show();
	     }
	     
	     else {
	    	 if (mRowId == null) {
	 	        long id = mDbHelper.createLoan(person, description, amount, date, time);
	 	        if (id > 0) {
	 	           mRowId = id;
	 	        }
	 	     } else {
	 	       mDbHelper.updateLoan(mRowId, person, description, amount, date, time);
	 	     } 
	     }
	}

	@Override
	protected void onPause() {
	        super.onPause();
	        saveState();
	}
	 
	 @Override
	 protected void onResume() {
	        super.onResume();
	        populateFields();
	 }
}