package org.stjs.generator.minify;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Allocates distinct, predictable, non-conflicting short names. The names are generated following a pattern similar to
 * excel column names, but since the generated names are case sensitive, both lowercase and uppercase letters are used.
 */
public class NameAllocator {

	private static final Set<String> JS_KEYWORDS = new HashSet<String>(Arrays.asList( //
			"break", "case", "catch", "continue", "default", "delete", "do", "else", //
			"finally", "for", "function", "if", "in", "instanceof", "new", "return", //
			"switch", "this", "throw", "try", "typeof", "var", "void", "while", //
			"with"));
	
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
	    
	    if(JS_KEYWORDS.contains(name)){
	    	// We must make sure that the name we return is not a JS Keyword, or that
	    	// is going to cause all sorts of trouble in the generated code.
	    	return nextName();
	    }
	    return name;
	}
}
