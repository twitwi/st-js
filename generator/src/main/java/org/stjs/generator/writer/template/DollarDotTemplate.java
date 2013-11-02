package org.stjs.generator.writer.template;

import japa.parser.ast.expr.MethodCallExpr;

import org.stjs.generator.GenerationContext;
import org.stjs.generator.writer.JavascriptWriterVisitor;

public class DollarDotTemplate implements MethodCallTemplate {

	@Override
	public boolean write(JavascriptWriterVisitor currentHandler, MethodCallExpr n, GenerationContext context) {
		TemplateUtils.printScope(currentHandler, n, context, false);
		currentHandler.getPrinter().print(n.getName().replaceAll("[$]", "."));
		currentHandler.printArguments(n.getArgs(), context);
		return true;
	}

}
