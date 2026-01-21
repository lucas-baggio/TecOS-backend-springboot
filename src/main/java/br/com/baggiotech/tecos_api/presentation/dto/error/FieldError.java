package br.com.baggiotech.tecos_api.presentation.dto.error;

public record FieldError(
    String field,
    String message,
    Object rejectedValue
) {
}
