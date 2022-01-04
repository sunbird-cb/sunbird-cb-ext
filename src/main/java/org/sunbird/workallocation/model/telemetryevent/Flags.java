package org.sunbird.workallocation.model.telemetryevent;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Flags {
	@JsonProperty("pp_duplicate_skipped")
	private boolean ppDuplicateSkipped;

	public boolean isPpDuplicateSkipped() {
		return ppDuplicateSkipped;
	}

	public void setPpDuplicateSkipped(boolean ppDuplicateSkipped) {
		this.ppDuplicateSkipped = ppDuplicateSkipped;
	}

	public boolean isPpValidationProcessed() {
		return ppValidationProcessed;
	}

	public void setPpValidationProcessed(boolean ppValidationProcessed) {
		this.ppValidationProcessed = ppValidationProcessed;
	}

	@JsonProperty("pp_validation_processed")
	private boolean ppValidationProcessed;

}
