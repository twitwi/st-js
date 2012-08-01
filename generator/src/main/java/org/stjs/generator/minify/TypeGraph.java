package org.stjs.generator.minify;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.TypeWrapper;

/**
 * A strongly connected, canonical graph of types linked through inheritance/behavior (extends, implements) 
 * relationships. Since this is a strongly connected graph, each node in this graph can reach all
 * the other nodes of this graph by following an arbitrary number of edges. Each extends (or implements)
 * relationship between types (ie: nodes) leads to two edges being created, one labeled extends
 * (or implements) and one labeled extendedBy (or implementedBy).<br>
 * <br>
 * In a type graph, two interfaces which are declared in Java to extend one another (eg: interface A extends B)
 * will be linked by implements and implementedBy edges. This is because this graph considers interface extension
 * to be an extension of behavior rather than type, and is therefore more accurately represented by implements/
 * implementedBy edges.<br>
 * <br>
 * The type graph represented by this class is guaranteed to be canonical. It contains the minimum number of
 * edges required to accurately describe the type hierarchy. This also means that, in some cases, the graph may 
 * not reflect exactly the interface inheritance as declared in the java source code. Example:
 * <pre>
 * interface A {}
 * interface B extends A {}
 * class Z implements A, B {}
 * </pre>
 * According to the java source, Z implements both A and B. But since implementing B already implies implementing
 * A, a TypeGraph will only reflect the following (canonical) structure:
 * <pre>
 *             A
 *            ^ \
 * implements | | implementedBy
 *            \ v
 *             B
 *            ^ \
 * implements | | implementedBy
 *            \ v
 *             Z
 * </pre>
 */
public class TypeGraph {

	private Map<TypeWrapper, TypeGraphNode> nodes = new HashMap<TypeWrapper, TypeGraphNode>();
	
	/**
	 * Constructs a new TypeGraph containing only the specified type. The specified type can only be
	 * a root node.
	 */
	public TypeGraph(ClassWrapper type){
		Class<?> clazz = type.getClazz();
		if(clazz.getInterfaces().length > 0){
			throw new IllegalArgumentException("Type " + clazz.getName() + " implements interfaces " + // 
					Arrays.toString(clazz.getInterfaces()));
		}
		if(clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class){
			throw new IllegalArgumentException("Type " + clazz.getName() + " extends class " + clazz.getSuperclass().getName());
		}
		TypeGraphNode node = new TypeGraphNode(type);
		this.nodes.put(type, node);
	}
	
	/**
	 * Creates extends and extendedBy edges between the nodes associated to the specified types. If the specified
	 * type does not have an associated node in the graph, such a node will be created. If the specified super type
	 * does not have a associated node in the grah, an IllegalArgumentException is thrown. If the specified super
	 * type is an interface, an IllegalArgumentException is thrown.
	 */
	public TypeGraphNode connectWithExtends(ClassWrapper type, ClassWrapper superType){
		if(type == null){
			throw new IllegalArgumentException("Cannot add a null type");
		} else if(superType == null){
			throw new IllegalArgumentException("Cannot connect type to a null superType");
		} else if(superType.getClazz() == Object.class){
			throw new IllegalArgumentException("Cannot connect type to java.lang.Object");
		} else if(superType.isInterface()){
			throw new IllegalArgumentException("Type " + type.getName() + " is an interface");
		}
		
		TypeGraphNode superNode = getNode(superType);
		if(superNode == null){
			throw new IllegalArgumentException("Cannnot find class " + superType.getName() + " in the graph");
		}
		
		TypeGraphNode node = this.nodes.get(type);
		if(node == null){
			node = new TypeGraphNode(type);
			this.nodes.put(type, node);
		}
		node.setSuperClass(superNode);
		return node;
	}
	
	/**
	 * Creates implements and implementedBy edges between the nodes associated to the specified types. 
	 * If the specified implementing type does not have an associated node in the graph, such a node will 
	 * be created. If the specified implemented type does not have a associated node in the grah, an 
	 * IllegalArgumentException is thrown.
	 */
	public TypeGraphNode connectWithImplements(ClassWrapper type, ClassWrapper implementedInterface){
		if(type == null){
			throw new IllegalArgumentException("Cannot add a null type");
		} else if(implementedInterface == null){
			throw new IllegalArgumentException("Cannot connect type to a null interface");
		} else if(!implementedInterface.isInterface()){
			throw new IllegalArgumentException("Type " + type.getName() + " is not an interface");
		}
		
		TypeGraphNode ifaceNode = getNode(implementedInterface);
		if(ifaceNode == null){
			throw new IllegalArgumentException("Cannot find interface " + implementedInterface.getName() + " in the graph");
		}
		
		TypeGraphNode node = this.nodes.get(type);
		if(node == null){
			node = new TypeGraphNode(type);
			this.nodes.put(type, node);
		}
		node.addInterface(ifaceNode);
		return node;
	}
	
	/**
	 * Merges the target TypeGraph into this TypeGraph using the specified common type as junction point.
	 * This method has no effect if the target graph is the same object as this graph. The node in the 
	 * target graph corresponding to the common type will be replaced by the equivalent node in this graph
	 * all all the edges to and from the node in the target graph will be moved to point to the node in this 
	 * graph. As a result, the target graph will no longer be self contained after this method is called,
	 * but can be considered a correct subgraph of this graph.
	 */
	public void merge(TypeGraph target, ClassWrapper commonType){
		if(this == target){
			return;
		}
		
		TypeGraphNode thisCommon = this.getNode(commonType);
		TypeGraphNode targetCommon = target.getNode(commonType);
		if(thisCommon == null){
			throw new IllegalArgumentException("There is no node corresponding to " + commonType.getName() + " in this graph");
		} else if (targetCommon == null){
			throw new IllegalArgumentException("There is no node corresponding to " + commonType.getName() + " in the target graph");
		}
		
		// re-link the super class (in the target graph) to the common node (in this graph)
		TypeGraphNode superNode = targetCommon.getSuperClass();
		if(superNode != null){
			thisCommon.setSuperClass(superNode);
			superNode.removeSubType(targetCommon);
		}
		
		// re-link the interfaces (in the target graph) to the common node (in this graph)
		for(TypeGraphNode iface : targetCommon.getInterfaces()){
			thisCommon.addInterface(iface);
			iface.removeSubType(targetCommon);
		}
		
		// Make sure all the nodes of the target graph are present as nodes of this graph
		for(TypeGraphNode targetNode : target.nodes.values()){
			if(targetNode != targetCommon){
				this.nodes.put(targetNode.getType(), targetNode);
			}
		}
		
		target.nodes.put(commonType, thisCommon);
	}
	
	/**
	 * Returns the node of the graph associated to the specified type.
	 */
	public TypeGraphNode getNode(ClassWrapper type){
		return this.nodes.get(type);
	}
	
	/**
	 * Returns a set containing all the nodes in this graph.
	 */
	public Set<TypeGraphNode> getNodes(){
		return new HashSet<TypeGraphNode>(this.nodes.values());
	}
	
	public String toString(){
		StringWriter str = new StringWriter();
		PrintWriter out = new PrintWriter(str);
		
		for(TypeGraphNode node : this.nodes.values()){
			if(node.isRootNode()){
				printNode(node, "", out);
			}
		}
		
		out.flush();
		return str.toString();
	}
	
	private void printNode(TypeGraphNode node, String indent, PrintWriter out){
		out.print(indent);
		out.println(node);
		for(TypeGraphNode sub : node.getSubTypes()){
			printNode(sub, indent + "  ", out);
		}
	}
}
