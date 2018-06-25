package com.ianthesnake;

import com.utility.CacheFont;
import com.utility.Salvataggio;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MazeActivity extends Activity  {
	private MazeView mMazeView;
	private Button buttonDestra, buttonSinistra;
	private Button ok;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sezione_labirinti);

		recuperaGrafica();
		inizializzaListener();
		impostaFont();
		visibilitaPulsanti();
	}

	private void recuperaGrafica() {
		mMazeView = (MazeView) findViewById(R.id.maze);
		
		buttonDestra = (Button) findViewById(R.id.buttonDestraLab);
		buttonSinistra = (Button) findViewById(R.id.buttonSinistraLab);
		
		ok = (Button) findViewById(R.id.buttonLabirintiOk);
	}
	
	private void inizializzaListener() {
		buttonDestra.setOnClickListener(cambiaLabirinto);
		buttonSinistra.setOnClickListener(cambiaLabirinto);
		
		ok.setOnClickListener(okClicked);
	}

	private void impostaFont() {
		ok.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}
	
	private OnClickListener cambiaLabirinto = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(v == buttonDestra){
				mMazeView.aumentaLabirinto();				
			} else if(v == buttonSinistra){
				mMazeView.diminuisciLabirinto();
			}
			mMazeView.creaLabirinto();
			impostaLabirinto();
			visibilitaPulsanti();
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
	
	
	/**
	 * imposta il labirinto nelle opzioni generali dell'applicazione
	 */	
	private void impostaLabirinto() {
		ContenitoreOpzioni.labirinto = mMazeView.getLabirinto();		
	}
	
	/**
	 * disattiva o riattiva i pulsanti a seconda se sia visualizzato il primo o l'ultimo labirinto disponibile
	 */
	public void visibilitaPulsanti(){
		//abilito disabilito i pulsanti
		buttonDestra.setEnabled(true);
		buttonSinistra.setEnabled(true);
		if(mMazeView.getLabirinto() <= 1){
			buttonSinistra.setEnabled(false);
		}
		if(mMazeView.getLabirinto() >= MazeView.NUMERO_LABIRINTI){
			buttonDestra.setEnabled(false);
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
