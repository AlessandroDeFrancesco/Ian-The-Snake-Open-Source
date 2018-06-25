package com.utility;

import java.util.ArrayList;

/**
 * Interfaccia per i differenti thread tra bluetooth e wifi
 * per inviare oggetti
 * @author Ianfire
 *
 */
public interface InterfaceConnectedThread {
	public void sendObjects(ArrayList<OggettoInviabile> oggetti);
	public int getID();
}
