package com.utility.bluetooth;

import java.io.IOException;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.ianthesnake.R;
import com.utility.InterfaceConnectedThread;
import com.utility.OggettoInviabile;

public abstract class AbstractMultiplayerBluetoothServerActivity extends AbstractBluetoothClientServer {
	
	protected final static int NUM_MAX_CLIENT = 3; // massimo numero di client che i possono connettere
	protected ArrayList<InterfaceConnectedThread> lista_client = new ArrayList<InterfaceConnectedThread>(); // array contenente tutti i client collegati
	protected AcceptThread accettaConnessioni;
	public handlerNuoviClient hNuoviClient = new handlerNuoviClient();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * crea il server
	 */
	@Override
	protected void creaClientServerBluetooth() {
		if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			startActivity(discoverableIntent);
		}
		
		accettaConnessioni = new AcceptThread();
		accettaConnessioni.start();
	}
	
	@Override
	protected void chiudiActivity() {
		if(accettaConnessioni != null){
			//chiudo la connessione se c'è
			accettaConnessioni.cancel();
		}
		finish();		
	}

	/** 
	 * metodo astratto per utilizzare i messaggi arrivati(per adesso solo stringhe)
	 */
	@Override
	protected abstract void messaggioRicevuto(int tipo, Object oggettoRicevuto, int ID);
	

	/**
	 *  metodo astratto utilizzato per avvertire che un client si è connesso
	 * @param string il nome del client che si è connesso
	 */
	protected abstract void nuovoClient(String string);
	
	/**
	 * thread che si mette in ascolto di nuove connessioni
	 * @author Ianfire
	 *
	 */
	protected class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	    private Boolean serverChiuso = true;
	    
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(getString(R.string.app_name), MY_UUID);
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	        messageHandler.obtainMessage(TRACE,1,1,getString(R.string.multiplayer_server_attesa)).sendToTarget();
	        serverChiuso = false;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Rimango in ascolto finche non viene chiuso il server
	        while (!serverChiuso) {
	            try {
	                socket = mmServerSocket.accept();
	            } catch (IOException e) {
	            	// errore generale chiudo l'activity
	            	chiudiActivity();
	                break;
	            }
	            // Se si accetta una connessione e la lista dei client non è piena aggiungo il nuovo client
	            if (socket != null && lista_client.size() < NUM_MAX_CLIENT) {
	                // un client si è connesso, avvio il thread per inviare e ricevere messaggi da questo client e lo aggiungo alla lista dei client
	            	ConnectedThreadBluetooth client = new ConnectedThreadBluetooth(socket, lista_client.size()+1);
	            	client.start();
	            	lista_client.add(client);
	            	messageHandler.obtainMessage(TRACE,1,1,getString(R.string.multiplayer_server_trovato_client)).sendToTarget();
	            	hNuoviClient.obtainMessage(NUOVO_CLIENT, socket.getRemoteDevice().getName()).sendToTarget();	            	
	            	
	            	// Invio un messaggio per l'avvenuta connessione
	            	// creo l'arrayList di OggettoInviabile da inviare
	        		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
	        		// invio al client
	        		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, PRONTO));
	        		client.sendObjects(oggetti);
	            }
	        }
	    }
	 
	    /** Will cancel the listening socket, and cause the thread to finish */
	    public void cancel() {
	        try {
	        	if(!serverChiuso){
	        		mmServerSocket.close();
	            	messageHandler.obtainMessage(TRACE,1,1,getString(R.string.multiplayer_server_chiusura_server)).sendToTarget();
	            	serverChiuso = true;
	        	}
	        } catch (IOException e) { System.out.println(e.toString());}
	    }
	}
	
	/**
	 * classe handler che si occupa di aggiungere i nuovi client
	 * @author Ianfire
	 *
	 */
	protected class handlerNuoviClient extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == NUOVO_CLIENT){
				if(msg.obj != null){
					nuovoClient(msg.obj.toString());
				} else {
					nuovoClient("Unknown"); //TODO
				}
			}
		}
	}

}
