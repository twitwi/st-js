package org.stjs.generator.minify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.stjs.generator.type.TypeWrappers.wrap;

import org.junit.Test;

public class TestTypeForest {
	
	TypeForest forest = new TypeForest();
	
	@Test
	public void testSimpleClass(){
		// when
		forest.addType(wrap(Animal.class));
		
		// then
		assertGraphCount(forest, 1);
		TypeGraph graph = forest.getGraph(wrap(Animal.class));
		assertHasSingleNode(graph, Animal.class);
	}
	
	@Test
	public void testSimpleInterface(){
		// when
		forest.addType(wrap(Honker.class));
		
		// then
		assertGraphCount(forest, 1);
		TypeGraph graph = forest.getGraph(wrap(Honker.class));
		assertHasSingleNode(graph, Honker.class);
	}
	
	@Test
	public void testSimpleClassHierarchy(){
		// when
		forest.addType(wrap(Dog.class));
		
		// then
		assertGraphCount(forest, 1);
		TypeGraph graph = forest.getGraph(wrap(Dog.class));
		assertNodeCount(graph, 2);
		assertExtendsEdge(graph, Dog.class, Animal.class);
	}
	
	@Test
	public void testSimpleInterfaceHierarchy(){
		// when
		forest.addType(wrap(MelodiousHonker.class));
		
		// then
		assertGraphCount(forest, 1);
		TypeGraph graph = forest.getGraph(wrap(MelodiousHonker.class));
		assertNodeCount(graph, 2);
		assertImplementsEdges(graph, MelodiousHonker.class, Honker.class);
	}
	
	@Test
	public void testSimpleMixedHierarchy(){
		// when
		forest.addType(wrap(Car.class));
		
		// then
		assertGraphCount(forest, 1);
		TypeGraph graph = forest.getGraph(wrap(Car.class));
		assertNodeCount(graph, 3);
		assertImplementsEdges(graph, Car.class, Honker.class);
		assertExtendsEdge(graph, Car.class, Vehicle.class);
		assertRootNode(graph, Honker.class);
		assertRootNode(graph, Vehicle.class);
	}
	
	@Test
	public void testDiamondInterfaceInheritance(){
		// when
		forest.addType(wrap(DiamondImpl.class));
		
		// then
		assertGraphCount(forest, 1);
		TypeGraph graph = forest.getGraph(wrap(DiamondImpl.class));
		assertNodeCount(graph, 4);
		assertRootNode(graph, Diamond1.class);
		assertLeafNode(graph, DiamondImpl.class);
		assertEdges(graph, DiamondA.class, null, Diamond1.class);
		assertEdges(graph, DiamondB.class, null, Diamond1.class);
		assertEdges(graph, DiamondImpl.class, null, DiamondA.class, DiamondB.class);
	}
	
	// We must be able to build exactly the same tree no matter which order the classes are added. 
	// The three tests below are designed to verify that constraint.
	
	@Test
	public void testFullForestFromLeafTypes(){
		doTestFullForest(Minify1.class, Plane.class, Boat.class, Duck.class, 
				Minify3.class, Dog.class, Minify2.class, MonsterTruck.class);
	}
	
	@Test
	public void testFullForestFromRootTypes(){
		doTestFullForest(Vehicle.class, Animal.class, Honker.class, Flyer.class, MelodiousHonker.class,
				Boat.class, Plane.class, Car.class, Dog.class, Duck.class, MonsterTruck.class,
				Minify1.class, Minify2.class, Minify3.class);
	}
	
	@Test
	public void testFullForestRandomOrder(){
		doTestFullForest(Minify1.class,  Plane.class, Animal.class, Minify2.class, Flyer.class, 
				MonsterTruck.class, MelodiousHonker.class, Boat.class, Car.class, Duck.class, Honker.class, 
				Vehicle.class, Minify3.class, Dog.class);
		
	}
	
	private void doTestFullForest(Class<?>... classes){
		// when
		for(Class<?> clazz : classes){
			System.out.println("About to add " + clazz);
			forest.addType(wrap(clazz));
			System.out.println("After adding " + clazz);
			System.out.println(forest);
		}
		
		// then
		assertGraphCount(forest, 4);
		assertHasSingleNode(forest.getGraph(wrap(Minify1.class)), Minify1.class);
		assertHasSingleNode(forest.getGraph(wrap(Minify2.class)), Minify2.class);
		assertHasSingleNode(forest.getGraph(wrap(Minify3.class)), Minify3.class);
		TypeGraph graph = forest.getGraph(wrap(Car.class));
		assertNodeCount(graph, 11);
		assertRootNode(graph, Animal.class);
		assertRootNode(graph, Vehicle.class);
		assertRootNode(graph, Honker.class);
		assertRootNode(graph, Flyer.class);
		assertLeafNode(graph, Plane.class);
		assertLeafNode(graph, Boat.class);
		assertLeafNode(graph, Dog.class);
		assertLeafNode(graph, Duck.class);
		assertLeafNode(graph, MonsterTruck.class);
		assertEdges(graph, Plane.class, Vehicle.class, Flyer.class);
		assertEdges(graph, MonsterTruck.class, Car.class, MelodiousHonker.class);
		assertEdges(graph, MelodiousHonker.class, null, Honker.class);
		assertEdges(graph, Duck.class, Animal.class, Flyer.class);
		assertEdges(graph, Dog.class, Animal.class);
		assertEdges(graph, Car.class, Vehicle.class, Honker.class);
		assertEdges(graph, Boat.class, Vehicle.class, Honker.class);
	}
	
	private static void assertGraphCount(TypeForest forest, int expectedGraphCount){
		assertEquals(expectedGraphCount, forest.getGraphs().size());
	}
	
	private static void assertNodeCount(TypeGraph graph, int expectedNodeCount){
		assertEquals(expectedNodeCount, graph.getNodes().size());
	}
	
	private static void assertHasSingleNode(TypeGraph graph, Class<?> clazz){
		assertNodeCount(graph, 1);
		TypeGraphNode node = graph.getNode(wrap(clazz));
		assertNotNull(node);
		assertEquals(0, node.getSubTypes().size());
		assertRootNode(graph, clazz);
	}
	
	private static void assertRootNode(TypeGraph graph, Class<?> clazz){
		TypeGraphNode node = graph.getNode(wrap(clazz));
		assertNotNull(node);
		assertEquals(0, node.getInterfaces().size());
		assertNull(node.getSuperClass());
		assertEquals(wrap(clazz), node.getType());
	}
	
	private static void assertLeafNode(TypeGraph graph, Class<?> clazz){
		TypeGraphNode node = graph.getNode(wrap(clazz));
		assertNotNull(node);
		assertEquals(wrap(clazz), node.getType());
		assertEquals(0, node.getSubTypes().size());
	}
	
	private static void assertEdges(TypeGraph graph, Class<?> clazz, Class<?> superClass, Class<?>... ifaces){
		assertExtendsEdge(graph, clazz, superClass);
		assertImplementsEdges(graph, clazz, ifaces);
	}
	
	private static void assertExtendsEdge(TypeGraph graph, Class<?> subClass, Class<?> superClass){
		TypeGraphNode subNode = graph.getNode(wrap(subClass));
		assertNotNull(subNode);
		if(superClass != null){
			TypeGraphNode superNode = graph.getNode(wrap(superClass));
			assertNotNull(superNode);
			assertEquals(wrap(subClass), subNode.getType());
			assertEquals(superNode, subNode.getSuperClass());
			assertSubtypesContain(superNode, subNode);
		} else {
			assertNull(subNode.getSuperClass());
		}
	}
	
	private static void assertImplementsEdges(TypeGraph graph, Class<?> classOrInterface, Class<?>... ifaces){
		TypeGraphNode subNode = graph.getNode(wrap(classOrInterface));
		assertNotNull(subNode);
		assertEquals(wrap(classOrInterface), subNode.getType());
		
		for(Class<?> iface : ifaces){
			TypeGraphNode ifaceNode = graph.getNode(wrap(iface));
			assertNotNull(ifaceNode);
			assertInterfacesContain(subNode, ifaceNode);
			assertSubtypesContain(ifaceNode, subNode);
		}
		
		assertEquals(ifaces.length, subNode.getInterfaces().size());
	}
	
	private static void assertSubtypesContain(TypeGraphNode node, TypeGraphNode expectedSubtype){
		assertTrue(node.getSubTypes().contains(expectedSubtype));
	}
	
	private static void assertInterfacesContain(TypeGraphNode node, TypeGraphNode ifaceNode){
		assertTrue(node.getInterfaces().contains(ifaceNode));
	}
}
