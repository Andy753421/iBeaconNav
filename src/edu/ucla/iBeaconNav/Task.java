package edu.ucla.iBeaconNav;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.os.Messenger;
import android.preference.PreferenceManager;

public class Task extends Service implements Runnable
{
	/* Commands */
	public static final int REGISTER   = 0;
	public static final int POSITION   = 1;
	public static final int CONNECT    = 2;
	public static final int DISCONNECT = 3;
	public static final int NOTIFY     = 4;

	/* Private data */
	private Messenger  messenger;
	private Thread     thread;
	private boolean    active;

	/* Private methods */
	private void tellMain(int cmd, Object value)
	{
		try {
			android.os.Message msg = android.os.Message.obtain();
			msg.what = cmd;
			msg.obj  = value;
			this.messenger.send(msg);
		} catch (Exception e) {
			Util.debug("Task: error sending message", e);
		}
	}

	private void notify(String text, int icon)
	{
		// Notify Main
		this.tellMain(NOTIFY, text);

		// Notification bar
		//Notification  note   = new Notification(icon, null, 0);
		//Intent        intent = new Intent(this, Main.class);
		//PendingIntent pend   = PendingIntent.getActivity(this, 0, intent, 0);

		//note.setLatestEventInfo(this, "iBeaconNav!", text, pend);
		//this.startForeground(1, note);
	}

	private void handle(int cmd, Messenger mgr)
	{
		// Validate messenger
		if (cmd != REGISTER && mgr != null && mgr != this.messenger) {
			Util.debug("Task: handle - invalid messenger");
		}

		// Setup communication with Main
		if (cmd == REGISTER) {
			Util.debug("Task: handle - register");
			this.messenger = mgr;
			this.tellMain(REGISTER, this);
		}

		// Create client thread
		if (cmd == CONNECT && this.thread == null) {
			Util.debug("Task: handle - connect");
			this.thread = new Thread(this);
			this.thread.start();
		}

		// Stop client thread
		if (cmd == DISCONNECT && this.thread != null) {
			Util.debug("Task: handle - register");
			try {
				this.thread.join();
			} catch (Exception e) {
				Util.debug("Task: error stopping service", e);
			}
		}
	}

	/* Public methods */
	public boolean isRunning()
	{
		return this.thread != null;
	}

	/* Runnable methods */
	@Override
	public void run()
	{
		Util.debug("Task: thread run");

		// Run nav algorithm
		while (this.active) {
			// Read sensor data
			this.tellMain(POSITION, 0);
		}

		Util.debug("Task: thread exit");
	}

	/* Service Methods */
	@Override
	public void onCreate()
	{
		Util.debug("Task: onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		Util.debug("Task: onDestroy");
		this.handle(DISCONNECT, null);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Util.debug("Task: onStartCommand");
		int       rval = super.onStartCommand(intent, flags, startId);
		int       cmd  = intent.getExtras().getInt("Command");
		Messenger mgr  = (Messenger)intent.getExtras().get("Messenger");
		this.handle(cmd, mgr);
		return rval;
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		Util.debug("Task: onBind");
		return null;
	}
}
