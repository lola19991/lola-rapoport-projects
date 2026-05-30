package com.lola.cloudguard.cli;

enum ScanInputFormat {
    AUTO,
    YAML,
    TERRAFORM_PLAN;

    static ScanInputFormat from(String value) {
        if (value == null || value.isBlank()) {
            return AUTO;
        }
        return ScanInputFormat.valueOf(value.trim().replace('-', '_').replace('.', '_').toUpperCase());
    }
}
