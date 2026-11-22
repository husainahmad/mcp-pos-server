package com.harmoni.pos.mcp.domain.exception;

public class MissingCallerException extends RuntimeException {

    public MissingCallerException() {
        super("Missing gateway identity headers");
    }
}
