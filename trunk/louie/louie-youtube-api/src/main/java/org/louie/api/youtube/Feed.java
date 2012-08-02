package org.louie.api.youtube;

import java.util.List;

/**
 * This class is feed of item list.
 * 
 * @author Younggue Bae
 */
public abstract class Feed<T extends Item> {
	
	abstract public List<T> getItems();
	abstract public int getTotalItemsSize();

}