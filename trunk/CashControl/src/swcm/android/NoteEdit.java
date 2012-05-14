package swcm.android;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NoteEdit extends Activity {
	private EditText mTitleText;
	private EditText mDescriptionText;
	private Long mRowId;
	private NotesDbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new NotesDbAdapter(this);
		mDbHelper.open();
		
		setContentView(R.layout.note_edit);
		setTitle(R.string.edit_note);
		
		mTitleText = (EditText) findViewById(R.id.person);
		mDescriptionText = (EditText) findViewById(R.id.description);
		
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
