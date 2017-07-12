package edu.kit.ipd.sdq.kamp.ruledsl.support;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;
import edu.kit.ipd.sdq.kamp.ruledsl.support.ChangePropagationStepRegistry;

public interface IRule {
	void apply(AbstractArchitectureVersion version, ChangePropagationStepRegistry registry, AbstractChangePropagationAnalysis changePropagationAnalysis);
}
