package com.ianthesnake;

public class MangimeExtra extends com.utility.Coordinate{
	
	private int vitaRimanente;
	private final int divisore = 5;
	
	public MangimeExtra(){
		vitaRimanente = 35;
		Extra = true;
	}
	
	public boolean isMorto(){
		return (vitaRimanente == 0);
	}
	
	public int getMoltiplicatore(){
		return (int) Math.floor(vitaRimanente/divisore) + 1;
	}
	
	public int getVita(){
		return vitaRimanente;
	}
	
	public void diminuisciVita(){
		vitaRimanente--;
	}
}
