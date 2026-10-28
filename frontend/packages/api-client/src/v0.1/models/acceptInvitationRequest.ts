'use client'

export interface AcceptInvitationRequest {
  /**
     * @minLength 8
     * @maxLength 72
     */
  password: string;
  /** @minLength 1 */
  token: string;
}
