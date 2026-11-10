package in.instello.module.auth.dto;

public record CsrfTokenResponse(String token, String headerName, String parameterName) {}
