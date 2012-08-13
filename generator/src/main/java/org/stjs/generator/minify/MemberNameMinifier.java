package org.stjs.generator.minify;

import static org.stjs.generator.minify.MinifyLevel.PRIVATE_FIELDS_AND_METHODS;

/**
 * Allocates minified names to fields and methods in a TypeGraph according to a specific MinifyLevel
 * @author lordofthepigs
 */
public class MemberNameMinifier {

	private final TypeGraph graph;
	private final MinifyLevel level;
	
	public MemberNameMinifier(TypeGraph graph, MinifyLevel level){
		this.graph = graph;
		this.level = level;
	}
	
	public void allocate(){
		if(level.isLessAggressiveThan(PRIVATE_FIELDS_AND_METHODS)){
			// nothing to do. The minification for lower levels is done in lower level 
			// classes like JavascriptWriterVisitor and NameAllocatingVisitor
			return;
		}
		
		for(TypeGraphNode node : graph.getRootInterfaceNodes()){
			allocateInterfaceMemberNames(node);
		}
		
		for(TypeGraphNode node : graph.getRootClassNodes()){
			allocateClassMemberNames(node);
		}
	}
	
	private void allocateInterfaceMemberNames(TypeGraphNode ifaceNode){
		ifaceNode.allocateMemberNames();
		ifaceNode.passNamesDown();
		ifaceNode.forceDistantRelativesNameConstraints();
		
		for(TypeGraphNode subIfaceNode : ifaceNode.getDirectSubInterfaces()){
			allocateInterfaceMemberNames(subIfaceNode);
		}
	}

	private void allocateClassMemberNames(TypeGraphNode classNode){
		classNode.allocateMemberNames();
		classNode.passNamesDown();
		
		for(TypeGraphNode subClassNode : classNode.getDirectSubClasses()){
			allocateClassMemberNames(subClassNode);
		}
	}
}
