package in.instello.module.accessRequest.dto;

import in.instello.module.accessRequest.entity.AccessRequestStatus;

public record CreateAccessRequestResponse(String id, AccessRequestStatus status) {}
