'use client'
import type { UpdateAcademicPatternRequestSystemType } from './updateAcademicPatternRequestSystemType';

export interface UpdateAcademicPatternRequest {
  /**
     * @minLength 0
     * @maxLength 2000
     */
  description?: string;
  /**
     * @minimum 1
     * @maximum 20
     */
  durationInYears?: number;
  /**
     * @minLength 0
     * @maxLength 200
     */
  name?: string;
  systemType?: UpdateAcademicPatternRequestSystemType;
}
