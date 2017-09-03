package edu.kit.ipd.sdq.kamp.ruledsl.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
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
	private Map<EObject, CausingEntityMapping<?, ?>> parents = new LinkedHashMap<>();
	
	/**
	 * Creates a new mapping from an affected element to causing entities.
	 * 
	 * @param affectedElement the affected element
	 * @param causingEntities the causing entities
	 */
	public CausingEntityMapping(U affectedElement, Set<V> causingEntities) {
		this.affectedElement = affectedElement;
		this.causingEntities = causingEntities;
		this.parents.put(affectedElement, this);
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
		this.parents.put(affectedElement, this);
	}
	
	public CausingEntityMapping(U affectedElement) {
		this(affectedElement, new HashSet<>());
	}
	
	public CausingEntityMapping(U affectedElement, CausingEntityMapping<?, V> cem) {
		this(affectedElement, new HashSet<>(cem.getCausingEntities()));
		this.parents = cem.getParents();
		this.parents.put(affectedElement, this);
	}
	
	/**
	 * Returns a copy of the parent causing entities.
	 * @return parent causing entities
	 */
	public Map<EObject, CausingEntityMapping<?, ?>> getParents() {
		return new LinkedHashMap<>(this.parents);
	}
	
	/**
	 * Returns an iterator over the elements on the path between marked element and affected element.
	 * Please note that this path is unique if we traverse it backwards from affected element to marked element.
	 * Use iterator.previous() in order to do so.
	 * @return an iterator for the lookup path
	 */
	public ListIterator<Map.Entry<EObject, CausingEntityMapping<?, ?>>> getParentIterator() {
		return new ArrayList<Map.Entry<EObject, CausingEntityMapping<?, ?>>>(getParents().entrySet()).listIterator(getParents().size());
	}
	
	/**
	 * Returns the first parent element on the path back to the source element with the given {@code parentType}.
	 * @param parentType the type of the parent to look for - we also check for subtypes
	 * @throws thrown if no parent element of the given {@parentType} or subtype could be found.
	 * @return the parent element with the given type on the path back to the source of marked elements
	 */
	public <T extends EObject> T getParentOfType(Class<T> parentType) {
		return getParentMappingOfType(parentType).getAffectedElement();
	}
	
	/**
	 * Return the parent mapping. 
	 * @throws NoSuchElementException thrown if there is no parent and this is effectively one of the marked elements
	 * @return the direct predecessor (aka parent) mapping
	 */
	public CausingEntityMapping<?, ?> getParentMapping() {
		ListIterator<Entry<EObject, CausingEntityMapping<?, ?>>> it = getParentIterator();
		it.previous();	// skip the current element
		return it.previous().getValue();
	}
	
	/**
	 * Returns the parent of all elements in the path. This is the marked element.
	 * @return the marked element
	 */
	public CausingEntityMapping<?, ?> getSourceMapping() {
		return getParents().values().iterator().next();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends EObject> CausingEntityMapping<T, ?> getParentMappingOfType(Class<T> parentType) {
		ListIterator<Entry<EObject, CausingEntityMapping<?, ?>>> iterator = getParentIterator();
		while (iterator.hasPrevious()) {
			Entry<EObject, CausingEntityMapping<?, ?>> parent = iterator.previous();
			if(parentType.isAssignableFrom(parent.getValue().getAffectedElement().getClass())) {
				// cast is safe because we check it in the if clause above
				return (CausingEntityMapping<T, ?>) parent.getValue();
			}
		}
		
		throw new NoSuchElementException("An element with the given type '" + parentType.getSimpleName() + "' could not be found. Please note that the type must exactly match. Subtypes are not matched!");
	}
	
	/**
	 * Returns the affected element.
	 * @return the affected element
	 */
	public U getAffectedElement() {
		return affectedElement;
	}

	/**
	 * Returns a copy of the set with causing entities.
	 * @return the causing entities
	 */
	public Set<V> getCausingEntities() {
		return Collections.unmodifiableSet(new HashSet<>(causingEntities));
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
