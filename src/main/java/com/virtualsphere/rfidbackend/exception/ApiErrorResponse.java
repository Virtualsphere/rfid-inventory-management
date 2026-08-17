package com.virtualsphere.rfidbackend.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(int status, String message, Map<String, String> fieldErrors, Instant timestamp) {
}
