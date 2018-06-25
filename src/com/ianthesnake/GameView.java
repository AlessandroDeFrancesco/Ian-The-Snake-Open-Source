package com.ianthesnake;

import java.util.ArrayList;
import java.util.Date;

import com.google.android.gms.common.api.GoogleApiClient;
import com.utility.ImagesManager;
import com.utility.ClasseBindingConTextView;
import com.utility.Coordinate;
import com.utility.Salvataggio;
import com.utility.SoundManager;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * La view dove far vedere il gioco senza le frecce di direzione
 */
public class GameView extends AbstractDrawView {
	
	// Possibili stati del gioco
    public static final int PAUSA = 0;
    public static final int PRONTO = 1;
    public static final int VAI = 2;
    public static final int PERSO = 3;
	
    private Context context;
    
    private int StatoGioco;
    private TextView Display;
	
	public ClasseBindingConTextView punteggio;
	public ClasseBindingConTextView tempoGioco;
    private RefreshHandler HandlerRidisegna = new RefreshHandler();
	private static int RitardoMovimenti = 500;
	private int labirinto;
    private int velocita;
	
	private SnakeClass Snake;
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
	
	public GameView(Context cntxt) {
		super(cntxt);
		
		context = cntxt;
	}

	public GameView(Context cntxt, AttributeSet attrs) {
		super(cntxt, attrs); 
		
		context = cntxt;
	}

	public GameView(Context cntxt, AttributeSet attrs, int defStyle) {
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
        
		// creo lo snake
		Snake = new SnakeClass(Mangimi, MuriInvisibili, LunghezzaStage, AltezzaStage, 0);
		// passo gli ostacoli allo snake
		Snake.aggiungiOstacolo(Muri);
        // aggiungo lo snake iniziale
        Snake.aggiungiTesta();
        Snake.aggiungiPezzoCorpo();
        Snake.aggiungiPezzoCorpo();
        Snake.aggiungiPezzoCorpo();
        // setto le immagini del corpo dello snake
        Snake.setImmagini(ContenitoreOpzioni.immTesta, ContenitoreOpzioni.immCorpoPari, ContenitoreOpzioni.immCorpoDispari);
        // aggiungo mangime
     	aggiungiMangime(false);
     	// setto punteggio a 0
     	punteggio.setValore(0);
     	// refresho la View
        tempoGioco.setValore(0);
     	ultimoAggiornamento = new Date();
        update();
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
        	// mostro punteggio attuale e record
            str = res.getString(R.string.stato_gioco_perso_pref) + punteggio.getValore() ;
            str = str + res.getString(R.string.stato_gioco_perso_suff) + ContenitoreOpzioni.record;
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
            GameView.this.update();
            GameView.this.invalidate();
			long endTime = System.currentTimeMillis();
			Log.v("gioco","Aggiornamento took " + (endTime - startTime) + " milliseconds");
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
    
    /**
     * aggiorna lo stato degli achievements
     */
    private void updateAchievements() {
    	// incremento i cibi presi
    	if(mangimiNormaliPresi - mangimiNormaliInviati == 3){
    		Salvataggio.incrementaAchievements(R.string.id_achievement_10cibi, 3, context, apiClient);
    		Salvataggio.incrementaAchievements(R.string.id_achievement_50cibi, 3, context, apiClient);
    		Salvataggio.incrementaAchievements(R.string.id_achievement_100cibi, 3, context, apiClient);
    		Salvataggio.incrementaAchievements(R.string.id_achievement_250cibi, 3, context, apiClient);
    		Salvataggio.incrementaAchievements(R.string.id_achievement_500cibi, 3, context, apiClient);
    		Salvataggio.incrementaAchievements(R.string.id_achievement_1000cibi, 3, context, apiClient);
    		Salvataggio.incrementaAchievements(R.string.id_achievement_5000cibi, 3, context, apiClient);
    		mangimiNormaliInviati = mangimiNormaliPresi;
    	}
    	
    	// sblocco achiev punteggio
    	Salvataggio.controllaPunteggioPerAchievements(punteggio.getValore(), context, apiClient);
    	
    	// incremento le quasi morti
    	if(Snake.quasiMorto()){
	    	Salvataggio.sbloccaAchievements(R.string.id_achievement_1quasimorto, context, apiClient);
	    	Salvataggio.incrementaAchievements(R.string.id_achievement_20quasimorto, 1, context, apiClient);
	    	Salvataggio.incrementaAchievements(R.string.id_achievement_100quasimorto, 1, context, apiClient);
    	}
    	
    	// sblocco le combo dei cibi speciali
    	if(comboSpecialiPresi == 1 && !ContenitoreOpzioni.sbloccato_achievement_combo_speciali1){
    		Salvataggio.sbloccaAchievements(R.string.id_achievement_1cibospeciale, context, apiClient);
    		ContenitoreOpzioni.sbloccato_achievement_combo_speciali1 = true;
    	} else if(comboSpecialiPresi == 7 && !ContenitoreOpzioni.sbloccato_achievement_combo_speciali7){
    		Salvataggio.sbloccaAchievements(R.string.id_achievement_7cibospeciale, context, apiClient);
    		ContenitoreOpzioni.sbloccato_achievement_combo_speciali7 = true;
    	} else if(comboSpecialiPresi == 15 && !ContenitoreOpzioni.sbloccato_achievement_combo_speciali15){
    		Salvataggio.sbloccaAchievements(R.string.id_achievement_15cibospeciale, context, apiClient);
    		ContenitoreOpzioni.sbloccato_achievement_combo_speciali15 = true;
    	}
    	
    	// sblocco l'achiev di sopravvivenza se è sopravvisuto 120 secondi e il punteggio è superiore a 500
    	if(tempoGioco.getValore() >= 120 && punteggio.getValore() >= 500){
    		if(labirinto == 1 && !ContenitoreOpzioni.sbloccato_achievement_sopravvivenza1){
    			Salvataggio.sbloccaAchievements(R.string.id_achievement_sopravvivenza1, context, apiClient);
    			ContenitoreOpzioni.sbloccato_achievement_sopravvivenza1 = true;
    		}
    		if(labirinto == 2 && !ContenitoreOpzioni.sbloccato_achievement_sopravvivenza2){
    			Salvataggio.sbloccaAchievements(R.string.id_achievement_sopravvivenza2, context, apiClient);
    			ContenitoreOpzioni.sbloccato_achievement_sopravvivenza2 = true;
    		}
    		if(labirinto == 3 && !ContenitoreOpzioni.sbloccato_achievement_sopravvivenza3){
    			Salvataggio.sbloccaAchievements(R.string.id_achievement_sopravvivenza3, context, apiClient);
    			ContenitoreOpzioni.sbloccato_achievement_sopravvivenza3 = true;
    		}
    		if(labirinto == 4 && !ContenitoreOpzioni.sbloccato_achievement_sopravvivenza4){
    			Salvataggio.sbloccaAchievements(R.string.id_achievement_sopravvivenza4, context, apiClient);
    			ContenitoreOpzioni.sbloccato_achievement_sopravvivenza4 = true;
    		}
    	}
	}

	private void mangimeColpito(int i) {    	
    	if(Mangimi.get(i).Extra ){
    		// se il mangime preso è extra
    		mangimiSpecialiPresi++;
    		comboSpecialiPresi++;
    		punteggio.aggiungiValore(velocita * ((MangimeExtra) Mangimi.get(i)).getMoltiplicatore());
    		// riproduce suono
    		if(ContenitoreOpzioni.suonoOn)
    			SoundManager.getInstance(getContext()).playSound(SoundManager.MANGIME_EXTRA_PRESO);
    	} else { 
    		punteggio.aggiungiValore(velocita);
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

	private void perditaVita() {
		setStatoGioco(PERSO);
		
		// achievement
		Salvataggio.incrementaAchievements(R.string.id_achievement_10morti, 1, context, apiClient);
		Salvataggio.incrementaAchievements(R.string.id_achievement_25morti, 1, context, apiClient);
		Salvataggio.incrementaAchievements(R.string.id_achievement_100morti, 1, context, apiClient);
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
    	
    	// controllo che non vada sui muri, su snake o sugli altri mangimi
    	for(Coordinate pezzo:Snake.getSnake()){
    		nonAggiungere = Coordinate.confrontaPosizione(pezzo, mang);
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
		if(StatoGioco == VAI){
			Snake.setProssimoMovimento(mov);
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
		punteggio = new ClasseBindingConTextView(context.getString(R.string.punteggio), textViewPunteggio);
		tempoGioco = new ClasseBindingConTextView(context.getString(R.string.tempo_gioco), textViewTempoGioco);
	}
}
