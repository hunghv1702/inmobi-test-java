package com.hunghv.inmobitestjava.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApiStatus {
    SUCCESS("SUCCESS"),
    FAILED("FAILED");

    private final String value;
}
