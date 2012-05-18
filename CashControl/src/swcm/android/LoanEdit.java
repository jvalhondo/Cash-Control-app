package swcm.android;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class LoanEdit extends Activity {
	// for fields Person, Description, Amount
	private EditText mPersonText;
	private EditText mDescriptionText;
	private EditText mAmountText;
	
	private Long mRowId;
	private LoansDbAdapter mDbHelper;
	
	// for the date button"Set Date"
	private TextView mDateDisplay;
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;
    
    // for the date button"Set Date"
    private TextView mTimeDisplay;
    private Button mPickTime;
    private int mHour;
    private int mMinute;
    static final int TIME_DIALOG_ID = 1;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new LoansDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.loan_edit);
		setTitle(R.string.edit_loan);
		
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
        // get the current date
        final Calendar dateCalendar = Calendar.getInstance();
        mYear = dateCalendar.get(Calendar.YEAR);
        mMonth = dateCalendar.get(Calendar.MONTH);
        mDay = dateCalendar.get(Calendar.DAY_OF_MONTH);
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
        // get the current time
        final Calendar timeCalendar = Calendar.getInstance();
        mHour = timeCalendar.get(Calendar.HOUR_OF_DAY);
        mMinute = timeCalendar.get(Calendar.MINUTE);
        // display the current date
        updateTimeDisplay();
        
		
		Button confirmButton = (Button) findViewById(R.id.confirm);
		
		
		mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(LoansDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(LoansDbAdapter.KEY_ROWID)
                                    : null;
        }
        
        populateFields();
        
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
		    public void onClick(View view) {
		    	setResult(RESULT_OK);
		    	finish();
		    }
		    
		});
	} // close onCreate method
	
	
	// updates the date we display in the TextView
	private void updateDateDisplay() {
		mDateDisplay.setText(
	            new StringBuilder()
	                    // Month is 0 based so add 1
	                    .append(mMonth + 1).append("-")
	                    .append(mDay).append("-")
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
	                     mDateSetListener,
	                     mYear, mMonth, mDay);
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
		  Cursor loan = mDbHelper.fetchNote(mRowId);
		  startManagingCursor(loan);
		  mPersonText.setText(loan.getString(
		              loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_PERSON)));
		  mDescriptionText.setText(loan.getString(
		                   loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_DESCRIPTION)));
		  mAmountText.setText(loan.getString(
                  loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_AMOUNT)));
		  mDateDisplay.setText(loan.getString(
                  loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_DATE)));
		  mTimeDisplay.setText(loan.getString(
                  loan.getColumnIndexOrThrow(LoansDbAdapter.KEY_TIME)));
	   }
    }
		
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(LoansDbAdapter.KEY_ROWID, mRowId); //AÑADIR LOS OTROS CAMPOS. oN RESTORE INSTANCE STATE
    }
	
	 
	private void saveState() {
		 String person = mPersonText.getText().toString();
	     String description = mDescriptionText.getText().toString();
	     String amount = mAmountText.getText().toString(); //  double B = Double.parseDouble(b.getText().toString()); Por si es double
	     String date = mDateDisplay.getText().toString();
	     String time = mTimeDisplay.getText().toString();

	     if (mRowId == null) {
	        long id = mDbHelper.createLoan(person, description, amount, date, time);
	        if (id > 0) {
	           mRowId = id;
	        }
	     } else {
	       mDbHelper.updateLoan(mRowId, person, description, amount, date, time); //PARA EVITAR HUECOS
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
