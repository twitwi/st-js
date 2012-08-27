package org.stjs.generator.minify;

import org.stjs.generator.type.ClassWrapper;
import org.stjs.generator.type.FieldWrapper;
import org.stjs.generator.type.MethodWrapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ConstrainedNameAllocator {

	/** 
	 * Provides the set of all possibly valid names that could potentially be assigned
	 * to a type/field/method, shortest names first.
	 */
	private final NameIterator iterator = new NameIterator();
	
	/** 
	 * The allocations that are constrained due to some unrelated type (can happen for some
	 * interfaces). Any member of this type whose name is a key in this map must be mapped
	 * to the corresponding value. However, unlike the inherited map, it is not forbidden
	 * to allocate new names that are present as values in this map.
	 */
	private final BiMap<String, String> forced = HashBiMap.create();
	
	/** 
	 * All the allocations that have been done in the parent types, and that must be repeated
	 * identically in this class. It is also forbidden to allocate new names that are present
	 * as values in this map.
	 */
	private final BiMap<String, String> inherited = HashBiMap.create();
	
	/** 
	 * All the new allocations done in this type. The keys and values in this map are disjoint from
	 * the keys and values in the inherited map.
	 */
	private final BiMap<String, String> own = HashBiMap.create();
	
	
	public void allocateAll(ClassWrapper type){
		// first allocate the forced keys. We must do that first, because new names
		// may override unused allocations in the forced map.
		allocateMembers(type, new Allocator(){
			@Override
			public String allocate(String name) {
				return forced.get(name);
			}
		});
		
		// now allocate inherited members (each member of this type that has the same name
		// as a member of a parent type is given the same name as it was given in the parent 
		// type)
		allocateMembers(type, new Allocator(){
			@Override
			public String allocate(String name) {
				return inherited.get(name);
			}
		});
		
		// now allocate all the remaining members (each member that is still not allocated is
		// given the first free name returned by the NameIterator)
		allocateMembers(type, new Allocator(){
			@Override
			public String allocate(String name) {
				String newName;
				do{
					newName = iterator.nextName();
				}while(own.containsValue(newName) || inherited.containsValue(newName));
				return newName;
			}
		});
	}
	
	private void allocateMembers(ClassWrapper type, Allocator alloc){
		for(FieldWrapper field : type.getDeclaredFields()){
			String newName = allocateSingle(field.getName(), alloc);
			if(newName != null){
				field.setMinifiedName(newName);
			}
		}
		
		for(MethodWrapper method : type.getDeclaredMethods()){
			String newName = allocateSingle(method.getName(), alloc);
			if(newName != null){
				method.setMinifiedName(newName);
			}
		}
	}
	
	private String allocateSingle(String oldName, Allocator alloc){
		if(own.containsKey(oldName)){
			// already allocated, no need to allocate again
			return null;
		}
		String newName = alloc.allocate(oldName);
		if(newName != null){
			this.own.put(oldName, newName);
		}
		return newName;
	}

	public void inheritFrom(ConstrainedNameAllocator that) {
		this.inherited.putAll(that.own);
	}

	public void force(ConstrainedNameAllocator that) {
		this.forced.putAll(that.own);
	}

	private static interface Allocator {
		public String allocate(String name);
	}
}
