package com.utility;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ianthesnake.R;

public class SoundManager {
	/**
	 * Costanti pubbliche per l'indirizzamento dei suoni
	 */
	public static final int SUONO_VUOTO = 1;
	public static final int MANGIME_PRESO = 2;
	public static final int MANGIME_EXTRA_PRESO = 3;
	public static final int MORTE_SNAKE = 4;
		 
	private static SoundManager _instance;
	private static SoundPool mSoundPool;
	private static HashMap<Integer, Integer> mSoundPoolMap;
	private static AudioManager  mAudioManager;
	private static Context mContext;
	
	private static PlayThread playthread;
	 
	private SoundManager(Context theContext)
	{
		 mContext = theContext;
	     mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
	     mSoundPoolMap = new HashMap<Integer, Integer>();
	     mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
	     playthread = new PlayThread();
	     
		initSounds(theContext);
		// workaround per il primo suono che non si sente
		playSound(SUONO_VUOTO);
	}
	 
		/**
		 * Singleton Pattern
		 *
		 * @return ritorna la singola istanza del SoundManager
		 */
		static synchronized public SoundManager getInstance(Context theContext)
		{
		    if (_instance == null)
		      _instance = new SoundManager(theContext);
		    return _instance;
		 }
	 
		/**
		 * Carica i suoni in memoria
		 *
		 * @param theContext
		 */
		private static  void initSounds(Context theContext)
		{
			mSoundPoolMap.put(MANGIME_PRESO, mSoundPool.load(mContext, R.raw.mangime_preso, 1));
			mSoundPoolMap.put(MANGIME_EXTRA_PRESO, mSoundPool.load(mContext, R.raw.mangime_extra_preso, 1));
			mSoundPoolMap.put(SUONO_VUOTO, mSoundPool.load(mContext, R.raw.suono_vuoto, 1));
			mSoundPoolMap.put(MORTE_SNAKE, mSoundPool.load(mContext, R.raw.morte_snake, 1));
		} 
	 
		/**
		 * Riproduce un suono
		 *
		 * @param una delle costanti pubbliche dei suoni
		 */
		public void playSound(int index)
		{
			playthread.handler.obtainMessage(1, index).sendToTarget();
		}
	 
		/**
		 * Ferma un suono specifico
		 * @param una delle costanti pubbliche dei suoni che deve essere stoppata
		 */
		public void stopSound(int index)
		{
			mSoundPool.stop(mSoundPoolMap.get(index));
		}
	 
		/**
		 * Rilascia risorse
		 */
		public static void cleanup()
		{
			mSoundPool.release();
			mSoundPool = null;
		    mSoundPoolMap.clear();
		    mAudioManager.unloadSoundEffects();
		    _instance = null;
		}
	 
		
		/** 
		 * thread per fare il play sound da un'altro thread per non far laggare il chiamante del play sound
		 * @author Ianfire
		 *
		 */
		protected class PlayThread extends Thread {

			public HandlerPlaySound handler = new HandlerPlaySound();
					
			private class HandlerPlaySound extends Handler{
				@Override
				public void handleMessage(Message msg) {
					if(msg.obj != null){
						play((Integer) msg.obj);
					}
				}
			};
			
			public PlayThread(){
				
			}
			
			public void play(int index){
				float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);			
				mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, 0, 1);
			}
			
		}
		
	}