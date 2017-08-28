package edu.kit.ipd.sdq.kamp.ruledsl.support;

import java.util.Set;
import java.util.function.Consumer;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;

public interface IRuleProvider {
	void applyAllRules(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis);
	void register(KampRuleStub ruleClass) throws RegistryException;
	long getNumberOfRegisteredRules();
	void runEarlyHook(Consumer<Set<IRule>> instances);
	void setConfiguration(IConfiguration config);
	IConfiguration getConfiguration();
}
