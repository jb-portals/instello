'use client'

export type CreateAccessRequestResponseStatus = typeof CreateAccessRequestResponseStatus[keyof typeof CreateAccessRequestResponseStatus];


export const CreateAccessRequestResponseStatus = {
  PENDING: 'PENDING',
  CONTACTED: 'CONTACTED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;
