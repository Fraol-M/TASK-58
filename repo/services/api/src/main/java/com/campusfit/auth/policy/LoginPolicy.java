package com.campusfit.auth.policy;

public final class LoginPolicy {

    public static final int MAX_ATTEMPTS = 5;
    public static final int LOCKOUT_MINUTES = 15;
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    private LoginPolicy() {
    }
}
