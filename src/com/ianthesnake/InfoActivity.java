package com.ianthesnake;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.utility.CacheFont;

public class InfoActivity extends BaseGameActivity{
	public static final String urlAppMarket = "market://details?id=com.ianthesnake";
	
	TextView titolo_autore,autore,descrizione_rating;
	Button button_rating;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sezione_info);

		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
	}
	
	private void inizializzaGrafica() {
		titolo_autore = (TextView) findViewById(R.id.textViewTitoloAutore);
		autore = (TextView) findViewById(R.id.textViewAutore);
		descrizione_rating = (TextView) findViewById(R.id.textViewDescrizioneRating);
		button_rating = (Button) findViewById(R.id.buttonRatingGooglePlay);
	}
	
	private void inizializzaListener() {
		button_rating.setOnClickListener(vaiMarket);
	}
	
	private void impostaFont() {
		titolo_autore.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		autore.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		descrizione_rating.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		button_rating.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}
	
    @Override
    public void onBackPressed() {
    	finish();
    }
	
	private OnClickListener vaiMarket = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(urlAppMarket));
			startActivity(intent);
		}
	};

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
    
}
