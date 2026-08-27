package com.hunghv.inmobitestjava.util;

import org.springframework.http.HttpStatus;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ApiResponseUtils {

    @SuppressWarnings("unchecked")
    public static <R, D> R createResponse(Class<R> responseClass, HttpStatus status, String message, D data) {
        try {
            R response = responseClass.getDeclaredConstructor().newInstance();

            Method codeMethod = responseClass.getMethod("code", Integer.class);
            codeMethod.invoke(response, status.value());

            Method messageMethod = responseClass.getMethod("message", String.class);
            messageMethod.invoke(response, message);

            if (data != null) {
                Method dataMethod = Arrays.stream(responseClass.getMethods())
                    .filter(m -> m.getName().equals("data") && m.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new NoSuchMethodException("No 'data' method found in " + responseClass.getName()));
                dataMethod.invoke(response, data);
            }

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct API response wrapper for " + responseClass.getName(), e);
        }
    }

    public static <R, D> R success(Class<R> responseClass, D data) {
        return createResponse(responseClass, HttpStatus.OK, "Success", data);
    }

    public static <R, D> R created(Class<R> responseClass, D data) {
        return createResponse(responseClass, HttpStatus.CREATED, "Created", data);
    }
}
