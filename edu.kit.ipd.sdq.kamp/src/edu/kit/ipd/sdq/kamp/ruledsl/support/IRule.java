package edu.kit.ipd.sdq.kamp.ruledsl.support;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;
import edu.kit.ipd.sdq.kamp.propagation.AbstractChangePropagationAnalysis;

public interface IRule {
	void apply(AbstractArchitectureVersion version, AbstractChangePropagationAnalysis changePropagationAnalysis);
}
