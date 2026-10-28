'use client'

export type UpdateAccessRequestRequestStatus = typeof UpdateAccessRequestRequestStatus[keyof typeof UpdateAccessRequestRequestStatus];


export const UpdateAccessRequestRequestStatus = {
  PENDING: 'PENDING',
  CONTACTED: 'CONTACTED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;
