package com.ianthesnake;

import java.util.ArrayList;

import android.util.Log;

import com.utility.ImagesManager;
import com.utility.Coordinate;
import com.utility.JoystickManager;

import java.util.LinkedList;

/**
 * Classe che incorpora i metodi e gli attributi necessari per uno snake 
 * @author Ianfire
 *
 */
public class SnakeClass {	
	public static final int PERSOVITA = 0;
	public static final int MANGIMECOLPITO = 1;
	public static final int CONTINUA = 2;
	
	private ArrayList<Coordinate> Snake;
	private ArrayList<ArrayList<Coordinate>> Ostacoli;
	private ArrayList<Coordinate> MuriInvisibili;
	private ArrayList<Coordinate> Mangimi;
	
	private int LunghezzaStage, AltezzaStage;
	private Boolean isCPU;
	private Boolean isLive;

	private LinkedList<String> prossimiMovimenti;
	private String ultimoMovimento;
	private int persoVita; // 0 se no, 1 se si sta per perderla, >=2 se si è persa
	private int mangimeColpito;
	private int pos_iniziale_x, pos_iniziale_y;
	private int joystick = JoystickManager.QUATTRO_FRECCE;
	private boolean quasi_morto;
	
	/**
	 * immagini dei pezzi dello snake
	 */
	private int immCorpoPari = ImagesManager.immagineCorpoVerdeScuro;
	private int immCorpoDispari =  ImagesManager.immagineCorpoVerdeChiaro;
	private int immTesta = ImagesManager.immagineTesta;
	
	/**
	 * 
	 * @param mangimi
	 * @param muriInvisibili
	 * @param lunghezzaStage
	 * @param altezzaStage
	 * @param offset_y_pos_iniziale usato per distanziare diversi snake, con 0 viene posizionato al centro della Griglia
	 */
	public SnakeClass(ArrayList<Coordinate> mangimi, ArrayList<Coordinate> muriInvisibili, int lunghezzaStage, int altezzaStage, int offset_y_pos_iniziale){
		Snake = new ArrayList<Coordinate>();
		prossimiMovimenti = new LinkedList<String>();
		prossimiMovimenti.add("destra");
		ultimoMovimento = "destra";
		persoVita = 0;
		quasi_morto = false;
		isLive = true;
		isCPU = false;
		
		Mangimi = mangimi;
		MuriInvisibili = muriInvisibili;
		LunghezzaStage = lunghezzaStage;
		AltezzaStage = altezzaStage;
		Ostacoli = new ArrayList<ArrayList<Coordinate>>();
   	 	// setto la posizione iniziale
        pos_iniziale_x = LunghezzaStage/2;
        pos_iniziale_y = AltezzaStage/2 + offset_y_pos_iniziale;
	}
	
	/**
	 * aggiunge un ostacolo alla lista degli ostacoli che fanno morire lo snake
	 * @param ostacolo un ArrayList<Coordinate> contenente le coordinate considerate ostacoli
	 */
	public void aggiungiOstacolo(ArrayList<Coordinate> ostacolo){
		Ostacoli.add(ostacolo);
	}
	
	/**
	 * 
	 * @return una delle costanti pubbliche, a seconda di cosa è successo
	 */
    public int muoviSnake() {
		// controllo se la lista dei comandi è vuota, se si aggiungo l'ultimo comando immesso
		if(prossimiMovimenti.isEmpty()){
			prossimiMovimenti.add(ultimoMovimento);
		}
		ultimoMovimento = prossimiMovimenti.removeFirst();

		quasi_morto = false;
		
    	// bisogna necessariamente partire dalla coda perchè bisogna vedere il movPrec del pezzo precedente
    	// e aggiornare il movPrec del pezzo preso in considerazione
    	int mangimColpito = doveAndare(0);	//controllo per prima la testa per vedere se colpisce qualcosa
    	for (int i=Snake.size() - 1; i > 0; i--)
    	{
    		doveAndare(i);
    	}
    	
    	// aspetto del tempo nel caso in cui si stia per sbattere contro qualcosa
    	if(persoVita == 0) {    		
    		// assegno il prossimo movimento alla testa
    		Snake.get(0).movPrec = ultimoMovimento;
    	} else if(persoVita == 1){
    		// aspetto del tempo se si sta per perdere una vita (fa automaticamente con l'handler)
    	} else if(persoVita >= 2){
    		// vita persa
    		isLive = false;
    		return PERSOVITA;
    	}
    	
    	if(mangimColpito != -1){
    		mangimeColpito = mangimColpito;
    		return MANGIMECOLPITO;
    	}
    	
    	return CONTINUA;
	}
    
    /**
     * 
     * @return la posizione nell'array dei mangimi del mangime colpito
     */
    public int getMangimeColpito(){
    	return mangimeColpito;
    }
    
    /**
     * 
     * @return l'ArrayList<Coordinate> rappresentante lo snake
     */
    public ArrayList<Coordinate> getSnake(){
    	return Snake;
    }
    
    /**
     * 
     * @param i la posizione del pezzo dello snake nell'array
     * @return -1 se non si è colpito mangime, altrimenti la posizione del mangime colpito nell'array dei mangimi
     */
	private int doveAndare(int i){
    	int ritorno = -1;
		// per bloccare il tentativo di andare nella direzione opposta (e se il joystick è orario antiorario se ritorna true devo terminare il gioco)
		if(direzioneOpposta()){
			persoVita = 2;
			return ritorno;
		}
    	
    	// se è la testa
    	if(i == 0){
    		Coordinate testa = Snake.get(0);

    		if (ultimoMovimento == "su")
    		{
    			testa.x +=  0;
    			testa.y +=   -  1;
    		}
    		else if (ultimoMovimento == "giu")
    		{
    			testa.x +=  0;
    			testa.y +=  1;
    		}
    		else if (ultimoMovimento == "destra")
    		{
    			testa.x +=  1;
    			testa.y +=  0;
    		}
    		else if (ultimoMovimento == "sinistra")
    		{
    			testa.x +=   -  1;
    			testa.y +=  0;
    		}   
    		
    		int persoVitaPrec = persoVita;
    		// quando la testa colpisce un ostacolo
    		for(ArrayList<Coordinate> ostacolo:Ostacoli){
	    		for (Coordinate muro:ostacolo){
	    			if (Coordinate.confrontaPosizione(testa,muro))
	    			{
	    				persoVita ++;
	    				//Log.v("gioco", "colpito ostacolo");
	    			}
	    		}
    		}

    		// quando la testa colpisce un muro invisibile vado dall'altra parte
    		for (int q = 0; q < MuriInvisibili.size(); q++){
    			if (Coordinate.confrontaPosizione(testa,MuriInvisibili.get(q)))
    			{
    				vaiDallAltraParte(i);
    				//Trace.trace(getContext(),"vado dall'altra parte");
    			}
    		}    			

    		// quando la testa colpisce le altre parti del corpo (la 0 è la testa)
    		for (int j = 1; j < Snake.size(); j++)
    		{
    			if (Coordinate.confrontaPosizione(testa,Snake.get(j)))
    			{
    				persoVita ++;
    				//Log.v("gioco", "colpito corpo");
    			}
    		}
    		
    		//quando la testa colpisce il mangime
    		for (int k = 0; k < Mangimi.size(); k++)
    		{
    			if (Coordinate.confrontaPosizione(testa,Mangimi.get(k)))
    			{
    				ritorno = k;
    				//trace("colpito mangime");
    			}
    		}
    		
    		if(persoVita >= 1){
    			// se prima persoVita era maggiore 1 e adesso rimane uguale
    			// vuol dire che non si sta più andando a sbattere contro qualcosa
    			if(persoVita == persoVitaPrec) {
    				persoVita = 0;
    				quasi_morto = true;
    			} else { // altrimenti adesso si sta per perdere una vita
    				annullaMovimentoTesta();
    			}
    		}
    	}
    	else if(persoVita == 0){
    		Coordinate pezzoPrec = Snake.get(i-1);

    		if (pezzoPrec.movPrec == "su")
    		{
    			Snake.get(i).x +=  0;
    			Snake.get(i).y +=   -  1;
    			Snake.get(i).movPrec = "su";
    		}
    		else if (pezzoPrec.movPrec == "giu")
    		{
    			Snake.get(i).x +=  0;
    			Snake.get(i).y +=  1;
    			Snake.get(i).movPrec = "giu";
    		}
    		else if (pezzoPrec.movPrec == "destra")
    		{
    			Snake.get(i).x +=  1;
    			Snake.get(i).y +=  0;
    			Snake.get(i).movPrec = "destra";
    		}
    		else if (pezzoPrec.movPrec == "sinistra")
    		{
    			Snake.get(i).x +=   -  1;
    			Snake.get(i).y +=  0;
    			Snake.get(i).movPrec = "sinistra";
    		}
    		// controllo se si sta andando oltre il quadrato del livello e faccio apparire il pezzo dall'altra parte
    		vaiDallAltraParte(i);
    	}

    	return ritorno;
    }

	/**
	 * fa andare un pezzo dello snake dalla parte opposta dello stage
	 * @param i la posizione del pezzo dello snake nell'array
	 */
	private void vaiDallAltraParte(int i) {
		if(Snake.get(i).x < 1){
			Snake.get(i).x = LunghezzaStage - 2;
		}
		if(Snake.get(i).x >= LunghezzaStage - 1){
			Snake.get(i).x = 1;
		}
		if(Snake.get(i).y < 1){
			Snake.get(i).y = AltezzaStage - 2;
		}
		if(Snake.get(i).y >= AltezzaStage - 1){
			Snake.get(i).y = 1;
		}
	}

	/**
	 * usato quando si sta per perdere una vita, fa ritornare indietro la testa per bloccare momentaneamente i movimenti dello snake
	 */
	private void annullaMovimentoTesta() {
		if (ultimoMovimento == "su")
		{
			Snake.get(0).x +=  0;
			Snake.get(0).y +=  1;
		}
		else if (ultimoMovimento == "giu")
		{
			Snake.get(0).x +=  0;
			Snake.get(0).y +=   -  1;
		}
		else if (ultimoMovimento == "destra")
		{
			Snake.get(0).x +=   -  1;
			Snake.get(0).y +=  0;
		}
		else if (ultimoMovimento == "sinistra")
		{
			Snake.get(0).x +=  1;
			Snake.get(0).y +=  0;
		}
		// controllo per vedere se va sopra ai muri invisibili
		for(int h = 0; h<MuriInvisibili.size(); h++){
			if(Coordinate.confrontaPosizione(MuriInvisibili.get(h) , Snake.get(0))) {
				vaiDallAltraParte(0);
			}
		}
		
	}

	/**
	 * controlla che la nuova direzione data non sia in contrasto con la direzione dello snake
	 * @return se si è persa una vita(solo nel caso del joystick orario antiorario)
	 */
	private Boolean direzioneOpposta() {
		Boolean annulla = false;
		
		if (ultimoMovimento == "su" && Snake.get(0).movPrec == "giu")
		{
			ultimoMovimento = Snake.get(0).movPrec;
			annulla = true;
		}
		else if (ultimoMovimento == "giu" && Snake.get(0).movPrec == "su")
		{
			ultimoMovimento = Snake.get(0).movPrec;
			annulla = true;
		}
		else if (ultimoMovimento == "destra" && Snake.get(0).movPrec == "sinistra")
		{
			ultimoMovimento = Snake.get(0).movPrec;
			annulla = true;
		}
		else if (ultimoMovimento == "sinistra" && Snake.get(0).movPrec == "destra")
		{
			annulla = true;
			ultimoMovimento = Snake.get(0).movPrec;
		}	
		
		// se si sta usando il joystick orario antiorario e si è annullati la mossa vuol dire che il serpente si è arrotolato su se stesso
		// quindi deve morire
		if(joystick == JoystickManager.ORARIO_ANTIORARIO && annulla == true){
			return true;
		}
		
		return false;
	}
	
    public void aggiungiTesta() {
    	Coordinate testa = new Coordinate();
    	
    	testa.x = pos_iniziale_x;
    	testa.y = pos_iniziale_y;
    	testa.movPrec = ultimoMovimento; // il movPrec serve per indicare al pezzo precedente del corpo dove andare
    	
    	Snake.add(testa);
    }

    public void aggiungiPezzoCorpo() {
    	int numPezziCorpo = Snake.size();
    	Coordinate pezzo = new Coordinate();
    	
    	// prendo il pezzo precedente per vedere in che direzione va
    	Coordinate pezzoPrec = Snake.get(numPezziCorpo - 1);
    	
    	// controllo per vedere come attaccare il pezzo
    	if (pezzoPrec.movPrec == "su")
    	{
    		pezzo.x +=  pezzoPrec.x;
    		pezzo.y +=  pezzoPrec.y + 1;
    		pezzo.movPrec = "su";
    	}
    	else if (pezzoPrec.movPrec == "giu")
    	{
    		pezzo.x +=  pezzoPrec.x;
    		pezzo.y +=  pezzoPrec.y - 1;
    		pezzo.movPrec = "giu";
    	}
    	else if (pezzoPrec.movPrec == "destra")
    	{
    		pezzo.x +=  pezzoPrec.x - 1;
    		pezzo.y +=  pezzoPrec.y;
    		pezzo.movPrec = "destra";
    	}
    	else if (pezzoPrec.movPrec == "sinistra")
    	{
    		pezzo.x +=  pezzoPrec.x + 1;
    		pezzo.y +=  pezzoPrec.y;
    		pezzo.movPrec = "sinistra";
    	}
    	
    	Snake.add(pezzo);
    }
    
    /**
     * setta la direzione dello snake
     * @param mov la direzione dello snake(su, giu, destra, sinistra)
     */
	public void setProssimoMovimento(String mov) {
		if( mov.equals("orario") || mov.equals("antiorario")){
			joystick = JoystickManager.ORARIO_ANTIORARIO;
			mov = setProssimoMovimentoOrario( mov );
		}
		prossimiMovimenti.add(mov);
	}
	
	/**
	 * se il comando era di muoversi in senso orario o antiorario restituisco un movimento (destra, sinistra ecc..)
	 * @param mov
	 * @return
	 */
	private String setProssimoMovimentoOrario(String mov) {
		String ret = "";
		// ho bisogno di questa stringa perchè la lista dei movimenti può essere vuota se non ci sono stati movimenti ricevuti in input
		String confronto = ultimoMovimento;
		if(!prossimiMovimenti.isEmpty()){
			confronto = prossimiMovimenti.getLast();
		}
		
		if( mov.equals("orario")){
			if(confronto.equals("su")){
				ret = "destra";
			} else if(confronto.equals("destra")){
				ret = "giu";
			} else if(confronto.equals("giu")){
				ret = "sinistra";
			} else if(confronto.equals("sinistra")){
				ret = "su";
			}	
		} else if( mov.equals("antiorario")){
			if(confronto.equals("su")){
				ret = "sinistra";
			} else if(confronto.equals("destra")){
				ret = "su";
			} else if(confronto.equals("giu")){
				ret = "destra";
			} else if(confronto.equals("sinistra")){
				ret = "giu";
			}
		}
		return ret;
	}
	
	/**
	 * Passare gli interi presi dalle costanti di CacheImmagini
	 * @param testa
	 * @param corpoPari
	 * @param corpoDispari
	 */
	public void setImmagini(int testa, int corpoPari, int corpoDispari){
		// TODO immTesta = testa;
		immCorpoPari = corpoPari;
		immCorpoDispari = corpoDispari;
	}
	
	/**
	 * @return l'immagine del corpo pari
	 */
	public int getImmagineCorpoPari(){
		return immCorpoPari;
	}
	
	/**
	 * @return l'immagine del corpo dispari
	 */
	public int getImmagineCorpoDispari(){
		return immCorpoDispari;
	}
	
	/**
	 * @return l'immagine della testa
	 */
	public int getImmagineTesta(){
		return immTesta;
	}
	
	public Boolean quasiMorto(){
		return quasi_morto;
	}
	
	public Boolean getIsCPU() {
		return isCPU;
	}

	public void setIsCPU(Boolean isCPU) {
		this.isCPU = isCPU;
	}

	public boolean getIsLive() {
		return isLive;
	}
}
