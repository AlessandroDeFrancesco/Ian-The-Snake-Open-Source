package com.utility;

import android.widget.TextView;

public class ClasseBindingConTextView{
	private int valore = 0;
	private String testo = "";
	private TextView textView;
	
	public ClasseBindingConTextView(String testo,TextView textV){
		textView = textV;
		this.testo = testo;
	}
	
	public void setValore(int i){
		valore = i;
		textView.setText(testo + getValore());
	}

	public void aggiungiValore(int i) {
		valore += i;
		textView.setText(testo + getValore());
	}

	public int getValore(){
		return valore;
	}
};
