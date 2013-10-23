/**
 *  Copyright 2011 Alexandru Craciun, Eyal Kaspi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.stjs.generator.scope;

import static org.stjs.generator.ast.ASTNodeData.resolvedVariable;
import japa.parser.ast.Node;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.EnumDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.FieldAccessExpr;
import japa.parser.ast.expr.LiteralExpr;
import japa.parser.ast.expr.MethodCallExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.ObjectCreationExpr;
import japa.parser.ast.expr.SuperExpr;
import japa.parser.ast.expr.ThisExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.ForeachStmt;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.type.ClassOrInterfaceType;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import org.stjs.generator.GenerationContext;
import org.stjs.generator.GeneratorConstants;
import org.stjs.generator.JavascriptFileGenerationException;
import org.stjs.generator.ast.ASTNodeData;
import org.stjs.generator.ast.SourcePosition;
import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.FieldWrapper;
import org.stjs.generator.type.MethodWrapper;
import org.stjs.generator.type.TypeWrapper;
import org.stjs.generator.utils.ClassUtils;
import org.stjs.generator.utils.Option;
import org.stjs.generator.variable.Variable;
import org.stjs.generator.writer.JavascriptKeywords;
import org.stjs.javascript.annotation.GlobalScope;
import org.stjs.javascript.annotation.JavascriptFunction;

/**
 * this class generate different checks made on the Java statements before they are converted to Javascript
 * 
 * @author acraciun
 */
public final class Checks {
	private Checks() {
		//
	}

	private static void checkVarArgs(MethodDeclaration n, Parameter p, GenerationContext arg) {
		if (!p.getId().getName().equals(GeneratorConstants.ARGUMENTS_PARAMETER)) {
			throw new JavascriptFileGenerationException(arg.getInputFile(), new SourcePosition(n),
					"You can only have a vararg parameter that has to be called 'arguments'");

		}
		if (n.getParameters().size() != 1) {
			throw new JavascriptFileGenerationException(arg.getInputFile(), new SourcePosition(n),
					"You can only have a vararg parameter that has to be called 'arguments'");
		}
	}

	/**
	 * check a method declaration
	 * 
	 * @param n
	 * @param arg
	 */
	public static void checkMethodDeclaration(MethodDeclaration n, GenerationContext arg) {
		if (n.getParameters() != null) {
			for (Parameter p : n.getParameters()) {
				if (p.isVarArgs()) {
					checkVarArgs(n, p, arg);
				}
			}
		}

	}

	private static void checkInitializedInstanceField(VariableDeclarator v, FieldDeclaration n, GenerationContext arg) {
		if (!ClassUtils.isBasicType(n.getType())) {
			throw new JavascriptFileGenerationException(arg.getInputFile(), new SourcePosition(v),
					"Instance field inline initialization is allowed only for string and number field types");
		}
		// allowed x = 1
		if (v.getInit() instanceof LiteralExpr) {
			return;
		}
		// allowed x = -1
		if (v.getInit() instanceof UnaryExpr && ((UnaryExpr) v.getInit()).getExpr() instanceof LiteralExpr) {
			return;
		}
		throw new JavascriptFileGenerationException(arg.getInputFile(), new SourcePosition(v),
				"Instance field inline initialization can only done with literal constants");
	}

	/**
	 * check a field declaration
	 * 
	 * @param n
	 * @param arg
	 */
	public static void checkFieldDeclaration(FieldDeclaration n, GenerationContext arg) {
		for (VariableDeclarator v : n.getVariables()) {
			JavascriptKeywords.checkIdentifier(arg.getInputFile(), new SourcePosition(v), v.getId().getName());
			if (!ModifierSet.isStatic(n.getModifiers()) && v.getInit() != null) {
				checkInitializedInstanceField(v, n, arg);
			}
		}
	}

	private static void checkConstructor(ClassOrInterfaceDeclaration n, GenerationContext context) {
		ConstructorDeclaration constr = null;
		for (BodyDeclaration member : n.getMembers()) {
			if (member instanceof ConstructorDeclaration) {
				if (constr != null) {
					throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(member),
							"Only maximum one constructor is allowed");
				}
				constr = (ConstructorDeclaration) member;
			}
		}
	}

	private static void checkMethod(BodyDeclaration member, GenerationContext context, Set<String> existingNames) {
		if (member instanceof MethodDeclaration) {
			String name = ((MethodDeclaration) member).getName();
			if (!existingNames.add(name)) {
				throw new JavascriptFileGenerationException(
						context.getInputFile(),
						new SourcePosition(member),
						"The type contains already a method or a field called ["
								+ name
								+ "] with a different signature. Javascript cannot distinguish methods/fields with the same name");
			}
		}
	}

	private static void checkField(BodyDeclaration member, GenerationContext context, Set<String> existingNames) {
		if (member instanceof FieldDeclaration) {
			for (VariableDeclarator var : ((FieldDeclaration) member).getVariables()) {
				String name = var.getId().getName();
				if (!existingNames.add(name)) {
					throw new JavascriptFileGenerationException(
							context.getInputFile(),
							new SourcePosition(member),
							"The type contains already a method or a field called ["
									+ name
									+ "] with a different signature. Javascript cannot distinguish methods/fields with the same name");
				}
			}
		}
	}

	/**
	 * check a class declaration
	 * 
	 * @param n
	 * @param context
	 * @param scope
	 */
	public static void checkClassDeclaration(ClassOrInterfaceDeclaration n, GenerationContext context) {
		if (n.getMembers() == null) {
			return;
		}
		checkConstructor(n, context);
		Set<String> names = new HashSet<String>();
		for (BodyDeclaration member : n.getMembers()) {
			checkMethod(member, context, names);
			checkField(member, context, names);
		}
	}

	private static void checkImplements(ClassOrInterfaceDeclaration n, GenerationContext context) {
		if (n.getImplements() != null) {
			for (ClassOrInterfaceType impl : n.getImplements()) {
				TypeWrapper type = ASTNodeData.resolvedType(impl);
				if (type instanceof ClassWrapper
						&& ClassUtils.hasAnnotation((ClassWrapper) type, JavascriptFunction.class)) {
					throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
							"You cannot implement intefaces annotated with @JavascriptFunction. "
									+ "You can only have inline object creation with this type of interfaces");
				}
			}
		}
	}

	private static void checkExistentFieldOrMethod(String name, BodyDeclaration member, ClassWrapper superClass,
			GenerationContext context) {
		if (superClass.findField(name).isDefined() || !superClass.findMethods(name).isEmpty()) {
			throw new JavascriptFileGenerationException(
					context.getInputFile(),
					new SourcePosition(member),
					"One of the parent types contains already a method or a field called ["
							+ name
							+ "] with a different signature. Javascript cannot distinguish methods/fields with the same name");
		}
	}

	private static void postCheckMethod(BodyDeclaration member, ClassWrapper superClass, GenerationContext context) {
		if (!(member instanceof MethodDeclaration)) {
			return;
		}
		MethodDeclaration methodDeclaration = (MethodDeclaration) member;
		MethodWrapper resolvedMethod = ASTNodeData.resolvedMethod(methodDeclaration);
		if (Modifier.isStatic(resolvedMethod.getModifiers())) {
			return;
		}
		String name = methodDeclaration.getName();
		Option<MethodWrapper> overrideMethod = superClass.findMethod(name, resolvedMethod.getParameterTypes());
		if (overrideMethod.isDefined()) {
			if (Modifier.isPrivate(overrideMethod.getOrThrow().getModifiers())) {
				throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(member),
						"One of the parent types contains already a private method called [" + name
								+ "]. Javascript cannot distinguish methods/fields with the same name");
			}
			return;
		}

		checkExistentFieldOrMethod(name, member, superClass, context);
	}

	private static void postCheckField(BodyDeclaration member, ClassWrapper superClass, GenerationContext context) {
		if (!(member instanceof FieldDeclaration)) {
			return;
		}
		FieldDeclaration fieldDeclaration = (FieldDeclaration) member;
		if (Modifier.isStatic(fieldDeclaration.getModifiers())) {
			return;
		}
		for (VariableDeclarator var : fieldDeclaration.getVariables()) {
			String name = var.getId().getName();
			checkExistentFieldOrMethod(name, member, superClass, context);
		}
	}

	/**
	 * other checks after the types were set
	 * 
	 * @param n
	 * @param context
	 */
	public static void postCheckClassDeclaration(ClassOrInterfaceDeclaration n, GenerationContext context) {
		checkImplements(n, context);
		checkNamespace(n, context);
		if (n.getExtends() != null && !n.isInterface()) {
			TypeWrapper superType = ASTNodeData.resolvedType(n).getSuperClass();
			if (!(superType instanceof ClassWrapper)) {
				return;
			}
			ClassWrapper superClass = (ClassWrapper) superType;

			for (BodyDeclaration member : n.getMembers()) {
				postCheckMethod(member, superClass, context);
				postCheckField(member, superClass, context);
			}
		}

	}

	private static void checkNamespace(BodyDeclaration n, GenerationContext context) {
		String ns = ClassUtils.getNamespace(ASTNodeData.resolvedType(n));
		if (ns != null) {
			if (!GeneratorConstants.NAMESPACE_PATTERN.matcher(ns).matches()) {
				throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
						"The namespace must be in the form <identifier>[.<identifier>]..");
			}
			String[] identifiers = ns.split("\\.");
			for (String identifier : identifiers) {
				if (JavascriptKeywords.isReservedWord(identifier)) {
					throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
							"Identifier \"" + identifier + "\" cannot be used as part of a namespace, "
									+ "because it is a javascript keyword or a javascript reserved word");
				}
			}
		}
	}

	/**
	 * check enum declaration
	 * 
	 * @param n
	 * @param context
	 */
	public static void checkEnumDeclaration(EnumDeclaration n, GenerationContext context) {
		if (n.getMembers() != null && n.getMembers().size() > 0) {
			throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
					"Enums with fields or methods are not supported");
		}

	}

	public static void postCheckEnumDeclaration(EnumDeclaration n, GenerationContext context) {
		checkNamespace(n, context);
	}

	/**
	 * check a name expression
	 * 
	 * @param n
	 * @param context
	 */
	public static void checkNameExpr(NameExpr n, GenerationContext context) {
		JavascriptKeywords.checkIdentifier(context.getInputFile(), new SourcePosition(n), n.getName());
	}

	public static void checkVariableDeclaratorId(VariableDeclaratorId n, GenerationContext context) {
		JavascriptKeywords.checkIdentifier(context.getInputFile(), new SourcePosition(n), n.getName());
	}

	private static void throwCannotCallOuterType(Node n, GenerationContext context) {
		throw new JavascriptFileGenerationException(
				context.getInputFile(),
				new SourcePosition(n),
				"In Javascript you cannot call methods or fields from the outer type. "
						+ "You should define a variable var that=this outside your function definition and call the methods on this object");

	}

	public static void checkThisExpr(ThisExpr n, GenerationContext context) {
		if (n.getClassExpr() != null) {
			throwCannotCallOuterType(n, context);
		}

	}

	public static void checkSuperExpr(SuperExpr n, GenerationContext context) {
		if (n.getClassExpr() != null) {
			throwCannotCallOuterType(n, context);
		}
	}

	public static void checkScope(MethodCallExpr n, GenerationContext context) {
		if (n.getScope() == null && !isAccessInCorrectScope(n)) {
			throwCannotCallOuterType(n, context);
		}
	}

	public static void checkScope(NameExpr n, GenerationContext context) {
		if (!isAccessInCorrectScope(n)) {
			throwCannotCallOuterType(n, context);
		}

	}

	private static boolean isAccessInCorrectScope(MethodCallExpr n) {
		MethodWrapper method = ASTNodeData.resolvedMethod(n);
		if (Modifier.isStatic(method.getModifiers())) {
			return true;
		}
		TypeWrapper methodDeclaringClass = method.getOwnerType();
		Scope scope = ASTNodeData.scope(n);
		ClassScope thisClassScope = scope.closest(ClassScope.class);
		return thisClassScope.getClazz().equals(methodDeclaringClass);
	}

	private static boolean isAccessInCorrectScope(NameExpr n) {
		Variable var = ASTNodeData.resolvedVariable(n);
		if (!(var instanceof FieldWrapper)) {
			return true;
		}
		FieldWrapper field = (FieldWrapper) var;
		if (Modifier.isStatic(field.getModifiers())) {
			return true;
		}
		TypeWrapper declaringClass = field.getOwnerType();
		Scope scope = ASTNodeData.scope(n);
		ClassScope thisClassScope = scope.closest(ClassScope.class);
		return thisClassScope.getClazz().equals(declaringClass);
	}

	public static void checkObjectCreationExpr(ObjectCreationExpr n, GenerationContext context) {
		TypeWrapper type = ASTNodeData.resolvedType(n.getType());
		if (!ClassUtils.isJavascriptFunction(type)) {
			return;
		}
		if (n.getAnonymousClassBody() != null && n.getAnonymousClassBody().size() > 1) {
			throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
					"Initialization block for a Javascript function must contain exactly one method");
		}

	}

	/**
	 * makes sure only literals can be used as keys (so the even arguments)
	 * 
	 * @param n
	 * @param context
	 */
	public static void checkMapConstructor(MethodCallExpr n, GenerationContext context) {
		if (n.getArgs() != null) {
			for (int i = 0; i < n.getArgs().size(); i += 2) {
				if (!(n.getArgs().get(i) instanceof LiteralExpr)) {
					throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
							"The key of a map built this way can only be a literal. "
									+ "Use map.$put(variable) if you want to use variables as keys");
				}

			}
		}
	}

	private static boolean isLoop(Node n) {
		return n instanceof ForeachStmt || n instanceof ForStmt || n instanceof WhileStmt || n instanceof DoStmt;
	}

	private static boolean isMethodOrClassDeclaration(Node n) {
		return n instanceof MethodDeclaration || n instanceof ConstructorDeclaration
				|| n instanceof ClassOrInterfaceDeclaration;
	}

	public static void checkVariableDeclarationExpr(VariableDeclarationExpr n, GenerationContext context) {
		if (!ModifierSet.isFinal(n.getModifiers())) {
			return;
		}
		for (Node p = n; p != null; p = ASTNodeData.parent(p)) {
			if (isLoop(p)) {
				throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
						"To prevent unexpected behaviour in Javascript, final variables must be declared at method level and not inside loops");
			}
			if (isMethodOrClassDeclaration(p)) {
				break;
			}
		}
	}

	public static void checkGlobalVariable(NameExpr n, GenerationContext context, Scope currentScope) {
		Variable var = resolvedVariable(n);
		if (var instanceof FieldWrapper) {
			FieldWrapper field = (FieldWrapper) var;
			TypeWrapper scopeType = field.getOwnerType();
			checkGlobalVariable(n, scopeType, field.getName(), currentScope, context);
		}
	}

	public static void checkGlobalVariable(FieldAccessExpr n, GenerationContext context, Scope currentScope) {
		FieldWrapper field = (FieldWrapper) resolvedVariable(n);
		TypeWrapper scopeType = field.getOwnerType();
		checkGlobalVariable(n, scopeType, field.getName(), currentScope, context);
	}

	private static void checkGlobalVariable(Node n, TypeWrapper scopeType, String name, Scope currentScope,
			GenerationContext context) {
		if (scopeType.hasAnnotation(GlobalScope.class)) {
			// look if there is any variable with the same name in the method
			MethodScope methodScope = currentScope.closest(MethodScope.class);
			if (methodScope == null) {
				return;
			}
			Variable existent = resolveVariableInDescendantBlocks(methodScope, name);
			if (existent != null) {
				throw new JavascriptFileGenerationException(context.getInputFile(), new SourcePosition(n),
						"A variable with the same name as your global variable is already defined in this method's scope. "
								+ "Please rename either the local variable/parameter or the global variable.");
			}
		}
	}

	/**
	 * look for the variable in all the blocks (without going in other types)
	 * 
	 * @param methodScope
	 * @param name
	 * @return
	 */
	private static Variable resolveVariableInDescendantBlocks(Scope scope, String name) {
		VariableWithScope existent = scope.resolveVariable(name);
		if (existent != null && existent.getScope() == scope) {
			// take only this scope, not a parent class scope
			return existent.getVariable();
		}
		for (Scope child : scope.getChildren()) {
			if (child instanceof BasicScope) {
				Variable var = resolveVariableInDescendantBlocks(child, name);
				if (var != null) {
					return var;
				}
			}
		}
		return null;
	}
}
