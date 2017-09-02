package edu.kit.ipd.sdq.kamp.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.kamp.model.modificationmarks.AbstractModification;
import edu.kit.ipd.sdq.kamp.ruledsl.support.CausingEntityMapping;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;

/**
 * This class provides utility methods which support the management of ModificationMarks.
 * Inside a propagation step, we must ensure that ModificationMarks do not occur twice.
 * Instead, we append new causing entities to the existing ModificationMark.
 * 
 * @author Martin Loeper
 *
 */
public final class PropagationStepUtil {
	private PropagationStepUtil() {}
	
	/**
	 * Creates a new mapping for elements marked in a particular DuplicateAwareStep.
	 * @return a mapping between an affected object and its corresponding ModificationMark
	 */
	public static Map<EObject, AbstractModification<?, EObject>> createNewMap() {
		return new HashMap<EObject, AbstractModification<?, EObject>>();
	}
	
	public static boolean isNewEntry(CausingEntityMapping<?, ?> cem, int stepId, ChangePropagationStepRegistry registry) {

		if(stepId < 0)
			return true;
		
		Map<EObject, AbstractModification<?, EObject>> mapping = registry.getModificationMarkRegistry(stepId);
		if(mapping == null) {
			registry.setModificationMarkRegistry(stepId, createNewMap());
			
			return true;
		}
		
		return mapping.get(cem.getAffectedElement()) == null;
	}
	
	public static void addToExistingModificationMark(CausingEntityMapping<?, ?> cem, int stepId, ChangePropagationStepRegistry registry) {
		if(stepId < 0) {
			throw new IllegalArgumentException("The stepId must be non-negative.");
		}
		
		Map<EObject, AbstractModification<?, EObject>> mapping = registry.getModificationMarkRegistry(stepId);
		if(mapping == null) {
			throw new IllegalStateException("Could not find registry. This should not happen. Please ensure you called isNewEntry() before.");
		}
		
		AbstractModification<?, EObject> modificationMark = mapping.get(cem.getAffectedElement());
		if(modificationMark == null) { 
			throw new IllegalStateException("Could not find modification mark. Please ensure that the call to isNewEntry() returned false.");
		}
		
		for(EObject cCausingEntity : cem.getCausingEntities()) {
			if(!modificationMark.getCausingElements().contains(cCausingEntity)) {
				modificationMark.getCausingElements().add(cCausingEntity);
			}
		}
	}
	
	public static void addNewModificationMark(CausingEntityMapping<?, ?> cem, AbstractModification<?, EObject> modificationMark, int stepId, ChangePropagationStepRegistry registry) {
		// we do not add the mark to a registry if it is produced by an IndependentStep
		if(stepId < 0) {
			return;
		}
		
		Map<EObject, AbstractModification<?, EObject>> mapping = registry.getModificationMarkRegistry(stepId);
		if(mapping == null) {
			throw new IllegalStateException("Could not find registry. This should not happen. Please ensure you called isNewEntry() before.");
		}
		
		mapping.put(cem.getAffectedElement(), modificationMark);
	}
}
