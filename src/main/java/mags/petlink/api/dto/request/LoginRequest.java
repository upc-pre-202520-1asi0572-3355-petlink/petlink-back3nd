package mags.petlink.api.dto.request;

public record LoginRequest(
        String email,
        String password
) {
}
