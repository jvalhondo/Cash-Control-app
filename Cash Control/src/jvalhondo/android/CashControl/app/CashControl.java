package jvalhondo.android.CashControl.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
// Action Bar SherLock import
import com.actionbarsherlock.app.SherlockActivity;
// Admob advertisements import
import com.google.ads.AdView;

public class CashControl extends SherlockActivity {
    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    		
    private LoansDbAdapter mDbHelper;
    private TextView mTotalAmount;
    
    private AdView adView;
    
    private ListView mListView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loans_list);
        // Data Base
        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();
        // Loans List View
        mListView = (ListView) findViewById(android.R.id.list);
        // create an Image Button Listener for creating the first loan
        // This image button is only visible if the ListView is empty
        // Detect if ListView is empty with setEmptyView() implemented on fillData()
        ImageButton emptyAddLoanImgButton = (ImageButton) findViewById(R.id.emptyAddLoanImgButton);
        emptyAddLoanImgButton.setOnClickListener(new View.OnClickListener() {
        			
        		public void onClick(View view) {
        			Intent editIntent = new Intent(CashControl.this, LoanEdit.class);
                    startActivityForResult(editIntent, ACTIVITY_EDIT);        		    	
        		}
        });
        // Fill data on the List View
        fillData();
        // Create an On Item Click Listener to the ListView
        // editing/ updating when an element of the ListWiew is clicked
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parnet, View view , int position, long id) {
            	// start LoanEdit activity
            	Intent editIntent = new Intent(CashControl.this, LoanEdit.class);
                editIntent.putExtra(LoansDbAdapter.KEY_ROWID, id);
                startActivityForResult(editIntent, ACTIVITY_EDIT);
            }
        });
        // Implements a long click listener to the ListView
        registerForContextMenu(mListView);
        // Total amount element
        mTotalAmount = (TextView) findViewById(R.id.totalAmount);
        mTotalAmount.setText(Float.toString(totalAmount()));
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
        mListView.setEmptyView(findViewById(android.R.id.empty));
        mListView.setAdapter(loans);
        // setListAdapter(loans);
    }
   
    public float totalAmount() {
    	// for getting total amount parameter
        int columnIndex = 3; // Amount Column --> 3
        Cursor cursorAmount = mDbHelper.fetchAllNotes();
        float[] amountColum = new float[cursorAmount.getCount()];
        float totalAmount = 0;
        if (cursorAmount.moveToFirst()){                       
            for (int i = 0; i < cursorAmount.getCount(); i++){
                amountColum[i] = cursorAmount.getFloat(columnIndex);
                totalAmount = totalAmount + amountColum[i];
                cursorAmount.moveToNext();
            }           
        }
        cursorAmount.close();
    	return totalAmount;
    }

    // generate the ActionBarSherlock options
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
    	com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.action_bar_main_menu, menu);
        return true;
    }
    
    // main menu: bottom menu with add, help, about options
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            // Add Loan button clicked
            case R.id.add_action_bar:
            	Intent editIntent = new Intent(CashControl.this, LoanEdit.class);
                startActivityForResult(editIntent, ACTIVITY_CREATE);
                return true;
            // Help button clicked    
            case R.id.help_action_bar:
            	Intent helpIntent = new Intent(CashControl.this, HelpActivity.class);
            	startActivity(helpIntent);
                return true;
            // About button clicked   
            case R.id.about_action_bar:
            	Intent aboutIntent = new Intent(CashControl.this, AboutActivity.class);
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
             // Share Via option clicked
            case R.id.remind:
            	Intent shareViaIntent = new Intent(android.content.Intent.ACTION_SEND);
            	shareViaIntent.setType("text/plain");
            	Cursor loanCursor = mDbHelper.fetchNote(id);
                startManagingCursor(loanCursor);
                String person = loanCursor.getString(loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_PERSON));
                String description = loanCursor.getString(loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_DESCRIPTION));
                String amount = loanCursor.getString(loanCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_AMOUNT));
            	String shareBody = "Remember " + person + " that I lent you the amount of " + amount + "€ for " + description + 
            			". Could you pay me back when you can?";
            	shareViaIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.shareSubject);
            	shareViaIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            	startActivity(Intent.createChooser(shareViaIntent, "Share via"));
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
    					// Updating the value total amount after editing a loan
    			        mTotalAmount.setText(Float.toString(totalAmount()));
    				}});
            	alertDialogDelete.show();			
    			return true;
            // default option
            default:
                return super.onContextItemSelected(item);
        }
    }

    // we get the result of startActivityForResult through this method
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
        // Updating the value total amount after editing a loan
        mTotalAmount.setText(Float.toString(totalAmount()));
    }
    
    @Override
    public void onDestroy() {
      if (adView != null) {
        adView.destroy();
      }
      super.onDestroy();
    }
}