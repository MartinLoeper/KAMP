package edu.kit.ipd.sdq.kamp.ruledsl.support;

import edu.kit.ipd.sdq.kamp.ruledsl.support.IRule;

/**
 * This is a wrapper around an IRule which is to be instantiated.
 * It contains some information which is necessary for the initializer in order to perform the dependency injection etc.
 * 
 * @author Martin Loeper
 *
 */
public class KampRuleStub {
	private final Class<? extends IRule> clazz;
	private final Class<? extends IRule> parent;
	private final boolean active;
	
	public KampRuleStub(Class<? extends IRule> clazz, Class<? extends IRule> parent, boolean active) {
		this.clazz = clazz;
		this.parent = parent;
		this.active = active;
	}

	public Class<? extends IRule> getClazz() {
		return clazz;
	}

	public Class<? extends IRule> getParent() {
		return parent;
	}

	public boolean isActive() {
		return active;
	}
	
	public boolean hasParent() {
		return this.parent != null;
	}
}
