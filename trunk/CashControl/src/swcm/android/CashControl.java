package swcm.android;

import android.app.ListActivity;
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

public class CashControl extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private LoansDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.loans_list);
        
        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();
        
        fillData();
        
        registerForContextMenu(getListView());
    } // close onCreate method

    private void fillData() {
        // Get all of the rows from the database and create the item list
        Cursor loansCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(loansCursor);

        // Create an array to specify the fields we want to display in the list (only PERSON)
        String[] from = new String[]{LoansDbAdapter.KEY_PERSON};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter loans = 
            new SimpleCursorAdapter(this, R.layout.loans_row, loansCursor, from, to);
        setListAdapter(loans);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_newloan:
                createLoan();
                return true;
                
            case R.id.help:
                //poner el metodo que queremos
            	Intent i = new Intent(this, HelpActivity.class);
            	startActivity(i);
                return true;
                
            case R.id.about:
            	//poner el metodo que queremos
            	Intent intent = new Intent(this, AboutActivity.class);
            	startActivity(intent);
                return true;
                
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch(item.getItemId()) {
            case R.id.edit:
            	Intent i = new Intent(this, LoanEdit.class);
                i.putExtra(LoansDbAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
                return true;
            case R.id.delete:
                mDbHelper.deleteLoan(info.id);
                fillData();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    // creating
    private void createLoan() {
        Intent i = new Intent(this, LoanEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    // editing/ updating
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, LoanEdit.class);
        i.putExtra(LoansDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    // we get the result of startActivityForResult through this method
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
