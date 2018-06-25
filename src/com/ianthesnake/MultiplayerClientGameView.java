package com.ianthesnake;

import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.utility.ClasseBindingConTextView;

/**
 * La view dove far vedere il gioco multiplayer senza le frecce di direzione
 */
public class MultiplayerClientGameView extends AbstractDrawView {
	
	// Possibili stati del gioco
    public static final int PAUSA = 0;
    public static final int PRONTO = 1;
    public static final int VAI = 2;
    public static final int PERSO = 3;
    
    private TextView Display;
    private int StatoGioco = PRONTO;
	
	public ClasseBindingConTextView punteggio;
	public ClasseBindingConTextView tempoGioco;
	private Date ultimoAggiornamento;
	
	public MultiplayerClientGameView(Context context) {
		super(context);
	}

	public MultiplayerClientGameView(Context context, AttributeSet attrs) {
		super(context, attrs); 
	}

	public MultiplayerClientGameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle); 
	}

    protected void inizializzaView() { 	
        iniziaGioco();
	}
    
	public void iniziaGioco() {
		punteggio.setValore(0);
		ultimoAggiornamento = new Date();
	}
	
    /**
     * Setto lo stato del gioco (se in corso, pronto, pausa o perso) e 
     * mostro o no il display che riporta il record e il punteggio raggiunto quando si perde 
     * @param newMode
     */
    public void setStatoGioco(int newMode) {
    	StatoGioco = newMode;
    	
        Resources res = getContext().getResources();
        CharSequence str = "";
        
        if(newMode == PRONTO){
        	str = res.getText(R.string.stato_gioco_attesa_server);
            
            Display.setText(str);
            Display.setVisibility(View.VISIBLE);
            Display.bringToFront();
        } else if (newMode == PERSO) {
        	// aggiorno record
        	salvaRecord();
        	
            str = res.getString(R.string.stato_gioco_perso_pref) + punteggio.getValore() ;
            str = str + res.getString(R.string.stato_gioco_perso_suff) + ContenitoreOpzioni.record;
            
            Display.setText(str);
            Display.setVisibility(View.VISIBLE);
            Display.bringToFront();
        } else if (newMode == PAUSA) {
        	str = res.getText(R.string.stato_gioco_pausa);
            
            Display.setText(str);
            Display.setVisibility(View.VISIBLE);
            Display.bringToFront();
        } else  if (newMode == VAI) {
        	Display.setVisibility(View.INVISIBLE);
        }
    }
    
    private void salvaRecord() {
    	if(punteggio.getValore() > ContenitoreOpzioni.record) {
    		ContenitoreOpzioni.record = punteggio.getValore();
    	}
	}
	
	/** 
	 * serve per aggiornare la view con la nuova griglia ricevuta dal server
	 */
	public void update(byte[][] nuovaGriglia){		
		// aggiorno il tempo ogni secondo
		Date adesso = new Date();
		if(adesso.getTime() - ultimoAggiornamento.getTime() >= 1000){
			tempoGioco.aggiungiValore(1);
			ultimoAggiornamento = adesso;
		}
		
		for (int x = 0; x < nuovaGriglia.length; x += 1) {
            for (int y = 0; y < nuovaGriglia[0].length; y += 1) {
                GrigliaStage [x][y] = (byte) nuovaGriglia[x][y];
            }
        }
		
		invalidate();
	}
    
	/**
	 * setta display per pausa, morto ecc, e la text view per il punteggio
	 * @param displ
	 * @param textViewPunteggio
	 * @param api
	 */
	public void setDisplay(TextView displ, TextView textViewPunteggio, TextView textViewTempoGioco, GoogleApiClient api) {
		Display = displ;
		displ.setVisibility(View.INVISIBLE);
		punteggio = new ClasseBindingConTextView(getContext().getString(R.string.punteggio), textViewPunteggio);
		tempoGioco = new ClasseBindingConTextView(getContext().getString(R.string.punteggio), textViewTempoGioco);
	}
	
}
