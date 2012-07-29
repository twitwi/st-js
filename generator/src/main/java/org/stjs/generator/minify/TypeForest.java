package org.stjs.generator.minify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.TypeWrapper;

/**
 * A collection of disconnected TypeGraph. Each type added to this TypeForest exists within
 * one and only one TypeGraph.<br>
 * <br>
 * We expect each TypeGraph to contain few items (since they only represent types linked 
 * through inheritance), but to have a lot of independent TypeGraph (most types are completely 
 * unrelated).
 */
public class TypeForest {

	/**
	 * Allows fast retrieval of the TypeGraph that contains any TypeWrapper. 
	 */
	private Map<ClassWrapper, TypeGraph> index = new HashMap<ClassWrapper, TypeGraph>();
	
	public void addType(ClassWrapper type){
		if(type == null){
			throw new IllegalArgumentException("Cannot add a null type");
		}
		
		doAddType(type);
	}
	
	public boolean doAddType(ClassWrapper type){
		if(type == null || type.getClazz() == Object.class){
			// we are already at the root of the inheritance hierarchy,stop the recursion
			return false;
		}
		if(index.get(type) != null){
			return true;
		}
		
		// Make sure the graphs for all the supertypes are already properly constructed
		boolean hasSuperTypes = false;
		hasSuperTypes |= doAddType((ClassWrapper)type.getSuperClass());
		for(TypeWrapper iface : type.getInterfaces()){
			hasSuperTypes |= doAddType((ClassWrapper)iface);
		}
		
		if(!hasSuperTypes){
			TypeGraph graph = new TypeGraph(type);
			this.index.put(type, graph);
			
		} else {
			// add the current type to the graphs of all its super types
			Set<TypeGraph> connectedGraphs = new HashSet<TypeGraph>();
			addToGraphIfNecessary(type, (ClassWrapper)type.getSuperClass(), connectedGraphs);
			for(TypeWrapper iface : type.getInterfaces()){
				addToGraphIfNecessary(type, (ClassWrapper)iface, connectedGraphs);
			}
			
			// merge all the graphs of all the super types to one single graph, using the current
			// type as junction point
			Iterator<TypeGraph> iter = connectedGraphs.iterator();
			TypeGraph merged = iter.next();
			while(iter.hasNext()){
				merged.merge(iter.next(), type);
			}
			
			// update the index to reflect the fact that all they types that used to be in
			// separate graphs are now in the same
			for(TypeGraphNode node : merged.getNodes()){
				this.index.put(node.getType(), merged);
			}
		}
		return true;
	}
	
	private void addToGraphIfNecessary(ClassWrapper type, ClassWrapper superClassOrInterface, Set<TypeGraph> graphs){
		if(superClassOrInterface == null || superClassOrInterface.getClazz() == Object.class){
			return;
		}
		// given the context, we know for sure that the current type is not already in the super types graph
		TypeGraph graph = this.index.get(superClassOrInterface);
		if(superClassOrInterface.isInterface()){
			graph.connectWithImplements(type, superClassOrInterface);
		} else {
			graph.connectWithExtends(type, superClassOrInterface);
		}
		graphs.add(graph);
	}
	
	public Set<TypeGraph> getGraphs(){
		return new HashSet<TypeGraph>(this.index.values());
	}
	
	public TypeGraph getGraph(ClassWrapper clazz) {
		return this.index.get(clazz);
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("{\n");
		for(TypeGraph graph : this.getGraphs()){
			builder.append(graph.toString());
			builder.append(",\n");
		}
		builder.append("}");
		return builder.toString();
	}
}
