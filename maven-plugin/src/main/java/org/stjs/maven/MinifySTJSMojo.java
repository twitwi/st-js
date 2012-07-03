package org.stjs.maven;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
import org.stjs.generator.GenerationDirectory;
import org.stjs.generator.Generator;
import org.stjs.generator.GeneratorConfiguration;
import org.stjs.generator.GeneratorConfigurationBuilder;

/**
 * 
 * @goal minify
 * @phase prepare-package
 * @requiresDependencyResolution compile
 * 
 * @author lordofthepigs
 */
public class MinifySTJSMojo extends MainSTJSMojo {

	
	
	@Override
	protected GeneratorConfigurationBuilder getGeneratorConfiguration()
			throws MojoExecutionException {
		return super.getGeneratorConfiguration().minified(true).generateSourceMap(false);
	}

	@Override
	protected boolean processFile(File source, File sourceDir, File absoluteTarget, GenerationDirectory gendir, 
			Generator generator, GeneratorConfiguration config, ClassLoader builtProjectClassLoader){
		
		System.out.println("Minifying " + source + "...");
		return super.processFile(source, sourceDir, absoluteTarget, gendir, generator, config, builtProjectClassLoader);
	}

	/**
	 * Makes sure that we include all the source files in the process, and that we regenerate everything cleanly. This is
	 * required because changed to the way one class is minified can affect many other classes. 
	 */
	protected SourceInclusionScanner getSourceInclusionScanner(int staleMillis) {
		if (includes.isEmpty()) {
			includes.add("**/*.java");
		}
		return new SimpleSourceInclusionScanner(includes, excludes);
	}
}
