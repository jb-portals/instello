'use client'
import type { UpdateAccessRequestRequestStatus } from './updateAccessRequestRequestStatus';

export interface UpdateAccessRequestRequest {
  /**
     * @minLength 0
     * @maxLength 5000
     */
  adminNotes?: string;
  status?: UpdateAccessRequestRequestStatus;
}
