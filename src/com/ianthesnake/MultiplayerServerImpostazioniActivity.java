package com.ianthesnake;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.utility.CacheFont;
import com.utility.ImagesManager;
import com.utility.Salvataggio;

public class MultiplayerServerImpostazioniActivity extends Activity {
	private TextView textViewTitoloVelocita, textViewTitoloColore, textViewVelocita, textViewInfo;
	private SeekBar seekBarVelocita;
	private Button buttonPariSinistra, buttonPariDestra, buttonDispariSinistra, buttonDispariDestra, buttonOK;
	private ImageView imageViewCorpoPari, imageViewCorpoDispari;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiplayer_impostazioni);
		
		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
		caricaImpostazioniPrecedenti();		
	}

	private void caricaImpostazioniPrecedenti() {
		// controllo la velocita precedentemente impostata (tolgo 2 perchè onProgressChanged aggiungerà 2)
		seekBarVelocita.setProgress(ContenitoreOpzioni.velocita - 2);
		
		// controllo il colore dei pezzi del corpo precedentemente impostati
		imageViewCorpoPari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoPari ));
		imageViewCorpoDispari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoDispari ));
	}

	private void inizializzaGrafica() {
		textViewTitoloVelocita = (TextView) findViewById(R.id.textViewTitoloMultiplayerImpostazioniVelocita);		
		seekBarVelocita = (SeekBar) findViewById(R.id.seekBarMultiplayerImpostazioniVelocita);
		textViewVelocita = (TextView) findViewById(R.id.textViewMultiplayerImpostazioniVelocita);
		
		textViewTitoloColore = (TextView) findViewById(R.id.textViewTitoloMultiplayerImpostazioniColore);
		buttonPariSinistra = (Button) findViewById(R.id.buttonMultiplayerImpostazioniSinistraPari);
		buttonPariDestra = (Button) findViewById(R.id.buttonMultiplayerImpostazioniDestraPari);
		buttonDispariSinistra = (Button) findViewById(R.id.buttonMultiplayerImpostazioniSinistraDispari);
		buttonDispariDestra = (Button) findViewById(R.id.buttonMultiplayerImpostazioniDestraDispari);
		
		imageViewCorpoPari = (ImageView) findViewById(R.id.imageViewMultiplayerImpostazioniCorpoPari);
		imageViewCorpoDispari = (ImageView) findViewById(R.id.imageViewMultiplayerImpostazioniCorpoDispari);
		
		textViewInfo = (TextView) findViewById(R.id.textViewMultiplayerImpostazioniInfo);
		textViewInfo.setText(getString(R.string.multiplayer_server_impostazioni_info));
		
		buttonOK = (Button) findViewById(R.id.buttonMultiplayerImpostazioniOK);
	}
	
	private void inizializzaListener() {
		buttonPariSinistra.setOnClickListener(frecceColoreCorpoClicked);
		buttonPariDestra.setOnClickListener(frecceColoreCorpoClicked);
		buttonDispariSinistra.setOnClickListener(frecceColoreCorpoClicked);
		buttonDispariDestra.setOnClickListener(frecceColoreCorpoClicked);
		
		seekBarVelocita.setOnSeekBarChangeListener(sceltaVelocita);
		
		buttonOK.setOnClickListener(OkClicked);
	}
	
	private void impostaFont() {
		textViewTitoloVelocita.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		textViewVelocita.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		
		textViewTitoloColore.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		textViewInfo.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		buttonOK.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}


	
	private OnClickListener frecceColoreCorpoClicked = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.buttonMultiplayerImpostazioniDestraDispari){
				cambiaColoreCorpo(1,1);
			} else if(v.getId() == R.id.buttonMultiplayerImpostazioniDestraPari){
				cambiaColoreCorpo(1,0);
			} else if(v.getId() == R.id.buttonMultiplayerImpostazioniSinistraDispari){
				cambiaColoreCorpo(0,1);
			} else if(v.getId() == R.id.buttonMultiplayerImpostazioniSinistraPari){
				cambiaColoreCorpo(0,0);
			}
		}
		
	};
	
	private OnSeekBarChangeListener sceltaVelocita = new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			int vel = progress + 2; // aggiungo 2 perchè non posso impostare il minimo e quindi evito lo zero
			textViewVelocita.setText("" + vel);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int vel = seekBar.getProgress() + 2; // aggiungo 2 perchè non posso impostare il minimo e quindi evito lo zero
			textViewVelocita.setText("" + vel);
			ContenitoreOpzioni.velocita = vel;
		}
		
	};
	
	private OnClickListener OkClicked = new OnClickListener(){

		@Override
		public void onClick(View v) {
	        // salva
	        Salvataggio.salvaImpostazioni(getBaseContext());
			
			Intent intent = new Intent( v.getContext(), MultiplayerBluetoothServerActivity.class);
			startActivity(intent);
		}
		
	};
	
	/**
	 * cambia il colore dell'image view a seconda della modalita e del corpo
	 * @param mod ( 0 = indietro, >1 = avanti )
	 * @param corpo ( 0 = pari, >1 = dispari )
	 */
	private void cambiaColoreCorpo(int mod, int corpo){
		if(mod == 0){
			// imm precedente
			if(corpo == 0){
				ContenitoreOpzioni.immCorpoPari = ImagesManager.getImmagineCorpoPrec(ContenitoreOpzioni.immCorpoPari);
				imageViewCorpoPari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoPari ));
			} else{
				ContenitoreOpzioni.immCorpoDispari = ImagesManager.getImmagineCorpoPrec(ContenitoreOpzioni.immCorpoDispari);
				imageViewCorpoDispari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoDispari ));
			}
		} else {
			// imm successiva
			if(corpo == 0){
				ContenitoreOpzioni.immCorpoPari = ImagesManager.getImmagineCorpoSucc(ContenitoreOpzioni.immCorpoPari);
				imageViewCorpoPari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoPari ));
			} else{
				ContenitoreOpzioni.immCorpoDispari = ImagesManager.getImmagineCorpoSucc(ContenitoreOpzioni.immCorpoDispari);
				imageViewCorpoDispari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoDispari ));
			}
		}
	}
	
	/**
	 * quando si preme indietro si torna al menu principale e si chiude l'activity
	 */
    @Override
    public void onBackPressed() {
    	finish();
    }
}
