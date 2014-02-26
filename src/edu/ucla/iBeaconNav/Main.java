package edu.ucla.iBeaconNav;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import android.os.Bundle;
 
import com.google.android.gms.maps.MapView;

public class Main extends Activity
{
	/* Private data */
	private Handler      handler;
	private Messenger    messenger;
	private Task         task;
	private Toast        toast;
	private boolean      running;

	/* Widgets */
	private TabHost      window;
	private TabWidget    tabs;
	private LinearLayout map;
	private LinearLayout state;
	private TextView     debug;
	private ScrollView   scroll;

	/* Private helper methods */
	private void notice(String text)
	{
		String    msg  = "*** " + text + "\n";
		Spannable span = new SpannableString(msg);
		span.setSpan(new StyleSpan(Typeface.BOLD), 0, msg.length(), 0);
		this.debug.append(span);
	}

	/* Private handler methods */
	private void onRegister(Task task)
	{
		Util.debug("Main: onRegister");
		this.task    = task;
		this.running = this.task.isRunning();
	}

	private void onPosition()
	{
		Util.debug("Main: onPosition");
	}

	private void onNotify(String text)
	{
		Util.debug("Main: onNotify - " + text);
		this.notice(text);
		this.toast.setText(text);
		this.toast.show();
	}

	/* Private service methods */
	private void register()
	{
		Util.debug("Main: register");
		startService(new Intent(this, Task.class)
				.putExtra("Command",   Task.REGISTER)
				.putExtra("Messenger", this.messenger));
	}

	private void connect()
	{
		Util.debug("Main: connect");
		startService(new Intent(this, Task.class)
				.putExtra("Command", Task.CONNECT));
		this.running = true;
	}

	private void disconnect()
	{
		Util.debug("Main: disconnect");
		startService(new Intent(this, Task.class)
				.putExtra("Command", Task.DISCONNECT));
		this.running = false;
	}

	private void quit()
	{
		this.debug.setText("");
		stopService(new Intent(this, Task.class));
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	/* Activity Methods */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try {
			super.onCreate(savedInstanceState);
			Util.debug("Main: onCreate");

			// Setup toast
			this.toast     = Toast.makeText(this, "", Toast.LENGTH_SHORT);

			// Setup communication
			this.handler   = new MainHandler();
			this.messenger = new Messenger(this.handler);

			// Setup main layout
			this.setContentView(R.layout.main);

			// Find widgets
			this.window    = (TabHost)      findViewById(android.R.id.tabhost);
			this.tabs      = (TabWidget)    findViewById(android.R.id.tabs);
			this.map       = (LinearLayout) findViewById(R.id.map);
			this.state     = (LinearLayout) findViewById(R.id.state);
			this.debug     = (TextView)     findViewById(R.id.debug);
			this.scroll    = (ScrollView)   findViewById(R.id.debug_scroll);

			// Get a handle to the Map Fragment
			//GoogleMap map = ((MapFragment)getFragmentManager()
			//	.findFragmentById(R.id.map_fragment)).getMap();

			//LatLng sydney = new LatLng(-33.867, 151.206);

			//map.setMyLocationEnabled(true);
			//map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

			//map.addMarker(new MarkerOptions()
			//	.title("Sydney")
			//	.snippet("The most populous city in Australia.")
			//	.position(sydney));

			// Add window tabs
			this.window.setup();

			this.window.addTab(this.window
					.newTabSpec("map")
					.setIndicator("Map")
					.setContent(R.id.map));
			this.window.addTab(this.window
					.newTabSpec("state")
					.setIndicator("State")
					.setContent(R.id.state));
			this.window.addTab(this.window
					.newTabSpec("debug")
					.setIndicator("Debug")
					.setContent(R.id.debug));

			// Attach to background service
			this.register();

		} catch (Exception e) {
			Util.debug("Error setting content view", e);
			return;
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		this.register();
		Util.debug("Main: onStart");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Util.debug("Main: onResume");
	}

	@Override
	public void onPause()
	{
		super.onPause();
		Util.debug("Main: onPause");
	}

	@Override
	public void onStop()
	{
		super.onStop();
		Util.debug("Main: onStop");
	}

	@Override
	public void onRestart()
	{
		super.onRestart();
		Util.debug("Main: onRestart");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Util.debug("Main: onDestroy");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.connect).setVisible(!this.running);
		menu.findItem(R.id.disconnect).setVisible(this.running);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.connect:
				this.connect();
				return true;
			case R.id.disconnect:
				this.disconnect();
				return true;
			case R.id.quit:
				this.quit();
				return true;
			default:
				return false;
		}
	}

	/* Handler class */
	class MainHandler extends Handler
	{
		public void handleMessage(android.os.Message msg)
		{
			switch (msg.what) {
				case Task.REGISTER:
					Main.this.onRegister((Task)msg.obj);
					break;
				case Task.POSITION:
					Main.this.onPosition();
					break;
				case Task.CONNECT:
					Main.this.running = true;
					break;
				case Task.DISCONNECT:
					Main.this.running = false;
					break;
				case Task.NOTIFY:
					Main.this.onNotify((String)msg.obj);
					break;
				default:
					Util.debug("Main: unknown message - " + msg.what);
					break;
			}
		}
	}
}
