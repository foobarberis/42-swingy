package com.swingy.view;

import java.io.IOException;
import java.util.Objects;

public record ViewInput(Type type, String line, String failure) {
    public enum Type {
        LINE,
        END_OF_INPUT,
        VIEW_CLOSED,
        FAILURE
    }

    public ViewInput {
        Objects.requireNonNull(type, "Input type is required.");
        if (type == Type.LINE) {
            Objects.requireNonNull(line, "Input line is required.");
        }
        if (type == Type.FAILURE) {
            Objects.requireNonNull(failure, "Input failure is required.");
        }
    }

    public static ViewInput line(String line) {
        return new ViewInput(Type.LINE, line, null);
    }

    public static ViewInput endOfInput() {
        return new ViewInput(Type.END_OF_INPUT, null, null);
    }

    public static ViewInput viewClosed() {
        return new ViewInput(Type.VIEW_CLOSED, null, null);
    }

    public static ViewInput failure(IOException failure) {
        Objects.requireNonNull(failure, "Input failure is required.");
        String detail = failure.getMessage();
        if (detail == null || detail.isBlank()) {
            detail = failure.getClass().getSimpleName();
        }
        return new ViewInput(Type.FAILURE, null, detail);
    }
}
