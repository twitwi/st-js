package org.stjs.generator.minify;

import static org.stjs.generator.type.TypeWrappers.wrap;

import java.io.File;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.stjs.generator.type.TypeWrappers;

public class TypeForestBuilderTest {
	
	@Test
	public void testBuildGraph() throws ClassNotFoundException{
		// given
		TypeForestBuilder.ClassFileFilter filter = new TypeForestBuilder.ClassFileFilter(){
			@Override
			public boolean accept(File classFile, File classpathRoot,
					String pathRelativeToRoot, String packageName) {
				return "org.stjs.generator.minify".equals(packageName);
			}
		};
		TypeForestBuilder builder = new TypeForestBuilder(new File("target/test-classes/"), filter);
		
		// when
		TypeForest forest = builder.buildGraph();
		
		// Then
		TypeGraph graph = forest.getGraph(wrap(Animal.class));
		Assert.assertEquals(11, graph.getNodes().size());
	}

}
