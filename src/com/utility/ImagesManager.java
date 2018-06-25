package com.utility;

import java.util.Hashtable;

import com.ianthesnake.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

/**
 * Classe di caching per le immagini
 * @author Ianfire
 *
 */
public class ImagesManager {
	/**
	 * costanti per l'indirizzamento delle immagini
	 */
	public static final int immagineCorpoVerdeScuro = 1;
	public static final int immagineCorpoVerdeChiaro = 2;
	public static final int immagineCorpoRosaScuro = 3;
	public static final int immagineCorpoRosaChiaro = 4;
	public static final int immagineCorpoViolaScuro = 5;
	public static final int immagineCorpoViolaChiaro = 6;
	public static final int immagineCorpoMarroneScuro = 7;
	public static final int immagineCorpoMarroneChiaro = 8;
	public static final int immagineCorpoGrigioScuro = 9;
	public static final int immagineCorpoGrigioChiaro = 10;
	public static final int immagineCorpoArancioneChiaro = 11;
	public static final int immagineCorpoArancioneScuro = 12;
	public static final int immagineCorpoBluChiaro = 13;
	public static final int immagineCorpoBluScuro = 14;
	public static final int immagineCorpoRossoChiaro = 15;
	public static final int immagineCorpoRossoScuro = 16;
	
	private static final int ultimaImmagineCorpo = 16;
	//TODO(cercare soluzione migliore) qui finiscono le immagini del corpo (metterle sempre prima degli altri)
	public static final int immagineMangime = 50;
	public static final int immagineMuro = 51;
	public static final int immagineMuroInvisibile = 52;
	public static final int immagineTesta = 53;
	public static final int immagineMangimeExtraPari = 54;
	public static final int immagineMangimeExtraDispari = 55;
	
	public static final int immagineDefault = 999;
	
	private static Hashtable<Integer, Drawable> immaginiCacheDrawable = new Hashtable<Integer, Drawable>();
	private static Hashtable<Integer, Bitmap> immaginiCacheBitmap = new Hashtable<Integer, Bitmap>();
	
	/**
	 * carica tutte le drawable in memoria
	 */
	public static void inizializzaCacheDrawable(Context context){
		immaginiCacheDrawable.put(immagineDefault, context.getResources().getDrawable(R.drawable.immagine_default));
		
		immaginiCacheDrawable.put(immagineTesta, context.getResources().getDrawable(R.drawable.testa));
		immaginiCacheDrawable.put(immagineMangime ,context.getResources().getDrawable(R.drawable.mangime));
		immaginiCacheDrawable.put(immagineMuro ,context.getResources().getDrawable(R.drawable.muro));
		immaginiCacheDrawable.put(immagineMuroInvisibile ,context.getResources().getDrawable(R.drawable.muro_invisibile));
		immaginiCacheDrawable.put(immagineMangimeExtraPari ,context.getResources().getDrawable(R.drawable.mangime_extra_pari));
		immaginiCacheDrawable.put(immagineMangimeExtraDispari ,context.getResources().getDrawable(R.drawable.mangime_extra_dispari));
		immaginiCacheDrawable.put(immagineCorpoVerdeScuro ,context.getResources().getDrawable(R.drawable.corpo_verde_scuro));
		immaginiCacheDrawable.put(immagineCorpoVerdeChiaro ,context.getResources().getDrawable(R.drawable.corpo_verde_chiaro));
		immaginiCacheDrawable.put(immagineCorpoGrigioScuro ,context.getResources().getDrawable(R.drawable.corpo_grigio_scuro));
		immaginiCacheDrawable.put(immagineCorpoGrigioChiaro ,context.getResources().getDrawable(R.drawable.corpo_grigio_chiaro));
		immaginiCacheDrawable.put(immagineCorpoRosaScuro ,context.getResources().getDrawable(R.drawable.corpo_rosa_scuro));
		immaginiCacheDrawable.put(immagineCorpoRosaChiaro ,context.getResources().getDrawable(R.drawable.corpo_rosa_chiaro));
		immaginiCacheDrawable.put(immagineCorpoViolaScuro ,context.getResources().getDrawable(R.drawable.corpo_viola_scuro));
		immaginiCacheDrawable.put(immagineCorpoViolaChiaro ,context.getResources().getDrawable(R.drawable.corpo_viola_chiaro));
		immaginiCacheDrawable.put(immagineCorpoMarroneScuro ,context.getResources().getDrawable(R.drawable.corpo_marrone_scuro));
		immaginiCacheDrawable.put(immagineCorpoMarroneChiaro ,context.getResources().getDrawable(R.drawable.corpo_marrone_chiaro));
		immaginiCacheDrawable.put(immagineCorpoArancioneChiaro ,context.getResources().getDrawable(R.drawable.corpo_arancione_chiaro));
		immaginiCacheDrawable.put(immagineCorpoArancioneScuro ,context.getResources().getDrawable(R.drawable.corpo_arancione_scuro));
		immaginiCacheDrawable.put(immagineCorpoBluChiaro ,context.getResources().getDrawable(R.drawable.corpo_blu_chiaro));
		immaginiCacheDrawable.put(immagineCorpoBluScuro ,context.getResources().getDrawable(R.drawable.corpo_blu_scuro));
		immaginiCacheDrawable.put(immagineCorpoRossoChiaro ,context.getResources().getDrawable(R.drawable.corpo_rosso_chiaro));
		immaginiCacheDrawable.put(immagineCorpoRossoScuro ,context.getResources().getDrawable(R.drawable.corpo_rosso_scuro));
	}
	
	/**
	 * carica tutte le immagini in bitmap in memoria
	 * @param lunghezza la lunghezza del bitmap
	 * @param altezza l'altezza del bitmap
	 */
	public static void inizializzaCacheBitmap(Context context, int lunghezza, int altezza){
		if(immaginiCacheDrawable.isEmpty()){
			inizializzaCacheDrawable(context);
		}
		immaginiCacheBitmap.put(immagineDefault, creaBitmap(lunghezza, altezza, getDrawable(immagineDefault)));
		
		immaginiCacheBitmap.put(immagineTesta, creaBitmap(lunghezza, altezza, getDrawable(immagineTesta)));
		immaginiCacheBitmap.put(immagineMangime ,creaBitmap(lunghezza, altezza, getDrawable(immagineMangime)));
		immaginiCacheBitmap.put(immagineMuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineMuro)));
		immaginiCacheBitmap.put(immagineMuroInvisibile ,creaBitmap(lunghezza, altezza, getDrawable(immagineMuroInvisibile)));
		immaginiCacheBitmap.put(immagineMangimeExtraPari ,creaBitmap(lunghezza, altezza, getDrawable(immagineMangimeExtraPari)));
		immaginiCacheBitmap.put(immagineMangimeExtraDispari ,creaBitmap(lunghezza, altezza, getDrawable(immagineMangimeExtraDispari)));
		immaginiCacheBitmap.put(immagineCorpoVerdeScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoVerdeScuro)));
		immaginiCacheBitmap.put(immagineCorpoVerdeChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoVerdeChiaro)));
		immaginiCacheBitmap.put(immagineCorpoGrigioScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoGrigioScuro)));
		immaginiCacheBitmap.put(immagineCorpoGrigioChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoGrigioChiaro)));
		immaginiCacheBitmap.put(immagineCorpoRosaScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoRosaScuro)));
		immaginiCacheBitmap.put(immagineCorpoRosaChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoRosaChiaro)));
		immaginiCacheBitmap.put(immagineCorpoViolaScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoViolaScuro)));
		immaginiCacheBitmap.put(immagineCorpoViolaChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoViolaChiaro)));
		immaginiCacheBitmap.put(immagineCorpoMarroneScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoMarroneScuro)));
		immaginiCacheBitmap.put(immagineCorpoMarroneChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoMarroneChiaro)));
		immaginiCacheBitmap.put(immagineCorpoArancioneChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoArancioneChiaro)));
		immaginiCacheBitmap.put(immagineCorpoArancioneScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoArancioneScuro)));
		immaginiCacheBitmap.put(immagineCorpoBluChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoBluChiaro)));
		immaginiCacheBitmap.put(immagineCorpoBluScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoBluScuro)));
		immaginiCacheBitmap.put(immagineCorpoRossoChiaro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoRossoChiaro)));
		immaginiCacheBitmap.put(immagineCorpoRossoScuro ,creaBitmap(lunghezza, altezza, getDrawable(immagineCorpoRossoScuro)));
	}

	public static Drawable getDrawable(int imm) {
		return immaginiCacheDrawable.get(imm);
	}
	
	public static Bitmap getBitmap(int imm) {
		if(immaginiCacheBitmap.get(imm) != null){
			return immaginiCacheBitmap.get(imm);
		} else {
			return immaginiCacheBitmap.get(immagineDefault);
		}
	}

	private static Bitmap creaBitmap(int lunghezza, int altezza, Drawable immagine){
        Bitmap bitmap = Bitmap.createBitmap(lunghezza, altezza, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        immagine.setBounds(0, 0, lunghezza, altezza);
        immagine.draw(canvas);
        
        return bitmap;
	}

	/**
	 * restituisce il drawable del corpo successivo a quello chiesto
	 * @param i
	 * @return
	 */
	public static int getImmagineCorpoSucc(int i) {
		i++;
		if(i > ultimaImmagineCorpo) i=1;
		
		return i;
	}

	/**
	 * restituisce il drawable del corpo precedente a quello chiesto
	 * @param i
	 * @return
	 */
	public static int getImmagineCorpoPrec(int i) {
		i--;
		if(i < 1) i=ultimaImmagineCorpo;
		
		return i;
	}
	
	/**
	 * restituisce un immagine random del corpo del serpente
	 * @return
	 */
	public static int getImmagineCorpoRandom() {
		return (int) Math.floor(Math.random() * ultimaImmagineCorpo) + 1;
	}
}
