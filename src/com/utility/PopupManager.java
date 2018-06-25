package com.utility;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.ianthesnake.CPUGameActivity;
import com.ianthesnake.CPUGameView;
import com.ianthesnake.GameActivity;
import com.ianthesnake.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;

public class PopupManager {
	public static final int POPUP_CLASSIFICA = 1;
	public static final int POPUP_SCELTA_GIOCO_SINGOLO = 2;
	public static final int POPUP_NUMERO_CPU = 3;
	
	
	private static final int REQUEST_LEADERBOARD = 154;
	private static final int REQUEST_ACHIEVEMENTS = 195;
	
	private static GoogleApiClient apiClient;
	private static Context context;
	private static PopupManager _instance;
	
	private static Dialog ultimo_popup_aperto = null;
	
	private PopupManager(){
	}
	 
	/**
	 * Singleton Pattern
	 *
	 * @return ritorna la singola istanza del PopupManager
	 */
	static synchronized public PopupManager getInstance(){
	    if (_instance == null)
	      _instance = new PopupManager();
	    return _instance;
	 }
	
	/**
	 * crea Popup scelto da una delle costanti pubbliche
	 * @param id_popup
	 * @param context il contesto deve essere un'activity
	 * @param apiClient
	 */
	public void creaPopup(int id_popup, Context context, GoogleApiClient apiClient){
		switch (id_popup){
			case POPUP_CLASSIFICA:
				ultimo_popup_aperto = creaPopupClassifica(context, apiClient);
				break;
			case POPUP_SCELTA_GIOCO_SINGOLO:
				ultimo_popup_aperto = creaPopupSceltaGiocoSingolo(context);
				break;
			case POPUP_NUMERO_CPU:
				ultimo_popup_aperto = creaPopupNumeroCpu(context);
				break;
		}
	}
	
	private Dialog creaPopupSceltaGiocoSingolo(Context context) {
		PopupManager.context = context;
		
		Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.popup_scelta_gioco_singolo);
		
		Button singolo = (Button) dialog.findViewById(R.id.buttonSingolo);
		Button vsCpu = (Button) dialog.findViewById(R.id.buttonVsCpu);
		
		singolo.setOnClickListener(vaiGiocoSingolo);
		vsCpu.setOnClickListener(vaiGiocoVsCpu);
		
		dialog.show();
		return dialog;
	}


	public Dialog creaPopupClassifica(Context context, GoogleApiClient apiClient){
		PopupManager.apiClient = apiClient;
		PopupManager.context = context;
		
		Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.popup_classifica);
		
		Button classifica = (Button) dialog.findViewById(R.id.buttonClassifica);
		Button classifica_cpu = (Button) dialog.findViewById(R.id.buttonClassificaCpu);
		Button achievements = (Button) dialog.findViewById(R.id.buttonAchievements);
		
		classifica.setOnClickListener(mostraClassifica);
		classifica_cpu.setOnClickListener(mostraClassifica);
		achievements.setOnClickListener(mostraAchievements);
		
		classifica.setTypeface(CacheFont.getFontPrincipale(context));
		classifica_cpu.setTypeface(CacheFont.getFontPrincipale(context));
		achievements.setTypeface(CacheFont.getFontPrincipale(context));
		
		dialog.show();
		return dialog;
	}
	
	public Dialog creaPopupNumeroCpu(Context context){
		PopupManager.context = context;
		
		Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.popup_numero_cpu);
		
		TextView titolo = (TextView) dialog.findViewById(R.id.textViewNumeroCpu);
		Button cpu1 = (Button) dialog.findViewById(R.id.buttonUnaCpu);
		Button cpu2 = (Button) dialog.findViewById(R.id.buttonDueCpu);
		Button cpu3 = (Button) dialog.findViewById(R.id.buttonTreCpu);
		
		cpu1.setOnClickListener(cambiaNumeroCpu);
		cpu2.setOnClickListener(cambiaNumeroCpu);
		cpu3.setOnClickListener(cambiaNumeroCpu);
		
		titolo.setTypeface(CacheFont.getFontPrincipale(context));
		cpu1.setTypeface(CacheFont.getFontPrincipale(context));
		cpu2.setTypeface(CacheFont.getFontPrincipale(context));
		cpu3.setTypeface(CacheFont.getFontPrincipale(context));
		
		dialog.show();
		return dialog;
	}

	private OnClickListener mostraClassifica = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(apiClient.isConnected()){
				if(v.getId() == R.id.buttonClassifica)
					((Activity)context).startActivityForResult(Games.Leaderboards.getLeaderboardIntent(apiClient, context.getString(R.string.LEADERBOARD_ID)), REQUEST_LEADERBOARD);
				else if(v.getId() == R.id.buttonClassificaCpu)
					((Activity)context).startActivityForResult(Games.Leaderboards.getLeaderboardIntent(apiClient, context.getString(R.string.LEADERBOARD_CPU_ID)), REQUEST_LEADERBOARD);
			} else {
				Trace.trace(context, context.getString(R.string.nessuna_connessione_google_play));
			}
		}
	};
	
	private OnClickListener mostraAchievements = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(apiClient.isConnected()){
				((Activity)context).startActivityForResult(Games.Achievements.getAchievementsIntent(apiClient), REQUEST_ACHIEVEMENTS);
			} else {
				Trace.trace(context, context.getString(R.string.nessuna_connessione_google_play));
			}
		}
	};
	
	private OnClickListener vaiGiocoSingolo = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			((Activity)context).startActivity(new Intent( v.getContext(), GameActivity.class));
		}
	};
	
	private OnClickListener vaiGiocoVsCpu = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			((Activity)context).startActivity(new Intent( v.getContext(), CPUGameActivity.class));
		}
	};
	
	private OnClickListener cambiaNumeroCpu = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(v.getId() == R.id.buttonUnaCpu){
				CPUGameView.setNumeroCpu(1);
			} else if(v.getId() == R.id.buttonDueCpu){
				CPUGameView.setNumeroCpu(2);
			} else if(v.getId() == R.id.buttonTreCpu){
				CPUGameView.setNumeroCpu(3);
			}
			
			if(ultimo_popup_aperto != null)
				ultimo_popup_aperto.dismiss();
		}
	};

}
