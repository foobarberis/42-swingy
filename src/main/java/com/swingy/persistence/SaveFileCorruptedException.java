package com.swingy.persistence;

import java.io.IOException;
import java.nio.file.Path;

public class SaveFileCorruptedException extends IOException {
    private final Path path;
    private final int lineNumber;

    public SaveFileCorruptedException(Path path, int lineNumber, String reason) {
        super("Save file " + path.getFileName() + " is corrupted at line " + lineNumber + ": " + reason);
        this.path = path;
        this.lineNumber = lineNumber;
    }

    public SaveFileCorruptedException(Path path, int lineNumber, String reason, Throwable cause) {
        super("Save file " + path.getFileName() + " is corrupted at line " + lineNumber + ": " + reason, cause);
        this.path = path;
        this.lineNumber = lineNumber;
    }

    public Path getPath() {
        return path;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
