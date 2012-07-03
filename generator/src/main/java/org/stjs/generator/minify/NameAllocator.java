package org.stjs.generator.minify;

/**
 * Allocates distinct, predictable, non-conflicting short names. The names are generated following a pattern similar to
 * excel column names, but since the generated names are case sensitive, both lowercase and uppercase letters are used.
 */
public class NameAllocator {

	private int next = 1;
	
	public String nextName() {
	    String name = "";
	    int n = next;
	    while (n > 0) {
	        n--;
	        int mod = n % 52;
			if(mod < 26){
				name = (char)('a' + mod) + name;
			} else {
				name = (char)('A' + (mod-26)) + name;
			}
	        n /= 52;
	    }
	    next ++;
	    return name;
	}
}
