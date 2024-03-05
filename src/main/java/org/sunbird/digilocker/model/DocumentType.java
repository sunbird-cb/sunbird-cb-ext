package org.sunbird.digilocker.model;

public enum DocumentType {
    OUCER("Course Completion Certificate");

    private final String value;

    DocumentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static String getValueForKey(String key) {
        for (DocumentType type : DocumentType.values()) {
            if (type.name().equals(key)) {
                return type.getValue();
            }
        }
        return null;
    }
}
