package org.stjs.generator.minify;

import static org.stjs.generator.minify.MinifyLevel.PARAMETERS_AND_LOCALS;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;

import org.stjs.generator.ast.ASTNodeData;
import org.stjs.generator.variable.LocalVariable;
import org.stjs.generator.variable.ParameterVariable;
import org.stjs.generator.variable.Variable;
import org.stjs.generator.visitor.ForEachNodeVisitor;

public class NameAllocatorVisitor extends ForEachNodeVisitor<Void> {

	private NameAllocator allocator;
	private MinifyLevel level;

	private boolean inMethodBody = false;

	public NameAllocatorVisitor(MinifyLevel level) {
		this.level = level;
	}

	@Override
	public void visit(ConstructorDeclaration n, Void arg) {
		MethodBodyContextBackup backup = this.enterMethodBody();
		super.visit(n, arg);
		backup.restore();
	}

	@Override
	public void visit(MethodDeclaration n, Void arg) {
		MethodBodyContextBackup backup = this.enterMethodBody();
		super.visit(n, arg);
		backup.restore();
	}

	private MethodBodyContextBackup enterMethodBody() {
		MethodBodyContextBackup backup = new MethodBodyContextBackup();
		if (!inMethodBody) {
			// in order to avoid conflict of variable names between local variable within nested
			// methods, we use only one allocator for a method and for all the nested methods it
			// might contain.

			// Immagine the following case: 
			// public void foobar(){
			//   int meuh = 0;
			//   this.element.onclick=new Function1<>(){
			//     public void $invoke(){
			//        int jambon;
			//        meuh = jambon; 
			//        //^ here we must make sure we don't allocate the same name for "meuh" and
			//        //  and "jambon" even though they seem to be in different scopes.
			// }}};

			allocator = new NameAllocator();
			inMethodBody = true;
		}
		return backup;
	}

	@Override
	public void visit(Parameter n, Void arg) {
		if (level.isMoreAggressiveOrEquals(PARAMETERS_AND_LOCALS)) {
			Variable var = ASTNodeData.resolvedVariable(n);
			var.setMinifiedName(allocator.nextName());
		}
	}

	@Override
	public void visit(VariableDeclarator n, Void arg) {
		Variable var = ASTNodeData.resolvedVariable(n);

		if (level.isMoreAggressiveOrEquals(PARAMETERS_AND_LOCALS) && (var instanceof LocalVariable || var instanceof ParameterVariable)) {
			// we can only generate the minified names easily for local variables and
			// parameter variables
			var.setMinifiedName(allocator.nextName());
		}

		if (n.getInit() != null) {
			// there is no visit(Expression) method, so let's just do it manually here
			before(n.getInit(), arg);
			n.getInit().accept(this, arg);
			after(n.getInit(), arg);
		}
	}

	private class MethodBodyContextBackup {
		NameAllocator allocator;
		boolean inMethodBody;

		MethodBodyContextBackup() {
			this.allocator = NameAllocatorVisitor.this.allocator;
			this.inMethodBody = NameAllocatorVisitor.this.inMethodBody;
		}

		void restore() {
			NameAllocatorVisitor.this.allocator = allocator;
			NameAllocatorVisitor.this.inMethodBody = inMethodBody;
		}
	}
}
