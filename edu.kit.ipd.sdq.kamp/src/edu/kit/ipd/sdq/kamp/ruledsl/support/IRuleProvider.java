package edu.kit.ipd.sdq.kamp.ruledsl.support;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;

public interface IRuleProvider {
	void applyAllRules(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis);
	<T extends IRule> void register(T rule);
	void onRegistryReady();
	boolean areStandardRulesEnabled();
}
