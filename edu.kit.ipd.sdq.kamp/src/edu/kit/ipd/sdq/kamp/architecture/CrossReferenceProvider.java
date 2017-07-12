package edu.kit.ipd.sdq.kamp.architecture;

import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;

public interface CrossReferenceProvider {
	/**
	 * Returns the cross reference adapter which is assigned to the underlying ResourceSet if available.
	 * <br><br>
	 * Caution! This method might return null.
	 * This is the case if there is no underlying ResourceSet available such as in e.g. {@link BPArchitectureModelFactoryFacade#createEmptyBPModel(String)}.
	 * 
	 * @return the assigned cross reference adapter or null
	 */
	public ECrossReferenceAdapter getECrossReferenceAdapter();
}
