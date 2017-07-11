package edu.kit.ipd.sdq.kamp.ruledsl.support;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;

public interface IRuleProvider {
	void applyAllRules(AbstractArchitectureVersion version, AbstractChangePropagationAnalysis changePropagationAnalysis);
	<T extends IRule> void register(T rule);
	void onRegistryReady();
	boolean areStandardRulesEnabled();
}
