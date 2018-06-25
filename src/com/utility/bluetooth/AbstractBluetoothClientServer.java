package com.utility.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.ianthesnake.R;
import com.utility.InterfaceConnectedThread;
import com.utility.OggettoInviabile;
import com.utility.Trace;

public abstract class AbstractBluetoothClientServer extends BaseGameActivity {

	public static final int MESSAGE_READ = 1; // codice per l'handler per gestire un messaggio arrivato
	public static final int TRACE = 2; // codice per l'handler per gestire un messaggio di cui fare il Trace
	public static final int NUOVO_CLIENT = 3; // serve per far sapere al server che un nuovo client si è connesso
	
	private static final int MAX_BYTE = 1024; // quantità massima di byte inviabile con un sendobjects()

	/** costanti per discriminare gli oggetti inviati o ricevuti */
	public static final int STRINGA = 1;
	public static final int GRIGLIA_STAGE_BYTE_ARRAY = 2;
	public static final int PUNTEGGIO_INT = 3;
	public static final int COLORI_SNAKE_INT_ARRAY = 4; 
	public static final int RIPRODUCI_SUONO = 5;

	/** costanti per lo scambio di messaggi tra client e server */
	public static final String PRONTO = "pronto"; // il gioco è pronto per iniziare (serve anche per scambiarsi l'handshake iniziale)
	public static final String VAI = "vai"; // il gioco è in corso
	public static final String PAUSA = "pausa"; // il gioco è stato messo in pausa
	public static final String FINE = "fine"; // il gioco è stato perso
	public static final String CHIUDI_GIOCO = "chiudi_gioco"; // serve per far chiudere il gioco sia se inviato dal client che dal server
	public static final String ARRIVATO = "arrivato"; // messaggio di risposta da parte del client quando riceve un messaggio

	/** 
	 * Costanti per la richiesta dell'accensione bluetooth e l'id univoco della connessione bluetooth
	 * (solo i server, client con lo stesso UUID possono comunicare tra loro)
	 */
	public static final int REQUEST_ENABLE_BT = 141;
	public static final UUID MY_UUID = UUID.fromString("6bae37cb-cf12-4446-9c18-2747334dbc86");

    /**
     * variabili per la gestione dei messaggi
     */
	protected static BluetoothAdapter mBluetoothAdapter; // l'adattatore che gestisce i servizi bluetooth
	public MessageHandler messageHandler = new MessageHandler(); // l'handler che gestisce i messaggi ricevuti
	public ConnectedThreadBluetooth manageConnection; // thread che utilizza il socket bluetooth per inviare i messaggi

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * Controlla che il dispositivo abbia il bluetooth e richiede al bluetooth di attivarsi
	 * Se l'utente accetta e il bluetooth viene attivato si richiama il metodo astratto creaClientServerBluetooth();
	 * 
	 */
	protected void inizializzaBluetooth(){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null){
			// non supporta il bluetooth
			Trace.trace(getBaseContext(), getString(R.string.multiplayer_bluetooth_no_supp));
			finish();
		}else {
			if(!mBluetoothAdapter.isEnabled()){ // se non è già attivato richiedo l'attivazione
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			} else { // è già attivo posso direttamente cercare i dispositivi
				creaClientServerBluetooth();
			}
		}
	}

	/**
	 * crea il client o il server viene chiamato automaticamente da inizializzaBluetooth() solo se il bluetooth è attivo
	 */
	abstract protected void creaClientServerBluetooth();

	/**
	 * funzione per utilizzare i messaggi arrivati dagli altri device bluetooth
	 */
	abstract protected void messaggioRicevuto(int tipo, Object oggettoRicevuto, int ID);

	/**
	 * quando si preme indietro si chiude l'activity e si torna all'activity precedente
	 */
	@Override
	public void onBackPressed() {
		chiudiActivity();
	}

	abstract protected void chiudiActivity();

	/**
	 * serve per ricevere dal bluetooth la conferma dell'attivazione del Bluetooth
	 * e creare il server o il client di conseguenza
	 */
	@Override
	protected void onActivityResult(int requestCode ,int resultCode, Intent data){
		if(requestCode == REQUEST_ENABLE_BT){
			if(resultCode == RESULT_OK){
				creaClientServerBluetooth();
			} else {
				finish();
			}
		}
	}

	/**
	 * Classe per inviare e ricevere messaggi da una socket bluetooth
	 * @author Ianfire
	 *
	 */
	public class ConnectedThreadBluetooth extends Thread implements  InterfaceConnectedThread{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;
		private final int ID;
		
		public ConnectedThreadBluetooth(BluetoothSocket socket,int id) {
			ID = id;
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}
		
		// ritorna l'id assegnato a questo thread (per discriminare quale client ha inviato il messaggio)
		public int getID(){
			return ID;
		}

		public void run() {
			byte[] buffer = new byte[MAX_BYTE]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					messageHandler.obtainMessage(MESSAGE_READ, bytes, ID, buffer).sendToTarget();
				} catch (Exception e) {
					// se c'è un errore chiudo la connessione e chiudo l'activity
					cancel();
					chiudiActivity();
					break;
				}
			}
		}

		/** 
		 * Chiamare questo metodo per inviare oggetti serializzati all'altro dispositivo (utilizzare un ArrayList di OggettoInviabile)
		 */
		public void sendObjects(ArrayList<OggettoInviabile> oggetti) {
			try {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				ObjectOutputStream o = new ObjectOutputStream(b);

				for(OggettoInviabile oggettoInviabile: oggetti){					
					o.writeInt(oggettoInviabile.tipo);
					o.writeObject(oggettoInviabile.oggetto);

					System.out.println("invio: "+ oggettoInviabile.toString());
				}

				mmOutStream.write(b.toByteArray());

			} catch (IOException e) { e.printStackTrace(); }
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}

	}

	/**
	 * classe handler che si occupa di leggere i messaggi arrivati dai dispositivi bluetooth
	 * @author Ianfire
	 *
	 */
	protected class MessageHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MESSAGE_READ){
				// recupero l'id del mittente
				int ID = msg.arg2;
				// recupero l'oggetto del messaggio, ossia i byte ricevuti
				ByteArrayInputStream b = new ByteArrayInputStream((byte[]) msg.obj);

				/* poichè sendObjects() può inviare più oggetti li leggo tutti e li faccio
				 * gestire dal client o server con messaggioRicevuto()
				 */
				try {
					ObjectInputStream o = new ObjectInputStream(b);
					// ciclo finchè non finiscono i byte da leggere
					while(o.available() > 0){
						int tipo = o.readInt();
						Object oggetto = o.readObject();
						/* invio il tipo e l'oggetto letti al metodo del client o server 
						 * che li gestirà */
						messaggioRicevuto(tipo, oggetto, ID);
						
						System.out.println("ricevuto: "+ oggetto.toString());
					}
				}catch (Exception e) {	
					e.printStackTrace(); 
				}

			} else if(msg.what == TRACE){
				// ricevuto un messaggio dai thread per stampare il trace
				String mess = msg.obj.toString();
				Trace.trace(getBaseContext(), mess);
			}
		}
	}
}
