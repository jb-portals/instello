package in.instello.module.accessRequest.entity;

public enum AccessRequestStatus {
  PENDING,
  CONTACTED,
  APPROVED,
  REJECTED;

  public boolean isOpen() {
    return this == PENDING || this == CONTACTED;
  }
}
