package com.utility;

import android.content.Context;
import android.widget.Toast;

public class Trace {
	
	/**
	 * crea un toast del messaggio
	 * @param context
	 * @param stringa
	 */
	public static void trace(Context context, String stringa){
		Toast.makeText(context, stringa, Toast.LENGTH_SHORT).show();
	}
	
}
