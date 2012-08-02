package org.stjs.generator.minify;

import java.io.File;
import java.io.FileFilter;

import org.stjs.generator.type.TypeWrappers;

/**
 * Provides a convenient way to build an exhaustive TypeForest from a collection of .class files. Starting from
 * a specified directory, the TypeForestBuilder will find all .class files in that directory and its subdirectories
 * recursively, load the corresponding class and add it to the TypeForest. Exactly which subset of classes need to
 * be added to the TypeForest can be controlled by constructing the TypeForestBuilder with a 
 * TypeForestBuilder.ClassFileFilter.
 * @author lordofthepigs
 */
public class TypeForestBuilder {
	
	private static final ClassFileFilter ACCEPT_ALL_FILTER = new ClassFileFilter(){
		@Override
		public boolean accept(File classFile, File classpathRoot, String pathRelativeToRoot, String packageName) {
			return true;
		}
	};
	
	private static final FileFilter CLASS_AND_FOLDERS = new FileFilter(){
		@Override
		public boolean accept(File f) {
			return f.isDirectory() || f.getName().endsWith(".class");
		}
	};
	
	private ClassFileFilter filter;
	private File classpathRoot;
	
	public TypeForestBuilder(File classpathRoot, ClassFileFilter filter){
		this.filter = filter;
		this.classpathRoot = classpathRoot;
	}
	
	public TypeForestBuilder(File classpathRoot){
		this(classpathRoot, ACCEPT_ALL_FILTER);
	}

	public TypeForest buildGraph() throws ClassNotFoundException {
		TypeForest forest = new TypeForest();
		doBuildGraph(forest, classpathRoot, "", "");
		return forest;
	}
	
	private void doBuildGraph(TypeForest forest, File folder, String pathRelativeToRoot, String packageName)  throws ClassNotFoundException {
		File[] files = folder.listFiles(CLASS_AND_FOLDERS);
		
		for(File file : files){
			if(file.isDirectory()){
				// directory keep scanning recursively
				String path = pathRelativeToRoot + file.getName() + "/";
				String pack = (packageName.isEmpty() ? "" : packageName + ".") + file.getName();
				doBuildGraph(forest, file, path, pack);
				
			} else if(filter.accept(file, classpathRoot, pathRelativeToRoot, packageName)) {
				// acceptable class file
				String classSimpleName = file.getName().substring(0, file.getName().length() - ".class".length());
				Class<?> clazz = Class.forName(packageName + "." + classSimpleName);
				forest.addType(TypeWrappers.wrap(clazz));
			}
		}
	}
	
	public interface ClassFileFilter {
		/** 
		 * Determines if the Class contained within the specified class file must be added to the TypeForest.
		 * 
		 * @param classFile The .class file
		 * @param classpathRoot The root folder from which the relative path is calculated
		 * @param pathRelativeToRoot The '/' delimited path to the directory containing the .class file, relative to classPathRoot
		 * @param packageName The package in which the Class is located.
		 * @return true if the file must be added to the TypeForest, false if not
		 */
		public boolean accept(File classFile, File classpathRoot, String pathRelativeToRoot, String packageName);
	}
}
