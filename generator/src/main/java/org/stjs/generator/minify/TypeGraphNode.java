package org.stjs.generator.minify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.TypeWrapper;

public class TypeGraphNode {
	// this node
	private final ClassWrapper type;
	
	// the edges in the direction of the inheritance roots
	private TypeGraphNode superClass;

	private final List<TypeGraphNode> interfaces = new ArrayList<TypeGraphNode>();
	
	// the edges in the direction of the inheritance leafs
	private final List<TypeGraphNode> subTypes = new ArrayList<TypeGraphNode>();
	
	public TypeGraphNode(ClassWrapper type){
		this.type = type;
	}
	
	public ClassWrapper getType() {
		return type;
	}

	public void addInterface(TypeGraphNode iface){
		this.interfaces.add(iface);
		iface.subTypes.add(this);
	}
	
	public List<TypeGraphNode> getSubTypes(){
		return new ArrayList<TypeGraphNode>(subTypes);
	}
	
	public List<TypeGraphNode> getInterfaces(){
		return new ArrayList<TypeGraphNode>(interfaces);
	}
	
	public TypeGraphNode getSuperClass() {
		return superClass;
	}

	public void setSuperClass(TypeGraphNode superClass) {
		this.superClass = superClass;
		if(superClass != null){
			superClass.subTypes.add(this);
		}
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