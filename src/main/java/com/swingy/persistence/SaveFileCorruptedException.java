package com.swingy.persistence;

import java.io.IOException;
import java.nio.file.Path;

public final class SaveFileCorruptedException extends IOException {
    private static final long serialVersionUID = 1L;

    private final transient Path path;
    private final int lineNumber;
    private final String reason;

    public SaveFileCorruptedException(Path path, int lineNumber, String reason) {
        super(message(path, lineNumber, reason));
        this.path = path;
        this.lineNumber = lineNumber;
        this.reason = reason;
    }

    public SaveFileCorruptedException(Path path, int lineNumber, String reason, Throwable cause) {
        super(message(path, lineNumber, reason), cause);
        this.path = path;
        this.lineNumber = lineNumber;
        this.reason = reason;
    }

    public Path getPath() {
        return path;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getReason() {
        return reason;
    }

    private static String message(Path path, int lineNumber, String reason) {
        Path name = path == null ? null : path.getFileName();
        String displayName = name == null ? "<unknown>" : name.toString();
        return "Save file "
            + displayName
            + " is corrupted (line "
            + lineNumber
            + "): "
            + reason
            + ".";
    }
}
