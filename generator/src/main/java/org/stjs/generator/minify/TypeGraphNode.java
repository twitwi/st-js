package org.stjs.generator.minify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stjs.generator.type.ClassWrapper;

public class TypeGraphNode {
	/** The type associated to this node. */
	private final ClassWrapper type;
	
	/** The "extends" edge of the graph leaving from this node. */
	private TypeGraphNode superClass;

	/** The "implements" edges of the graph leaving from this node. */
	private final List<TypeGraphNode> interfaces = new ArrayList<TypeGraphNode>();
	
	/** The "implementedBy" and "extendedBy" edges of the graph leaving from this node. */
	private final List<TypeGraphNode> subTypes = new ArrayList<TypeGraphNode>();
	
	/** 
	 * All the nodes that are reachable from this node by following the "implements" 
	 * and "extends" edges of the graph. This set is guaranteed to contain at least the
	 * superclass and all the implemented interfaces.
	 */
	private final Set<TypeGraphNode> ancestorTypes = new HashSet<TypeGraphNode>();
	
	/** 
	 * All the nodes that are reachable from this node by following the "implementedBy" 
	 * and "extendedBy" edges of the graph.
	 */
	private final Set<TypeGraphNode> descendantTypes = new HashSet<TypeGraphNode>();
	
	private final ConstrainedNameAllocator names = new ConstrainedNameAllocator();
	
	public TypeGraphNode(ClassWrapper type){
		this.type = type;
	}
	
	public ClassWrapper getType() {
		return type;
	}

	public void addInterface(TypeGraphNode iface){
		// The graph must be canonical, we need to check a few things 
		if(this.ancestorTypes.contains(iface)){
			// the interface we want want to add can already be reached by following "implements"
			// or "extends" edges. Nothing to do
			return;
		}
		
		// We cannot reach the specified interface through any of the current interfaces,
		// but maybe the opposite is true (we can reach some of the interfaces that this node
		// implements through the interface we are trying to add).
		Set<TypeGraphNode> reachable = new HashSet<TypeGraphNode>();
		for(TypeGraphNode existingIface : this.interfaces){
			if(iface.ancestorTypes.contains(existingIface)){
				// existingIface can be reached from iface
				reachable.add(existingIface);
			}
		}
		
		// To keep the graph canonical we must remove from this node all the edges to the 
		// already reachable interfaces. Removing all those edges does not change the set
		// of ancestor types since the nodes we are removing edges to will still be reachable
		// after the new interface has been linked to this node.
		for(TypeGraphNode n : reachable){
			this.interfaces.remove(n); // remove "implements" edge
			n.subTypes.remove(this); // remove "implementedBy" edge
		}
		
		this.interfaces.add(iface); // add "implements" edge
		iface.subTypes.add(this); // add "implementedBy" edge
		iface.descendantTypes.add(this);
		this.ancestorTypes.add(iface);
		this.ancestorTypes.addAll(iface.ancestorTypes);
	}
	
	/**
	 * Returns a read-only view of this nodes sub types. The returned list contains items
	 * that are the union of the sets returned by getDirectSubInterfaces(), and getDirectSubclasses(). 
	 */
	public List<TypeGraphNode> getDirectSubTypes(){
		return Collections.unmodifiableList(subTypes);
	}
	
	/**
	 * Returns the set of interfaces that directly extend this node (in a canonical type graph). 
	 * For nodes that represent classes the returned set is empty.
	 */
	public Set<TypeGraphNode> getDirectSubInterfaces(){
		Set<TypeGraphNode> result = new HashSet<TypeGraphNode>();
		for(TypeGraphNode n : this.subTypes){
			if(n.isInterface()){
				result.add(n);
			}
		}
		return result;
	}
	
	/**
	 * Returns the set of classes that directly extend this node. If this node represents a class, 
	 * the returned set can either be empty or contain a single element. If this node represents
	 * an interface, the set may contain multiple elements (all classes that directly implement 
	 * the interface).
	 */
	public Set<TypeGraphNode> getDirectSubClasses(){
		Set<TypeGraphNode> result = new HashSet<TypeGraphNode>();
		for(TypeGraphNode n : this.subTypes){
			if(!n.isInterface()){
				result.add(n);
			}
		}
		return result;
	}
	
	/** Returns a read-only view of this nodes interfaces. */
	public List<TypeGraphNode> getInterfaces(){
		return Collections.unmodifiableList(interfaces);
	}
	
	public TypeGraphNode getSuperClass() {
		return superClass;
	}

	public void setSuperClass(TypeGraphNode superClass) {
		this.superClass = superClass; // add "extends" edge
		if(superClass != null){
			superClass.subTypes.add(this); // add "extendedBy" edge
			superClass.descendantTypes.add(this);
			this.ancestorTypes.add(superClass);
			this.ancestorTypes.addAll(superClass.ancestorTypes);
		}
	}
	
	public void removeSubType(TypeGraphNode node){
		this.subTypes.remove(node);
	}

	public boolean isInterface(){
		return this.type.isInterface();
	}
	
	public boolean isLeafNode(){
		return this.subTypes.isEmpty();
	}
	
	public boolean isRootNode(){
		return this.descendantTypes.isEmpty();
	}
	
	/**
	 * Allocates minified names for all the declared members of the type represented by 
	 * this node. 
	 */
	public void allocateMemberNames(){
		this.names.allocateAll(type);
	}
	
	/**
	 * Passes the minified names allocated to the members of the type represented by 
	 * this node down to all subtype nodes recursively.
	 */
	public void passNamesDown(){
		for(TypeGraphNode descendantNode : this.descendantTypes){
			descendantNode.names.inheritFrom(this.names);
		}
	}
	
	/**
	 * Propagates the names allocated to this node as forced allocations to all distant 
	 * relatives of this node. The set of distant relatives of this node contains all the
	 * nodes in the graph that are ancestors of this node's descendants, but excludes nodes
	 * that are descendants or ancestors of this node...<br>
	 * <br>
	 * I feel an example would be useful in this case:
	 * <pre>
	 *     interface A     interface B
	 *         ^               ^
	 *          \              | implements
	 *           \             |
	 * implements \        interface C
	 *             \           ^
	 *              \         / implements
	 *               \       /
	 *                class Z
	 * </pre>
	 * Here are the distant relatives relationships for the case above:
	 * <pre>
	 * +------+-------------------+
	 * | type | distant relatives |
	 * +------+-------------------+
	 * | A    | {B, C}            |
	 * | B    | {A}               |
	 * | C    | {A}               |
	 * | Z    | {}                |
	 * +------+-------------------+
	 * </pre>
	 */
	public void forceDistantRelativesNameConstraints() {
		// build the set of all relatives that can be considered distant
		Set<TypeGraphNode> distantRelatives = new HashSet<TypeGraphNode>();
		for(TypeGraphNode desc : this.descendantTypes){
			distantRelatives.addAll(desc.ancestorTypes);
		}
		distantRelatives.removeAll(this.ancestorTypes);
		distantRelatives.removeAll(this.descendantTypes);
		
		// propagate the constraints
		for(TypeGraphNode relative : distantRelatives){
			relative.names.force(this.names);
		}
	}
	
	public String toString(){
		return this.type.getName();
	}

}