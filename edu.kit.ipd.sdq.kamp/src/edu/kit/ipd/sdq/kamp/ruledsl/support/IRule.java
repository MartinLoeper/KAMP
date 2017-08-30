package edu.kit.ipd.sdq.kamp.ruledsl.support;

import edu.kit.ipd.sdq.kamp.architecture.AbstractArchitectureVersion;

public interface IRule {
	void apply(AbstractArchitectureVersion<?> version, ChangePropagationStepRegistry registry);
}
