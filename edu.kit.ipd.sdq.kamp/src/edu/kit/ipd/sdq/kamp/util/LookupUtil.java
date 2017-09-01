package edu.kit.ipd.sdq.kamp.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.architecture.CrossReferenceProvider;
import static edu.kit.ipd.sdq.kamp.architecture.ArchitectureModelLookup.*;

/**
 * This is a utility class which provides utility methods for the most common lookups which are shared across
 * specific lookups and KAMP-DSL rules.
 * 
 * @author Martin Loeper
 *
 */
public final class LookupUtil {
	private LookupUtil() {}
	
	/**
	 * Returns a collection containing all elements which reference one of the {@code sourceElements} and which are assignable from {@code targetClass}.
	 * 
	 * @param version the version which contains the elements to be queried and which provides a
	 * @param targetClass the type of elements which are retrieved and which reference one of the {@code sourceElements}
	 * @param featureName the name of the feature which holds the {@code targetClass}, or null if the feature should not be taken into account
	 * @param sourceElements the elements which are referenced by an element of type {@code targetClass}
	 * @return a collection containing all elements of type {@code targetClass} which reference one of the given {@code sourceElements} - duplicates are removed
	 * @throws UnsupportedOperationException thrown if the given {@code version} does not implement the CrossReferenceProvider interface or returns null from getECrossReferenceAdapter
	 */
	public static final <T extends EObject, M extends EObject> Collection<T> lookupBackreference(AbstractArchitectureVersion<?> version, Class<T> targetClass, String featureName, Collection<M> sourceElements) {
		return lookupBackreference(version, targetClass, featureName, sourceElements.stream());
	}
	
	/**
	 * Returns a collection containing all elements which reference one of the {@code sourceElements} and which are assignable from {@code targetClass}.
	 * 
	 * @param version the version which contains the elements to be queried and which provides a
	 * @param targetClass the type of elements which are retrieved and which reference one of the {@code sourceElements}
	 * @param featureName the name of the feature which holds the {@code targetClass}, or null if the feature should not be taken into account
	 * @param sourceStream the elements which are referenced by an element of type {@code targetClass}
	 * @return a collection containing all elements of type {@code targetClass} which reference one of the given {@code sourceElements} - duplicates are removed
	 * @throws UnsupportedOperationException thrown if the given {@code version} does not implement the CrossReferenceProvider interface or returns null from getECrossReferenceAdapter
	 */
	public static final <T extends EObject, M extends EObject> Collection<T> lookupBackreference(AbstractArchitectureVersion<?> version, Class<T> targetClass, String featureName, Stream<M> sourceStream) {
		if(!(version instanceof CrossReferenceProvider)) {
			throw new UnsupportedOperationException("The given ArchitectureVersion does not support following backreferences. It must implement CrossReferenceProvider to do so.");
		}
		
		CrossReferenceProvider crossReferenceProvider = (CrossReferenceProvider) version;
		ECrossReferenceAdapter crossReferenceAdapter = crossReferenceProvider.getECrossReferenceAdapter();
		
		if(crossReferenceAdapter == null) {
			throw new UnsupportedOperationException("The given ArchitectureVersion returns null as crossReferenceAdapter which is not allowed.");
		}
		
		List<T> result = sourceStream.flatMap(sig -> crossReferenceAdapter.getInverseReferences(sig, true).stream()).filter(setting -> targetClass.isAssignableFrom(setting.getEObject().getClass()) && (featureName == null || setting.getEStructuralFeature().getName().equals(featureName))).map(Setting::getEObject).map(obj -> targetClass.cast(obj)).distinct().collect(Collectors.toList());
		EqualityHelper eqHelper = new EcoreUtil.EqualityHelper();
		
		// remove duplicates
		Set<T> distinctResults = new HashSet<>();
		outer:
		for(T esc : result) {
			for(T cCheckObj : distinctResults) {
				if(eqHelper.equals(esc, cCheckObj)) {
					continue outer;
				}
			}
			distinctResults.add(esc);
		}
		
		return distinctResults;
	}
	
	/**
	 * Performs the given {@code lookupMethod} for every marked element of type {@code sourceClass} which is queried from {@code version}
	 * 
	 * @param version the version which contains all elements to be retrieved
	 * @param sourceClass the types of marked elements which are retrieved
	 * @param lookupMethod the lookup method which is applied for each element
	 * @return a stream with mappings from affected elements to their corresponding causing entities
	 */
	public static final <U extends EObject, V extends EObject> Stream<CausingEntityMapping<U, V>> lookup(AbstractArchitectureVersion<?> version, Class<V> sourceClass, BiFunction<V, AbstractArchitectureVersion<?>, Set<U>> lookupMethod) {				
		return lookUpMarkedObjectsOfAType(version, sourceClass).stream().flatMap(obj -> createPairsStream(lookupMethod, obj, version));
	}
	
	/**
	 * Performs the {@code lookupMethod} on the given {@code obj} and creates a {@link CausingEntityMapping} for the affected elements.
	 * 
	 * @param lookupMethod the method which is performed on the given {@code obj}
	 * @param obj the element which is used as the source element of the lookup
	 * @param version the version is the element source which is passed to the {@code lookupMethod} in order to allow the lookup method do to advanced queries (such as resolving backreferences).
	 * @return a stream of mappings from affected element to causing entities
	 */
	private static <U extends EObject, V extends EObject> Stream<CausingEntityMapping<U, V>> createPairsStream(BiFunction<V, AbstractArchitectureVersion<?>, Set<U>> lookupMethod, V obj, AbstractArchitectureVersion<?> version) {
		return lookupMethod.apply(obj, version).stream().map(res -> new CausingEntityMapping<U, V>(res, obj));
	}

	/**
	 * This class maps an affected element to its corresponding causing entity.
	 * A causing entity is the element which is responsible for affected element being marked.
	 * 
	 * @author Martin Loeper
	 *
	 * @param <U> the type of affected element
	 * @param <V> the type of causing entity
	 */
	public static final class CausingEntityMapping<U extends EObject, V extends EObject> {
		private final U affectedElement;
		private final Collection<V> causingEntities;
		
		/**
		 * Creates a new mapping from an affected element to causing entities.
		 * 
		 * @param affectedElement the affected element
		 * @param causingEntities the causing entities
		 */
		public CausingEntityMapping(U affectedElement, Collection<V> causingEntities) {
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
			this(affectedElement, Collections.singleton(causingEntity));
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
		public Collection<V> getCausingEntities() {
			return causingEntities;
		}
	}
}
