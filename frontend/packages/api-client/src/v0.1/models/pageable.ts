'use client'

export interface Pageable {
  /** @minimum 0 */
  page?: number;
  /** @minimum 1 */
  size?: number;
  sort?: string[];
}
