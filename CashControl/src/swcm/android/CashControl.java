package swcm.android;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class CashControl extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    
    private static final String LOGGER="MemoCoco";
    		
    private LoansDbAdapter mDbHelper;
    private NotificationManager mNM;
    private static final int CASHCONTROL_ID = 1;
    
    private NotificationService mNotificationService;
    private Intent intent;
    private ServiceConnection mConnection;
   

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        intent = new Intent(getBaseContext(), NotificationService.class);
        startService(intent);
        
        setContentView(R.layout.loans_list);
      //  mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
       
        /*// getting a reference to the NotificationManager
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        
        // creating a Notification
        int icon = R.drawable.piggy_bank_icon;
        CharSequence tickerText = "Remember that you lent some money!";
        long when = System.currentTimeMillis();
        Context context = getApplicationContext();
        CharSequence contentTitle = "Cash Control";
        CharSequence contentText = "It's time to get your money back!";
        
        Intent notificationIntent = new Intent(this, CashControl.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification(icon, tickerText, when);
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        
        // passing the Notification to the NotificationManager
        mNotificationManager.notify(CASHCONTROL_ID, notification);*/
        
        

        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                mNotificationService = ((NotificationService.LocalBinder)service).getService();

                // Tell the user about this for our demo.
                Toast.makeText(getBaseContext(), R.string.notification_service_connected,
                        Toast.LENGTH_SHORT).show();
            }

            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                // Because it is running in our same process, we should never
                // see this happen.
                mNotificationService = null;
                Toast.makeText(getBaseContext(), R.string.notification_service_disconnected,
                        Toast.LENGTH_SHORT).show();
            }
        };
        
        mDbHelper = new LoansDbAdapter(this);
        mDbHelper.open();
        
        fillData();
        
        registerForContextMenu(getListView());
        
        checkForNotifications();
    } // close onCreate method
    
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        /*bindService(new Intent(Binding.this, 
                NotificationService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;*/
    }
    
    void doUnbindService() {
        /*if (mIsBound) {
             // Detach our existing connection.
             unbindService(mConnection);
             mIsBound = false;
        }*/
     }
    
    protected void onDestroy() {
        super.onDestroy();
        //doUnbindService();
    }

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
    }

    // context menu: edit or delete an element of the ListView
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	// Handle item selection
    	switch(item.getItemId()) {
    	    // Edit option clicked
            case R.id.edit:
            	Intent i = new Intent(this, LoanEdit.class);
                i.putExtra(LoansDbAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
                return true;
            // Delete option clicked
            case R.id.delete:
                mDbHelper.deleteLoan(info.id);
                fillData();
                return true;
            
            default:
                return super.onContextItemSelected(item);
        }
    }

    // editing/ updating when an element of the ListWiew is clicked
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

	
	private int isOutdated(String date) {
		
		String[] tokens = date.split("-");
		Calendar presentCalendar = Calendar.getInstance();
		
		//Toast.makeText(getBaseContext(), "Date testing: "+"Year: "+tokens[0]+" Month: "+tokens[1]+"Day:"+tokens[2],Toast.LENGTH_SHORT).show();
		Integer nDay = presentCalendar.get(Calendar.DAY_OF_MONTH);

		Integer nMonth = presentCalendar.get(Calendar.MONTH) + 1;

		//Toast.makeText(getBaseContext(),"dia actual: "+nday.toString()+"mes actual: "+nmonth.toString(),Toast.LENGTH_SHORT).show();
		Log.v(LOGGER,new Integer(nMonth.compareTo(new Integer(tokens[1]))).toString());
		
		if(nMonth<new Integer(tokens[1])) {
			return 1; //no est‡ outdated
			
		} else if(nMonth.compareTo(new Integer(tokens[1]))==0 && nDay<new Integer(tokens[2])) {
			return 1; //no est‡ outdated
			
		} else if(nMonth.compareTo(new Integer(tokens[1]))==0 && nDay.compareTo(new Integer(tokens[2]))==0) {
			return 0; //es hoy
			
		} else {
			//Toast.makeText(getBaseContext(),"dia actual: "+nday+"dia testeado: "+new Integer(tokens[2])+"mes actual: "+nmonth.toString()+" mes testeado: "+new Integer(tokens[1]),Toast.LENGTH_LONG).show();
			return -1; //est‡ outdated
		}
	}
    
    private void checkForNotifications(){
		Cursor loansCursor = mDbHelper.fetchAllNotes();
		loansCursor.moveToFirst();

		ArrayList<String> delayReminds= new ArrayList<String>();
		ArrayList<String> todayReminds= new ArrayList<String>();

		while(loansCursor.isAfterLast() == false) { 
			
			String date = new String(loansCursor.getString(
									 loansCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_DATE)));
			
			if(isOutdated(date) == -1) {
				
				String remind = new String(loansCursor.getString(
						                  loansCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_PERSON)));
				delayReminds.add(remind);
				
			} else if(isOutdated(date) == 0) {
				String remind = new String(loansCursor.getString(
						                  loansCursor.getColumnIndexOrThrow(LoansDbAdapter.KEY_PERSON)));
				todayReminds.add(remind);
			}
			
		loansCursor.moveToNext();
		}
		
	showNotification( todayReminds, 0);
	showNotification( delayReminds, -1);
	}
    
    private void showNotification(ArrayList<String> list, int flag) {
		String titleText="";
		
		if( list.isEmpty() ) {
			return;
			
		} else {

			switch(flag){
			case -1:
				titleText = "ÁTienes recocordatorios atrasados!";
				break;
			case 0:
				titleText = "ÁTienes recocordatorios hoy!";
				break;
			}

			String contentText="Recocordatorios: ";
			for(String s:list){
				contentText=contentText.concat(s);
				contentText=contentText.concat(", ");

			}
			Notification notification = new Notification(R.drawable.piggy_bank_icon, titleText,
					System.currentTimeMillis());

			// The PendingIntent to launch our activity if the user selects this notification
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					new Intent(this, CashControl.class), 0);


			// Set the info for the views that show in the notification panel.
			notification.setLatestEventInfo(this, titleText,contentText, contentIntent);

			// Send the notification.
			// We use a string id because it is a unique number.  We use it later to cancel.
			mNM.notify(flag, notification);
		}
	}
}