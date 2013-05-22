package org.stjs.generator.writer.template;

import japa.parser.ast.expr.MethodCallExpr;

import org.stjs.generator.GenerationContext;
import org.stjs.generator.scope.Checks;
import org.stjs.generator.writer.JavascriptWriterVisitor;

/**
 * $map() -> {}
 * 
 * @author acraciun
 * 
 */
public class MapTemplate implements MethodCallTemplate {

	@Override
	public boolean write(JavascriptWriterVisitor currentHandler, MethodCallExpr n, GenerationContext context) {
		if ((n.getArgs() != null) && (n.getArgs().size() > 1)) {
			// currentHandler.getPrinter().printLn();
			currentHandler.getPrinter().indent();
		}
		currentHandler.getPrinter().print("{");
		if (n.getArgs() != null) {
			Checks.checkMapConstructor(n, context);
			boolean first = true;
			for (int i = 0; i < n.getArgs().size(); i += 2) {
				if (!first) {
					currentHandler.getPrinter().print(", ");
					currentHandler.getPrinter().printLn();
				}
				n.getArgs().get(i).accept(currentHandler, context);
				currentHandler.getPrinter().print(": ");
				n.getArgs().get(i + 1).accept(currentHandler, context);
				first = false;
			}
		}
		if ((n.getArgs() != null) && (n.getArgs().size() > 1)) {
			currentHandler.getPrinter().unindent();
		}

		currentHandler.getPrinter().print("}");
		return true;

	}

}
