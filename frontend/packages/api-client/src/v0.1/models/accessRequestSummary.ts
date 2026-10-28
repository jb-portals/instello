'use client'
import type { AccessRequestSummaryStatus } from './accessRequestSummaryStatus';

export interface AccessRequestSummary {
  city?: string;
  createdAt?: string;
  email?: string;
  firstName?: string;
  id?: string;
  lastName?: string;
  organizationName?: string;
  phone?: string;
  state?: string;
  status?: AccessRequestSummaryStatus;
}
