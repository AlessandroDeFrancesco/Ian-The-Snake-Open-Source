package com.utility.bluetooth;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ianthesnake.R;
import com.utility.Trace;

/**
 * Prima di richiamare il metodo inizializzaBluetooth() assegnare gli oggetti grafici alle variabili protette
 * viewDispositiviBluetooth, progressBar, textAttesa, buttonRicerca
 * @author Ianfire
 *
 */
public abstract class AbstractMultiplayerBluetoothClientActivity extends AbstractBluetoothClientServer {

	/**
	 * Gli elementi grafici
	 */
	protected ArrayAdapter<String> mArrayAdapter; // serve per memorizzare i dispositivi trovati
	protected ListView viewDispositiviBluetooth; // serve per visualizzare i dispositivi trovati (collegato ad mArrayAdapter)
	protected ProgressBar progressBar; // serve per visualizzare l'attesa
	protected TextView textAttesa; // serve per visualizzare l'attesa
	protected Button buttonRicerca; // serve per ricerca nuovamente
	
	private ArrayList<BluetoothDevice> listaDispositiviBluetooth = new ArrayList<BluetoothDevice>();
	private ConnectThread connettiDispositivo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * crea il client
	 */
	@Override
	protected void creaClientServerBluetooth(){
		// Registro il BroadcastReceiver che mi fornisce i dispositivi trovati
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(mReceiver, filter);
		// inizio a cercare i dispositivi bluetooth
		cercaDispositiviBluetooth();
		// cerco i dispositivi e aspetto che l'utente tocchi il dispositivo nella lista a cui vuole connettersi
	}

	/**
	 * inizia la ricerca, setta il testo di attesa, mostra la progressBar e nasconde il bottone ricerca
	 */
	private void cercaDispositiviBluetooth() {
		String txtAttesa = "";
		if(mBluetoothAdapter.startDiscovery()){
			txtAttesa = getString(R.string.multiplayer_client_attesa);
			textAttesa.setText(txtAttesa);
			progressBar.setVisibility(View.VISIBLE);
			buttonRicerca.setVisibility(View.INVISIBLE);
		} else {
			Trace.trace(getBaseContext(),getString(R.string.multiplayer_client_err_ricerca));
			finish();
		}		
	}
	
	/**
	 * la ricerca è finita quindi setto il testo su fine ricerca, nascondo la progressBar e mostro il bottone per una nuova ricerca
	 */
	private void fineRicerca(){
		String txtAttesa = getString(R.string.multiplayer_client_fine_ricerca);
		textAttesa.setText(txtAttesa);
		progressBar.setVisibility(View.INVISIBLE);
		buttonRicerca.setVisibility(View.VISIBLE);
		buttonRicerca.setOnClickListener(ricercaNuovamente);
	}

	OnClickListener ricercaNuovamente = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cercaDispositiviBluetooth();
			}
		};
	
	@Override
	protected void chiudiActivity() {
		// cancella la registrazione del broadcast receiver e cancella la discovery se è stato attivato il bluetooth
		try{
			unregisterReceiver(mReceiver);
		} catch(IllegalArgumentException e){}
		
		if(connettiDispositivo != null){
			mBluetoothAdapter.cancelDiscovery();
			//chiudo la connessione se c'è
			connettiDispositivo.cancel();
		}
		finish();
	}

	/**
	 * CLASSI CHE RICHIEDONO OVERRIDE
	 */

	/**
	 * listener per la scelta del dispositivo a cui connettersi
	 */
	protected OnItemClickListener dispositivoScelto = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int posizione, long id) {
			// provo a connettermi al dispotivo cliccato
			BluetoothDevice dispositivo = listaDispositiviBluetooth.get(posizione);
			if(dispositivo != null){
				connettiDispositivo = new ConnectThread(dispositivo);
				connettiDispositivo.start();
			}
		}
	};

	/**
	 * BroadcastReceiver per visualizzare e tenere traccia dei dispositivi trovati e di quando la ricerca finisce
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to an array adapter to show in a ListView
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				// Aggiungo il dispositivo in una lista per poi usarlo per connettermi
				listaDispositiviBluetooth.add(device);
			}
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				// finita ricerca
				fineRicerca();
			}
		}
	};

	/**
	 * metodo astratto per utilizzare i messaggi arrivati
	 * @param tipo: il tipo di oggetto ricevuto
	 * @param oggettoRicevuto: l'Object ricevuto
	 */
	@Override
	abstract protected void messaggioRicevuto(int tipo, Object oggettoRicevuto, int ID);
	
	/**
	 * Trhead per creare la connessione col dispositivo scelto
	 * @author Ianfire
	 *
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final			
			BluetoothSocket tmp = null;
			mmDevice = device;

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) { }
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				messageHandler.obtainMessage(TRACE, 1, 1, getString(R.string.multiplayer_client_connessione)).sendToTarget();
			} catch (IOException connectException) {
				// TODO Errore( connectException );
				// Connessione non riuscita, chiudo il socket e l'activity
				try {
					messageHandler.obtainMessage(TRACE, 1, 1, getString(R.string.multiplayer_client_err_connessione)).sendToTarget();
					mmSocket.close();
					chiudiActivity();
				} catch (IOException closeException) { }
				return;
			}

			// La connessione è stata creata, posso inviare e ricevere messaggi attraverso un nuovo thread
			manageConnection = new ConnectedThreadBluetooth(mmSocket, 1);
			manageConnection.start();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}

	
	/**
	 * invia l'errore tramite e-mail
	 * @param context
	 * @param e
	 */
	public void Errore( Exception e){
		// converto l'errore in stringa
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		String err = errors.toString();
		
		// invio l'errore come e-mail
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"Alessandro.de.francesco.92@gmail.com"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Error Ian The Snake");
		i.putExtra(Intent.EXTRA_TEXT   , err);
		try {
			startActivity(Intent.createChooser(i, "Send bug report to developer"));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}

}

