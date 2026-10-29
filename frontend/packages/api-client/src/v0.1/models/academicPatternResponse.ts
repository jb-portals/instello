'use client'
import type { AcademicPatternResponseSystemType } from './academicPatternResponseSystemType';
import type { AcademicPatternResponseTemplateKey } from './academicPatternResponseTemplateKey';
import type { AcademicStageResponse } from './academicStageResponse';

export interface AcademicPatternResponse {
  canBeEdited?: boolean;
  createdAt?: string;
  description?: string;
  durationInYears?: number;
  id?: string;
  name?: string;
  organizationId?: string;
  stages?: AcademicStageResponse[];
  systemType?: AcademicPatternResponseSystemType;
  templateKey?: AcademicPatternResponseTemplateKey;
  updatedAt?: string;
}
