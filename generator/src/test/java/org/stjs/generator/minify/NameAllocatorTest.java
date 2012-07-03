package org.stjs.generator.minify;

import org.junit.Test;

import junit.framework.Assert;

public class NameAllocatorTest {

	@Test
	public void testAllocation(){
		doTestAllocation(0, "a");
		doTestAllocation(25, "z");
		doTestAllocation(26, "A");
		doTestAllocation(51, "Z");
		doTestAllocation(52, "aa");
		doTestAllocation(77, "az");
		doTestAllocation(78, "aA");
		doTestAllocation(2703, "YZ");
		doTestAllocation(2755, "ZZ");
		doTestAllocation(2756, "aaa");
		doTestAllocation(143363, "ZZZ");
		doTestAllocation(143364, "aaaa");
	}
	
	private void doTestAllocation(int index, String expectedName){
		NameAllocator alloc = new NameAllocator();
		
		for(int i = 0; i < index; i ++){
			alloc.nextName();
		}
		
		Assert.assertEquals(expectedName, alloc.nextName());
	}
}
