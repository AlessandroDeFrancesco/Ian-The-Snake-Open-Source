package com.ianthesnake;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;

import com.utility.ImagesManager;
import com.utility.Coordinate;

public class MazeView extends AbstractDrawView{	
	
	public final static int NUMERO_LABIRINTI = 4;
	private ArrayList<Coordinate> Muri;
	private ArrayList<Coordinate> MuriInvisibili;
	
	private int labirinto = ContenitoreOpzioni.labirinto;

	public MazeView(Context context) {
		super(context);
	}

	public MazeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MazeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void inizializzaView() {
		Muri = new ArrayList<Coordinate>();
		MuriInvisibili = new ArrayList<Coordinate>();
		
		creaLabirinto();
	}
	
    public void creaLabirinto(){
    	//trace("creazione labirinto " + labirinto)
    	// eliminazione di qualsiasi altro labirinto
    	Muri = new ArrayList<Coordinate>();
    	MuriInvisibili = new ArrayList<Coordinate>();
    	resettaGriglia();
    	
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
    	
    	updateMuri();
    	this.invalidate();
    }
    
    private void updateMuri() {
    	for(Coordinate mang:Muri){
    		setImmagineInGriglia(ImagesManager.immagineMuro, mang.x, mang.y);
    	}
    	for(Coordinate mang:MuriInvisibili){
    		setImmagineInGriglia(ImagesManager.immagineMuroInvisibile, mang.x, mang.y);
    	}
    }
    
	public int getLabirinto() {
		return labirinto;
	}

	public void aumentaLabirinto() {
		if(labirinto <4){
			labirinto ++;
		}
	}
	
	public void diminuisciLabirinto() {
		if(labirinto >1){
			labirinto --;
		}	
	}
    
}
