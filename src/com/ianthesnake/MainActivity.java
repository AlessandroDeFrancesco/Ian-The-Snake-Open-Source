package com.ianthesnake;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.utility.CacheFont;
import com.utility.ImagesManager;
import com.utility.PopupManager;
import com.utility.Salvataggio;
import com.utility.SoundManager;

public class MainActivity extends BaseGameActivity{

	private Button buttGioca, buttOpzioni, buttLabirinti, buttMultiplayer, buttInfo, buttClassifica;
	private SignInButton login;
	private Button logout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sezione_menu);

		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
		
		Salvataggio.caricaImpostazioni(getBaseContext()); // carico le impostazioni ed altro
		ImagesManager.inizializzaCacheDrawable(this); // carico le immagini necessarie per il gioco in cache
		// controllo se devo connettermi al google play
		if(ContenitoreOpzioni.loggaGooglePlay == true){
			beginUserInitiatedSignIn();
		}
		
		// permette all'utente di cambiare il volume con i tasti del dispositivo
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		// carico i suoni
		SoundManager.getInstance(getApplicationContext());
	}

	private void inizializzaGrafica() {
		buttGioca = (Button) findViewById(R.id.buttonGioca);
		buttOpzioni = (Button) findViewById(R.id.buttonOpzioni);
		buttLabirinti = (Button) findViewById(R.id.buttonLabirinti);
		buttMultiplayer = (Button) findViewById(R.id.buttonMultiplayer);
		buttInfo = (Button) findViewById(R.id.buttonInfo);
		buttClassifica = (Button) findViewById(R.id.buttonClassifica);
		
		login = (SignInButton) findViewById(R.id.sign_in_button);
		logout = (Button) findViewById(R.id.sign_out_button);
	}

	private void inizializzaListener() {
		buttGioca.setOnClickListener(iniziaGioco);
		buttOpzioni.setOnClickListener(vaiOpzioni);
		buttLabirinti.setOnClickListener(sceltaLabirinto);
		buttMultiplayer.setOnClickListener(iniziaMultiplayer);
		buttInfo.setOnClickListener(vaiInfo);
		buttClassifica.setOnClickListener(mostraPopupClassifica);
		
		login.setOnClickListener(loggaGooglePlay);
		logout.setOnClickListener(loggaGooglePlay);
	}
	
	private void impostaFont() {
		buttGioca.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		buttOpzioni.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		buttLabirinti.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		buttMultiplayer.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		buttClassifica.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}

	private OnClickListener iniziaGioco = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupManager.getInstance().creaPopup(PopupManager.POPUP_SCELTA_GIOCO_SINGOLO, MainActivity.this, getApiClient());
		}
	};

	private OnClickListener vaiOpzioni = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent( v.getContext(), OptionActivity.class);
			startActivity(intent);
		}
	};
	
	private OnClickListener sceltaLabirinto = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent( v.getContext(), MazeActivity.class);
			startActivity(intent);
		}
	};
	
	private OnClickListener iniziaMultiplayer = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent( v.getContext(), MultiplayerActivity.class);
			startActivity(intent);
		}
	};
	
	private OnClickListener vaiInfo = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent( v.getContext(), InfoActivity.class);
			startActivity(intent);
		}
	};
	
	private OnClickListener mostraPopupClassifica = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			PopupManager.getInstance().creaPopup(PopupManager.POPUP_CLASSIFICA, MainActivity.this, getApiClient());
		}
	};
	
	private OnClickListener loggaGooglePlay = new OnClickListener(){

		@Override
		public void onClick(View view) {
			 if (view.getId() == R.id.sign_in_button) {
			        // start the asynchronous sign in flow
			        beginUserInitiatedSignIn();
			    }
			    else if (view.getId() == R.id.sign_out_button) {
			        // sign out.
			        signOut();

			        // show sign-in button, hide the sign-out button
			        findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
			        findViewById(R.id.sign_out_button).setVisibility(View.GONE);
			        
			        // salvo che si è disconnesso
			        ContenitoreOpzioni.loggaGooglePlay = false;
			    }
		}
		
	};
	
	/**
	 *  Esco dal gioco
	 */
    @Override
    public void onBackPressed() {
    	finish();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // salva
        Salvataggio.salvaImpostazioni(getBaseContext());
    }

	@Override
	public void onSignInFailed() {
		// Sign in has failed. So show the user the sign-in button.
	    findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
	    findViewById(R.id.sign_out_button).setVisibility(View.GONE);
	}

	@Override
	public void onSignInSucceeded() {
		// show sign-out button, hide the sign-in button
	    findViewById(R.id.sign_in_button).setVisibility(View.GONE);
	    findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
	    
	    // salvo che si è connesso
        ContenitoreOpzioni.loggaGooglePlay = true;
	}
	
}
