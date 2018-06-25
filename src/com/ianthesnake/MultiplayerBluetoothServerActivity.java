package com.ianthesnake;

import java.util.ArrayList;
import java.util.Hashtable;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.utility.CacheFont;
import com.utility.InterfaceConnectedThread;
import com.utility.JoystickManager;
import com.utility.OggettoInviabile;
import com.utility.Salvataggio;
import com.utility.Trace;
import com.utility.bluetooth.AbstractBluetoothClientServer;
import com.utility.bluetooth.AbstractMultiplayerBluetoothServerActivity;

public class MultiplayerBluetoothServerActivity extends AbstractMultiplayerBluetoothServerActivity {

	private static final int RICEZIONE_CLIENT = 1;
	private static final int GIOCO = 2;
	private int mode = RICEZIONE_CLIENT;
	
	private MultiplayerServerGameView mSnakeView;
	private Button buttonGiu;
	private Button buttonSu;
	private Button buttonDestra;
	private Button buttonSinistra;
	private Button buttonOrario, buttonAntiorario;
	private Button buttonInizia;
	private TextView punteggio, display, attesa;
	private ArrayAdapter<String> mArrayAdapter;
	private ListView viewListaClient;
	
	private Hashtable<Integer,int[]> ColoriSnakes  = new Hashtable<Integer,int[]>(); // dizionario contenente i colori degli snake associati agli id dei client collegati, sarà passato poi alla SnakeView
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multiplayer_server_activity);
		
		inizializzaGrafica();
		inizializzaListener();
		impostaFont();
		Salvataggio.caricaImpostazioni(getBaseContext());
		// permette all'utente di cambiare il volume con i tasti del dispositivo
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		inizializzaBluetooth();	
	}
	
	private void inizializzaGrafica() {
		if(mode == RICEZIONE_CLIENT){
			viewListaClient = (ListView) findViewById(R.id.listViewClientConnessi);
			mArrayAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.text_view_per_array_adapter);
			viewListaClient.setAdapter(mArrayAdapter);
			
			buttonInizia = (Button) findViewById(R.id.buttonServerInizia);
			attesa = (TextView) findViewById(R.id.textViewServerAttesa);
		}else{
			mSnakeView = (MultiplayerServerGameView) findViewById(R.id.multiplayerGameView);
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
		}
	}
	
	private void inizializzaListener(){
		if(mode == RICEZIONE_CLIENT){
			buttonInizia.setOnClickListener(iniziaGiocoMultiplayer);
		} else {
			buttonGiu.setOnClickListener(inviaMovimento);
			buttonSu.setOnClickListener(inviaMovimento);
			buttonDestra.setOnClickListener(inviaMovimento);
			buttonSinistra.setOnClickListener(inviaMovimento);
			buttonOrario.setOnClickListener(inviaMovimento);
			buttonAntiorario.setOnClickListener(inviaMovimento);
		}
	}
	
	private void impostaFont() {
		if(mode == RICEZIONE_CLIENT){
			buttonInizia.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
			attesa.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		} else {
			punteggio.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
			display.setTypeface(CacheFont.getFontPrincipale(getBaseContext()));
		}
	}
	
	
	/**
	 *  l'utente ha premuto su Inizia, posso iniziare il gioco e chiudere il server
	 */
	OnClickListener iniziaGiocoMultiplayer = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// controllo che ci sia almeno un client connesso
			if(!lista_client.isEmpty()){
				mode = GIOCO;
				setContentView(R.layout.sezione_gioco_multiplayer_server);
				
				// sblocco achievement
				Salvataggio.sbloccaAchievements(R.string.id_achievement_multiplayer, MultiplayerBluetoothServerActivity.this, getApiClient());
				
				inizializzaGrafica();
				inizializzaListener();
				impostaFont();
				
				mSnakeView.aggiungiColoriSnake(ColoriSnakes);
				mSnakeView.setUpdaterClients(lista_client);
				mSnakeView.setStatoGioco(MultiplayerServerGameView.PRONTO);
				
				// chiudo il server per le nuove connessioni
				//TODO accettaConnessioni.cancel();
			} else {
				Trace.trace(getApplicationContext(), getString(R.string.multiplayer_server_nessun_client_connesso));
			}
		}
	};
	
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
			
			// invio il tocco del pulsante alla view dello snake per il movimento
			mSnakeView.setProssimoMovimento(mov, MultiplayerServerGameView.ID_SERVER);
			// vibrazione
			 Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			 // Vibra per 50 millisecondi
			 vib.vibrate(25);
		}
	};
	
	

	@Override
	protected void nuovoClient(String nomeClient){
		mArrayAdapter.add(nomeClient);
	}
	
	/**
	 * da qui gestisco i messaggi ricevuti dai client
	 */
	@Override
	protected void messaggioRicevuto(int tipo, Object oggettoRicevuto, int ID) {
		switch(tipo){
			case STRINGA:
				String stringa = (String) oggettoRicevuto;
				eseguiComando(stringa, ID);
			break;
			case COLORI_SNAKE_INT_ARRAY:
				int[] colori = (int[]) oggettoRicevuto;
				eseguiComando(colori, ID);
			break;
			default:
			break;
		}
	}

	/**
	 * a seconda del messaggio ricevuto eseguo un comando
	 * @param comando
	 */
	private void eseguiComando(String stringa, int ID) {
		if(stringa.equals(PRONTO)){ // vuol dire che il client ha ricevuto il mio pronto e si può iniziare il gioco appena l'utente preme su Inizia
			
		} else if(stringa.equals(CHIUDI_GIOCO)){ // vuol dire che il client si  disconnesso o ha chiuso l'activity
			Trace.trace(getBaseContext(), getString(R.string.multiplayer_server_client_disconnesso));
			inviaChiudiTutto();
			chiudiActivity();
		} else if(stringa.equals("giu")){ // ho ricevuto il movimento del client
			if(mode == GIOCO)
				mSnakeView.setProssimoMovimento("giu", ID);
		} else if(stringa.equals("su")){
			if(mode == GIOCO)
				mSnakeView.setProssimoMovimento("su", ID);
		} else if(stringa.equals("destra")){
			if(mode == GIOCO)
				mSnakeView.setProssimoMovimento("destra", ID);
		} else if(stringa.equals("sinistra")){
			if(mode == GIOCO)
				mSnakeView.setProssimoMovimento("sinistra", ID);
		} else if(stringa.equals("orario")){
			if(mode == GIOCO)
				mSnakeView.setProssimoMovimento("orario", ID);
		} else if(stringa.equals("antiorario")){
			if(mode == GIOCO)
				mSnakeView.setProssimoMovimento("antiorario", ID);
		}
		
	}
	
	private void eseguiComando(int[] colori, int ID) {
		Log.d("colore", "Colore " + ID + " ricevuto");
		ColoriSnakes.put(ID, colori);
	}
	
	/**
	 * invia ai client di chiudere il gioco
	 */
	private void inviaChiudiTutto() {
		// creo l'arrayList di OggettoInviabile da inviare
		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
		// invio al client che mi sto disconnettendo
		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, CHIUDI_GIOCO));
		inviaMessaggioAiClient(oggetti);
	}
	
	/**
	 * metodo per inviare lo stesso messaggio a tutti i client
	 * @param oggetti
	 */
	public void inviaMessaggioAiClient(ArrayList<OggettoInviabile> oggetti) {
		for( InterfaceConnectedThread client:lista_client){
			client.sendObjects(oggetti);
		}
	}
	
	/**
	 * quando si preme indietro si torna al menu principale e si chiude l'activity
	 */
    @Override
    public void onBackPressed() {
    	if(mode == GIOCO){
    		mSnakeView.setStatoGioco(GameView.PERSO);
    		inviaChiudiTutto();
    	}
    	// salva
        Salvataggio.salvaImpostazioni(getBaseContext());
    	super.onBackPressed();
    }

    @Override
    protected void onPause() {
        // Mette in pausa il gioco
    	super.onPause();

        if(mode == GIOCO){
        	mSnakeView.setStatoGioco(GameView.PAUSA);
        }
        // salva
        if(isSignedIn()){
        	Salvataggio.salvaImpostazioni(getBaseContext(), getApiClient());
        } else {
        	Salvataggio.salvaImpostazioni(getBaseContext());
        }
    }

    @Override
    protected void onStop(){
    	super.onStop();
    	
    	if(mode == GIOCO){
        	chiudiActivity();
        	inviaChiudiTutto();
        }
        // salva
        Salvataggio.salvaImpostazioni(getBaseContext());
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
