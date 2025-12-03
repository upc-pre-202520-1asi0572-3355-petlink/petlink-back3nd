package mags.petlink.api.dto.response;

public record LoginResponse(
        boolean success,
        String message
) {
}
