package org.stjs.generator.minify;

import static org.stjs.generator.minify.MinifyLevel.PARAMETERS_AND_LOCALS;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;

import org.stjs.generator.ast.ASTNodeData;
import org.stjs.generator.variable.Variable;
import org.stjs.generator.visitor.ForEachNodeVisitor;

public class NameAllocatorVisitor extends ForEachNodeVisitor<Void> {

	private NameAllocator allocator;
	private MinifyLevel level;
	
	public NameAllocatorVisitor(MinifyLevel level){
		this.level = level;
	}
	
	@Override
	public void visit(MethodDeclaration n, Void arg) {
		NameAllocator backup = allocator;
		allocator = new NameAllocator();
		super.visit(n, arg);
		allocator = backup;
	}

	@Override
	public void visit(Parameter n, Void arg) {
		if(level.isMoreAggressiveOrEquals(PARAMETERS_AND_LOCALS)){
			Variable var = ASTNodeData.resolvedVariable(n);
			var.setMinifiedName(allocator.nextName());
		}
	}

	@Override
	public void visit(VariableDeclarator n, Void arg) {
		if(level.isMoreAggressiveOrEquals(PARAMETERS_AND_LOCALS)){
			Variable var = ASTNodeData.resolvedVariable(n);
			var.setMinifiedName(allocator.nextName());
		}
	}

	
}
