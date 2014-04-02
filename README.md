OrderedBroadcastExample: Simulating background / foreground notifications in Android
=======================

In a recent [article](http://www.objc.io/issue-11/android-notifications.html) I wrote for objc.io detailing how notifications in Android differ from those in iOS, a reader messaged me and asked 

>“How do I detect if an Application is in the foreground when a notification arrives? Android seems to be missing this functionality, where iOS has it readily available.”

It’s true, there is no intrinsic differentiation for foreground and background notifications in Android. This is largely in fundamental difference in how Android handles notifications altogether. In Android, the developer has full control over the lifecycle and presentation of a notification, and such, it could be presented at any time. Whether the app is actively running or not, the developer is in charge of presenting notifications to the user. The same callbacks are presented in all scenarios. In light of this question, I created a sample project to demonstrate one such approach to this problem.

[OrderedBroadcast Example](https://github.com/kevinthecity/OrderedBroadcastExample "OrderedBroadcast Example") (Github)

##OrderedBroadcasts 
One strategy for mitigating this problem is using a not-so-well-known API title [sendOrderedBroadcast](http://developer.android.com/reference/android/content/Context.html#sendOrderedBroadcast%28android.content.Intent,%20java.lang.String%29) available on any [Context](http://developer.android.com/reference/android/content/Context.html) within your application. An ordered broadcast takes the same intent you would use with a normal broadcast, the difference lies in the receiver. Lets take a look at some code.

	@Override
	protected void onResume() {
	 super.onResume();
	 IntentFilter filter = new IntentFilter(AlarmReceiver.ACTION_RECEIVE_NOTIFICATION);
	 // The default priority is 0. Positive values will be before
	 // the default, lower values will be after it.
	 filter.setPriority(1);

	 // It’s good practice to register your BroadcastReceiver’s in
	 // onResume() unless you have a good reason not to
	 registerReceiver(mForegroundReceiver, filter);
	}

When registering a receiver programmatically, we have the ability to set a priority on it. You might have seen this before, but never known why to use it. Well, now you know! Ordered broadcasts will inspect this priority before sending to the receivers. Receivers with a higher priority will catch the broadcast first, and then send itself on to lower ones (default is 0).

The beauty of using an ordered broadcast is that you can decide whether or not your want that broadcast propagated. For example, if you have two BroadcastReceivers catching the broadcast, one as a foreground receiver and one as a background receiver, you can tell the foreground receiver to abort the broadcast, so that any lower priority receivers won’t catch it.

	private BroadcastReceiver mForegroundReceiver = new BroadcastReceiver() {
	 @Override
	 public void onReceive(Context context, Intent intent) {
	 // Don’t send this intent to anyone else!
	 abortBroadcast();

	 // Let the user know we received a broadcast (if we want).
	 Toast.makeText(MainActivity.this, R.string.received_in_foreground, Toast.LENGTH_SHORT).show();
	 }
	};

##Summary 
That’s it! Using the ordered broadcast strategy, you can send the same intents for background and foreground notifications, and display them in different ways by using different priorities.

You can go even crazier with this approach by setting different priorities for different Activitys. Maybe when you’re on the main screen, you want to intercept all notifications, but on subscreens you only want to intercept notifications related to that specific screen. The possibilities are endless!
