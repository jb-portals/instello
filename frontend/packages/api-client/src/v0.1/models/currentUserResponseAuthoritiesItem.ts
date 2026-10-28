'use client'

export type CurrentUserResponseAuthoritiesItem = typeof CurrentUserResponseAuthoritiesItem[keyof typeof CurrentUserResponseAuthoritiesItem];


export const CurrentUserResponseAuthoritiesItem = {
  ROLE_ADMIN: 'ROLE_ADMIN',
  ROLE_OWNER: 'ROLE_OWNER',
  ROLE_USER: 'ROLE_USER',
} as const;
