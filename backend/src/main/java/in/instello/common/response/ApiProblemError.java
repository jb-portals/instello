package in.instello.common.response;

public record ApiProblemError(String code, String target, String message) {}
