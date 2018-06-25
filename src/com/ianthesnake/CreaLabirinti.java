package com.ianthesnake;

import java.util.ArrayList;

import com.utility.Coordinate;

public class CreaLabirinti {
	
	/**
	 * passare i muri che si vogliono creare in modo standard (normali o invisibili)
	 * @param Muri
	 * @param dimensioniStage
	 */
	
	private static void creaLabirintoStandard(ArrayList<Coordinate> Muri, int[] dimensioniStage){
		int LunghezzaStage = dimensioniStage[0];
		int AltezzaStage = dimensioniStage[1];
		
    	for (int x = 0; x < LunghezzaStage; x++) {
        	Muri.add(new Coordinate(x, 0));
        	Muri.add(new Coordinate(x, AltezzaStage - 1));
        }
        for (int y = 1; y < AltezzaStage - 1; y++) {
        	Muri.add(new Coordinate(0, y));
        	Muri.add(new Coordinate(LunghezzaStage - 1, y));
        }
	}
	
	
	public static void creaLabirinto1(ArrayList<Coordinate> Muri, ArrayList<Coordinate> MuriInvisibili, int[] dimensioniStage){
		creaLabirintoStandard( Muri,  dimensioniStage);
    }
	
	
	public static void creaLabirinto2(ArrayList<Coordinate> Muri, ArrayList<Coordinate> MuriInvisibili, int[] dimensioniStage){
		creaLabirintoStandard( MuriInvisibili,  dimensioniStage);
    }
	
	public static void creaLabirinto3(ArrayList<Coordinate> Muri, ArrayList<Coordinate> MuriInvisibili, int[] dimensioniStage){
		int LunghezzaStage = dimensioniStage[0];
		int AltezzaStage = dimensioniStage[1];
		
		Boolean bordiInvisibiliVerticali;
		Boolean bordiInvisibiliOrizzontali;
	
		// creazione bordi verticali
		for(int i = 0; i < AltezzaStage ; i++) {
			bordiInvisibiliVerticali = i > AltezzaStage/4 && i < AltezzaStage - AltezzaStage/4;
			
			Coordinate muro = new Coordinate();
			Coordinate muro1 = new Coordinate();
			//muro sinistro
			muro.y = i;
			//muro destro
			muro1.y = i;
			muro1.x = LunghezzaStage - 1; 
			
			if(bordiInvisibiliVerticali){
				MuriInvisibili.add(muro);
				MuriInvisibili.add(muro1);
			} else {
				Muri.add(muro);
				Muri.add(muro1);
			}
		}
		
		// creazione bordi orizzontali
		for(int j = 1; j < LunghezzaStage - 1 ; j++) {
			bordiInvisibiliOrizzontali = j > LunghezzaStage/4 && j < LunghezzaStage - LunghezzaStage/4;
			
			Coordinate muro = new Coordinate();
			Coordinate muro1 = new Coordinate();
			//muro alto
			muro.x = j;
			//muro basso
			muro1.x = j;
			muro1.y = AltezzaStage - 1;
			
			if(bordiInvisibiliOrizzontali){
				MuriInvisibili.add(muro);
				MuriInvisibili.add(muro1);
			} else {
				Muri.add(muro);
				Muri.add(muro1);
			}
		}
    }
	
	public static void creaLabirinto4(ArrayList<Coordinate> Muri, ArrayList<Coordinate> MuriInvisibili, int[] dimensioniStage){
		int LunghezzaStage = dimensioniStage[0];
		int AltezzaStage = dimensioniStage[1];
		
		creaLabirintoStandard(Muri, dimensioniStage);
		
		Coordinate muro;
		Coordinate muro1;
		
		int offsetDaiBordi = 5;
		
		for(int j = offsetDaiBordi; j < AltezzaStage - offsetDaiBordi ; j ++) {
			muro = new Coordinate();
			muro1 = new Coordinate();
			
			//muro sinistro piccolo
			muro.x = offsetDaiBordi;
			muro.y = j;
			//muro destro piccolo
			muro1.x = LunghezzaStage - offsetDaiBordi;
			muro1.y = j;
			
			Muri.add(muro);
			Muri.add(muro1);
		}
    }
	
}
