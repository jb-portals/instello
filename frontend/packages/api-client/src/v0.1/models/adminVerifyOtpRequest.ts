'use client'

export interface AdminVerifyOtpRequest {
  /**
     * @minLength 1
     * @pattern \d{6}
     */
  code: string;
  /** @minLength 1 */
  email: string;
}
