package org.stjs.generator.minify.full;

import static junit.framework.Assert.assertEquals;
import static org.stjs.generator.type.TypeWrappers.wrap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.stjs.generator.minify.Animal;
import org.stjs.generator.minify.MemberNameMinifier;
import org.stjs.generator.minify.MinifyLevel;
import org.stjs.generator.minify.TypeForest;
import org.stjs.generator.minify.TypeGraph;
import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.MethodWrapper;
import org.stjs.generator.type.TypeWrappers;

public class MinifyFullMembersTest {

	/** 
	 * Make sure that we pick up a clean testing environment, and leave it
	 * as clean as we found it.
	 */
	@Before
	@After
	public void clearTypeWrapperCache(){
		TypeWrappers.clearCache();
	}
	
	@Test
	public void testSingleClass(){
		// given
		TypeGraph graph = buildGraph(Animal.class);
		MemberNameMinifier minifier = new MemberNameMinifier(graph, MinifyLevel.FULL);
		
		// when
		minifier.allocate();
		
		// then
		assertMinifiedMethodName("a", Animal.class, "breathe");
	}
	
	private static TypeGraph buildGraph(Class<?>... classes){
		TypeForest forest = new TypeForest();
		for(Class<?> c : classes){
			forest.addType(wrap(c));
		}
		
		return forest.getGraph(wrap(classes[0]));
	}
	
	public void assertMinifiedMethodName(String expectedName, Class<?> clazz, String originalMethodName){
		ClassWrapper type = wrap(clazz);
		MethodWrapper method = type.findMethod(originalMethodName).getOrThrow();
		assertEquals(expectedName, method.getMinifiedName());
	}
}
