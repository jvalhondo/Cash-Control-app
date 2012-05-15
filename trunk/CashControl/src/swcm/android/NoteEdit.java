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

public class NoteEdit extends Activity {
	private EditText mTitleText;
	private EditText mDescriptionText;
	private Long mRowId;
	private NotesDbAdapter mDbHelper;
	
	//for the date button"Set Date"
	private TextView mDateDisplay;
    private Button mPickDate;
    private int mYear;
    private int mMonth;
    private int mDay;

    static final int DATE_DIALOG_ID = 0;
    
    //for the date button"Set Date"
    private TextView mTimeDisplay;
    private Button mPickTime;

    private int mHour;
    private int mMinute;

    static final int TIME_DIALOG_ID = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.note_edit);
		setTitle(R.string.edit_note);
		
		mTitleText = (EditText) findViewById(R.id.person);
		mDescriptionText = (EditText) findViewById(R.id.description);
		
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

        // display the current date (this method is below)
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
            (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                                    : null;
        }
        
        populateFields();
        
		confirmButton.setOnClickListener(new View.OnClickListener() {
			
		    public void onClick(View view) {
		    	setResult(RESULT_OK);
		    	finish();
		    }
		    
		});
	}
	// updates the date we display in the TextView
	private void updateDateDisplay() {
		mDateDisplay.setText(
	            new StringBuilder()
	                    // Month is 0 based so add 1
	                    .append(mMonth + 1).append("-")
	                    .append(mDay).append("-")
	                    .append(mYear).append(" "));
		
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
    
 // updates the time we display in the TextView
    private void updateTimeDisplay() {
        mTimeDisplay.setText(
            new StringBuilder()
                    .append(pad(mHour)).append(":")
                    .append(pad(mMinute)));
    }
    
    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

	private void populateFields() {
		if (mRowId != null) {
	        Cursor note = mDbHelper.fetchNote(mRowId);
	        startManagingCursor(note);
	        mTitleText.setText(note.getString(
	                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
	        mDescriptionText.setText(note.getString(
	                note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
	    }
	}
	
	// the callback received when the user "sets" the time in the dialog
	private TimePickerDialog.OnTimeSetListener mTimeSetListener =
	    new TimePickerDialog.OnTimeSetListener() {
	        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
	            mHour = hourOfDay;
	            mMinute = minute;
	            updateTimeDisplay();
	        }
	    };
	
	    @Override
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
	    
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
    }
	
	 private void saveState() {
		 String title = mTitleText.getText().toString();
	     String body = mDescriptionText.getText().toString();

	     if (mRowId == null) {
	        long id = mDbHelper.createNote(title, body);
	        if (id > 0) {
	           mRowId = id;
	        }
	     } else {
	       mDbHelper.updateNote(mRowId, title, body);
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
