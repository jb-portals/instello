'use client'

export type AcademicPatternResponseSystemType = typeof AcademicPatternResponseSystemType[keyof typeof AcademicPatternResponseSystemType];


export const AcademicPatternResponseSystemType = {
  SEMESTER: 'SEMESTER',
  ANNUAL: 'ANNUAL',
} as const;
