package com.orderedbroadcastexample.app;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button start = (Button) findViewById(R.id.btn_start_broadcasts);
		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Scheduling the same alarm over and over again is ok
				AlarmReceiver.scheduleAlarm(MainActivity.this);
				Toast.makeText(MainActivity.this, R.string.notifications_started, Toast.LENGTH_SHORT).show();
			}
		});

		Button stop = (Button) findViewById(R.id.btn_stop_broadcasts);
		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlarmReceiver.cancelAlarm(MainActivity.this);
				Toast.makeText(MainActivity.this, R.string.notifications_cancelled, Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(AlarmReceiver.ACTION_RECEIVE_NOTIFICATION);
		// The default priority is 0. Positive values will be before
		// the default, lower values will be after it.
		filter.setPriority(1);

		// It's good practice to register your BroadcastReceiver's in
		// onResume() unless you have a good reason not to
		registerReceiver(mForegroundReceiver, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// It's good practice to unregister your BroadcastReceiver's in
		// onPause() unless you have a good reason not to
		unregisterReceiver(mForegroundReceiver);
	}

	private BroadcastReceiver mForegroundReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			// This is part of the magic, don't send this intent to anyone else!
			abortBroadcast();

			// Let the user know we received a broadcast (if we want).
			Toast.makeText(MainActivity.this, R.string.received_in_foreground, Toast.LENGTH_SHORT).show();
		}
	};

	/**
	 * This BroadcastReceiver will receive notifications in the background. Because this receiver is uniquely
	 * registered in the AndroidManifest.xml, it will recieve broadcasts even while the application is not actively
	 * running.
	 * <p/>
	 * NOTE: This does NOT have to be an inner class of the Activity, it's just here for simplicity.
	 */
	public static class BackgroundReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {

			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(context)
							.setAutoCancel(true)
							.setSmallIcon(R.drawable.logo)
							.setContentTitle(context.getString(R.string.notification_title))
							.setContentText(context.getString(R.string.received_in_background));

			Intent launchIntent = new Intent(context, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, 0);
			builder.setContentIntent(pendingIntent);

			// Get an instance of the notification manager
			NotificationManager noteManager = (NotificationManager)
					context.getSystemService(Context.NOTIFICATION_SERVICE);

			// Post to the system bar
			noteManager.notify(1, builder.build());
		}
	}

	/**
	 * Implementation detail:
	 * <p/>
	 * This alarm is used to illustrate data coming in while the application is open or closed,
	 * and is not specifically related to the ordered broadcast strategy.
	 * <p/>
	 * NOTE: This does NOT have to be an inner class of the Activity, it's just here for simplicity.
	 */
	public static class AlarmReceiver extends BroadcastReceiver {

		/**
		 * The action we will broadcast with. This can be any String you want, but you
		 * should take care that it doesn't collide with any other actions, such as
		 * system actions like "android.intent.action.MAIN".
		 */
		public static final String ACTION_RECEIVE_NOTIFICATION =
				"com.orderedbroadcastexample.app.action_receive_notification";

		/**
		 * Helper function to schedule this alarm.
		 *
		 * @param context
		 */
		public static void scheduleAlarm(final Context context) {

			Intent intent = new Intent(context, AlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			// Alarm should go off in this amount of time
			long inHowManySeconds = 5 * DateUtils.SECOND_IN_MILLIS;

			AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + inHowManySeconds, pendingIntent);
		}

		/**
		 * Helper function to cancel this previously scheduled alarm.
		 *
		 * @param context
		 */
		public static void cancelAlarm(final Context context) {

			Intent intent = new Intent(context, AlarmReceiver.class);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

			AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			manager.cancel(pendingIntent);
		}

		@Override
		public void onReceive(Context context, Intent intent) {

			// Schedule a new alarm to run
			scheduleAlarm(context);

			// Create an intent to broadcast
			final Intent notificationIntent = new Intent(ACTION_RECEIVE_NOTIFICATION);

			// sendOrderedBroadcast is the magic! sendBroadcast will not produce the desired behavior!
			context.sendOrderedBroadcast(notificationIntent, null);
		}
	}
}
