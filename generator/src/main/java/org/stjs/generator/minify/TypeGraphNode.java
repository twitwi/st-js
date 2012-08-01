package org.stjs.generator.minify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.TypeWrapper;

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
		this.ancestorTypes.add(iface);
		this.ancestorTypes.addAll(iface.ancestorTypes);
	}
	
	/** Returns a read-only view of this nodes sub types. */
	public List<TypeGraphNode> getSubTypes(){
		return Collections.unmodifiableList(subTypes);
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
		}
		this.ancestorTypes.add(superClass);
		this.ancestorTypes.addAll(superClass.ancestorTypes);
	}
	
	/** Returns a read-only view of this nodes ancestor types. */
	public Set<TypeGraphNode> getAncestors(){
		return Collections.unmodifiableSet(this.ancestorTypes);
	}
	
	public void removeSubType(TypeGraphNode node){
		this.subTypes.remove(node);
	}

	public boolean isInterface(){
		return this.type.isInterface();
	}
	
	public boolean isLeafNode(){
		return this.subTypes.size() == 0;
	}
	
	public boolean isRootNode(){
		return this.superClass == null && this.interfaces.size() == 0;
	}
	
	public String toString(){
		return this.type.getName();
	}
}