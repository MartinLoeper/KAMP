package edu.kit.ipd.sdq.kamp.ruledsl.support;

import java.util.Set;
import java.util.function.Consumer;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;

public interface IRuleProvider {
	void applyAllRules(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry);
	void register(KampRuleStub ruleClass) throws RegistryException;
	long getNumberOfRegisteredRules();
	void runEarlyHook(Consumer<Set<IRule>> instances);
	void setConfiguration(IConfiguration config);
	IConfiguration getConfiguration();
}
