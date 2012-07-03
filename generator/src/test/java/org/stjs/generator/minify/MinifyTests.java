package org.stjs.generator.minify;

import static org.stjs.generator.utils.GeneratorTestHelper.generateMinified;

import org.junit.Test;

public class MinifyTests {
	@Test
	public void testMethodParameters() {
		generateMinified(Minify1.class, MinifyLevel.PARAMETERS_AND_LOCALS);
	}
}
