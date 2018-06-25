package com.utility;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;


/**
 * Classe di caching per i font
 * @author Ianfire
 *
 */
public class CacheFont {
	private static Hashtable<String,Typeface> fontCache = new Hashtable<String,Typeface>();
	private final static String fontPrincipale = "Volter__28Goldfish.ttf";
	
	/**
	 * metodo per prendere qualsiasi font
	 * @param name
	 * @param context
	 * @return
	 */
	public static Typeface get(String name, Context context){
		Typeface tf = fontCache.get(name);
		if(tf == null){
			try{
				tf = Typeface.createFromAsset(context.getAssets(), name);
			} catch (Exception e){
				return null;
			}
			fontCache.put(name, tf);
		}
		return tf;
	}
	
	/**
	 * metodo per prendere il font principale dell'app
	 * @param context
	 * @return
	 */
	public static Typeface getFontPrincipale(Context context){
		Typeface tf = fontCache.get(fontPrincipale);
		if(tf == null){
			try{
				tf = Typeface.createFromAsset(context.getAssets(), fontPrincipale);
			} catch (Exception e){
				return null;
			}
			fontCache.put(fontPrincipale, tf);
		}
		return tf;
	}
}
