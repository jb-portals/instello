'use client'
import type { AddressRequest } from './addressRequest';

export interface ApproveAccessRequestRequest {
  address?: AddressRequest;
  /**
     * @minLength 0
     * @maxLength 255
     */
  email?: string;
  /**
     * @minLength 1
     * @maxLength 200
     */
  organizationName?: string;
  /** @pattern ^\+?[0-9 ()-]{7,20}$ */
  phone?: string;
}
