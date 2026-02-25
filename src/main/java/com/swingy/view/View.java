package com.swingy.view;

public interface View {
    void println(String s);
    void println(String s, RenderColor color);
    void renderStatus(String status);
    void renderMap(char[][] window);
    String readLine();
    String readLine(long timeoutMillis);
    void clearPendingInput();
    void setQuitLocked(boolean locked);
    boolean consumeQuitAttempt();
    boolean isClosed();
    void close();
}
