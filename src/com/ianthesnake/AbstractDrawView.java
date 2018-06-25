package com.ianthesnake;

import com.utility.ImagesManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
/**
 * Classe che permette di disegnare su uno stage di forma quadrata
 * Utilizzo: Sovrascrivere inizializzaView() e caricare le immagini attraverso il metodo caricaImmagini()
 * @author Ianfire
 *
 */
public abstract class AbstractDrawView extends View{	
	// variabili per il disegno
	protected int[][] GrigliaStage;
	protected Paint mPaint = new Paint();
	protected int LunghezzaStage, AltezzaStage, XOffset, YOffset;
	protected final float GrandezzaSchermoBase = 720; // Lunghezza dello schermo del galaxy nexus, la uso come base per il calcolo della GrandezzaQuadrati
	protected final int GrandezzaQuadratiBase = 30; // La grandezza base dei quadrati in caso si usi il galaxy nexus; 
	protected int GrandezzaQuadrati; // La grandezza dei quadrati che viene ricalcolata in base alla grandezza dello schermo
	
	public AbstractDrawView(Context context) {
		super(context);
	}

	public AbstractDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AbstractDrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * metodo astratto per l'inizializzazione della view chiamato dopo che sono state calcolate le sue dimensioni
	 */
	protected abstract void inizializzaView();
	
    /**
     * Funzione che sovrascrive l'OnDraw della View e permette di disegnare le immagini specificate
     * nella GrigliaStage
     */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		for (int x = 0; x < LunghezzaStage; x += 1) {
            for (int y = 0; y < AltezzaStage; y += 1) {
                if (GrigliaStage[x][y] > 0) {
                    canvas.drawBitmap(ImagesManager.getBitmap(GrigliaStage[x][y]), 
                    		XOffset + x * GrandezzaQuadrati,
                    		YOffset + y * GrandezzaQuadrati,
                    		mPaint);
                }
            }
        }
	}

	// inizializzo da qui la View perchè viene chiamato dall'OS quando ha calcolato le dimensioni della View
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(w < h){ // per calcolare la grandezza dei quadrati prendo la dimensione più corta dello schermo
			calcolaGrandezzaQuadrati(w);
		} else {
			calcolaGrandezzaQuadrati(h);
		}
			
        int NumQuadratiX = (int) Math.floor(w / GrandezzaQuadrati);
        int NumQuadratiY = (int) Math.floor(h / GrandezzaQuadrati);
        
        if(NumQuadratiX < NumQuadratiY){
        	LunghezzaStage = AltezzaStage = NumQuadratiX;
        } else {
        	LunghezzaStage = AltezzaStage = NumQuadratiY;
        }

        XOffset = ((w - (GrandezzaQuadrati * LunghezzaStage)) / 2);
        YOffset = ((h - (GrandezzaQuadrati * AltezzaStage)) / 2);
       
    	// creo la griglia su cui posso aggiungere le immagini
        GrigliaStage = new int[LunghezzaStage][AltezzaStage];
        
        // ora che so l'altezza e la lunghezza della view posso caricare le immagini e inizializzare la View
        ImagesManager.inizializzaCacheBitmap(getContext(),GrandezzaQuadrati,GrandezzaQuadrati);
        inizializzaView();
    }
	
	/**
	 * Calcola la Grandezza del quadrato a seconda della grandezza dello schermo
	 * @param dimensione
	 */
	private void calcolaGrandezzaQuadrati(int dimensione) {
		GrandezzaQuadrati = (int) (Math.floor(dimensione / GrandezzaSchermoBase * (double) GrandezzaQuadratiBase));
	}

	/**
	 * Date delle coordinate e un'immagine la funzione le inserisce nella GrigliaStage
	 * che verrà disegnata ad ogni chiamata dell' onDraw()
	 * @param immagine
	 * @param x
	 * @param y
	 */
    protected void setImmagineInGriglia(int immagine, int x, int y){
    	GrigliaStage[x][y] = immagine;
    }
    
    protected void resettaGriglia(){
    	for (int x = 0; x < LunghezzaStage; x++) {
            for (int y = 0; y < AltezzaStage; y++) {
                setImmagineInGriglia(0, x, y);
            }
        }
    }
}
