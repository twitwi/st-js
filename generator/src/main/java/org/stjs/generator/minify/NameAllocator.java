package org.stjs.generator.minify;

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
