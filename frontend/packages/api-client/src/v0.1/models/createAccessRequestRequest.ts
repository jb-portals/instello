'use client'
import type { AddressRequest } from './addressRequest';

export interface CreateAccessRequestRequest {
  address: AddressRequest;
  /**
     * @minLength 0
     * @maxLength 255
     */
  email: string;
  /**
     * @minLength 0
     * @maxLength 100
     */
  firstName: string;
  /**
     * @minLength 0
     * @maxLength 100
     */
  lastName: string;
  /**
     * @minLength 0
     * @maxLength 2000
     */
  message?: string;
  /**
     * @minLength 0
     * @maxLength 200
     */
  organizationName: string;
  /**
     * @minLength 1
     * @pattern ^\+?[0-9 ()-]{7,20}$
     */
  phone: string;
}
