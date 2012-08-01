package org.stjs.generator.minify;

public interface Diamond1 {

}

interface DiamondA extends Diamond1 {
	
}

interface DiamondB extends Diamond1 {
	
}

class DiamondImpl implements DiamondA, DiamondB {
	
}
