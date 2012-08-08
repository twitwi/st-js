package org.stjs.generator.minify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConstrainedNameAllocator {

	/** 
	 * Provides the set of all possibly valid names that could potentially be assigned
	 * to a type/field/method, shortest names first.
	 */
	private final NameIterator iterator = new NameIterator();
	
	/** The set of all names that cannot be used. */
	private final Set<String> forbidden = new HashSet<String>();
	
	/** 
	 * A map of all allocations that are already known, and must be followed. This
	 * map takes precedence over the forbiden set (ie: if an allocation is defined in 
	 * this map, then it will be respected even if one or both of the mapped names are
	 * present in the forbidden set).
	 */
	private final Map<String, String> forced = new HashMap<String, String>();
	
	/** The set of non-forced names that were allocated by this ConstrainedNameAllocator. */
	private final Set<String> own = new HashSet<String>();
	
	public String allocate(String originalName){
		if(forced.containsKey(originalName)){
			return forced.get(originalName);
		}
		
		String minified;
		do {
			minified = iterator.nextName();
		}while(!forbidden.contains(minified));
		own.add(minified);
		
		return minified;
	}
}
