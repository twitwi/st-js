package org.stjs.generator.variable;

import org.stjs.generator.type.TypeWrapper;

public abstract class AbstractVariable implements Variable {

	protected final TypeWrapper type;
	protected final String name;
	
	// Cannot be final, because the name minification process can only be done after all variables have
	// already been created and resolved.
	protected String minifiedName;

	public AbstractVariable(TypeWrapper type, String name) {
		this.type = type;
		this.name = name;
	}

	public TypeWrapper getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getMinifiedName() {
		return minifiedName;
	}

	public void setMinifiedName(String minifiedName) {
		this.minifiedName = minifiedName;
	}
}
