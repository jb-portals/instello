'use client'
import type { AccessRequestSummary } from './accessRequestSummary';

export interface PageResponseAccessRequestSummary {
  content?: AccessRequestSummary[];
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
}
