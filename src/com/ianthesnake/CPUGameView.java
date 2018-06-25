package com.ianthesnake;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.utility.ImagesManager;
import com.utility.ClasseBindingConTextView;
import com.utility.Coordinate;
import com.utility.Nomi;
import com.utility.Salvataggio;
import com.utility.SoundManager;
import com.utility.pathfinder.Path;
import com.utility.pathfinder.PathFinder;
import com.utility.pathfinder.AStarPathFinder;
import com.utility.pathfinder.Step;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * La view dove far vedere il gioco senza le frecce di direzione
 */
public class CPUGameView extends AbstractDrawView {
	
	// Possibili stati del gioco
    public static final int PAUSA = 0;
    public static final int PRONTO = 1;
    public static final int VAI = 2;
    public static final int PERSO = 3;
	
    private Context context;
    
    private int StatoGioco;
    private TextView Display;
	
	public ClasseBindingConTextView punteggioGiocatore;
	public ClasseBindingConTextView tempoGioco;
	private SnakeClass snakeGiocatore;
    private RefreshHandler HandlerRidisegna = new RefreshHandler();
	private static int RitardoMovimenti = 500;
	private int labirinto;
    private int velocita;
    private Boolean giocatoreMorto;
	
    private ArrayList<SnakeClass> Snakes;
	private ArrayList<Coordinate> Muri;
	private ArrayList<Coordinate> MuriInvisibili;
	private ArrayList<Coordinate> Mangimi;
	
	private Date ultimoAggiornamento;
	
	private int mangimiNormaliPresi;
	// per achievement
	private GoogleApiClient apiClient;
	private int mangimiNormaliInviati = 0;
	private int mangimiSpecialiPresi = 0;
	private int comboSpecialiPresi = 0; // mangimi speciali presi di fila senza perderne uno
	
	// per CPU
    private static int numero_cpu = 1;
    private Hashtable<SnakeClass,Integer> punteggiCPU;
	private MappaGioco mappa;
	private PathFinder finder;
	private CPUThread cpuThread;
	
	public CPUGameView(Context cntxt) {
		super(cntxt);
		
		context = cntxt;
	}

	public CPUGameView(Context cntxt, AttributeSet attrs) {
		super(cntxt, attrs); 
		
		context = cntxt;
	}

	public CPUGameView(Context cntxt, AttributeSet attrs, int defStyle) {
		super(cntxt, attrs, defStyle); 
		
		context = cntxt;
	}

    protected void inizializzaView() { 	
         iniziaGioco();
	}
    
	protected void iniziaGioco() {
		recuperaOpzioniGioco();
		Mangimi = new ArrayList<Coordinate>();
		Muri = new ArrayList<Coordinate>();
		MuriInvisibili = new ArrayList<Coordinate>();
		
        // setto la griglia a 0, ossia nessuna immagine
        resettaGriglia();
        // creo il labirinto
        creaLabirinto();
        // mangimi presi a 0
        mangimiNormaliPresi = 0;
        mangimiNormaliInviati = 0;
        mangimiSpecialiPresi = 0;
        comboSpecialiPresi = 0;
        
		// creo gli snakes
        Snakes = new ArrayList<SnakeClass>();
		creaSnakes();
        // aggiungo mangime
     	aggiungiMangime(false);
     	// setto punteggio a 0
     	punteggioGiocatore.setValore(0);
     	giocatoreMorto = false;
     	// refresho la View
        tempoGioco.setValore(0);
     	ultimoAggiornamento = new Date();
     	// per CPU
     	variabiliPerCPU();
     	
        update();
	}
	
	private void variabiliPerCPU(){
		// creo la mappa per l'algoritmo a star aggiungengo tutti gli ostacoli
		mappa = new MappaGioco(LunghezzaStage, AltezzaStage);
		mappa.aggiungiOstacolo(Muri);
		for(int i = 0; i<numero_cpu + 1 ; i++){
	     	mappa.aggiungiOstacolo(Snakes.get(i).getSnake());
		}
		finder = new AStarPathFinder(mappa, 100, false);
		cpuThread = new CPUThread();
		
		// creo il dizionario contenente i punteggi per ogni snake controllato dalla cpu
		punteggiCPU = new Hashtable<SnakeClass, Integer>();
		for(SnakeClass snake:Snakes){
			if(snake.getIsCPU())
				punteggiCPU.put(snake, 0);
		}
	}
	
	/**
	 * qui creo gli snake
	 */
	private void creaSnakes(){
		int offset = 2 - (numero_cpu * 2); // offset per il posizionamento degli snakes
		// aggiungo lo snake del giocatore (deve essere sempre il primo)
		SnakeClass snake = new SnakeClass(Mangimi, MuriInvisibili, LunghezzaStage, AltezzaStage, offset);
		snake.aggiungiOstacolo(Muri);
		// aggiungo lo snake iniziale
		snake.aggiungiTesta();
		snake.aggiungiPezzoCorpo();
		snake.aggiungiPezzoCorpo();
		snake.aggiungiPezzoCorpo();
		// setto il colore
		snake.setImmagini(ContenitoreOpzioni.immTesta, ContenitoreOpzioni.immCorpoPari, ContenitoreOpzioni.immCorpoDispari);
		
		// setto la variabile che mi dice quale snake è del giocatore
		snakeGiocatore = snake;
		// aggiungo lo snake all'array degli snakes
		Snakes.add(snake);
		
		// aggiungo gli snake del cpu
		for(int i = 0; i<numero_cpu ; i++){
			offset += 2;
			snake = new SnakeClass(Mangimi, MuriInvisibili, LunghezzaStage, AltezzaStage, offset);
			snake.aggiungiOstacolo(Muri);
			// aggiungo lo snake iniziale
			snake.aggiungiTesta();
			snake.aggiungiPezzoCorpo();
			snake.aggiungiPezzoCorpo();
			snake.aggiungiPezzoCorpo();
			// setto il colore
			int testa = ImagesManager.immagineTesta;
			int corpo_pari = ImagesManager.getImmagineCorpoRandom();
			int corpo_dispari = ImagesManager.getImmagineCorpoRandom();
			snake.setImmagini(testa, corpo_pari, corpo_dispari);
			// setto che è una CPU
			snake.setIsCPU(true);
			
			// aggiungo lo snake all'array degli snakes
			Snakes.add(snake);
		}
		
		// dopo aver creato tutti gli snakes posso aggiungere ad ognuno come ostacolo gli altri snake
		for(int i = 0; i<Snakes.size() ; i++){
			// aggiungo a tutti gli altri client lui come ostacolo
			for(int j = 0; j<Snakes.size() ; j++){
				// se è lui stesso lo salto
				if(!(i == j)){
					Snakes.get(j).aggiungiOstacolo(Snakes.get(i).getSnake());
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
            return;
        }

        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSA) {
            str = res.getText(R.string.stato_gioco_pausa);
        }
        if (newMode == PRONTO) {
            str = res.getText(R.string.stato_gioco_pronto);
        }
        if (newMode == PERSO) {
        	// aggiorno record
        	salvaRecord();
        	updateAchievements();
        	// mostro i punteggi di tutti
            str = creaStringaPunteggi();
        }

        Display.setText(str);
        Display.setVisibility(View.VISIBLE);
        Display.bringToFront();
    }
	
    private CharSequence creaStringaPunteggi() {
    	Resources res = getContext().getResources();
    	CharSequence str = "";
    	
    	// controllo il punteggio più alto tra le cpu
    	int punteggioCpuMigliore = 0;
    	for(SnakeClass snake:Snakes){
    		if(snake.getIsCPU()){
	    		int punteggio = punteggiCPU.get(snake);
	    		if(punteggio > punteggioCpuMigliore)
	    			punteggioCpuMigliore = punteggio;
    		}
    	}
    	
    	// confronto il punteggio migliore tra le cpu e quello del giocatore
    	if(punteggioGiocatore.getValore() > punteggioCpuMigliore){
    		str = str + res.getString(R.string.stato_gioco_perso_vittoria);
    	} else if(punteggioGiocatore.getValore() < punteggioCpuMigliore){
    		str = str + res.getString(R.string.stato_gioco_perso_perdita);
    	} else {
    		str = str + res.getString(R.string.stato_gioco_perso_pareggio);
    	}
    	str = str + "\n\n";
    	
    	// stampo tutti i punteggi
    	str = str + res.getString(R.string.stato_gioco_perso_pref) + punteggioGiocatore.getValore() + "\n";
    	for(SnakeClass snake:Snakes){
    		if(snake.getIsCPU()){
	    		int punteggio = punteggiCPU.get(snake);
	    		str = str + res.getString(R.string.stato_gioco_perso_punteggio_cpu) + " " + Nomi.getNomeRandom() + ": " + punteggio + "\n";
    		}
    	}
    	
    	// stampo record
        str = str + res.getString(R.string.stato_gioco_perso_suff) + ContenitoreOpzioni.record_vs_cpu;
		return str;
	}
    
	private void salvaRecord() {
    	if(punteggioGiocatore.getValore() > ContenitoreOpzioni.record_vs_cpu) {
    		ContenitoreOpzioni.record_vs_cpu = punteggioGiocatore.getValore();
    	}
	}

	/**
     * 
     * @return lo stato corrente del gioco
     */
    public int getStatoGioco(){
    	return StatoGioco;
    }

	/**
	 * classe handler che si occupa di ridisegnare ogni tot tempo definito da RitardoMovimenti
	 * @author Ianfire
	 *
	 */
	class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {    
        	long startTime = System.currentTimeMillis();
            CPUGameView.this.update();
            CPUGameView.this.invalidate();
			long endTime = System.currentTimeMillis();
			Log.v("gioco","Aggiornamento " + (endTime - startTime) + " milliseconds");
			
			// calcolo le mosse dopo l'aggiornamento
			if(StatoGioco == VAI){
	    		prossimaMossaCPU();
			}
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }
	
    public void update() {
    	if(StatoGioco == VAI){
    		resettaGriglia();
    		updateSnake();
    		updateMuri();
    		updateMangime();
    		updateAchievements();

    		// aggiorno il tempo ogni secondo
    		Date adesso = new Date();
    		if(adesso.getTime() - ultimoAggiornamento.getTime() >= 1000){
    			tempoGioco.aggiungiValore(1);
    			ultimoAggiornamento = adesso;
    		}
    		
    		HandlerRidisegna.sleep(RitardoMovimenti);
    	} else if(StatoGioco == PRONTO){
    		resettaGriglia();
    		updateSnake();
    		updateMuri();
    		updateMangime();
    	}
    }
    
    private void prossimaMossaCPU() {
    	cpuThread.handler.obtainMessage(CPUThread.CALCOLA_MOSSE).sendToTarget();
	}

	/**
     * aggiorna lo stato degli achievements
     */
    private void updateAchievements() {
    	// controllo il punteggio più alto tra le cpu
    	int punteggioCpuMigliore = 0;
    	for(SnakeClass snake:Snakes){
    		if(snake.getIsCPU()){
	    		int punteggio = punteggiCPU.get(snake);
	    		if(punteggio > punteggioCpuMigliore)
	    			punteggioCpuMigliore = punteggio;
    		}
    	}
    	
    	// achievements vittoria contro cpu
    	if(StatoGioco == PERSO && punteggioGiocatore.getValore() > punteggioCpuMigliore ){
    		if(punteggioGiocatore.getValore() >= 100){
    			if(numero_cpu == 1)
    				Salvataggio.sbloccaAchievements(R.string.id_achievement_vittoria_cpu1, context, apiClient);
    			else if(numero_cpu == 2)
    				Salvataggio.sbloccaAchievements(R.string.id_achievement_vittoria_cpu2, context, apiClient);
    			else if(numero_cpu == 3)
    				Salvataggio.sbloccaAchievements(R.string.id_achievement_vittoria_cpu3, context, apiClient);
    		}
    	}	
	}

    /**
     * crea nuovo mangime e aggiunge il valore del mangime preso al punteggio dello snake che l'ha preso
     * @param i l'indice del mangime preso
     * @param chi lo snake che ha preso il mangime
     */
	private void mangimeColpito(int i, SnakeClass chi) {    	
    	if(Mangimi.get(i).Extra ){
    		// se il mangime preso è extra
    		mangimiSpecialiPresi++;
    		comboSpecialiPresi++;
    		int valore = velocita * ((MangimeExtra) Mangimi.get(i)).getMoltiplicatore();
    		// aggiungo il punteggio allo snake che l'ha preso
    		if(chi == snakeGiocatore){
    			punteggioGiocatore.aggiungiValore(valore);
    		} else {
    			punteggiCPU.put(chi, punteggiCPU.get(chi) + valore);
    		}
    		// riproduce suono
    		if(ContenitoreOpzioni.suonoOn)
    			SoundManager.getInstance(getContext()).playSound(SoundManager.MANGIME_EXTRA_PRESO);
    	} else { 
    		int valore = velocita;
    		// aggiungo il punteggio allo snake che l'ha preso
    		if(chi == snakeGiocatore){
    			punteggioGiocatore.aggiungiValore(valore);
    		} else {
    			punteggiCPU.put(chi, punteggiCPU.get(chi) + valore);
    		}
    		// riproduce suono
    		if(ContenitoreOpzioni.suonoOn)
    			SoundManager.getInstance(getContext()).playSound(SoundManager.MANGIME_PRESO);
    		// crea del nuovo mangime
    		aggiungiMangime(false);  		
    		mangimiNormaliPresi++;
    		// crea mangime extra se ne sono stati mangiati 5 normali
    		if(mangimiNormaliPresi % 5 == 0){
    			aggiungiMangime(true);
    		}
    	}
    	
    	Mangimi.remove(i); // elimina l'elemento i dall'array;
	}

	private void eliminaSnake(int chi) {
		//suono
		SoundManager.getInstance(context).playSound(SoundManager.MORTE_SNAKE);
		// vibrazione
		 Vibrator vib = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		 vib.vibrate(500);
		 
		// rimuovo lo snake che ha perso la vita
		Snakes.get(chi).getSnake().clear();
		
		// controllo se ne è rimasto solo uno
		int snakeVivi = 0;
		for(SnakeClass snake:Snakes){
			if(snake.getIsLive())
				snakeVivi++;
		}
		if(snakeVivi <= 1){
			setStatoGioco(PERSO);
		}
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
					comboSpecialiPresi = 0; // resetto la combo perchè il giocatore ne ha mancato uno
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
	
    private void updateSnake() {    	    	
    	// update snakes
    	for(int i = 0; i<Snakes.size() ; i++){
    		if(!Snakes.get(i).getIsLive())
    			continue;
    			
    		SnakeClass Snake = Snakes.get(i);
	    	int statoSnake = Snake.muoviSnake();
	    	
	    	if(statoSnake == SnakeClass.PERSOVITA){
	    		// elimino lo snake e continuo col prossimo
	    		eliminaSnake(i);
	    		continue;
	    	} else if(statoSnake == SnakeClass.MANGIMECOLPITO){
	    		mangimeColpito(Snake.getMangimeColpito(), Snakes.get(i));
	    		Snake.aggiungiPezzoCorpo();
	    	} else if(statoSnake == SnakeClass.CONTINUA){
	    		// non faccio nulla
	    	}
	    	
	    	ArrayList<Coordinate> snake = Snake.getSnake();
	    	int numPezziCorpo = snake.size();
	    	// update della testa sulla griglia
	    	setImmagineInGriglia(Snake.getImmagineTesta(), snake.get(0).x, snake.get(0).y);
	    	
	    	//update del corpo sulla griglia
	    	for (int j = 1; j<numPezziCorpo; j++){        	
	        	if (j % 2 == 0)
	        	{
	        		setImmagineInGriglia(Snake.getImmagineCorpoPari(), snake.get(j).x, snake.get(j).y);
	        	}
	        	else
	        	{
	        		setImmagineInGriglia(Snake.getImmagineCorpoDispari(), snake.get(j).x, snake.get(j).y);
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
    	for(int i = 0; i<Snakes.size() ; i++){
    		SnakeClass Snake = Snakes.get(i);
	    	for(Coordinate pezzo:Snake.getSnake()){
	    		nonAggiungere = Coordinate.confrontaPosizione(pezzo, mang);
	    		if(nonAggiungere){break;};
	    	}
	    	if(nonAggiungere){break;};
    	}
    	if(nonAggiungere == false){
    		for(Coordinate muro:Muri){
    			nonAggiungere = Coordinate.confrontaPosizione(muro, mang);
    			if(nonAggiungere){break;};
    		}
    	}
    	if(nonAggiungere == false){
    		for(Coordinate muro:MuriInvisibili){
    			nonAggiungere = Coordinate.confrontaPosizione(muro, mang);
    			if(nonAggiungere){break;};
    		}
    	}
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
    	Muri = new ArrayList<Coordinate>();
    	MuriInvisibili = new ArrayList<Coordinate>();
    	
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

	public void setProssimoMovimento(String mov) {		
		if(StatoGioco == VAI && !giocatoreMorto){
			snakeGiocatore.setProssimoMovimento(mov);
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
            iniziaGioco();
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
	public void setDisplay(TextView displ, TextView textViewPunteggio, TextView textViewTempoGioco, GoogleApiClient api) {
		Display = displ;
		apiClient = api;
		punteggioGiocatore = new ClasseBindingConTextView(context.getString(R.string.punteggio), textViewPunteggio);
		tempoGioco = new ClasseBindingConTextView(context.getString(R.string.tempo_gioco), textViewTempoGioco);
	}
	
	public static void setNumeroCpu(int nCpu){
		numero_cpu = nCpu;
	}
	
	protected class CPUThread extends Thread {
		public static final int CALCOLA_MOSSE = 1;
		public HandlerCalcolo handler = new HandlerCalcolo();
				
		private class HandlerCalcolo extends Handler{
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == CALCOLA_MOSSE){
					long startTime = System.currentTimeMillis();
					
					calcolaMosse();
					
					long endTime = System.currentTimeMillis();
					Log.v("gioco","Calcolo mosse CPU: " + (endTime - startTime) + " millisecondi");
				}
			}
		};
		
		public void calcolaMosse(){
			Coordinate migliorMang = migliorMangime();
	    	
	    	// faccio fare la mossa a tutte le cpu;
	    	for(int i = 0; i<Snakes.size() ; i++){
	    		// verifico che lo snake sia controllato dalla CPU e che sia vivo
	    		if(!Snakes.get(i).getIsCPU() || !Snakes.get(i).getIsLive())
	    			continue;
	    		
		    	Coordinate testa = new Coordinate(Snakes.get(i).getSnake().get(0).x, Snakes.get(i).getSnake().get(0).y);
		    	
		    	Step step = null;
		    	Path path = finder.bestSemiPath(new UnitMover(), testa.x, testa.y , migliorMang.x, migliorMang.y);
		    	if(path != null)
		    		step = path.getStep(1);
		    	
		    	
		    	if(step != null){  	
			    	String prossimamossa = "";
			    	
			    	if(step.getX() - testa.x > 0){
			    		prossimamossa = "destra";
			    	} else if(step.getX() - testa.x < 0) {
			    		prossimamossa = "sinistra";
			    	} else if(step.getY() - testa.y > 0) {
			    		prossimamossa = "giu";
			    	} else if(step.getY() - testa.y < 0) {
			    		prossimamossa = "su";
			    	}
			    	
			    	Snakes.get(i).setProssimoMovimento(prossimamossa); 
		    	}
	    	}
		}
		
	    private Coordinate migliorMangime(){
	    	// se ce n'è uno speciale restituisco quello
	    	for(Coordinate mang:Mangimi){
	    		if(mang.Extra)
	    			return mang;
	    	}    	
	    	// altrimenti prendo il primo
			return Mangimi.get(0);
	    }
		
	}
}
