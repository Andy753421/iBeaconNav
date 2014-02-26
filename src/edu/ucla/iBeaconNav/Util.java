package edu.ucla.iBeaconNav;

import android.util.Log;

public class Util
{
	/* Debugging */
	public static void debug(String txt, Exception e)
	{
		Log.d("iBeaconNav", txt, e);
	}
	public static void debug(String txt)
	{
		Log.d("iBeaconNav", txt);
	}
}
