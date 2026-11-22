package com.harmoni.pos.mcp.application.security;

import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.exception.MissingCallerException;

public final class CallerContextHolder {

    private static final ThreadLocal<CallerContext> CURRENT = new ThreadLocal<>();

    private CallerContextHolder() {
    }

    public static void set(CallerContext caller) {
        CURRENT.set(caller);
    }

    public static CallerContext current() {
        CallerContext caller = CURRENT.get();
        if (caller == null || !caller.isPresent()) {
            throw new MissingCallerException();
        }
        return caller;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
