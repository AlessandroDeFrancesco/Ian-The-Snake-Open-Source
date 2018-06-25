package com.utility;

import java.util.ArrayList;

public class Nomi {
	private static ArrayList<String> nomi = new ArrayList<String>();
	
	private static void inizializza(){
		nomi = new ArrayList<String>();
		
		nomi.add("Antonidas");
		nomi.add("Azshara");
		nomi.add("Vashj");
		nomi.add("Garrosh");
		nomi.add("Gul'dan");
		nomi.add("Jaina");
		nomi.add("Illidan");
		nomi.add("Kael'thas");
		nomi.add("Kel'Thuzad");
		nomi.add("Kil'jaeden");
		nomi.add("Maiev");
		nomi.add("Malfurion");
		nomi.add("Medivh");
		nomi.add("Lich");
		nomi.add("Sargeras");
		nomi.add("Sylvanas");
		nomi.add("Thrall");
		nomi.add("Tirion");
		nomi.add("Tyrande");
		nomi.add("Uther");
		nomi.add("Vol'jin");
		nomi.add("Zul'jin");
	}
	
	public static String getNomeRandom(){
		if(nomi == null || nomi.isEmpty()){
			inizializza();
		}
		
		int rand = (int) (Math.random()*nomi.size());
		
		return nomi.get(rand);
	}
}
