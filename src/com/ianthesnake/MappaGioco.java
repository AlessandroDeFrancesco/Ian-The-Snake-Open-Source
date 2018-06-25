package com.ianthesnake;

import java.util.ArrayList;

import com.utility.Coordinate;
import com.utility.pathfinder.Mover;
import com.utility.pathfinder.TileBasedMap;

public class MappaGioco implements TileBasedMap {
	
	int[][] mappa;
	private ArrayList<ArrayList<Coordinate>> Ostacoli;
	private boolean[][] visited;
	private int lunghezza, altezza;
	
	public MappaGioco(int lunghezza, int altezza){
		this.lunghezza = lunghezza;
		this.altezza = altezza;
		
		mappa = new int[lunghezza][altezza];
		visited = new boolean[lunghezza][altezza];
		Ostacoli = new ArrayList<ArrayList<Coordinate>>();
		
		for (int x = 0; x < lunghezza; x += 1) {
            for (int y = 0; y < altezza; y += 1) {
            	mappa [x][y] = 0;
            }
        }
	}

	/**
	 * aggiunge un ostacolo alla lista degli ostacoli che non possono essere passati
	 * @param ostacolo un ArrayList<Coordinate> contenente le coordinate considerate ostacoli
	 */
	public void aggiungiOstacolo(ArrayList<Coordinate> ostacolo){
		Ostacoli.add(ostacolo);
	}
	
	@Override
	public int getWidthInTiles() {
		return lunghezza;
	}

	@Override
	public int getHeightInTiles() {
		return altezza;
	}

	@Override
	public void pathFinderVisited(int x, int y) {
		visited[x][y] = true;		
	}

	@Override
	public boolean blocked(Mover mover, int x, int y) {
		Coordinate coor = new Coordinate(x, y);
		
		for(ArrayList<Coordinate> ostacolo:Ostacoli){
    		for (Coordinate muro:ostacolo){
    			if (Coordinate.confrontaPosizione(coor,muro))
    			{
    				return true;
    			}
    		}
		}
		
		return false;
	}

	@Override
	public float getCost(Mover mover, int sx, int sy, int tx, int ty) {
		return 1;
	}

}
