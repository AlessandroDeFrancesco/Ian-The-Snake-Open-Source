package com.ianthesnake;

import com.google.android.gms.common.SignInButton;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.utility.CacheFont;
import com.utility.Salvataggio;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class IntroActivity extends BaseGameActivity {
	private Button collegatiDopo;
	private TextView introPerGooglePlay;
	private SignInButton login;
	private Button logout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// carico le impostazioni
		Salvataggio.caricaImpostazioni(getBaseContext());
		// se era gia connesso all'ultimo accesso allora riloggo e vado direttamente al main
		if(ContenitoreOpzioni.loggaGooglePlay == false){
			setContentView(R.layout.sezione_intro);

			inizializzaGrafica();
			inizializzaListener();
			impostaFont();

			// permette all'utente di cambiare il volume con i tasti del dispositivo
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		} else {
			vaiAlMain();
		}
	}



	private void inizializzaGrafica() {
		introPerGooglePlay = (TextView) findViewById(R.id.textViewIntroPerGooglePlay);
		collegatiDopo = (Button) findViewById(R.id.buttonCollegatiDopoIntro);

		login = (SignInButton) findViewById(R.id.sign_in_button_intro);
		logout = (Button) findViewById(R.id.sign_out_button_intro);
	}



	private void inizializzaListener() {
		collegatiDopo.setOnClickListener(dopo);

		login.setOnClickListener(loggaGooglePlay);
		logout.setOnClickListener(loggaGooglePlay);
	}

	private void impostaFont() {
		introPerGooglePlay.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		collegatiDopo.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}

	private OnClickListener loggaGooglePlay = new OnClickListener(){

		@Override
		public void onClick(View view) {
			if (view.getId() == R.id.sign_in_button_intro) {
				// start the asynchronous sign in flow
				beginUserInitiatedSignIn();
			}
			else if (view.getId() == R.id.sign_out_button_intro) {
				// sign out.
				signOut();

				// show sign-in button, hide the sign-out button
				findViewById(R.id.sign_in_button_intro).setVisibility(View.VISIBLE);
				findViewById(R.id.sign_out_button_intro).setVisibility(View.INVISIBLE);
			}
		}

	};

	private OnClickListener dopo = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			vaiAlMain();
		}
	};

	// chiamato se si preme "collegati dopo" o il "sign in"
	protected void vaiAlMain() {
		Intent intent = new Intent(this , MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	public void onSignInFailed() {
		// Sign in has failed. So show the user the sign-in button.
		findViewById(R.id.sign_in_button_intro).setVisibility(View.VISIBLE);
		findViewById(R.id.sign_out_button_intro).setVisibility(View.INVISIBLE);
	}

	@Override
	public void onSignInSucceeded() {
		// show sign-out button, hide the sign-in button
		findViewById(R.id.sign_in_button_intro).setVisibility(View.INVISIBLE);
		findViewById(R.id.sign_out_button_intro).setVisibility(View.VISIBLE);

		vaiAlMain();
	}

}
