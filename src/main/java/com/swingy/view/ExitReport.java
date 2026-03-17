package com.swingy.view;

import java.util.Objects;

public record ExitReport(
    Reason reason,
    SaveState saveState,
    String inputFailure,
    String saveFailure
) {
    public enum Reason {
        QUIT,
        END_OF_INPUT,
        VIEW_CLOSED,
        INPUT_FAILURE
    }

    public enum SaveState {
        NOT_REQUIRED,
        SAVED,
        FAILED
    }

    public ExitReport {
        Objects.requireNonNull(reason, "Exit reason is required.");
        Objects.requireNonNull(saveState, "Save state is required.");
    }
}
