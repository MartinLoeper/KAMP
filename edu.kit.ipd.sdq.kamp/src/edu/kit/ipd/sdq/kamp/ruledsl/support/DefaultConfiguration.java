package edu.kit.ipd.sdq.kamp.ruledsl.support;

public class DefaultConfiguration implements IConfiguration {

	@Override
	public boolean areKampStandardRulesEnabled() {
		return true;
	}

	@Override
	public boolean isKampDslEnabled() {
		return true;
	}
}
