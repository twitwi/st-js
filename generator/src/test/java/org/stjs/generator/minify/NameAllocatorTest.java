package org.stjs.generator.minify;

import org.junit.Test;

import junit.framework.Assert;

public class NameAllocatorTest {

	@Test
	public void testAllocation() {
		doTestAllocation(0, "a");
		doTestAllocation(25, "z");
		doTestAllocation(26, "A");
		doTestAllocation(51, "Z");
		doTestAllocation(52, "aa");
		doTestAllocation(77, "az");
		doTestAllocation(78, "aA");
		doTestAllocation(2700, "YZ"); // should have been 2703, but the range
		                              // contains "do", "in", "if"
		doTestAllocation(2752, "ZZ");
		doTestAllocation(2753, "aaa"); 
		doTestAllocation(143356, "ZZZ");// should have been 143363, but the range
                                        // contains "for", "new", "try", "var"
		doTestAllocation(143357, "aaaa");
	}

	private void doTestAllocation(int index, String expectedName) {
		NameAllocator alloc = new NameAllocator();

		for (int i = 0; i < index; i++) {
			alloc.nextName();
		}

		Assert.assertEquals(expectedName, alloc.nextName());
	}
}
