package com.utility;

/**
 * Classe con lo scopo di contenere un object e un intero indicante il suo tipo
 * da utilizzare per l'invio di messaggi tra client e server
 * @author Ianfire
 *
 */
public class OggettoInviabile {
	public int tipo;
	public Object oggetto;
	
	public OggettoInviabile(int t, Object o){
		tipo = t;
		oggetto = o;
	}
}
