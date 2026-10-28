'use client'
import type { CreateAccessRequestResponseStatus } from './createAccessRequestResponseStatus';

export interface CreateAccessRequestResponse {
  id?: string;
  status?: CreateAccessRequestResponseStatus;
}
