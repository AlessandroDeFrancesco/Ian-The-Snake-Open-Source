package com.ianthesnake;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.utility.ImagesManager;
import com.utility.ClasseBindingConTextView;
import com.utility.Coordinate;
import com.utility.InterfaceConnectedThread;
import com.utility.SoundManager;
import com.utility.bluetooth.AbstractBluetoothClientServer;
import com.utility.OggettoInviabile;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * La view dove far vedere il gioco multiplayer senza le frecce di direzione
 */
public class MultiplayerServerGameView extends AbstractDrawView {
	
	// Possibili stati del gioco
    public static final int PAUSA = 0;
    public static final int PRONTO = 1;
    public static final int VAI = 2;
    public static final int PERSO = 3;
    
    public static final int ID_SERVER = 99999;
	
    private int StatoGioco;
    private TextView Display;
	
	public ClasseBindingConTextView punteggio;
	public ClasseBindingConTextView tempoGioco;
    private Date ultimoAggiornamento;
	private int punteggio_prec;
    private RefreshHandler HandlerRidisegna = new RefreshHandler();
	private static long RitardoMovimenti = 500;
	private int labirinto;
    private int velocita;
	
	private ArrayList<Coordinate> Muri;
	private ArrayList<Coordinate> MuriInvisibili;
	private ArrayList<Coordinate> Mangimi;
	private Hashtable<Integer,SnakeClass> Snakes;  // dizionario contenente gli snake associati agli id dei client collegati
	private Hashtable<Integer,int[]> ColoriSnakes; // dizionario contenente i colori degli snake associati agli id dei client collegati
	
	private ArrayList<InterfaceConnectedThread> lista_client;
	private int conteggioMangimiCreati;
	
	public MultiplayerServerGameView(Context context) {
		super(context);
	}

	public MultiplayerServerGameView(Context context, AttributeSet attrs) {
		super(context, attrs); 
	}

	public MultiplayerServerGameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle); 
	}

    protected void inizializzaView() { 	
    	// inizializzo le variabili
    	Muri = new ArrayList<Coordinate>();
    	MuriInvisibili = new ArrayList<Coordinate>();
		Mangimi = new ArrayList<Coordinate>();
		Snakes = new Hashtable<Integer,SnakeClass>();

        iniziaGioco();
	}
    
	public void iniziaGioco() {
		recuperaOpzioniGioco();
		// resetto i mangimi
		Mangimi = new ArrayList<Coordinate>();
		conteggioMangimiCreati = 0;
		// creo il labirinto
		creaLabirinto();
        // setto la griglia a 0, ossia nessuna immagine
        resettaGriglia();
		// creo gli snake
		creaSnakes();
        // aggiungo mangime
        aggiungiMangime(false);
     	// setto punteggio a 0
     	punteggio.setValore(0);
     	// refresho la View
     	ultimoAggiornamento = new Date();
     	tempoGioco.setValore(0);
     	update();
	}
	
	/**
	 * qui creo gli snake e la biunivocità tra gli snake e la lista dei client
	 */
	private void creaSnakes(){
		int offset = 2 - (lista_client.size() * 2); // offset per il posizionamento degli snakes
		// aggiungo lo snake del server
		SnakeClass snake = new SnakeClass(Mangimi, MuriInvisibili, LunghezzaStage, AltezzaStage, offset);
		snake.aggiungiOstacolo(Muri);
		// aggiungo lo snake iniziale
		snake.aggiungiTesta();
		snake.aggiungiPezzoCorpo();
		snake.aggiungiPezzoCorpo();
		snake.aggiungiPezzoCorpo();
		// setto il colore
		snake.setImmagini(ContenitoreOpzioni.immTesta, ContenitoreOpzioni.immCorpoPari, ContenitoreOpzioni.immCorpoDispari);
		
		// aggiungo lo snake al dizionario
		Snakes.put(ID_SERVER, snake);
		
		// aggiungo gli snake dei client
		for(InterfaceConnectedThread client:lista_client){
			int ID = client.getID();
			
			offset += 2;
			snake = new SnakeClass(Mangimi, MuriInvisibili, LunghezzaStage, AltezzaStage, offset);
			snake.aggiungiOstacolo(Muri);
			// aggiungo lo snake iniziale
			snake.aggiungiTesta();
			snake.aggiungiPezzoCorpo();
			snake.aggiungiPezzoCorpo();
			snake.aggiungiPezzoCorpo();
			// setto il colore
			int testa = ColoriSnakes.get(ID)[0];
			int corpo_pari = ColoriSnakes.get(ID)[1];
			int corpo_dispari = ColoriSnakes.get(ID)[2];
			snake.setImmagini(testa, corpo_pari, corpo_dispari);
			
			// aggiungo lo snake al dizionario
			Snakes.put(ID, snake);
		}
		
		// dopo aver creato tutti gli snakes posso aggiungere ad ognuno come ostacolo gli altri snake
		for(InterfaceConnectedThread client:lista_client){
			int ID = client.getID();
			
			// lo aggiungo come ostacolo al server
			Snakes.get(ID_SERVER).aggiungiOstacolo(Snakes.get(ID).getSnake());
			// aggiungo a lui il server come ostacolo
			Snakes.get(ID).aggiungiOstacolo(Snakes.get(ID_SERVER).getSnake());
			// aggiungo a tutti gli altri client lui come ostacolo
			for(InterfaceConnectedThread client2:lista_client){
				// se è lui stesso lo salto
				if(!(ID == client2.getID())){
					Snakes.get(client2.getID()).aggiungiOstacolo(Snakes.get(ID).getSnake());
				}
			}
		}
	}
	
    /**
     * Setto lo stato del gioco (se in corso, pronto, pausa o perso) e 
     * mostro o no il display che riporta il record e il punteggio raggiunto quando si perde 
     * @param newMode
     */
    public void setStatoGioco(int newMode) {
        int oldMode = StatoGioco;
        StatoGioco = newMode;

        if (newMode == VAI & oldMode != VAI) {
            Display.setVisibility(View.INVISIBLE);
            HandlerRidisegna.sleep(RitardoMovimenti);
            // invio inizio gioco
            inviaStatoGiocoClient(AbstractBluetoothClientServer.VAI);
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSA) {
            str = res.getText(R.string.stato_gioco_pausa);
            // invio pausa gioco
            inviaStatoGiocoClient(AbstractBluetoothClientServer.PAUSA);
        }
        if (newMode == PRONTO) {
            str = res.getText(R.string.stato_gioco_pronto);
        }
        if (newMode == PERSO) {
        	// aggiorno record
        	salvaRecord();
        	// mostro punteggio attuale e record
            str = res.getString(R.string.stato_gioco_perso_pref) + punteggio.getValore() ;
            str = str + res.getString(R.string.stato_gioco_perso_suff) + ContenitoreOpzioni.record;
            // invio fine gioco
            inviaStatoGiocoClient(AbstractBluetoothClientServer.FINE);
        }

        Display.setText(str);
        Display.setVisibility(View.VISIBLE);
        Display.bringToFront();
    }
	
    private void salvaRecord() {
    	if(punteggio.getValore() > ContenitoreOpzioni.record) {
    		ContenitoreOpzioni.record = punteggio.getValore();
    	}
	}

	/**
	 * classe handler che si occupa di ridisegnare ogni tot tempo definito da RitardoMovimenti
	 * @author Ianfire
	 *
	 */
	class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
        	MultiplayerServerGameView.this.update();
        	MultiplayerServerGameView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    public void update() {
    	if(StatoGioco == VAI){
    		resettaGriglia();
    		updateSnakes();
    		updateMuri();
    		updateMangime();
    		inviaUpdateClient();
    		
    		// aggiorno il tempo ogni secondo
    		Date adesso = new Date();
    		if(adesso.getTime() - ultimoAggiornamento.getTime() >= 1000){
    			tempoGioco.aggiungiValore(1);
    			ultimoAggiornamento = adesso;
    		}
    		
    		HandlerRidisegna.sleep(RitardoMovimenti);
    	} else if(StatoGioco == PRONTO){
    		resettaGriglia();
    		updateSnakes();
    		updateMuri();
    		updateMangime();
    		inviaUpdateClient();
    	}
    }

	private void perditaVita() {
		setStatoGioco(PERSO);
	}
	
	/**
	 * aggiunge il punteggio quando viene mangiato un cibo, riproduce un suono e invia al client di riprodurre lo stesso suono
	 * @param i
	 */
    private void mangimeColpito(int i) {
		// creo l'arrayList di OggettoInviabile da inviare
		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
    	
    	if(Mangimi.get(i).Extra ){
    		punteggio.aggiungiValore(velocita * ((MangimeExtra) Mangimi.get(i)).getMoltiplicatore());
    		// riproduce suono
    		if(ContenitoreOpzioni.suonoOn)
    			SoundManager.getInstance(getContext()).playSound(SoundManager.MANGIME_EXTRA_PRESO);
    		// aggiungo il suono al messaggio da inviare
    		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.RIPRODUCI_SUONO, SoundManager.MANGIME_EXTRA_PRESO));
    	} else {
    		punteggio.aggiungiValore(velocita);
    		// riproduce suono
    		if(ContenitoreOpzioni.suonoOn)
    			SoundManager.getInstance(getContext()).playSound(SoundManager.MANGIME_PRESO);
    		// aggiungo il suono al messaggio da inviare
    		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.RIPRODUCI_SUONO, SoundManager.MANGIME_PRESO));
    		
    		// crea del nuovo mangime
    		aggiungiMangime(false);
    		conteggioMangimiCreati++;
    		// crea mangime extra se ne sono stati mangiati 5 normali
    		if(conteggioMangimiCreati % 5 == 0){
    			aggiungiMangime(true);
    		}
    	}
    	
    	// invio il messaggio
    	inviaMessaggioAiClient(oggetti);
    	
    	Mangimi.remove(i); // elimina l'elemento i dall'array;
	}

	/**
     * I metodi update servono a scegliere l'immagine da mettere nella griglia
     * 
     */
    private void updateMuri() {
    	for(Coordinate mang:Muri){
    		setImmagineInGriglia(ImagesManager.immagineMuro, mang.x, mang.y);
    	}
    	for(Coordinate mang:MuriInvisibili){
    		setImmagineInGriglia(ImagesManager.immagineMuroInvisibile, mang.x, mang.y);
    	}
    }

    private void updateMangime() {
    	
    	// diminuisco la vita degli extra e li tolgo nel caso
    	for(int i=0; i<Mangimi.size(); i++){
    		if(Mangimi.get(i).Extra ){
	    		MangimeExtra manExtra = (MangimeExtra) Mangimi.get(i);
				manExtra.diminuisciVita();
		    	// se è scaduto il tempo del mangime extra lo elimino altrimenti lo visualizzo
				if(manExtra.isMorto()){
					Mangimi.remove(i);
				}
    		}
    	}
    	
    	// aggiorno i mangimi
    	for(int i=0; i<Mangimi.size(); i++){
    		if(Mangimi.get(i).Extra ){
    			MangimeExtra manExtra = (MangimeExtra) Mangimi.get(i);
    			if(manExtra.getVita() % 2 == 0){
    				setImmagineInGriglia(ImagesManager.immagineMangimeExtraPari, manExtra.x, manExtra.y);
    			} else {
    				setImmagineInGriglia(ImagesManager.immagineMangimeExtraDispari, manExtra.x, manExtra.y);
    			}
    		}else{
    			setImmagineInGriglia(ImagesManager.immagineMangime, Mangimi.get(i).x, Mangimi.get(i).y);
    		}
    	}
    }
	
    /**
     * per ogni client prendo lo snake associato e lo aggiorno
     */
    private void updateSnakes() {
    	// update snake del server
    	SnakeClass Snake = Snakes.get(ID_SERVER);
    	int statoSnake = Snake.muoviSnake();
    	
    	if(statoSnake == SnakeClass.PERSOVITA){
    		perditaVita();
    	} else if(statoSnake == SnakeClass.MANGIMECOLPITO){
    		mangimeColpito(Snake.getMangimeColpito());
    		Snake.aggiungiPezzoCorpo();
    	} else if(statoSnake == SnakeClass.CONTINUA){
    		// non faccio nulla
    	}
    	
    	ArrayList<Coordinate> snake = Snake.getSnake();
    	int numPezziCorpo = snake.size();
    	// update della testa sulla griglia
    	setImmagineInGriglia(Snake.getImmagineTesta(), snake.get(0).x, snake.get(0).y);
    	
    	//update del corpo sulla griglia
    	for (int i = 1; i<numPezziCorpo; i++){        	
        	if (i % 2 == 0)
        	{
        		setImmagineInGriglia(Snake.getImmagineCorpoPari(), snake.get(i).x, snake.get(i).y);
        	}
        	else
        	{
        		setImmagineInGriglia(Snake.getImmagineCorpoDispari(), snake.get(i).x, snake.get(i).y);
        	}
    	}
    	
    	// update snake dei client
    	for(InterfaceConnectedThread client:lista_client){
    		Snake = Snakes.get(client.getID());
	    	statoSnake = Snake.muoviSnake();
	    	
	    	if(statoSnake == SnakeClass.PERSOVITA){
	    		perditaVita();
	    	} else if(statoSnake == SnakeClass.MANGIMECOLPITO){
	    		mangimeColpito(Snake.getMangimeColpito());
	    		Snake.aggiungiPezzoCorpo();
	    	} else if(statoSnake == SnakeClass.CONTINUA){
	    		// non faccio nulla
	    	}
	    	
	    	snake = Snake.getSnake();
	    	numPezziCorpo = snake.size();
	    	// update della testa sulla griglia
	    	setImmagineInGriglia(Snake.getImmagineTesta(), snake.get(0).x, snake.get(0).y);
	    	
	    	//update del corpo sulla griglia
	    	for (int i = 1; i<numPezziCorpo; i++){        	
	        	if (i % 2 == 0)
	        	{
	        		setImmagineInGriglia(Snake.getImmagineCorpoPari(), snake.get(i).x, snake.get(i).y);
	        	}
	        	else
	        	{
	        		setImmagineInGriglia(Snake.getImmagineCorpoDispari(), snake.get(i).x, snake.get(i).y);
	        	}
	    	}
    	}
    	
    }    
    
    private void aggiungiMangime(Boolean Extra){
    	Boolean nonAggiungere = false;
    	Coordinate mang;
    	
    	// controllo se devo creare quello extra
    	if(Extra){
    		mang = new MangimeExtra();
    	} else {
    		mang = new Coordinate();
    	}
    	
    	mang.x = (int) (Math.random() * LunghezzaStage);
    	mang.y = (int) (Math.random() * AltezzaStage);
    	
    	// controllo che non vada sui muri, sugli snakes o sugli altri mangimi
    	// snakes
    	for(InterfaceConnectedThread client:lista_client){
    		SnakeClass Snake = Snakes.get(client.getID());
	    	for(Coordinate pezzo:Snake.getSnake()){
	    		nonAggiungere = Coordinate.confrontaPosizione(pezzo, mang);
	    		if(nonAggiungere){break;};
	    	}
	    	if(nonAggiungere){break;};
    	}
    	// muri
    	if(nonAggiungere == false){
    		for(Coordinate muro:Muri){
    			nonAggiungere = Coordinate.confrontaPosizione(muro, mang);
    			if(nonAggiungere){break;};
    		}
    	}
    	// muri invisib
    	if(nonAggiungere == false){
    		for(Coordinate muro:MuriInvisibili){
    			nonAggiungere = Coordinate.confrontaPosizione(muro, mang);
    			if(nonAggiungere){break;};
    		}
    	}
    	// mangimi
    	if(nonAggiungere == false){
    		for(Coordinate mangime:Mangimi){
    			nonAggiungere = Coordinate.confrontaPosizione(mangime, mang);
    			if(nonAggiungere){break;};
    		}
    	}
    	
    	if(nonAggiungere){
    		aggiungiMangime(Extra);
    	} else {
    		Mangimi.add(mang);
    	}
    }
    
    private void creaLabirinto(){
    	//trace("creazione labirinto " + labirinto)
    	// eliminazione di qualsiasi altro labirinto
    	Muri.clear();
    	MuriInvisibili.clear();
    	
    	int[]  dimensioniStage = new int[2];
    	dimensioniStage[0] = LunghezzaStage;
    	dimensioniStage[1] = AltezzaStage;
    	
    	switch (labirinto) {
    		case 1:
    			CreaLabirinti.creaLabirinto1(Muri, MuriInvisibili, dimensioniStage);
    			break;
    		case 2:
    			CreaLabirinti.creaLabirinto2(Muri, MuriInvisibili, dimensioniStage);
    			break;
    		case 3:
    			CreaLabirinti.creaLabirinto3(Muri, MuriInvisibili, dimensioniStage);
    			break;
    		case 4:
    			CreaLabirinti.creaLabirinto4(Muri, MuriInvisibili, dimensioniStage);
    			break;
    		default:
    			CreaLabirinti.creaLabirinto1(Muri, MuriInvisibili, dimensioniStage);
    			break;
    	}
    }

    /**
     * setta il prossimo movimento per lo snake identificato dall'id
     * @param mov "sinistra" "destra" "su" "giu"
     * @param ID id dello snake a cui settare il prossimo movimento( utilizzare la costante ID_SERVER per il movimento dello snake del server )
     */
	public void setProssimoMovimento(String mov, int ID) {
		if(StatoGioco == VAI){
			Snakes.get(ID).setProssimoMovimento(mov);
		}
	}
	
	public void setVelocita(int vel) {
		if(vel != 0){
			velocita = vel;
			RitardoMovimenti = 1000/velocita;
		} else {
			velocita = 2;
		}
	}
	
	public void setLabirinto(int lab) {
		if(lab != 0){
			labirinto = lab;
		} else {
			labirinto = 1;
		}
	}
	
	/**
	 * la lista dei client a cui inviare la griglia dello stage
	 * @param lista_cli
	 */
	public void setUpdaterClients(ArrayList<InterfaceConnectedThread> lista_cli){
		lista_client = lista_cli;
	}
	
	private void inviaUpdateClient(){
		byte[][] nuovaGriglia = new byte[LunghezzaStage][AltezzaStage];
		
		for (int x = 0; x < LunghezzaStage; x += 1) {
            for (int y = 0; y < AltezzaStage; y += 1) {
                nuovaGriglia[x][y] = (byte) GrigliaStage [x][y];
            }
        }
		
		// creo l'arrayList di OggettoInviabile da inviare
		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
		
		// aggiungo lo stage al messaggio da inviare
		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.GRIGLIA_STAGE_BYTE_ARRAY,nuovaGriglia));
		// aggiungo il punteggio se questo è cambiato
		if(punteggio_prec != punteggio.getValore()){
			oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.PUNTEGGIO_INT, punteggio.getValore()));
			punteggio_prec = punteggio.getValore();
		}
		
		// invio gli oggetti a tutti i client
		inviaMessaggioAiClient(oggetti);
	}

	private void inviaStatoGiocoClient(String nuovoStato){
		// creo l'arrayList di OggettoInviabile da inviare
		ArrayList<OggettoInviabile> oggetti = new ArrayList<OggettoInviabile>();
		// aggiungo il nuovo stato e invio
		oggetti.add(new OggettoInviabile(AbstractBluetoothClientServer.STRINGA, nuovoStato));
		// invio gli oggetti a tutti i client
		inviaMessaggioAiClient(oggetti);
	}
	
	/**
	 * metodo per inviare lo stesso messaggio a tutti i client
	 * @param oggetti
	 */
	private void inviaMessaggioAiClient(ArrayList<OggettoInviabile> oggetti) {
		for( InterfaceConnectedThread client:lista_client){
			client.sendObjects(oggetti);
		}
	}
	
	/**
	 * metodo per recuperare le opzioni necessarie per il gioco
	 */
	private void recuperaOpzioniGioco() {
		setVelocita(ContenitoreOpzioni.velocita);
		setLabirinto(ContenitoreOpzioni.labirinto);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	        if (StatoGioco == PRONTO) {
	            /*
	             * All'inizio del gioco se tocca parte lo snake
	             */
	            setStatoGioco(VAI);
	        }else if(StatoGioco == PAUSA) {
	            /*
	             * Se il gioco era in pausa riprendo
	             */
	        	setStatoGioco(VAI);
	        } else if(StatoGioco == PERSO) {
	            /*
	             * Se il gioco è stato perso inizio una nuova partita
	             */
	        	setStatoGioco(VAI);
	        	iniziaGioco();
		}
     return true;   
	}

	/**
	 * setta display per pausa, morto ecc, e la text view per il punteggio
	 * @param displ
	 * @param textViewPunteggio
	 * @param api
	 */
	public void setDisplay(TextView displ, TextView textViewPunteggio, TextView textViewTempoGioco,GoogleApiClient api) {
		Display = displ;
		punteggio = new ClasseBindingConTextView(getContext().getString(R.string.punteggio), textViewPunteggio);
		tempoGioco = new ClasseBindingConTextView(getContext().getString(R.string.punteggio), textViewTempoGioco);
	}
	
	/**
	 * aggiunge al dizionario ColoriSnakes l'associazione ID client, colori dello snake del client
	 */
	public void aggiungiColoriSnake( Hashtable<Integer,int[]> coloriSnakes){
		ColoriSnakes = coloriSnakes;
	}
	
}
