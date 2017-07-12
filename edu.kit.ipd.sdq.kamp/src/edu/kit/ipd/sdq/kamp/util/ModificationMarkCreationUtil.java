package edu.kit.ipd.sdq.kamp.util;

import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.kamp.model.modificationmarks.AbstractModification;
import edu.kit.ipd.sdq.kamp.util.LookupUtil.CausingEntityMapping;

public final class ModificationMarkCreationUtil {
	private ModificationMarkCreationUtil() { }
	
	public static final <T extends AbstractModification<U, ? super EObject>, U, V extends EObject> T createModificationMark(CausingEntityMapping<U, V> affectedElementMapping, T item) {
		item.setToolderived(true);
		item.setAffectedElement(affectedElementMapping.getElement());
		item.getCausingElements().add(affectedElementMapping.getCausingEntity()); 
		
		return item;
	}
}
