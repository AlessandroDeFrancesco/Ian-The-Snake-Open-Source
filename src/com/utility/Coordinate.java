package com.utility;

public class Coordinate extends Object{
	public int x;
	public int y;
	
	public String movPrec;
	public Boolean Extra;
	
	public Coordinate(){
		Extra = false;
		x = y = 0;
	}
	
	public Coordinate(int X, int Y){
		x = X;
		y = Y;
		Extra = false;
	}
	
	public String toString(){
		return "x = " + x + "; y = " + y;
	}
	

	public static boolean confrontaPosizione(Coordinate a, Coordinate b) {
		if (a.x == b.x && a.y == b.y)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
