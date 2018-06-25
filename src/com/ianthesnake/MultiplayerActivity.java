package com.ianthesnake;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.utility.CacheFont;

public class MultiplayerActivity extends Activity{
	
	private Button buttonClient, buttonServer;
	private TextView textViewInfo;
	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.sezione_gioco_multiplayer);
		
		inizializzaGrafica();
		impostaFont();
		
		checkBluetoothPermission();
	}
	
	private void checkBluetoothPermission() {
		if (android.os.Build.VERSION.SDK_INT >= 23)  
		{  
			// ANDROID 6.0 AND UP!  
			boolean accessCoarseLocationAllowed = false;  
			try {  
				// Invoke checkSelfPermission method from Android 6 (API 23 and UP)  
				java.lang.reflect.Method methodCheckPermission = Activity.class.getMethod("checkSelfPermission", java.lang.String.class);  
				Object resultObj = methodCheckPermission.invoke(this, Manifest.permission.ACCESS_COARSE_LOCATION);  
				int result = Integer.parseInt(resultObj.toString());  
				if (result == PackageManager.PERMISSION_GRANTED)  
				{  
					accessCoarseLocationAllowed = true;  
				}  
			} catch (Exception ex) { }  
			if (accessCoarseLocationAllowed)  
			{
				return;  
			} try {  
				// We have to invoke the method "void requestPermissions (Activity activity, String[] permissions, int requestCode) "  
				// from android 6  
				java.lang.reflect.Method methodRequestPermission = Activity.class.getMethod("requestPermissions", java.lang.String[].class, int.class);  
				methodRequestPermission.invoke(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0x12345);  
			} catch (Exception ex) { }  
		}
	}

	private void inizializzaGrafica() {
		buttonClient = (Button) findViewById(R.id.buttonClient);
		buttonServer = (Button) findViewById(R.id.buttonServer);
		
		textViewInfo = (TextView) findViewById(R.id.textViewMultiplayerInfo);
		textViewInfo.setMovementMethod(new ScrollingMovementMethod());
	}
	
	private void impostaFont() {
		buttonClient.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		buttonServer.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		
		textViewInfo.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}
	
	public void clientClicked(View v){
		Intent intent = null;
		
		if(ContenitoreOpzioni.wifi){
			//intent = new Intent( v.getContext(), MultiplayerWiFiClientActivity.class);	
		}else {
			intent = new Intent( v.getContext(), MultiplayerClientImpostazioniActivity.class);	
		}
		startActivity(intent);
	}
	
	public void serverClicked(View v){
Intent intent = null;
		
		if(ContenitoreOpzioni.wifi){
			//intent = new Intent( v.getContext(), MultiplayerWiFiServerActivity.class);	
		}else {
			intent = new Intent( v.getContext(), MultiplayerServerImpostazioniActivity.class);	
		}
		startActivity(intent);
	}
}
