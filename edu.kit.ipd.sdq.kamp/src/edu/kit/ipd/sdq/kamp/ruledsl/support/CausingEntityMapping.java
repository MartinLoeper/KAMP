package edu.kit.ipd.sdq.kamp.ruledsl.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;

/**
 * This class maps an affected element to its corresponding causing entity.
 * A causing entity is the element which is responsible for affected element being marked.
 * 
 * @author Martin Loeper
 *
 * @param <U> the type of affected element
 * @param <V> the type of causing entity
 */
public final class CausingEntityMapping<U extends EObject, V extends EObject> {
	private final U affectedElement;
	private final Set<V> causingEntities;
	
	/**
	 * Creates a new mapping from an affected element to causing entities.
	 * 
	 * @param affectedElement the affected element
	 * @param causingEntities the causing entities
	 */
	public CausingEntityMapping(U affectedElement, Set<V> causingEntities) {
		this.affectedElement = affectedElement;
		this.causingEntities = causingEntities;
	}

	/**
	 * Creates a new mapping from an affected element to causing entities.
	 * This is basically a convenience constructor for passing one single causing entity instead of a collection of causing entities.
	 * 
	 * @param affectedElement the affected element
	 * @param causingEntity one single causing entity
	 */
	public CausingEntityMapping(U affectedElement, V causingEntity) {
		Set<V> newSet = new HashSet<>();
		newSet.add(causingEntity);
		
		this.affectedElement = affectedElement;
		this.causingEntities = newSet;
	}
	
	public CausingEntityMapping(U affectedElement) {
		this(affectedElement, new HashSet<>());
	}
	
	public CausingEntityMapping(U affectedElement, CausingEntityMapping<?, V> cem) {
		this(affectedElement, cem.getCausingEntities());
	}
	
	/**
	 * Returns the affected element.
	 * @return the affected element
	 */
	public U getAffectedElement() {
		return affectedElement;
	}

	// TODO there might be more than one causing entity! We should make this a collection!!
	/**
	 * Returns the causing entities.
	 * @return the causing entities
	 */
	public Set<V> getCausingEntities() {
		return causingEntities;
	}

	public void addCausingEntityDistinct(V element) {
		EqualityHelper eqHelper = new EcoreUtil.EqualityHelper();
		
		// remove duplicates
		for(V e : this.causingEntities) {
			if(eqHelper.equals(e, element)) {
				return;
			}
		}
		
		this.causingEntities.add(element);
	}
}
