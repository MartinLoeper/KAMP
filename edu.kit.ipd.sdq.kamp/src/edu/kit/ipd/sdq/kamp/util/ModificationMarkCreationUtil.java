package edu.kit.ipd.sdq.kamp.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.kamp.model.modificationmarks.AbstractChangePropagationStep;
import edu.kit.ipd.sdq.kamp.model.modificationmarks.AbstractModification;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;
import edu.kit.ipd.sdq.kamp.util.LookupUtil.CausingEntityMapping;

/**
 * This is a utility class which provides utility methods for the most common creation mark insertions.
 * 
 * @author Martin Loeper
 *
 */
public final class ModificationMarkCreationUtil {
	private ModificationMarkCreationUtil() { }
	
	/**
	 * Inserts the elements from the given {@code causingEntityMapping} into the given {@code item}.
	 * @param causingEntityMapping the mapping containing information
	 * @param item the item which is to be initialized
	 * @return a fully initialized item which is ready to be inserted into the tree
	 */
	public static final <T extends AbstractModification<? super U, ? super EObject>, U, V extends EObject> T createModificationMark(CausingEntityMapping<U, V> causingEntityMapping, T item) {
		item.setToolderived(true);
		item.setAffectedElement(causingEntityMapping.getAffectedElement());
		item.getCausingElements().addAll(causingEntityMapping.getCausingEntities()); 
		
		return item;
	}
	
	@SuppressWarnings("unchecked")
	public static final <T extends AbstractChangePropagationStep> void insertModificationMark(AbstractModification<?, ?> modificationMark, ChangePropagationStepRegistry registry, Class<T> changePropagationStepClass, String targetMethodName) {
		Collection<T> changePropagationSteps = registry.getSubtypes(changePropagationStepClass);
		
		if(changePropagationSteps.isEmpty()) {
			throw new UnsupportedOperationException("The ChangePropagationAnalysis does not provide the requested ChangePropagationStep.");
		} else if(changePropagationSteps.size() > 1) {
			throw new UnsupportedOperationException("There is more than one candidate supplied for the selected ChangePropagationStep. Please make a more specific selection.");
		} else {
			T step = changePropagationSteps.iterator().next();
			// we must ensure via scoping that the user does only provide methods which return a list
			try {
				((List<? super EObject>) step.getClass().getMethod(targetMethodName).invoke(step)).add(modificationMark);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				// this should not happen! we must ensure via xText scoping that the methods which are passed via targetMethodName are valid
				e.printStackTrace();
				
				throw new IllegalArgumentException("The given method '" + targetMethodName + "' could not be executed successfully using reflection.", e);
			}
		}
	}
}
