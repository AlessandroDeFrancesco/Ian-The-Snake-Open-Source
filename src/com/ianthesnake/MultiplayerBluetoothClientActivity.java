package com.ianthesnake;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.utility.CacheFont;
import com.utility.JoystickManager;
import com.utility.OggettoInviabile;
import com.utility.Salvataggio;
import com.utility.SoundManager;
import com.utility.Trace;
import com.utility.bluetooth.AbstractBluetoothClientServer;
import com.utility.bluetooth.AbstractMultiplayerBluetoothClientActivity;

public class MultiplayerBluetoothClientActivity extends AbstractMultiplayerBluetoothClientActivity {
	
	private static final int CERCA_BLUETOOTH = 1;
	private static final int GIOCO = 2;
	private int mode = CERCA_BLUETOOTH;
	
	private MultiplayerClientGameView mSnakeView;
	private Button buttonGiu;
	private Button buttonSu;
	private Button buttonDestra;
	private Button buttonSinistra;
	private Button buttonOrario, buttonAntiorario;
	private TextView punteggio, tempoGioco, display;
	
	private Boolean achievementMultiplayerSbloccato = false;
	private long ultimoInvio; // variabile per rispondere ogni secondo al server

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiplayer_client_activity);

		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
		// permette all'utente di cambiare il volume con i tasti del dispositivo
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		inizializzaBluetooth();
	}
	
	private void inizializzaGrafica() {
		if(mode == CERCA_BLUETOOTH){
			
			viewDispositiviBluetooth = (ListView) findViewById(R.id.listViewDispositiviBluetooth);
			mArrayAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.text_view_per_array_adapter);
			viewDispositiviBluetooth.setAdapter(mArrayAdapter);
			
			textAttesa = (TextView) findViewById(R.id.textViewClientAttesa);
			progressBar = (ProgressBar) findViewById(R.id.progressBarClient);
			buttonRicerca = (Button) findViewById(R.id.buttonClientRicerca);
			
			progressBar.setVisibility(View.INVISIBLE);
			buttonRicerca.setVisibility(View.INVISIBLE);
		} else if(mode == GIOCO){
			
			mSnakeView = (MultiplayerClientGameView) findViewById(R.id.multiplayerGameView);
			mSnakeView.setDisplay((TextView) findViewById(R.id.DisplayMulti), (TextView) findViewById(R.id.testoPunteggioMulti), (TextView) findViewById(R.id.testoTempoGiocoMulti), getApiClient());
			
			display = (TextView) findViewById(R.id.DisplayMulti);
			
			buttonGiu = (Button) findViewById(R.id.buttonGiuMulti);
			buttonSu = (Button) findViewById(R.id.buttonSuMulti);
			buttonDestra = (Button) findViewById(R.id.buttonDestraMulti);
			buttonSinistra = (Button) findViewById(R.id.buttonSinistraMulti);
			
			buttonOrario = (Button) findViewById(R.id.buttonOrarioMulti);
			buttonAntiorario = (Button) findViewById(R.id.buttonAntiorarioMulti);
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
			
			punteggio = (TextView) findViewById(R.id.testoPunteggioMulti);
			tempoGioco = (TextView) findViewById(R.id.testoTempoGiocoMulti);
		}
	}

	private void inizializzaListener() {
		if(mode == CERCA_BLUETOOTH){
			viewDispositiviBluetooth.setOnItemClickListener(dispositivoScelto);
		} else if(mode == GIOCO){
			buttonGiu.setOnClickListener(inviaMovimento);
			buttonSu.setOnClickListener(inviaMovimento);
			buttonDestra.setOnClickListener(inviaMovimento);
			buttonSinistra.setOnClickListener(inviaMovimento);
			buttonOrario.setOnClickListener(inviaMovimento);
			buttonAntiorario.setOnClickListener(inviaMovimento);
		}
	}

	private void impostaFont() {
		if(mode == CERCA_BLUETOOTH){
			//TODO viewDispositiviBluetooth.setTypeface(FontCache.getFontPrincipale(getBaseContext()));
			textAttesa.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
			buttonRicerca.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		} else if(mode == GIOCO){
			punteggio.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
			display.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
			tempoGioco.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		}
	}
	
	/**
	 * cambio il layout quando si deve iniziare il gioco
	 */
	private void preparaGioco() {
		mode = GIOCO;
		setContentView(R.layout.sezione_gioco_multiplayer_client);
		// invio il colore dello snake
		inviaInformazioniClient();
		
		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
	}
	
	/**
	 * invia al server le informazioni riguardanti lo snake di questo client( per adesso solo il colore )
	 */
	private void inviaInformazioniClient() {
		int[] colori = new int[3];
		colori[0] = ContenitoreOpzioni.immTesta;
		colori[1] = ContenitoreOpzioni.immCorpoPari;
		colori[2] = ContenitoreOpzioni.immCorpoDispari;
		
		// creo l'arrayList di OggettoInviabile da inviare
		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
		// invio al server che sono pronto
		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.COLORI_SNAKE_INT_ARRAY, colori));
		manageConnection.sendObjects(oggetti);
	}

	/**
	 * metodo che richiama eseguiComando() a seconda che sia una stringa o un array a due dimensioni
	 */
	@Override
	protected void messaggioRicevuto(int tipo, Object oggettoRicevuto, int ID) {
		switch(tipo){
			case STRINGA:
				String stringa = (String) oggettoRicevuto;
				eseguiComando(stringa);
				break;
			case GRIGLIA_STAGE_BYTE_ARRAY:
				byte[][] array = (byte[][]) oggettoRicevuto;
				eseguiComando(array);
				break;
			case PUNTEGGIO_INT:
				int punteggio = (Integer) oggettoRicevuto;
				eseguiComando(punteggio, PUNTEGGIO_INT);
				break;
			case RIPRODUCI_SUONO:
				int suono = (Integer) oggettoRicevuto;
				eseguiComando(suono, RIPRODUCI_SUONO);
				break;
			default:
				break;
		}
	}

	/**
	 * ho ricevuto una stringa, le uniche stringhe possibili sono gli stati del gioco e quella di chiudere l'activity
	 * @param stringa
	 */
	private void eseguiComando(String stringa) {
		if(stringa.equals(PRONTO)){	// se il server mi invia pronto rispondo anche io con pronto per inziare il gioco
			if(mode != GIOCO){
				// creo l'arrayList di OggettoInviabile da inviare
	    		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
	    		// invio al server che sono pronto
	    		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, PRONTO));
	    		manageConnection.sendObjects(oggetti);

				preparaGioco(); // preparo il gioco e aggiorno la view quando ricevo dal server la griglia dello stage
			}
			mSnakeView.setStatoGioco(MultiplayerClientGameView.PRONTO);
		} else if(stringa.equals(FINE)){ // fine partita
			mSnakeView.setStatoGioco(GameView.PERSO);
		} else if(stringa.equals(PAUSA)){ // fine partita
			mSnakeView.setStatoGioco(GameView.PAUSA);
		}else if(stringa.equals(VAI)){ // reinizia partita
			mSnakeView.setStatoGioco(GameView.VAI);
		} else if(stringa.equals(CHIUDI_GIOCO)){ // vuol dire che il server si  disconnesso o ha chiuso l'activity
			Trace.trace(getBaseContext(), getString(R.string.multiplayer_client_server_disconnesso));
			chiudiActivity();
		}
	}

	/**
	 * ho ricevuto la griglia dello stage, posso disegnarla e comunicare al server che è stata ricevuta
	 */
	private void eseguiComando(byte[][] array) {
		// aggiorno la view
		mSnakeView.update(array);
		// sblocco achi
		if(!achievementMultiplayerSbloccato){
			Salvataggio.sbloccaAchievements(R.string.id_achievement_multiplayer, this, getApiClient());
		}
		
		// comunico al server di aver ricevuto la griglia solo se è passato almeno 800 millisec dall'ultimo messaggio inviato (per non sovraccaricare il server)
		long adesso = new Date().getTime();
		if(adesso - ultimoInvio > 800){
			ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
			oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, ARRIVATO));
			manageConnection.sendObjects(oggetti);
			
			ultimoInvio = adesso;
		}
	}	
	
	/**
	 * se è un intero può essere o il punteggio o un suono da riprodurre
	 */
	private void eseguiComando(int x, int tipo) {
		if(tipo == PUNTEGGIO_INT){
			mSnakeView.punteggio.setValore(x);
		} else if( tipo == RIPRODUCI_SUONO) {
			if(ContenitoreOpzioni.suonoOn)
				SoundManager.getInstance(getApplicationContext()).playSound(x);
		}
	}	
	
	OnClickListener inviaMovimento = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			String mov = "";
			switch (v.getId())
			{
			case R.id.buttonGiuMulti:
				mov = "giu";
				break;
			case R.id.buttonSuMulti:
				mov = "su";
				break;
			case R.id.buttonDestraMulti:
				mov = "destra";
				break;
			case R.id.buttonSinistraMulti:
				mov = "sinistra";
				break;
			case R.id.buttonOrarioMulti:
				mov = "orario";
				break;
			case R.id.buttonAntiorarioMulti:
				mov = "antiorario";
				break;
			}
			if(!mov.isEmpty()){
				// creo l'arrayList di OggettoInviabile da inviare
	    		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
	    		// invio al server il movimento
	    		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, mov));
	    		manageConnection.sendObjects(oggetti);
	    		
	    		ultimoInvio = new Date().getTime();
			}
			
			// vibrazione
			 Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			 // Vibra per 50 millisecondi
			 vib.vibrate(25);
		}
	};
	
	/**
	 * quando si preme indietro si torna al menu principale e si chiude l'activity
	 */
    @Override
    public void onBackPressed() {
    	// Invia al server che si sta per chiudere l'activity
    	if(mode == GIOCO){
    		mSnakeView.setStatoGioco(GameView.PERSO);
    		
    		// creo l'arrayList di OggettoInviabile da inviare
    		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
    		// invio al server che mi sto disconnettendo
    		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, CHIUDI_GIOCO));
    		manageConnection.sendObjects(oggetti);
    	}
    	// salva
        Salvataggio.salvaImpostazioni(getBaseContext());
    	super.onBackPressed();
    }

    @Override
    protected void onPause() {
        // Chiude l'activity se è stato creato il gioco, altrimenti lo mette solo in pausa
    	super.onPause();
    	
        if(mode == GIOCO){
        	mSnakeView.setStatoGioco(GameView.PERSO);
        	
        	// creo l'arrayList di OggettoInviabile da inviare
    		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
    		// invio al server che mi sto disconnettendo
    		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, CHIUDI_GIOCO));
    		manageConnection.sendObjects(oggetti);
    		
        	chiudiActivity();
        }
        // salva
        if(isSignedIn()){
        	Salvataggio.salvaImpostazioni(getBaseContext(), getApiClient());
        } else {
        	Salvataggio.salvaImpostazioni(getBaseContext());
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
