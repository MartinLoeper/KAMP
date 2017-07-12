package edu.kit.ipd.sdq.kamp.ruledsl.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.kit.ipd.sdq.kamp.model.modificationmarks.AbstractChangePropagationStep;

/**
 * This is basically a heterogenous typesafe container implementation which stores {@link AbstractChangePropagationStep}s.
 * 
 * @author Martin Löper
 */
public final class ChangePropagationStepRegistry implements Iterable<AbstractChangePropagationStep> {
	private Map<Class<? extends AbstractChangePropagationStep>, Object> changePropagationSteps = new HashMap<>();
	
	public <T extends AbstractChangePropagationStep> void register(T changePropagationStep) {
		this.changePropagationSteps.put(changePropagationStep.getClass(), changePropagationStep);
	}
	
	public <T extends AbstractChangePropagationStep> T get(Class<T> changePropagationStepClass) {
		return changePropagationStepClass.cast(this.changePropagationSteps.get(changePropagationStepClass));
	}
	
	public <T extends AbstractChangePropagationStep> Collection<T> getSubtype(Class<T> changePropagationStepClass) {
		Collection<T> results = new ArrayList<>();
		
		for(Entry<Class<? extends AbstractChangePropagationStep>, Object> entry : this.changePropagationSteps.entrySet()) {
			if(changePropagationStepClass.isAssignableFrom(entry.getKey())) {
				results.add( (T) entry.getValue());
			}
		}
		
		return results;
	}

	@Override
	public Iterator<AbstractChangePropagationStep> iterator() {
		Collection<AbstractChangePropagationStep> result = new HashSet<>();
		
		for(Entry<Class<? extends AbstractChangePropagationStep>, Object> entry : this.changePropagationSteps.entrySet()) {
			result.add(entry.getKey().cast(entry.getValue()));
		}
		
		return result.iterator();
	}
}
