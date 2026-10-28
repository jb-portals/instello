'use client'

export type AccessRequestSummaryStatus = typeof AccessRequestSummaryStatus[keyof typeof AccessRequestSummaryStatus];


export const AccessRequestSummaryStatus = {
  PENDING: 'PENDING',
  CONTACTED: 'CONTACTED',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
} as const;
