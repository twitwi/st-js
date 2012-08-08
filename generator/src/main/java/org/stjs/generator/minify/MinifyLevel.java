package org.stjs.generator.minify;

public enum MinifyLevel {
	/**
	 * No minification of the generated javascript is performed. This is the most useful
	 * option if you plan to use the generated javascript in a development environment
	 * or for debugging purposes. (Size of examples with this option: 5632 Bytes)
	 */
	NONE ("none", 0),
	
	/**
	 * Most naive option. All comments non-necessary whitespace will be removed from the generated
	 * javascript code. All code depending on the generated javascript will still work
	 * as expected. (Size of examples with this option: 3995 Bytes, 71% of original size)
	 */
	WHITESPACE_AND_COMMENTS ("whitespace", 1),

	/**
	 * Same as WHITESPACE, but will also shorten the names of function parameters 
	 * and local variable. This is the safest option that still provides some level of 
	 * minification. Just as with the WHITESPACE option, all code depending on the 
	 * generated javascript should still work as expected. (Size of examples with this
	 * option: 3648 Bytes, 91% of whitespace and comments size)
	 */
	PARAMETERS_AND_LOCALS ("locals", 2),
	
	/**
	 * Same as PARAMETERS_AND_LOCALS, but will also shorten the names of all private fields
	 * and methods. This provides a smaller generated javascript. Code depending on the
	 * generated javascript will be broken iif it accesses any of these private members 
	 * directly (which it shouldn't be doing in the first place anyway). 
	 */
	PRIVATE_FIELDS_AND_METHODS("private", 3),
	
	/**
	 * All class, method, field, parameters and locals will be renamed. This generates the
	 * smallest javascript, but is also the most unsafe minification option. Code depending
	 * directly on the generated javascript is guaranteed to fail. However, STJS code 
	 * depending on a library that was generated with this option should still be generated
	 * properly and work as expected.
	 */
	FULL("full", 4);
	
	private String configOptionName;
	private int level;
	
	MinifyLevel(String name, int level){
		this.configOptionName = name;
		this.level = level;
	}
	
	public String getConfigOptionName(){
		return this.configOptionName;
	}
	
	public boolean isLessAggressiveOrEquals(MinifyLevel reference){
		return this.level <= reference.level;
	}
	
	public boolean isLessAggressiveThan(MinifyLevel reference){
		return this.level < reference.level;
	}
	
	public boolean isMoreAggressiveOrEquals(MinifyLevel reference){
		return this.level >= reference.level;
	}
}
