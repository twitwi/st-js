package org.stjs.generator.minify;

import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;

import org.stjs.generator.ast.ASTNodeData;
import org.stjs.generator.variable.Variable;
import org.stjs.generator.visitor.ForEachNodeVisitor;

public class NameAllocatorVisitor extends ForEachNodeVisitor<Void> {

	private NameAllocator allocator;
	
	
	
	@Override
	public void visit(MethodDeclaration n, Void arg) {
		NameAllocator backup = allocator;
		allocator = new NameAllocator();
		super.visit(n, arg);
		allocator = backup;
	}



	@Override
	public void visit(Parameter n, Void arg) {
		Variable var = ASTNodeData.resolvedVariable(n);
		var.setMinifiedName(allocator.nextName());
	}

}
