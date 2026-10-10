package com.myfinance.util;

/**
 * Generic API response wrapper demonstrating Java Generics.
 *
 * @param <T> payload data type
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /**
     * Default constructor for serialization and framework use.
     */
    public ApiResponse() {
    }

    /**
     * Constructs a full API response wrapper.
     *
     * @param success whether operation succeeded
     * @param message descriptive message
     * @param data response payload
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * Creates a successful response with default success message.
     *
     * @param <T> payload type
     * @param data response payload
     * @return successful ApiResponse instance
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Operation successful", data);
    }

    /**
     * Creates a successful response with custom message.
     *
     * @param <T> payload type
     * @param message success explanation
     * @param data response payload
     * @return successful ApiResponse instance
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Creates an error response with error description.
     *
     * @param <T> payload type
     * @param message error description
     * @return error ApiResponse instance
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    /**
     * Returns true if operation succeeded.
     *
     * @return success flag
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets success flag.
     *
     * @param success success flag
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Returns response message.
     *
     * @return message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets response message.
     *
     * @param message message text
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Returns payload data.
     *
     * @return payload object
     */
    public T getData() {
        return data;
    }

    /**
     * Sets payload data.
     *
     * @param data payload object
     */
    public void setData(T data) {
        this.data = data;
    }
}
