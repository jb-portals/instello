'use client'

export interface AddressRequest {
  /**
     * @minLength 0
     * @maxLength 255
     */
  addressLine: string;
  /**
     * @minLength 0
     * @maxLength 100
     */
  city: string;
  /**
     * @minLength 0
     * @maxLength 100
     */
  country?: string;
  /**
     * @minLength 0
     * @maxLength 100
     */
  state: string;
}
