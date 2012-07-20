package jvalhondo.android.app;

import jvalhondo.android.app.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CashControl extends ListActivity {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    		
    private LoansDbAdapter mDbHelper;
    private TextView mTotalAmount;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loans_list);
        
        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();
        
        fillData();
        
        int columnIndex = 3; // Whichever column your float is in.
        Cursor cursor = mDbHelper.fetchAllNotes();
        float[] amountColum = new float[cursor.getCount()];
        float totalAmount = 0;

        if (cursor.moveToFirst()){                       
            for (int i = 0; i < cursor.getCount(); i++){
                amountColum[i] = cursor.getFloat(columnIndex);
                totalAmount = totalAmount + amountColum[i];
                cursor.moveToNext();
            }           
        }
        cursor.close();
        mTotalAmount = (TextView) findViewById(R.id.totalAmount);
        mTotalAmount.setText(Float.toString(totalAmount));
        
        registerForContextMenu(getListView());

    } // close onCreate method

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor loansCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(loansCursor);

        // Create an array to specify the fields we want to display in the list (only PERSON)
        String[] from = new String[]{LoansDbAdapter.KEY_PERSON, LoansDbAdapter.KEY_AMOUNT};
        
        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1, R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter loans = 
            new SimpleCursorAdapter(this, R.layout.loans_row, loansCursor, from, to);
        setListAdapter(loans);
    }

    // generate the bottom main menu
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    // main menu: bottom menu with add, help, about options
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            // Add Loan button clicked
            case R.id.add_newloan:
            	Intent editIntent = new Intent(this, LoanEdit.class);
                startActivityForResult(editIntent, ACTIVITY_CREATE);
                return true;
            // Help button clicked    
            case R.id.help:
            	Intent helpIntent = new Intent(this, HelpActivity.class);
            	startActivity(helpIntent);
                return true;
            // About button clicked   
            case R.id.about:
            	Intent aboutIntent = new Intent(this, AboutActivity.class);
            	startActivity(aboutIntent);
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // generate the context menu when there is a long click on an item of the ListView
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        menu.setHeaderTitle(R.string.context_menu_title);
    }

    // context menu: edit or delete an element of the ListView
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	// for using the same id on the onClick method
    	final long id = info.id;
    	// Handle item selection
    	switch(item.getItemId()) {
    	    // Edit option clicked
            case R.id.edit:
            	Intent editIntent = new Intent(this, LoanEdit.class);
                editIntent.putExtra(LoansDbAdapter.KEY_ROWID, info.id);
                startActivityForResult(editIntent, ACTIVITY_EDIT);
                return true;
            // Delete option clicked
            case R.id.delete:
            	AlertDialog.Builder alertDialogDelete = new AlertDialog.Builder(CashControl.this);
            	alertDialogDelete.setMessage(getResources().getString(R.string.delete_confirm_question));
            	alertDialogDelete.setNegativeButton(getResources().getString(R.string.alert_dialog_cancel), null);
            	alertDialogDelete.setPositiveButton(getResources().getString(R.string.alert_dialog_confirm), new AlertDialog.OnClickListener() {

    				// Delete confirm button
    				public void onClick(DialogInterface dialog, int which) {
    					mDbHelper.deleteLoan(id);
    					fillData();
    				}});
            	alertDialogDelete.show();			
    			return true;
            
            default:
                return super.onContextItemSelected(item);
        }
    }

    // editing/ updating when an element of the ListWiew is clicked
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent editIntent = new Intent(this, LoanEdit.class);
        editIntent.putExtra(LoansDbAdapter.KEY_ROWID, id);
        startActivityForResult(editIntent, ACTIVITY_EDIT);
    }

    // we get the result of startActivityForResult through this method
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}