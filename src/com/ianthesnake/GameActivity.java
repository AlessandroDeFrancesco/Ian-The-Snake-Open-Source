package com.ianthesnake;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.utility.CacheFont;
import com.utility.JoystickManager;
import com.utility.Salvataggio;

public class GameActivity extends BaseGameActivity {

	private GameView mSnakeView;
	private Button buttonGiu;
	private Button buttonSu;
	private Button buttonDestra;
	private Button buttonSinistra;
	private Button buttonOrario, buttonAntiorario;
	private TextView punteggio, tempoGioco, display;

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sezione_gioco);
		
		Salvataggio.caricaImpostazioni(getApplicationContext());
		recuperaGrafica();
		inizializzaListener();
		impostaFont();
		
		mSnakeView.setStatoGioco(GameView.PRONTO);
		// permette all'utente di cambiare il volume con i tasti del dispositivo
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	private void recuperaGrafica() {
		mSnakeView = (GameView) findViewById(R.id.snake);
		mSnakeView.setDisplay((TextView) findViewById(R.id.Display), (TextView) findViewById(R.id.testoPunteggio), (TextView) findViewById(R.id.testoTempoGioco), getApiClient());
		
		display = (TextView) findViewById(R.id.Display);
		
		buttonGiu = (Button) findViewById(R.id.buttonGiu);
		buttonSu = (Button) findViewById(R.id.buttonSu);
		buttonDestra = (Button) findViewById(R.id.buttonDestra);
		buttonSinistra = (Button) findViewById(R.id.buttonSinistra);
		
		buttonOrario = (Button) findViewById(R.id.buttonOrario);
		buttonAntiorario = (Button) findViewById(R.id.buttonAntiorario);
		
		// controllo quale joystick impostare
		if(ContenitoreOpzioni.sceltaComandi == JoystickManager.QUATTRO_FRECCE){
			buttonGiu.setVisibility(View.VISIBLE);
			buttonSu.setVisibility(View.VISIBLE);
			buttonDestra.setVisibility(View.VISIBLE);
			buttonSinistra.setVisibility(View.VISIBLE);
			buttonOrario.setVisibility(View.INVISIBLE);
			buttonAntiorario.setVisibility(View.INVISIBLE);
		} else if(ContenitoreOpzioni.sceltaComandi == JoystickManager.ORARIO_ANTIORARIO){
			buttonGiu.setVisibility(View.INVISIBLE);
			buttonSu.setVisibility(View.INVISIBLE);
			buttonDestra.setVisibility(View.INVISIBLE);
			buttonSinistra.setVisibility(View.INVISIBLE);
			buttonOrario.setVisibility(View.VISIBLE);
			buttonAntiorario.setVisibility(View.VISIBLE);
		}
		
		punteggio = (TextView) findViewById(R.id.testoPunteggio);
		tempoGioco = (TextView) findViewById(R.id.testoTempoGioco);
	}
	
	private void inizializzaListener(){
		buttonGiu.setOnClickListener(inviaMovimento);
		buttonSu.setOnClickListener(inviaMovimento);
		buttonDestra.setOnClickListener(inviaMovimento);
		buttonSinistra.setOnClickListener(inviaMovimento);
		buttonOrario.setOnClickListener(inviaMovimento);
		buttonAntiorario.setOnClickListener(inviaMovimento);
	}
	
	private void impostaFont() {
		punteggio.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		display.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		tempoGioco.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
	}
	
	OnClickListener inviaMovimento = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String mov = "";
			switch (v.getId())
			{
			case R.id.buttonGiu:
				mov = "giu";
				break;
			case R.id.buttonSu:
				mov = "su";
				break;
			case R.id.buttonDestra:
				mov = "destra";
				break;
			case R.id.buttonSinistra:
				mov = "sinistra";
				break;
			case R.id.buttonOrario:
				mov = "orario";
				break;
			case R.id.buttonAntiorario:
				mov = "antiorario";
				break;
			}
			
			// invio il tocco del pulsante alla view dello snake per il movimento
			mSnakeView.setProssimoMovimento(mov);
			// vibrazione
			 Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			 // Vibra per 50 millisecondi
			 vib.vibrate(25);
		}
	};
	
	
	/**
	 * quando si preme indietro setta lo stato del gioco su pausa o si torna al menu principale e si chiude l'activity
	 */
    @Override
    public void onBackPressed() {
    	if(mSnakeView.getStatoGioco() != GameView.VAI){
    		finish();
    	} else {
    		mSnakeView.setStatoGioco(GameView.PAUSA);
    	}
    }

    /**
     * quando l'applicazione va in pausa, setta lo stato del gioco su pausa
     */
    @Override
    protected void onPause() {
        super.onPause();
        // Mette in pausa il gioco se il gioco è in corso, altrimenti lascia cosi com'è lo stato del gioco
        if(mSnakeView.getStatoGioco() == GameView.VAI){
        	mSnakeView.setStatoGioco(GameView.PAUSA);
        }
        // salva
        if(isSignedIn()){
        	Salvataggio.salvaImpostazioni(getBaseContext(), getApiClient());
        } else {
        	Salvataggio.salvaImpostazioni(getBaseContext());
        }
    }
    
    /**
     * quando l'applicazione viene riesumata se nella classe ContenitoreOpzioni non sono inizializzate le variabili allora chiudo l'activity(non sono inizializzate solo se il GC di android ha tolto risorse all'applicazione)
     * TODO fare in modo che quando vengono tolte risorse all'applicazione quando è in stop questa salva lo stato della partita
     * e possa riprenderle successivamente
     */
    @Override
    protected void onRestart(){
    	super.onRestart();
    	if(ContenitoreOpzioni.velocita == 0){
    		finish();
    	}
    }

	@Override
	public void onSignInFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSignInSucceeded() {
		// TODO Auto-generated method stub
		
	}
}
