package edu.kit.ipd.sdq.kamp.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.EcoreUtil.EqualityHelper;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.architecture.CrossReferenceProvider;
import static edu.kit.ipd.sdq.kamp.architecture.ArchitectureModelLookup.*;

public final class LookupUtil {
	private LookupUtil() {}
	
	public static final <T extends EObject, M extends EObject> Collection<T> lookupBackreference(AbstractArchitectureVersion<?> version, Class<T> targetClass, Stream<M> sourceElements) {
		if(!(version instanceof CrossReferenceProvider)) {
			throw new UnsupportedOperationException("The current ArchitectureVersion does not support following backreferences. It must implement CrossReferenceProvider to do so.");
		}
		
		CrossReferenceProvider crossReferenceProvider = (CrossReferenceProvider) version;
		
		List<T> result = sourceElements.flatMap(sig -> crossReferenceProvider.getECrossReferenceAdapter().getInverseReferences(sig, true).stream()).filter(setting -> targetClass.isAssignableFrom(setting.getEObject().getClass())).map(Setting::getEObject).map(obj -> targetClass.cast(obj)).distinct().collect(Collectors.toList());
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
	
	// TODO make typesafe!
	public static final <U, V> Stream<CausingEntityMapping<U, V>> lookupMarkedObjectsWithLookupMethod(AbstractArchitectureVersion version, Class<V> sourceClass, Class<U> targetClass, BiFunction<V, AbstractArchitectureVersion, Set> lookupMethod) {				
		// TODO make this typesafe by changing the return type of generated functions!! then remove the cast...
		// return lookUpMarkedObjectsOfAType(version, sourceClass).stream().flatMap(obj -> lookupMethod.apply(obj, version).stream()).map(obj -> targetClass.cast(obj));
		
		return lookUpMarkedObjectsOfAType(version, sourceClass).stream().flatMap(obj -> createPairsStream(lookupMethod, obj, version, targetClass));
	}
	
	private static <U, V> Stream<CausingEntityMapping<U, V>> createPairsStream(BiFunction<V, AbstractArchitectureVersion, Set> lookupMethod, V obj, AbstractArchitectureVersion version, Class<U> targetClass) {
		return lookupMethod.apply(obj, version).stream().map(res -> new CausingEntityMapping<U, V>(targetClass.cast(res), obj));
	}

	public static final class CausingEntityMapping<U, V> {
		private final U element;
		private final V causingEntity;
		
		public CausingEntityMapping(U element, V causingEntity) {
			this.element = element;
			this.causingEntity = causingEntity;
		}

		public U getElement() {
			return element;
		}

		public V getCausingEntity() {
			return causingEntity;
		}
	}
}
