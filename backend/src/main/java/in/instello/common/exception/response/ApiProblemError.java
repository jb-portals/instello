package in.instello.common.exception.response;

public record ApiProblemError(String code, String target, String detail) {}
