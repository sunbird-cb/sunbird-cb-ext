package org.sunbird.workallocation.model.telemetryevent;

public class Flags {
    private boolean ppDuplicateSkipped;
    private boolean ppValidationProcessed;

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
}
