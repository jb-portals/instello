'use client'

export type AccessRequestDetailStatus = typeof AccessRequestDetailStatus[keyof typeof AccessRequestDetailStatus];


export const AccessRequestDetailStatus = {
  PENDING: 'PENDING',
  CONTACTED: 'CONTACTED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;
