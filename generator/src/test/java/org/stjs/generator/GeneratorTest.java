package org.stjs.generator;

import japa.parser.ParseException;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import test.Declaration1;

public class GeneratorTest {
	@Test
	public void testGenerator() throws ParseException, IOException {
		generate("src/test/resources/test/Declaration1.java", Declaration1.class);
	}

	private void generate(String sourceFile, Class<?> clazz) throws ParseException, IOException {

		Generator generator = new Generator();

		generator.generateJavascript(Thread.currentThread().getContextClassLoader(), clazz, new File(sourceFile),
				new File("target/x.js"));

	}

}