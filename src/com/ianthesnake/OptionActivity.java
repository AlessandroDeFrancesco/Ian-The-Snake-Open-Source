package com.ianthesnake;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.utility.CacheFont;
import com.utility.ImagesManager;
import com.utility.JoystickManager;
import com.utility.Salvataggio;

public class OptionActivity extends Activity {
	private TextView textViewTitoloVelocita, textViewTitoloColore, textViewTitoloSuono , textViewVelocita, textViewTitoloJoystick, textViewSceltaJoystick;
	private SeekBar seekBarVelocita;
	private Button buttonPariSinistra, buttonPariDestra, buttonDispariSinistra, buttonDispariDestra,buttonSinistraJoystick, buttonDestraJoystick;
	private Button ok;
	private ImageView imageViewCorpoPari, imageViewCorpoDispari;
	private ToggleButton toggleButtonSuono;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sezione_opzioni);
		
		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
		caricaImpostazioniPrecedenti();		
	}

	private void caricaImpostazioniPrecedenti() {
		Salvataggio.caricaImpostazioni(getApplicationContext());
		
		// controllo la velocita precedentemente impostata (tolgo 2 perchè onProgressChanged aggiungerà 2)
		seekBarVelocita.setProgress(ContenitoreOpzioni.velocita - 2);
		
		// controllo il colore dei pezzi del corpo precedentemente impostati
		imageViewCorpoPari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoPari ));
		imageViewCorpoDispari.setImageDrawable(ImagesManager.getDrawable( ContenitoreOpzioni.immCorpoDispari ));
		
		// controllo se il suono era impostato su on o off
		toggleButtonSuono.setChecked(ContenitoreOpzioni.suonoOn);
		
		// controllo che joystick stava impostato
		if(ContenitoreOpzioni.sceltaComandi == JoystickManager.ORARIO_ANTIORARIO){
			textViewSceltaJoystick.setText(getString(R.string.joystick_orario_antiorario));
		} else {
			textViewSceltaJoystick.setText(getString(R.string.joystick_quattro_frecce));
		}
	}

	private void inizializzaGrafica() {
		textViewTitoloVelocita = (TextView) findViewById(R.id.textViewTitoloVelocita);		
		seekBarVelocita = (SeekBar) findViewById(R.id.seekBarVelocita);
		textViewVelocita = (TextView) findViewById(R.id.textViewVelocita);
		
		textViewTitoloColore = (TextView) findViewById(R.id.textViewTitoloColore);
		buttonPariSinistra = (Button) findViewById(R.id.buttonSinistraPari);
		buttonPariDestra = (Button) findViewById(R.id.buttonDestraPari);
		buttonDispariSinistra = (Button) findViewById(R.id.buttonSinistraDispari);
		buttonDispariDestra = (Button) findViewById(R.id.buttonDestraDispari);
		
		imageViewCorpoPari = (ImageView) findViewById(R.id.imageViewCorpoPari);
		imageViewCorpoDispari = (ImageView) findViewById(R.id.imageViewCorpoDispari);
		
		textViewTitoloSuono = (TextView) findViewById(R.id.textViewTitoloSuono);
		toggleButtonSuono = (ToggleButton) findViewById(R.id.toggleButtonSuono);
		
		textViewTitoloJoystick = (TextView) findViewById(R.id.textViewTitoloJoystick);
		textViewSceltaJoystick = (TextView) findViewById(R.id.textViewSceltaJoystick);
		buttonSinistraJoystick = (Button) findViewById(R.id.buttonSinistraJoystick);
		buttonDestraJoystick = (Button) findViewById(R.id.buttonDestraJoystick);
		
		ok = (Button) findViewById(R.id.buttonOpzioniOk);
	}
	
	private void inizializzaListener() {
		buttonPariSinistra.setOnClickListener(frecceColoreCorpoClicked);
		buttonPariDestra.setOnClickListener(frecceColoreCorpoClicked);
		buttonDispariSinistra.setOnClickListener(frecceColoreCorpoClicked);
		buttonDispariDestra.setOnClickListener(frecceColoreCorpoClicked);
		
		seekBarVelocita.setOnSeekBarChangeListener(sceltaVelocita);
		
		toggleButtonSuono.setOnCheckedChangeListener(sceltaSuono);
		
		buttonSinistraJoystick.setOnClickListener(frecceJoystickClicked);
		buttonDestraJoystick.setOnClickListener(frecceJoystickClicked);
		
		ok.setOnClickListener(okClicked);
	}
	
	private void impostaFont() {
		textViewTitoloVelocita.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		textViewVelocita.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		
		textViewTitoloColore.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		
		textViewTitoloSuono.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		toggleButtonSuono.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		
		textViewTitoloJoystick.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		textViewSceltaJoystick.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		
		ok.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}

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
	
	private OnClickListener frecceColoreCorpoClicked = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.buttonDestraDispari){
				cambiaColoreCorpo(1,1);
			} else if(v.getId() == R.id.buttonDestraPari){
				cambiaColoreCorpo(1,0);
			} else if(v.getId() == R.id.buttonSinistraDispari){
				cambiaColoreCorpo(0,1);
			} else if(v.getId() == R.id.buttonSinistraPari){
				cambiaColoreCorpo(0,0);
			}
		}
		
	};
	
	private OnClickListener frecceJoystickClicked = new OnClickListener(){

		@Override
		public void onClick(View v) {
			if(ContenitoreOpzioni.sceltaComandi == JoystickManager.ORARIO_ANTIORARIO){
				textViewSceltaJoystick.setText(getString(R.string.joystick_quattro_frecce));
				ContenitoreOpzioni.sceltaComandi = JoystickManager.QUATTRO_FRECCE;
			} else {
				textViewSceltaJoystick.setText(getString(R.string.joystick_orario_antiorario));
				ContenitoreOpzioni.sceltaComandi = JoystickManager.ORARIO_ANTIORARIO;
			}
		}
		
	};
	
	private OnClickListener okClicked = new OnClickListener(){

		@Override
		public void onClick(View v) {
			finish();
	        // salva
	        Salvataggio.salvaImpostazioni(getBaseContext());
		}
		
	};
	
	
	private OnCheckedChangeListener sceltaSuono = new CompoundButton.OnCheckedChangeListener() {
	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
	        if (isChecked) {
	        	ContenitoreOpzioni.suonoOn = true;
	        } else {
	        	ContenitoreOpzioni.suonoOn = false;
	        }
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
        // salva
        Salvataggio.salvaImpostazioni(getBaseContext());
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // salva
        Salvataggio.salvaImpostazioni(getBaseContext());
    }
}
