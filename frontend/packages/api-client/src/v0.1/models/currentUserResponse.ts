'use client'
import type { CurrentUserResponseAuthoritiesItem } from './currentUserResponseAuthoritiesItem';
import type { Profile } from './profile';

export interface CurrentUserResponse {
  authorities?: CurrentUserResponseAuthoritiesItem[];
  email?: string;
  emailVerifiedAt?: string;
  id?: string;
  profile?: Profile;
}
