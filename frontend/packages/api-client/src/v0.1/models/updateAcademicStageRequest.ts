'use client'

export interface UpdateAcademicStageRequest {
  /**
     * @minLength 0
     * @maxLength 100
     */
  alias?: string;
  /**
     * @minLength 0
     * @maxLength 2000
     */
  description?: string;
  /**
     * @minLength 0
     * @maxLength 200
     */
  name?: string;
  /** @minimum 1 */
  sequenceNumber?: number;
  /**
     * @minimum 1
     * @maximum 20
     */
  yearNumber?: number;
}
