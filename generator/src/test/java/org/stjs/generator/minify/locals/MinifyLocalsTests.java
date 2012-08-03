package org.stjs.generator.minify.locals;

import static org.stjs.generator.minify.MinifyLevel.PARAMETERS_AND_LOCALS;
import static org.stjs.generator.utils.GeneratorTestHelper.assertMinifiedCodeContains;

import org.junit.Test;

public class MinifyLocalsTests {
	@Test
	public void testMethodParametersAndLocals() {
		assertMinifiedCodeContains(Minify1.class, PARAMETERS_AND_LOCALS, "Minify1.prototype.average=function(a, b){var c = a+b;");
	}

	@Test
	public void testTryCatch() {
		assertMinifiedCodeContains(Minify2.class, PARAMETERS_AND_LOCALS, "catch(c){var d = c.toString();}");
	}

	@Test
	public void testForEach() {
		assertMinifiedCodeContains(Minify3.class, PARAMETERS_AND_LOCALS, "for(var b in a){if(!(a).hasOwnProperty(b)) continue;");
	}

	@Test
	public void testInnerClassMethod() {
		assertMinifiedCodeContains(Minify4.class, PARAMETERS_AND_LOCALS, "Minify4.Builder.prototype.build=function(a,b){return a+b;}");
	}

	@Test
	public void testAnonymousClassMethod() {
		assertMinifiedCodeContains(Minify5.class, PARAMETERS_AND_LOCALS, "_InlineType.prototype.handle=function(a, b){var c = a + \" : \" + b;}");
	}
}
