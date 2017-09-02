package edu.kit.ipd.sdq.kamp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.architecture.CrossReferenceProvider;
import edu.kit.ipd.sdq.kamp.ruledsl.support.CausingEntityMapping;

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
	 * @param addCausingEntities whether the {@code sourceElements} should be added to causing entities inside the CausingEntityMapping
	 * @return a collection containing all elements of type {@code targetClass} which reference one of the given {@code sourceElements} - duplicates are removed
	 * @throws UnsupportedOperationException thrown if the given {@code version} does not implement the CrossReferenceProvider interface or returns null from getECrossReferenceAdapter
	 */
	public static final <T extends EObject, M extends EObject> Collection<CausingEntityMapping<T, EObject>> lookupBackwardReference(AbstractArchitectureVersion<?> version, Class<T> targetClass, String featureName, Collection<CausingEntityMapping<M, EObject>> sourceElements, boolean addCausingEntities) {
		return lookupBackwardReference(version, targetClass, featureName, sourceElements.stream(), addCausingEntities);
	}
	
	/**
	 * Returns a collection containing all elements which reference one of the {@code sourceElements} and which are assignable from {@code targetClass}.
	 * 
	 * @param version the version which contains the elements to be queried and which provides a
	 * @param targetClass the type of elements which are retrieved and which reference one of the {@code sourceElements}
	 * @param featureName the name of the feature which holds the {@code targetClass}, or null if the feature should not be taken into account
	 * @param sourceStream the elements which are referenced by an element of type {@code targetClass}
	 * @param addCausingEntities whether the {@code sourceElements} should be added to causing entities inside the CausingEntityMapping
	 * @return a collection containing all elements of type {@code targetClass} which reference one of the given {@code sourceElements} - duplicates are removed
	 * @throws UnsupportedOperationException thrown if the given {@code version} does not implement the CrossReferenceProvider interface or returns null from getECrossReferenceAdapter
	 */
	public static final <T extends EObject, M extends EObject> Collection<CausingEntityMapping<T, EObject>> lookupBackwardReference(AbstractArchitectureVersion<?> version, Class<T> targetClass, String featureName, Stream<CausingEntityMapping<M, EObject>> sourceStream, boolean addCausingEntities) {
		if(!(version instanceof CrossReferenceProvider)) {
			throw new UnsupportedOperationException("The given ArchitectureVersion does not support following backreferences. It must implement CrossReferenceProvider to do so.");
		}
		
		CrossReferenceProvider crossReferenceProvider = (CrossReferenceProvider) version;
		ECrossReferenceAdapter crossReferenceAdapter = crossReferenceProvider.getECrossReferenceAdapter();
		
		if(crossReferenceAdapter == null) {
			throw new UnsupportedOperationException("The given ArchitectureVersion returns null as crossReferenceAdapter which is not allowed.");
		}
	
		return sourceStream.flatMap(new Function<CausingEntityMapping<M, EObject>, Stream<CausingEntityMapping<T, EObject>>>() {

			@Override
			public Stream<CausingEntityMapping<T, EObject>> apply(CausingEntityMapping<M, EObject> cem) {
				Collection<Setting> settings = crossReferenceAdapter.getInverseReferences(cem.getAffectedElement(), true);
				
				return settings.stream().filter(setting -> targetClass.isAssignableFrom(setting.getEObject().getClass()) && (featureName == null || setting.getEStructuralFeature().getName().equals(featureName))).map(Setting::getEObject).filter(distinctByEqualityHelper()).map(new Function<EObject, CausingEntityMapping<T, EObject>>() {

					@Override
					public CausingEntityMapping<T, EObject> apply(EObject obj) {
						if(addCausingEntities) {
							CausingEntityMapping<T, EObject> newMapping = new CausingEntityMapping<>(targetClass.cast(obj), cem);
							newMapping.addCausingEntityDistinct(targetClass.cast(obj));
							
							return newMapping;
						}
						else
							return new CausingEntityMapping<>(targetClass.cast(obj), cem);
					}	
				});
			}
			
		}).collect(Collectors.toSet());
	}
	
	public static <T extends EObject> Predicate<T> distinctByEqualityHelper() {
	    Predicate<T> p = new Predicate<T>() {
	    	EqualityHelper eqHelper = new EcoreUtil.EqualityHelper();
	    	KeySetView<EObject, Boolean> seen = ConcurrentHashMap.newKeySet();
		    
			@Override
			public boolean test(T t) {
				for(EObject eobj : seen) {
			    	if(eqHelper.equals(eobj, t)) {
			    		return false;
			    	}
			    }
				
				seen.add(t);
				return true;
			}
		};
		
		return p;
	}
	
//	public static final <T extends EObject, M extends EObject> Collection<CausingEntityMapping<T, M>> lookupBackreference(AbstractArchitectureVersion<?> version, Class<T> targetClass, String featureName, Stream<M> sourceStream) {
//		
//	}
	
	public static final <M extends EObject, T extends EObject> Stream<CausingEntityMapping<T, EObject>> lookupForwardReference(Stream<CausingEntityMapping<M, EObject>> sourceElements, boolean isFeatureMany, String featureName, Class<T> targetClass, boolean addCausingEntities) {
		Stream<CausingEntityMapping<T, EObject>> stream;
		
		if(isFeatureMany) {
			stream = sourceElements.flatMap(new Function<CausingEntityMapping<M, EObject>, Stream<CausingEntityMapping<T, EObject>>>() {

				@Override
				public Stream<CausingEntityMapping<T, EObject>> apply(CausingEntityMapping<M, EObject> e) {
					// the cast is allowed because we checked whether the feature has the is many flag set
					@SuppressWarnings("unchecked")
					EList<T> elements = (EList<T>) e.getAffectedElement().eGet(e.getAffectedElement().eClass().getEStructuralFeature(featureName));
					List<CausingEntityMapping<T, EObject>> causingEntityMappings = new ArrayList<>();
					
					for(T elem : elements) {
						if(elem != null) {
							CausingEntityMapping<T, EObject> newMapping = new CausingEntityMapping<>(elem, e);
							if(addCausingEntities) {
								newMapping.addCausingEntityDistinct(elem);
							}
							
							causingEntityMappings.add(newMapping);
						}
					}
					
					return causingEntityMappings.stream();
				}
			});
		} else {
			stream = sourceElements.map(new Function<CausingEntityMapping<M, EObject>, CausingEntityMapping<T, EObject>>() {

				@Override
				public CausingEntityMapping<T, EObject> apply(CausingEntityMapping<M, EObject> e) {
					// this cast is allowed since we check during scoping if the feature holds the type we expect
					@SuppressWarnings("unchecked")
					T elem = (T) e.getAffectedElement().eGet(e.getAffectedElement().eClass().getEStructuralFeature(featureName));
					if(elem == null)
						return null;
					
					CausingEntityMapping<T, EObject> newMapping = new CausingEntityMapping<>(elem, e);
					if(addCausingEntities) {
						newMapping.addCausingEntityDistinct(elem);
					}
					
					return newMapping;
				}
			});
		}
		
		return stream.filter(obj -> obj != null);
	}
	
	/**
	 * Performs the given {@code lookupMethod} for every marked element of type {@code sourceClass} which is queried from {@code version}
	 * 
	 * @param version the version which contains all elements to be retrieved
	 * @param sourceClass the types of marked elements which are retrieved
	 * @param lookupMethod the lookup method which is applied for each element
	 * @return a stream with mappings from affected elements to their corresponding causing entities
	 */
	public static final <U extends EObject, V extends EObject> Stream<CausingEntityMapping<U, EObject>> lookup(AbstractArchitectureVersion<?> version, Class<V> sourceClass, BiFunction<CausingEntityMapping<V, EObject>, AbstractArchitectureVersion<?>, Set<CausingEntityMapping<U, EObject>>> lookupMethod) {				
		return lookUpMarkedObjectsOfAType(version, sourceClass).stream().flatMap(obj -> lookupMethod.apply(new CausingEntityMapping<V, EObject>(obj), version).stream());
	}
	
	// instead of retrieving marked elements, we provide a source stream
//	public static final <U extends EObject, V extends EObject> Stream<CausingEntityMapping<U, V>> lookup(AbstractArchitectureVersion<?> version, Stream<V> source, BiFunction<V, AbstractArchitectureVersion<?>, Set<U>> lookupMethod) {				
//		return source.flatMap(obj -> createPairsStream(lookupMethod, obj, version));
//	}
	
	/**
	 * Performs the {@code lookupMethod} on the given {@code obj} and creates a {@link CausingEntityMapping} for the affected elements.
	 * 
	 * @param lookupMethod the method which is performed on the given {@code obj}
	 * @param obj the element which is used as the source element of the lookup
	 * @param version the version is the element source which is passed to the {@code lookupMethod} in order to allow the lookup method do to advanced queries (such as resolving backreferences).
	 * @return a stream of mappings from affected element to causing entities
	 */
//	private static <U extends EObject, V extends EObject> Stream<CausingEntityMapping<U, V>> createPairsStream(BiFunction<V, AbstractArchitectureVersion<?>, Set<U>> lookupMethod, V obj, AbstractArchitectureVersion<?> version) {
//		return lookupMethod.apply(obj, version).stream().map(res -> new CausingEntityMapping<U, V>(res, obj));
//	}
}
