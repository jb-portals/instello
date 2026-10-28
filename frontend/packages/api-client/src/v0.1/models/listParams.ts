'use client'
import type { ListStatus } from './listStatus';
import type { Pageable } from './pageable';

export type ListParams = {
status?: ListStatus;
search?: string;
pageable: Pageable;
};
