package org.stjs.generator.minify;

public interface Canonical1 {

}

interface Canonical2 extends Canonical1 {
	
}

class CanonicalImpl implements Canonical1, Canonical2 {
	
}

class CanonicalImpl2 implements Canonical2, Canonical1 {
	
}