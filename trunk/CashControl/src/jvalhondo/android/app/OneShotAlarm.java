package jvalhondo.android.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import jvalhondo.android.app.R;

public class OneShotAlarm extends BroadcastReceiver {
	String ns = Context.NOTIFICATION_SERVICE;
	NotificationManager mNotificationManager;
	
	private static final int CASHCONTROL_ID = 1;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	mNotificationManager= (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    	// creating a Notification
		int icon = R.drawable.piggy_bank_icon;
		CharSequence tickerText = "Remember that you lent some money!";
		long when = System.currentTimeMillis();
		
		Notification notification = new Notification(icon, tickerText, when);

		CharSequence contentTitle = "Cash Control";
		CharSequence contentText = "It's time to get your money back!";
		Intent notificationIntent = new Intent(context, CashControl.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		// passing the Notification to the NotificationManager
		mNotificationManager.notify(CASHCONTROL_ID, notification);
    }
}