package com.utility;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.ianthesnake.ContenitoreOpzioni;
import com.ianthesnake.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Salvataggio {
	private static final String MY_PREFERENCES = "IanTheSnake";
	private static String keyRecord = "pref_record";
	private static String keyRecordVsCpu = "pref_record_vs_cpu";
	private static String keyVelocita = "pref_velocita";
	private static String keyLabirinto = "pref_labirinto";
	private static String keyWifi = "pref_wifi";
	private static String keyImmagineTesta = "pref_immagine_testa";
	private static String keyImmagineCorpoPari = "pref_immagine_corpo_pari";
	private static String keyImmagineCorpoDispari = "pref_immagine_corpo_dispari";
	private static String keySuonoOn = "SuonoOn";
	private static String keyLoggaGooglePlay = "LoggaGooglePlay";
	private static String keySceltaComandi = "SceltaComandi";
	
	private static Salvataggio salvataggio = new Salvataggio();
	private static SbloccaAchievementThread thread;
	
	public Salvataggio(){
		
	}

	public static void salvaImpostazioni(Context context){
		// Otteniamo il riferimento alle Preferences
		SharedPreferences prefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
		// Otteniamo il corrispondente Editor
		SharedPreferences.Editor editor = prefs.edit();
		
		// Modifichiamo il valore con quello dentro il ContenitoreOpzioni
		int record = ContenitoreOpzioni.record;
		int record_vs_cpu = ContenitoreOpzioni.record_vs_cpu;
		int velocita = ContenitoreOpzioni.velocita;
		int labirinto = ContenitoreOpzioni.labirinto;
		Boolean wifi = ContenitoreOpzioni.wifi;
		int immTesta = ContenitoreOpzioni.immTesta;
		int immCorpoPari = ContenitoreOpzioni.immCorpoPari;
		int immCorpoDispari = ContenitoreOpzioni.immCorpoDispari;
		Boolean suonoOn = ContenitoreOpzioni.suonoOn;
		Boolean loggaGooglePlay = ContenitoreOpzioni.loggaGooglePlay;
		int SceltaComandi = ContenitoreOpzioni.sceltaComandi;


		// Li salviamo nelle Preferences
		editor.putInt(keyRecord, record);
		editor.putInt(keyRecordVsCpu, record_vs_cpu);
		editor.putInt(keyVelocita, velocita);
		editor.putInt(keyLabirinto, labirinto);
		editor.putBoolean(keyWifi, wifi);
		editor.putInt(keyImmagineTesta, immTesta);
		editor.putInt(keyImmagineCorpoPari, immCorpoPari);
		editor.putInt(keyImmagineCorpoDispari, immCorpoDispari);
		editor.putBoolean(keySuonoOn, suonoOn);
		editor.putBoolean(keyLoggaGooglePlay, loggaGooglePlay);
		editor.putInt(keySceltaComandi, SceltaComandi);

		editor.commit();

	}
	
	/**
	 * salva e controlla gli achievements
	 * @param context
	 * @param apiClient
	 */
	public static void salvaImpostazioni(Context context, GoogleApiClient apiClient) {
		salvaImpostazioni(context);
		
		// se è connesso a google play salvo il record on-line e controllo gli achievements
		if(apiClient.isConnected()){
			Games.Leaderboards.submitScore(apiClient, context.getString(R.string.LEADERBOARD_ID), ContenitoreOpzioni.record);
			Games.Leaderboards.submitScore(apiClient, context.getString(R.string.LEADERBOARD_CPU_ID), ContenitoreOpzioni.record_vs_cpu);
			controllaPunteggioPerAchievements(ContenitoreOpzioni.record ,context, apiClient);
		}
	}

	public static void caricaImpostazioni(Context context){
		SharedPreferences prefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
		
		int prefRecord = prefs.getInt(keyRecord, 0);
		int prefRecordVsCpu = prefs.getInt(keyRecordVsCpu, 0);
		int prefVelocita = prefs.getInt(keyVelocita, 6);
		int prefLabirinto = prefs.getInt(keyLabirinto, 1);
		Boolean prefWifi = prefs.getBoolean(keyWifi, false);
		int prefImmTesta = prefs.getInt(keyImmagineTesta, ImagesManager.immagineTesta);
		int prefImmCorpoPari = prefs.getInt(keyImmagineCorpoPari, ImagesManager.immagineCorpoVerdeChiaro);
		int prefImmCorpoDispari = prefs.getInt(keyImmagineCorpoDispari, ImagesManager.immagineCorpoVerdeScuro);
		Boolean suonoOn = prefs.getBoolean(keySuonoOn, true);
		Boolean loggaGooglePlay = prefs.getBoolean(keyLoggaGooglePlay, false);
		int sceltaComandi = prefs.getInt(keySceltaComandi, JoystickManager.QUATTRO_FRECCE);
		
		ContenitoreOpzioni.record = prefRecord;
		ContenitoreOpzioni.record_vs_cpu = prefRecordVsCpu;
		ContenitoreOpzioni.velocita = prefVelocita;
		ContenitoreOpzioni.labirinto = prefLabirinto;
		ContenitoreOpzioni.wifi = prefWifi;
		ContenitoreOpzioni.immTesta = prefImmTesta;
		ContenitoreOpzioni.immCorpoPari = prefImmCorpoPari;
		ContenitoreOpzioni.immCorpoDispari = prefImmCorpoDispari;
		ContenitoreOpzioni.suonoOn = suonoOn;
		ContenitoreOpzioni.loggaGooglePlay = loggaGooglePlay;
		ContenitoreOpzioni.sceltaComandi = sceltaComandi;
	}

	/**
	 * qui vanno tutti i controlli per gli achievement riferiti al punteggio
	 * @param punteggio
	 * @param context
	 * @param apiClient
	 */
	public static void controllaPunteggioPerAchievements(int punteggio, Context context, GoogleApiClient apiClient){
		long startTime = System.currentTimeMillis();
		
		if(apiClient.isConnected()){
			thread = salvataggio.new SbloccaAchievementThread(context, apiClient);
			
			if(!ContenitoreOpzioni.sbloccato_achievement_100punti && punteggio >= 100){
				thread.handler.obtainMessage(SbloccaAchievementThread.SBLOCCA_ACHIEVEMENT, R.string.id_achievement_100punti).sendToTarget();
				ContenitoreOpzioni.sbloccato_achievement_100punti = true;
			}
			if(!ContenitoreOpzioni.sbloccato_achievement_500punti && punteggio >= 500){
				thread.handler.obtainMessage(SbloccaAchievementThread.SBLOCCA_ACHIEVEMENT, R.string.id_achievement_500punti).sendToTarget();
				ContenitoreOpzioni.sbloccato_achievement_500punti = true;
			}
			if(!ContenitoreOpzioni.sbloccato_achievement_1000punti && punteggio >= 1000){
				thread.handler.obtainMessage(SbloccaAchievementThread.SBLOCCA_ACHIEVEMENT, R.string.id_achievement_1000punti).sendToTarget();
				ContenitoreOpzioni.sbloccato_achievement_1000punti = true;
			}
			if(!ContenitoreOpzioni.sbloccato_achievement_2000punti && punteggio >= 2000){
				thread.handler.obtainMessage(SbloccaAchievementThread.SBLOCCA_ACHIEVEMENT, R.string.id_achievement_2000punti).sendToTarget();
				ContenitoreOpzioni.sbloccato_achievement_2000punti = true;
			}
			if(!ContenitoreOpzioni.sbloccato_achievement_3000punti && punteggio >= 3000){
				thread.handler.obtainMessage(SbloccaAchievementThread.SBLOCCA_ACHIEVEMENT, R.string.id_achievement_3000punti).sendToTarget();
				ContenitoreOpzioni.sbloccato_achievement_3000punti = true;
			}
		}
		
		long endTime = System.currentTimeMillis();
		Log.v("salvataggio","controlla punteggio per achievement took " + (endTime - startTime) + " milliseconds");
	}
	
	/**
	 * chiamare questo metodo per sbloccare un achievement non riferito al punteggio
	 * @param id_achievement l'id dell'achievement da sbloccare(R.string.achievementdasbloccare)
	 * @param context
	 * @param apiClient
	 */
	public static void sbloccaAchievements(int id_achievement, Context context, GoogleApiClient apiClient){
		long startTime = System.currentTimeMillis();
		
		if(apiClient.isConnected()){
			thread = salvataggio.new SbloccaAchievementThread(context, apiClient);
			
			thread.handler.obtainMessage(SbloccaAchievementThread.SBLOCCA_ACHIEVEMENT, id_achievement).sendToTarget();
		}
		
		long endTime = System.currentTimeMillis();
		Log.v("salvataggio","sblocca achievement "+ context.getString(id_achievement) +" took " + (endTime - startTime) + " milliseconds");
	}
	
	/**
	 * chiamare questo metodo per incrementare un achievement
	 * @param id_achievement l'id dell'achievement da incrementare(R.string.achievementdaincrementare)
	 * @param incremento
	 * @param context
	 * @param apiClient
	 */
	public static void incrementaAchievements(int id_achievement, int incremento, Context context, GoogleApiClient apiClient){
		long startTime = System.currentTimeMillis();
		
		if(apiClient.isConnected()){
			thread = salvataggio.new SbloccaAchievementThread(context, apiClient);
			
			thread.handler.obtainMessage(SbloccaAchievementThread.INCREMENTA_ACHIEVEMENT, incremento, incremento, id_achievement).sendToTarget();
		}
		
		long endTime = System.currentTimeMillis();
		Log.v("salvataggio","incrementa achievement "+ context.getString(id_achievement) +" took " + (endTime - startTime) + " milliseconds");
	}
	
	/** 
	 * thread per fare sbloccare e incrementare gli achievement da un'altro thread per non far laggare il chiamante
	 * @author Ianfire
	 *
	 */
	protected class SbloccaAchievementThread extends Thread {
		public static final int SBLOCCA_ACHIEVEMENT = 1;
		public static final int INCREMENTA_ACHIEVEMENT = 2;
		
		GoogleApiClient apiClient;
		Context context;
		
		public HandlerPerAchievement handler = new HandlerPerAchievement();
		
		/**
		 * Inviare un messaggio attraverso questo handler al thread e specificare con una delle costanti pubbliche cosa voler fare
		 * (per l'incremento inviare come oggetto l'id_achiev, e come argomento l'incremento)
		 * @author Ianfire
		 *
		 */
		private class HandlerPerAchievement extends Handler{
			@Override
			public void handleMessage(Message msg) {
				if(msg.obj != null){
					if(msg.what == SBLOCCA_ACHIEVEMENT){
						sblocca((Integer) msg.obj);
					} else if(msg.what == INCREMENTA_ACHIEVEMENT){
						incrementa((Integer) msg.obj, msg.arg1);
					}
				}
			}
		};
		
		public SbloccaAchievementThread(Context context, GoogleApiClient apiClient){
			this.context = context;
			this.apiClient = apiClient;
		}
		
		public void sblocca(int id_achievement){
			Games.Achievements.unlock(apiClient, context.getString(id_achievement));
		}
		
		public void incrementa(int id_achievement, int incremento){
			Games.Achievements.increment(apiClient, context.getString(id_achievement), incremento);
		}
		
	}
	
}