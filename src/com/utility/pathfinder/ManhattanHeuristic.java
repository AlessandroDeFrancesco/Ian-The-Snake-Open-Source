package com.utility.pathfinder;
/**
 * A heuristic that uses the tile that is closest to the target
 * as the next best tile.
 * 
 * @author Alessandro De Francesco
 */
public class ManhattanHeuristic implements AStarHeuristic {
	/**
	 * @see AStarHeuristic#getCost(TileBasedMap, Mover, int, int, int, int)
	 */
	public float getCost(TileBasedMap map, Mover mover, int x, int y, int tx, int ty) {		
		float result = (float) Math.abs(x-tx) + Math.abs(y-ty);

		return result;
	}
	
}