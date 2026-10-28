'use client'
import type { AccessRequestDetailStatus } from './accessRequestDetailStatus';
import type { AddressResponse } from './addressResponse';
import type { InvitationInfo } from './invitationInfo';
import type { OrganizationInfo } from './organizationInfo';

export interface AccessRequestDetail {
  address?: AddressResponse;
  adminNotes?: string;
  createdAt?: string;
  email?: string;
  firstName?: string;
  id?: string;
  invitation?: InvitationInfo;
  lastName?: string;
  message?: string;
  organization?: OrganizationInfo;
  organizationName?: string;
  phone?: string;
  rejectionReason?: string;
  reviewedAt?: string;
  reviewedBy?: string;
  status?: AccessRequestDetailStatus;
  updatedAt?: string;
}
