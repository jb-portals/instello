'use client'

export type InvitationInfoStatus = typeof InvitationInfoStatus[keyof typeof InvitationInfoStatus];


export const InvitationInfoStatus = {
  PENDING: 'PENDING',
  ACCEPTED: 'ACCEPTED',
  REVOKED: 'REVOKED',
  EXPIRED: 'EXPIRED',
} as const;
