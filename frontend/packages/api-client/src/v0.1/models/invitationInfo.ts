'use client'
import type { InvitationInfoStatus } from './invitationInfoStatus';

export interface InvitationInfo {
  createdAt?: string;
  email?: string;
  expiresAt?: string;
  id?: string;
  status?: InvitationInfoStatus;
}
