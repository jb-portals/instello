'use client'

export type ListStatus = typeof ListStatus[keyof typeof ListStatus];


export const ListStatus = {
  PENDING: 'PENDING',
  CONTACTED: 'CONTACTED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;
