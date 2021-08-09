package org.sunbird.workallocation.model.telemetryEvent;

public class Flags {
    private boolean pp_duplicate_skipped;
    private boolean pp_validation_processed;

    public boolean isPp_duplicate_skipped() {
        return pp_duplicate_skipped;
    }

    public void setPp_duplicate_skipped(boolean pp_duplicate_skipped) {
        this.pp_duplicate_skipped = pp_duplicate_skipped;
    }

    public boolean isPp_validation_processed() {
        return pp_validation_processed;
    }

    public void setPp_validation_processed(boolean pp_validation_processed) {
        this.pp_validation_processed = pp_validation_processed;
    }
}
