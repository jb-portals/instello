'use client'
import {
  useMutation,
  useQuery
} from '@tanstack/react-query';
import type {
  DataTag,
  DefinedInitialDataOptions,
  DefinedUseQueryResult,
  MutationFunction,
  QueryClient,
  QueryFunction,
  QueryKey,
  UndefinedInitialDataOptions,
  UseMutationOptions,
  UseMutationResult,
  UseQueryOptions,
  UseQueryResult
} from '@tanstack/react-query';

import {
  apiBaseUrl
} from '../../mutator';

import type {
  AcademicPatternResponse,
  AcademicStageResponse,
  AcceptInvitationRequest,
  AcceptInvitationResponse,
  AccessRequestDetail,
  AdminLoginResponse,
  AdminOtpRequest,
  AdminVerifyOtpRequest,
  ApproveAccessRequestRequest,
  CreateAccessRequestRequest,
  CreateAccessRequestResponse,
  CsrfTokenResponse,
  CurrentUserResponse,
  EmailOtpRequest,
  GetCsrfTokenParams,
  InvitationPreviewResponse,
  ListParams,
  LoginRequest,
  PageResponseAccessRequestSummary,
  PreviewInvitationParams,
  RejectAccessRequestRequest,
  UpdateAcademicPatternRequest,
  UpdateAcademicStageRequest,
  UpdateAccessRequestRequest,
  VerifyEmailRequest
} from '../models';

import { apiFetchData } from '../../mutator';
import type { ErrorType , BodyType } from '../../mutator';


type SecondParameter<T extends (...args: never) => unknown> = Parameters<T>[1];



const withQueryKey = <T extends object, K>(query: T, queryKey: K): T & { queryKey: K } => {
  const result = { queryKey } as T & { queryKey: K };
  for (const key of Object.keys(query)) {
    // The explicit queryKey always wins, matching the previous
    // `{ ...query, queryKey }` spread where it was set last.
    if (key === 'queryKey') continue;
    Object.defineProperty(result, key, {
      enumerable: true,
      configurable: true,
      get: () => (query as Record<string, unknown>)[key],
    });
  }
  return result;
};

export const getListUrl = (params: ListParams,) => {
  const normalizedParams = new URLSearchParams();
  const formObjectParameters = new Map<string, string>([["pageable","flatten"]]);

  Object.entries(params || {}).forEach(([key, value]) => {
    if (typeof value === 'object' && value !== null && !Array.isArray(value) && !(value instanceof Date) && formObjectParameters.has(key)) {
      const commaParts: string[] = [];
      Object.entries(value).forEach(([subKey, subValue]) => {
        const items = (Array.isArray(subValue) ? subValue : [subValue])
          .filter((v) => v !== undefined)
          .map((v) => v === null ? 'null' : v instanceof Date ? v.toISOString() : typeof v === 'object' ? JSON.stringify(v) : String(v));
        if (formObjectParameters.get(key) === 'flatten') {
          items.forEach((item) => normalizedParams.append(subKey, item));
        } else if (items.length > 0) {
          commaParts.push(subKey, ...items);
        }
      });
      if (commaParts.length > 0) {
        normalizedParams.append(key, commaParts.join(','));
      }
      return;
    }

    if (value !== undefined) {
      normalizedParams.append(key, value === null ? 'null' : String(value))
    }
  });

  const stringifiedParams = normalizedParams.toString();

  return stringifiedParams.length > 0 ? `${apiBaseUrl()}/v0.1/access-requests?${stringifiedParams}` : `${apiBaseUrl()}/v0.1/access-requests`
}

export const list = async (params: ListParams, options?: Parameters<typeof apiFetchData>[1]): Promise<PageResponseAccessRequestSummary> => {

  return apiFetchData<PageResponseAccessRequestSummary>(getListUrl(params),
  {
    ...options,
    method: 'GET'


  }
);}





export const getListQueryKey = (params?: ListParams,) => {
    return [
    `${apiBaseUrl()}/v0.1/access-requests`, ...(params ? [params] : [])
    ] as const;
    }


export const getListQueryOptions = <TData = Awaited<ReturnType<typeof list>>, TError = ErrorType<unknown>>(params: ListParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof list>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getListQueryKey(params);



    const queryFn: QueryFunction<Awaited<ReturnType<typeof list>>> = ({ signal }) => list(params, { signal, ...requestOptions });





   return  { queryKey, queryFn, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof list>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type ListQueryResult = NonNullable<Awaited<ReturnType<typeof list>>>
export type ListQueryError = ErrorType<unknown>


export function useList<TData = Awaited<ReturnType<typeof list>>, TError = ErrorType<unknown>>(
 params: ListParams, options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof list>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof list>>,
          TError,
          Awaited<ReturnType<typeof list>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useList<TData = Awaited<ReturnType<typeof list>>, TError = ErrorType<unknown>>(
 params: ListParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof list>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof list>>,
          TError,
          Awaited<ReturnType<typeof list>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useList<TData = Awaited<ReturnType<typeof list>>, TError = ErrorType<unknown>>(
 params: ListParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof list>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function useList<TData = Awaited<ReturnType<typeof list>>, TError = ErrorType<unknown>>(
 params: ListParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof list>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getListQueryOptions(params,options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







export const getSubmitUrl = () => {




  return `${apiBaseUrl()}/v0.1/access-requests`
}

export const submit = async (createAccessRequestRequest: CreateAccessRequestRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<CreateAccessRequestResponse> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<CreateAccessRequestResponse>(getSubmitUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(createAccessRequestRequest)
  }
);}





export const getSubmitMutationKey = () => ['submit'] as const;

export const getSubmitMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof submit>>, TError,SubmitMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof submit>>, TError,SubmitMutationVariables, TContext> => {

const mutationKey = getSubmitMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof submit>>, SubmitMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  submit(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type SubmitMutationResult = NonNullable<Awaited<ReturnType<typeof submit>>>
    export type SubmitMutationBody = BodyType<CreateAccessRequestRequest>
    export type SubmitMutationError = ErrorType<unknown>
    export type SubmitMutationVariables = {data: BodyType<CreateAccessRequestRequest>}

    export const useSubmit = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof submit>>, TError,SubmitMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof submit>>,
        TError,
        SubmitMutationVariables,
        TContext
      > => {
      return useMutation(getSubmitMutationOptions(options), queryClient);
    }

export const getGetUrl = (id: string,) => {




  return `${apiBaseUrl()}/v0.1/access-requests/${id}`
}

export const get = async (id: string, options?: Parameters<typeof apiFetchData>[1]): Promise<AccessRequestDetail> => {

  return apiFetchData<AccessRequestDetail>(getGetUrl(id),
  {
    ...options,
    method: 'GET'


  }
);}





export const getGetQueryKey = (id: string,) => {
    return [
    `${apiBaseUrl()}/v0.1/access-requests/${id}`
    ] as const;
    }


export const getGetQueryOptions = <TData = Awaited<ReturnType<typeof get>>, TError = ErrorType<unknown>>(id: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof get>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getGetQueryKey(id);



    const queryFn: QueryFunction<Awaited<ReturnType<typeof get>>> = ({ signal }) => get(id, { signal, ...requestOptions });





   return  { queryKey, queryFn, enabled: id !== null && id !== undefined, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof get>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type GetQueryResult = NonNullable<Awaited<ReturnType<typeof get>>>
export type GetQueryError = ErrorType<unknown>


export function useGet<TData = Awaited<ReturnType<typeof get>>, TError = ErrorType<unknown>>(
 id: string, options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof get>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof get>>,
          TError,
          Awaited<ReturnType<typeof get>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useGet<TData = Awaited<ReturnType<typeof get>>, TError = ErrorType<unknown>>(
 id: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof get>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof get>>,
          TError,
          Awaited<ReturnType<typeof get>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useGet<TData = Awaited<ReturnType<typeof get>>, TError = ErrorType<unknown>>(
 id: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof get>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function useGet<TData = Awaited<ReturnType<typeof get>>, TError = ErrorType<unknown>>(
 id: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof get>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getGetQueryOptions(id,options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







export const getUpdateUrl = (id: string,) => {




  return `${apiBaseUrl()}/v0.1/access-requests/${id}`
}

export const update = async (id: string,
    updateAccessRequestRequest: UpdateAccessRequestRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AccessRequestDetail> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AccessRequestDetail>(getUpdateUrl(id),
  {
    ...options,
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(updateAccessRequestRequest)
  }
);}





export const getUpdateMutationKey = () => ['update'] as const;

export const getUpdateMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof update>>, TError,UpdateMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof update>>, TError,UpdateMutationVariables, TContext> => {

const mutationKey = getUpdateMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof update>>, UpdateMutationVariables> = (props) => {
          const {id,data} = props ?? {};

          return  update(id,data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type UpdateMutationResult = NonNullable<Awaited<ReturnType<typeof update>>>
    export type UpdateMutationBody = BodyType<UpdateAccessRequestRequest>
    export type UpdateMutationError = ErrorType<unknown>
    export type UpdateMutationVariables = {id: string;data: BodyType<UpdateAccessRequestRequest>}

    export const useUpdate = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof update>>, TError,UpdateMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof update>>,
        TError,
        UpdateMutationVariables,
        TContext
      > => {
      return useMutation(getUpdateMutationOptions(options), queryClient);
    }

export const getApproveUrl = (id: string,) => {




  return `${apiBaseUrl()}/v0.1/access-requests/${id}/approve`
}

export const approve = async (id: string,
    approveAccessRequestRequest?: ApproveAccessRequestRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AccessRequestDetail> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AccessRequestDetail>(getApproveUrl(id),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(approveAccessRequestRequest)
  }
);}





export const getApproveMutationKey = () => ['approve'] as const;

export const getApproveMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof approve>>, TError,ApproveMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof approve>>, TError,ApproveMutationVariables, TContext> => {

const mutationKey = getApproveMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof approve>>, ApproveMutationVariables> = (props) => {
          const {id,data} = props ?? {};

          return  approve(id,data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type ApproveMutationResult = NonNullable<Awaited<ReturnType<typeof approve>>>
    export type ApproveMutationBody = BodyType<ApproveAccessRequestRequest> | undefined
    export type ApproveMutationError = ErrorType<unknown>
    export type ApproveMutationVariables = {id: string;data?: BodyType<ApproveAccessRequestRequest>}

    export const useApprove = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof approve>>, TError,ApproveMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof approve>>,
        TError,
        ApproveMutationVariables,
        TContext
      > => {
      return useMutation(getApproveMutationOptions(options), queryClient);
    }

export const getRejectUrl = (id: string,) => {




  return `${apiBaseUrl()}/v0.1/access-requests/${id}/reject`
}

export const reject = async (id: string,
    rejectAccessRequestRequest: RejectAccessRequestRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AccessRequestDetail> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AccessRequestDetail>(getRejectUrl(id),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(rejectAccessRequestRequest)
  }
);}





export const getRejectMutationKey = () => ['reject'] as const;

export const getRejectMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof reject>>, TError,RejectMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof reject>>, TError,RejectMutationVariables, TContext> => {

const mutationKey = getRejectMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof reject>>, RejectMutationVariables> = (props) => {
          const {id,data} = props ?? {};

          return  reject(id,data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type RejectMutationResult = NonNullable<Awaited<ReturnType<typeof reject>>>
    export type RejectMutationBody = BodyType<RejectAccessRequestRequest>
    export type RejectMutationError = ErrorType<unknown>
    export type RejectMutationVariables = {id: string;data: BodyType<RejectAccessRequestRequest>}

    export const useReject = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof reject>>, TError,RejectMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof reject>>,
        TError,
        RejectMutationVariables,
        TContext
      > => {
      return useMutation(getRejectMutationOptions(options), queryClient);
    }

export const getResendInvitationUrl = (id: string,) => {




  return `${apiBaseUrl()}/v0.1/access-requests/${id}/resend-invitation`
}

export const resendInvitation = async (id: string, options?: Parameters<typeof apiFetchData>[1]): Promise<AccessRequestDetail> => {

  return apiFetchData<AccessRequestDetail>(getResendInvitationUrl(id),
  {
    ...options,
    method: 'POST'


  }
);}





export const getResendInvitationMutationKey = () => ['resendInvitation'] as const;

export const getResendInvitationMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof resendInvitation>>, TError,ResendInvitationMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof resendInvitation>>, TError,ResendInvitationMutationVariables, TContext> => {

const mutationKey = getResendInvitationMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof resendInvitation>>, ResendInvitationMutationVariables> = (props) => {
          const {id} = props ?? {};

          return  resendInvitation(id,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type ResendInvitationMutationResult = NonNullable<Awaited<ReturnType<typeof resendInvitation>>>

    export type ResendInvitationMutationError = ErrorType<unknown>
    export type ResendInvitationMutationVariables = {id: string}

    export const useResendInvitation = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof resendInvitation>>, TError,ResendInvitationMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof resendInvitation>>,
        TError,
        ResendInvitationMutationVariables,
        TContext
      > => {
      return useMutation(getResendInvitationMutationOptions(options), queryClient);
    }

export const getRequestAdminOtpUrl = () => {




  return `${apiBaseUrl()}/v0.1/auth/admin/otp`
}

export const requestAdminOtp = async (adminOtpRequest: AdminOtpRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<void> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<void>(getRequestAdminOtpUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(adminOtpRequest)
  }
);}





export const getRequestAdminOtpMutationKey = () => ['requestAdminOtp'] as const;

export const getRequestAdminOtpMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof requestAdminOtp>>, TError,RequestAdminOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof requestAdminOtp>>, TError,RequestAdminOtpMutationVariables, TContext> => {

const mutationKey = getRequestAdminOtpMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof requestAdminOtp>>, RequestAdminOtpMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  requestAdminOtp(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type RequestAdminOtpMutationResult = NonNullable<Awaited<ReturnType<typeof requestAdminOtp>>>
    export type RequestAdminOtpMutationBody = BodyType<AdminOtpRequest>
    export type RequestAdminOtpMutationError = ErrorType<unknown>
    export type RequestAdminOtpMutationVariables = {data: BodyType<AdminOtpRequest>}

    export const useRequestAdminOtp = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof requestAdminOtp>>, TError,RequestAdminOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof requestAdminOtp>>,
        TError,
        RequestAdminOtpMutationVariables,
        TContext
      > => {
      return useMutation(getRequestAdminOtpMutationOptions(options), queryClient);
    }

export const getVerifyAdminOtpUrl = () => {




  return `${apiBaseUrl()}/v0.1/auth/admin/verify-otp`
}

export const verifyAdminOtp = async (adminVerifyOtpRequest: AdminVerifyOtpRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AdminLoginResponse> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AdminLoginResponse>(getVerifyAdminOtpUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(adminVerifyOtpRequest)
  }
);}





export const getVerifyAdminOtpMutationKey = () => ['verifyAdminOtp'] as const;

export const getVerifyAdminOtpMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof verifyAdminOtp>>, TError,VerifyAdminOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof verifyAdminOtp>>, TError,VerifyAdminOtpMutationVariables, TContext> => {

const mutationKey = getVerifyAdminOtpMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof verifyAdminOtp>>, VerifyAdminOtpMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  verifyAdminOtp(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type VerifyAdminOtpMutationResult = NonNullable<Awaited<ReturnType<typeof verifyAdminOtp>>>
    export type VerifyAdminOtpMutationBody = BodyType<AdminVerifyOtpRequest>
    export type VerifyAdminOtpMutationError = ErrorType<unknown>
    export type VerifyAdminOtpMutationVariables = {data: BodyType<AdminVerifyOtpRequest>}

    export const useVerifyAdminOtp = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof verifyAdminOtp>>, TError,VerifyAdminOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof verifyAdminOtp>>,
        TError,
        VerifyAdminOtpMutationVariables,
        TContext
      > => {
      return useMutation(getVerifyAdminOtpMutationOptions(options), queryClient);
    }

export const getGetCsrfTokenUrl = (params: GetCsrfTokenParams,) => {
  const normalizedParams = new URLSearchParams();
  const formObjectParameters = new Map<string, string>([["csrfToken","flatten"]]);

  Object.entries(params || {}).forEach(([key, value]) => {
    if (typeof value === 'object' && value !== null && !Array.isArray(value) && !(value instanceof Date) && formObjectParameters.has(key)) {
      const commaParts: string[] = [];
      Object.entries(value).forEach(([subKey, subValue]) => {
        const items = (Array.isArray(subValue) ? subValue : [subValue])
          .filter((v) => v !== undefined)
          .map((v) => v === null ? 'null' : v instanceof Date ? v.toISOString() : typeof v === 'object' ? JSON.stringify(v) : String(v));
        if (formObjectParameters.get(key) === 'flatten') {
          items.forEach((item) => normalizedParams.append(subKey, item));
        } else if (items.length > 0) {
          commaParts.push(subKey, ...items);
        }
      });
      if (commaParts.length > 0) {
        normalizedParams.append(key, commaParts.join(','));
      }
      return;
    }

    if (value !== undefined) {
      normalizedParams.append(key, value === null ? 'null' : String(value))
    }
  });

  const stringifiedParams = normalizedParams.toString();

  return stringifiedParams.length > 0 ? `${apiBaseUrl()}/v0.1/auth/csrf-token?${stringifiedParams}` : `${apiBaseUrl()}/v0.1/auth/csrf-token`
}

export const getCsrfToken = async (params: GetCsrfTokenParams, options?: Parameters<typeof apiFetchData>[1]): Promise<CsrfTokenResponse> => {

  return apiFetchData<CsrfTokenResponse>(getGetCsrfTokenUrl(params),
  {
    ...options,
    method: 'GET'


  }
);}





export const getGetCsrfTokenQueryKey = (params?: GetCsrfTokenParams,) => {
    return [
    `${apiBaseUrl()}/v0.1/auth/csrf-token`, ...(params ? [params] : [])
    ] as const;
    }


export const getGetCsrfTokenQueryOptions = <TData = Awaited<ReturnType<typeof getCsrfToken>>, TError = ErrorType<unknown>>(params: GetCsrfTokenParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getCsrfToken>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getGetCsrfTokenQueryKey(params);



    const queryFn: QueryFunction<Awaited<ReturnType<typeof getCsrfToken>>> = ({ signal }) => getCsrfToken(params, { signal, ...requestOptions });





   return  { queryKey, queryFn, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof getCsrfToken>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type GetCsrfTokenQueryResult = NonNullable<Awaited<ReturnType<typeof getCsrfToken>>>
export type GetCsrfTokenQueryError = ErrorType<unknown>


export function useGetCsrfToken<TData = Awaited<ReturnType<typeof getCsrfToken>>, TError = ErrorType<unknown>>(
 params: GetCsrfTokenParams, options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof getCsrfToken>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof getCsrfToken>>,
          TError,
          Awaited<ReturnType<typeof getCsrfToken>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useGetCsrfToken<TData = Awaited<ReturnType<typeof getCsrfToken>>, TError = ErrorType<unknown>>(
 params: GetCsrfTokenParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getCsrfToken>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof getCsrfToken>>,
          TError,
          Awaited<ReturnType<typeof getCsrfToken>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useGetCsrfToken<TData = Awaited<ReturnType<typeof getCsrfToken>>, TError = ErrorType<unknown>>(
 params: GetCsrfTokenParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getCsrfToken>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function useGetCsrfToken<TData = Awaited<ReturnType<typeof getCsrfToken>>, TError = ErrorType<unknown>>(
 params: GetCsrfTokenParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getCsrfToken>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getGetCsrfTokenQueryOptions(params,options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







export const getEmailVerificationResendOtpUrl = () => {




  return `${apiBaseUrl()}/v0.1/auth/email-verification/otp`
}

export const emailVerificationResendOtp = async (emailOtpRequest: EmailOtpRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<void> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<void>(getEmailVerificationResendOtpUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(emailOtpRequest)
  }
);}





export const getEmailVerificationResendOtpMutationKey = () => ['emailVerificationResendOtp'] as const;

export const getEmailVerificationResendOtpMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof emailVerificationResendOtp>>, TError,EmailVerificationResendOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof emailVerificationResendOtp>>, TError,EmailVerificationResendOtpMutationVariables, TContext> => {

const mutationKey = getEmailVerificationResendOtpMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof emailVerificationResendOtp>>, EmailVerificationResendOtpMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  emailVerificationResendOtp(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type EmailVerificationResendOtpMutationResult = NonNullable<Awaited<ReturnType<typeof emailVerificationResendOtp>>>
    export type EmailVerificationResendOtpMutationBody = BodyType<EmailOtpRequest>
    export type EmailVerificationResendOtpMutationError = ErrorType<unknown>
    export type EmailVerificationResendOtpMutationVariables = {data: BodyType<EmailOtpRequest>}

    export const useEmailVerificationResendOtp = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof emailVerificationResendOtp>>, TError,EmailVerificationResendOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof emailVerificationResendOtp>>,
        TError,
        EmailVerificationResendOtpMutationVariables,
        TContext
      > => {
      return useMutation(getEmailVerificationResendOtpMutationOptions(options), queryClient);
    }

export const getEmailVerificationVerifyOtpUrl = () => {




  return `${apiBaseUrl()}/v0.1/auth/email-verification/verify`
}

export const emailVerificationVerifyOtp = async (verifyEmailRequest: VerifyEmailRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<void> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<void>(getEmailVerificationVerifyOtpUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(verifyEmailRequest)
  }
);}





export const getEmailVerificationVerifyOtpMutationKey = () => ['emailVerificationVerifyOtp'] as const;

export const getEmailVerificationVerifyOtpMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof emailVerificationVerifyOtp>>, TError,EmailVerificationVerifyOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof emailVerificationVerifyOtp>>, TError,EmailVerificationVerifyOtpMutationVariables, TContext> => {

const mutationKey = getEmailVerificationVerifyOtpMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof emailVerificationVerifyOtp>>, EmailVerificationVerifyOtpMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  emailVerificationVerifyOtp(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type EmailVerificationVerifyOtpMutationResult = NonNullable<Awaited<ReturnType<typeof emailVerificationVerifyOtp>>>
    export type EmailVerificationVerifyOtpMutationBody = BodyType<VerifyEmailRequest>
    export type EmailVerificationVerifyOtpMutationError = ErrorType<unknown>
    export type EmailVerificationVerifyOtpMutationVariables = {data: BodyType<VerifyEmailRequest>}

    export const useEmailVerificationVerifyOtp = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof emailVerificationVerifyOtp>>, TError,EmailVerificationVerifyOtpMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof emailVerificationVerifyOtp>>,
        TError,
        EmailVerificationVerifyOtpMutationVariables,
        TContext
      > => {
      return useMutation(getEmailVerificationVerifyOtpMutationOptions(options), queryClient);
    }

export const getLoginUrl = () => {




  return `${apiBaseUrl()}/v0.1/auth/login`
}

export const login = async (loginRequest: LoginRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<void> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<void>(getLoginUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(loginRequest)
  }
);}





export const getLoginMutationKey = () => ['login'] as const;

export const getLoginMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof login>>, TError,LoginMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof login>>, TError,LoginMutationVariables, TContext> => {

const mutationKey = getLoginMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof login>>, LoginMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  login(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type LoginMutationResult = NonNullable<Awaited<ReturnType<typeof login>>>
    export type LoginMutationBody = BodyType<LoginRequest>
    export type LoginMutationError = ErrorType<unknown>
    export type LoginMutationVariables = {data: BodyType<LoginRequest>}

    export const useLogin = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof login>>, TError,LoginMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof login>>,
        TError,
        LoginMutationVariables,
        TContext
      > => {
      return useMutation(getLoginMutationOptions(options), queryClient);
    }

export const getLogoutUrl = () => {




  return `${apiBaseUrl()}/v0.1/auth/logout`
}

export const logout = async ( options?: Parameters<typeof apiFetchData>[1]): Promise<void> => {

  return apiFetchData<void>(getLogoutUrl(),
  {
    ...options,
    method: 'POST'


  }
);}





export const getLogoutMutationKey = () => ['logout'] as const;

export const getLogoutMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof logout>>, TError,void, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof logout>>, TError,void, TContext> => {

const mutationKey = getLogoutMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof logout>>, void> = () => {


          return  logout(requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type LogoutMutationResult = NonNullable<Awaited<ReturnType<typeof logout>>>

    export type LogoutMutationError = ErrorType<unknown>


    export const useLogout = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof logout>>, TError,void, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof logout>>,
        TError,
        void,
        TContext
      > => {
      return useMutation(getLogoutMutationOptions(options), queryClient);
    }

export const getPreviewInvitationUrl = (params: PreviewInvitationParams,) => {
  const normalizedParams = new URLSearchParams();

  Object.entries(params || {}).forEach(([key, value]) => {

    if (value !== undefined) {
      normalizedParams.append(key, value === null ? 'null' : String(value))
    }
  });

  const stringifiedParams = normalizedParams.toString();

  return stringifiedParams.length > 0 ? `${apiBaseUrl()}/v0.1/organizations/invitations?${stringifiedParams}` : `${apiBaseUrl()}/v0.1/organizations/invitations`
}

export const previewInvitation = async (params: PreviewInvitationParams, options?: Parameters<typeof apiFetchData>[1]): Promise<InvitationPreviewResponse> => {

  return apiFetchData<InvitationPreviewResponse>(getPreviewInvitationUrl(params),
  {
    ...options,
    method: 'GET'


  }
);}





export const getPreviewInvitationQueryKey = (params?: PreviewInvitationParams,) => {
    return [
    `${apiBaseUrl()}/v0.1/organizations/invitations`, ...(params ? [params] : [])
    ] as const;
    }


export const getPreviewInvitationQueryOptions = <TData = Awaited<ReturnType<typeof previewInvitation>>, TError = ErrorType<unknown>>(params: PreviewInvitationParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof previewInvitation>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getPreviewInvitationQueryKey(params);



    const queryFn: QueryFunction<Awaited<ReturnType<typeof previewInvitation>>> = ({ signal }) => previewInvitation(params, { signal, ...requestOptions });





   return  { queryKey, queryFn, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof previewInvitation>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type PreviewInvitationQueryResult = NonNullable<Awaited<ReturnType<typeof previewInvitation>>>
export type PreviewInvitationQueryError = ErrorType<unknown>


export function usePreviewInvitation<TData = Awaited<ReturnType<typeof previewInvitation>>, TError = ErrorType<unknown>>(
 params: PreviewInvitationParams, options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof previewInvitation>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof previewInvitation>>,
          TError,
          Awaited<ReturnType<typeof previewInvitation>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function usePreviewInvitation<TData = Awaited<ReturnType<typeof previewInvitation>>, TError = ErrorType<unknown>>(
 params: PreviewInvitationParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof previewInvitation>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof previewInvitation>>,
          TError,
          Awaited<ReturnType<typeof previewInvitation>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function usePreviewInvitation<TData = Awaited<ReturnType<typeof previewInvitation>>, TError = ErrorType<unknown>>(
 params: PreviewInvitationParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof previewInvitation>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function usePreviewInvitation<TData = Awaited<ReturnType<typeof previewInvitation>>, TError = ErrorType<unknown>>(
 params: PreviewInvitationParams, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof previewInvitation>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getPreviewInvitationQueryOptions(params,options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







export const getAcceptInvitationUrl = () => {




  return `${apiBaseUrl()}/v0.1/organizations/invitations/accept`
}

export const acceptInvitation = async (acceptInvitationRequest: AcceptInvitationRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AcceptInvitationResponse> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AcceptInvitationResponse>(getAcceptInvitationUrl(),
  {
    ...options,
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(acceptInvitationRequest)
  }
);}





export const getAcceptInvitationMutationKey = () => ['acceptInvitation'] as const;

export const getAcceptInvitationMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof acceptInvitation>>, TError,AcceptInvitationMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof acceptInvitation>>, TError,AcceptInvitationMutationVariables, TContext> => {

const mutationKey = getAcceptInvitationMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof acceptInvitation>>, AcceptInvitationMutationVariables> = (props) => {
          const {data} = props ?? {};

          return  acceptInvitation(data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type AcceptInvitationMutationResult = NonNullable<Awaited<ReturnType<typeof acceptInvitation>>>
    export type AcceptInvitationMutationBody = BodyType<AcceptInvitationRequest>
    export type AcceptInvitationMutationError = ErrorType<unknown>
    export type AcceptInvitationMutationVariables = {data: BodyType<AcceptInvitationRequest>}

    export const useAcceptInvitation = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof acceptInvitation>>, TError,AcceptInvitationMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof acceptInvitation>>,
        TError,
        AcceptInvitationMutationVariables,
        TContext
      > => {
      return useMutation(getAcceptInvitationMutationOptions(options), queryClient);
    }

export const getListAcademicPatternsUrl = (organizationId: string,) => {




  return `${apiBaseUrl()}/v0.1/organizations/${organizationId}/academic-patterns`
}

export const listAcademicPatterns = async (organizationId: string, options?: Parameters<typeof apiFetchData>[1]): Promise<AcademicPatternResponse[]> => {

  return apiFetchData<AcademicPatternResponse[]>(getListAcademicPatternsUrl(organizationId),
  {
    ...options,
    method: 'GET'


  }
);}





export const getListAcademicPatternsQueryKey = (organizationId: string,) => {
    return [
    `${apiBaseUrl()}/v0.1/organizations/${organizationId}/academic-patterns`
    ] as const;
    }


export const getListAcademicPatternsQueryOptions = <TData = Awaited<ReturnType<typeof listAcademicPatterns>>, TError = ErrorType<unknown>>(organizationId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof listAcademicPatterns>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getListAcademicPatternsQueryKey(organizationId);



    const queryFn: QueryFunction<Awaited<ReturnType<typeof listAcademicPatterns>>> = ({ signal }) => listAcademicPatterns(organizationId, { signal, ...requestOptions });





   return  { queryKey, queryFn, enabled: organizationId !== null && organizationId !== undefined, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof listAcademicPatterns>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type ListAcademicPatternsQueryResult = NonNullable<Awaited<ReturnType<typeof listAcademicPatterns>>>
export type ListAcademicPatternsQueryError = ErrorType<unknown>


export function useListAcademicPatterns<TData = Awaited<ReturnType<typeof listAcademicPatterns>>, TError = ErrorType<unknown>>(
 organizationId: string, options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof listAcademicPatterns>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof listAcademicPatterns>>,
          TError,
          Awaited<ReturnType<typeof listAcademicPatterns>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useListAcademicPatterns<TData = Awaited<ReturnType<typeof listAcademicPatterns>>, TError = ErrorType<unknown>>(
 organizationId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof listAcademicPatterns>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof listAcademicPatterns>>,
          TError,
          Awaited<ReturnType<typeof listAcademicPatterns>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useListAcademicPatterns<TData = Awaited<ReturnType<typeof listAcademicPatterns>>, TError = ErrorType<unknown>>(
 organizationId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof listAcademicPatterns>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function useListAcademicPatterns<TData = Awaited<ReturnType<typeof listAcademicPatterns>>, TError = ErrorType<unknown>>(
 organizationId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof listAcademicPatterns>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getListAcademicPatternsQueryOptions(organizationId,options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







export const getGetAcademicPatternUrl = (organizationId: string,
    patternId: string,) => {




  return `${apiBaseUrl()}/v0.1/organizations/${organizationId}/academic-patterns/${patternId}`
}

export const getAcademicPattern = async (organizationId: string,
    patternId: string, options?: Parameters<typeof apiFetchData>[1]): Promise<AcademicPatternResponse> => {

  return apiFetchData<AcademicPatternResponse>(getGetAcademicPatternUrl(organizationId,patternId),
  {
    ...options,
    method: 'GET'


  }
);}





export const getGetAcademicPatternQueryKey = (organizationId: string,
    patternId: string,) => {
    return [
    `${apiBaseUrl()}/v0.1/organizations/${organizationId}/academic-patterns/${patternId}`
    ] as const;
    }


export const getGetAcademicPatternQueryOptions = <TData = Awaited<ReturnType<typeof getAcademicPattern>>, TError = ErrorType<unknown>>(organizationId: string,
    patternId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getAcademicPattern>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getGetAcademicPatternQueryKey(organizationId,patternId);



    const queryFn: QueryFunction<Awaited<ReturnType<typeof getAcademicPattern>>> = ({ signal }) => getAcademicPattern(organizationId,patternId, { signal, ...requestOptions });





   return  { queryKey, queryFn, enabled: organizationId !== null && organizationId !== undefined && patternId !== null && patternId !== undefined, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof getAcademicPattern>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type GetAcademicPatternQueryResult = NonNullable<Awaited<ReturnType<typeof getAcademicPattern>>>
export type GetAcademicPatternQueryError = ErrorType<unknown>


export function useGetAcademicPattern<TData = Awaited<ReturnType<typeof getAcademicPattern>>, TError = ErrorType<unknown>>(
 organizationId: string,
    patternId: string, options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof getAcademicPattern>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof getAcademicPattern>>,
          TError,
          Awaited<ReturnType<typeof getAcademicPattern>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useGetAcademicPattern<TData = Awaited<ReturnType<typeof getAcademicPattern>>, TError = ErrorType<unknown>>(
 organizationId: string,
    patternId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getAcademicPattern>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof getAcademicPattern>>,
          TError,
          Awaited<ReturnType<typeof getAcademicPattern>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useGetAcademicPattern<TData = Awaited<ReturnType<typeof getAcademicPattern>>, TError = ErrorType<unknown>>(
 organizationId: string,
    patternId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getAcademicPattern>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function useGetAcademicPattern<TData = Awaited<ReturnType<typeof getAcademicPattern>>, TError = ErrorType<unknown>>(
 organizationId: string,
    patternId: string, options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof getAcademicPattern>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getGetAcademicPatternQueryOptions(organizationId,patternId,options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







export const getUpdateAcademicPatternUrl = (organizationId: string,
    patternId: string,) => {




  return `${apiBaseUrl()}/v0.1/organizations/${organizationId}/academic-patterns/${patternId}`
}

export const updateAcademicPattern = async (organizationId: string,
    patternId: string,
    updateAcademicPatternRequest: UpdateAcademicPatternRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AcademicPatternResponse> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AcademicPatternResponse>(getUpdateAcademicPatternUrl(organizationId,patternId),
  {
    ...options,
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(updateAcademicPatternRequest)
  }
);}





export const getUpdateAcademicPatternMutationKey = () => ['updateAcademicPattern'] as const;

export const getUpdateAcademicPatternMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof updateAcademicPattern>>, TError,UpdateAcademicPatternMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof updateAcademicPattern>>, TError,UpdateAcademicPatternMutationVariables, TContext> => {

const mutationKey = getUpdateAcademicPatternMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof updateAcademicPattern>>, UpdateAcademicPatternMutationVariables> = (props) => {
          const {organizationId,patternId,data} = props ?? {};

          return  updateAcademicPattern(organizationId,patternId,data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type UpdateAcademicPatternMutationResult = NonNullable<Awaited<ReturnType<typeof updateAcademicPattern>>>
    export type UpdateAcademicPatternMutationBody = BodyType<UpdateAcademicPatternRequest>
    export type UpdateAcademicPatternMutationError = ErrorType<unknown>
    export type UpdateAcademicPatternMutationVariables = {organizationId: string;patternId: string;data: BodyType<UpdateAcademicPatternRequest>}

    export const useUpdateAcademicPattern = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof updateAcademicPattern>>, TError,UpdateAcademicPatternMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof updateAcademicPattern>>,
        TError,
        UpdateAcademicPatternMutationVariables,
        TContext
      > => {
      return useMutation(getUpdateAcademicPatternMutationOptions(options), queryClient);
    }

export const getUpdateAcademicStageUrl = (organizationId: string,
    patternId: string,
    stageId: string,) => {




  return `${apiBaseUrl()}/v0.1/organizations/${organizationId}/academic-patterns/${patternId}/stages/${stageId}`
}

export const updateAcademicStage = async (organizationId: string,
    patternId: string,
    stageId: string,
    updateAcademicStageRequest: UpdateAcademicStageRequest, options?: Parameters<typeof apiFetchData>[1]): Promise<AcademicStageResponse> => {

    const getHeaders = (h?: NonNullable<RequestInit['headers']>): Record<string, string | readonly string[]> => {
    if (!h) return {};
    if (h instanceof Headers) return Object.fromEntries(h.entries());
    if (Symbol.iterator in h) {
      return Object.fromEntries(
        Array.from(h as Iterable<Iterable<string>>, (entry) => Array.from(entry) as [string, string]),
      );
    }
    const headers: Record<string, string | readonly string[]> = {};
    for (const [name, value] of Object.entries<string | readonly string[] | undefined>(h)) {
      if (value !== undefined) headers[name] = value;
    }
    return headers;
  };
return apiFetchData<AcademicStageResponse>(getUpdateAcademicStageUrl(organizationId,patternId,stageId),
  {
    ...options,
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json', ...getHeaders(options?.headers) },
    body: JSON.stringify(updateAcademicStageRequest)
  }
);}





export const getUpdateAcademicStageMutationKey = () => ['updateAcademicStage'] as const;

export const getUpdateAcademicStageMutationOptions = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof updateAcademicStage>>, TError,UpdateAcademicStageMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
): UseMutationOptions<Awaited<ReturnType<typeof updateAcademicStage>>, TError,UpdateAcademicStageMutationVariables, TContext> => {

const mutationKey = getUpdateAcademicStageMutationKey();
const {mutation: mutationOptions, request: requestOptions} = options ?
      options.mutation && 'mutationKey' in options.mutation && options.mutation.mutationKey ?
      options
      : {...options, mutation: {...options.mutation, mutationKey}}
      : {mutation: { mutationKey, }, request: undefined};




      const mutationFn: MutationFunction<Awaited<ReturnType<typeof updateAcademicStage>>, UpdateAcademicStageMutationVariables> = (props) => {
          const {organizationId,patternId,stageId,data} = props ?? {};

          return  updateAcademicStage(organizationId,patternId,stageId,data,requestOptions)
        }






  return  { mutationFn, ...mutationOptions }}

    export type UpdateAcademicStageMutationResult = NonNullable<Awaited<ReturnType<typeof updateAcademicStage>>>
    export type UpdateAcademicStageMutationBody = BodyType<UpdateAcademicStageRequest>
    export type UpdateAcademicStageMutationError = ErrorType<unknown>
    export type UpdateAcademicStageMutationVariables = {organizationId: string;patternId: string;stageId: string;data: BodyType<UpdateAcademicStageRequest>}

    export const useUpdateAcademicStage = <TError = ErrorType<unknown>,
    TContext = unknown>(options?: { mutation?:UseMutationOptions<Awaited<ReturnType<typeof updateAcademicStage>>, TError,UpdateAcademicStageMutationVariables, TContext>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient): UseMutationResult<
        Awaited<ReturnType<typeof updateAcademicStage>>,
        TError,
        UpdateAcademicStageMutationVariables,
        TContext
      > => {
      return useMutation(getUpdateAcademicStageMutationOptions(options), queryClient);
    }

export const getCurrentUserUrl = () => {




  return `${apiBaseUrl()}/v0.1/users/me`
}

export const currentUser = async ( options?: Parameters<typeof apiFetchData>[1]): Promise<CurrentUserResponse> => {

  return apiFetchData<CurrentUserResponse>(getCurrentUserUrl(),
  {
    ...options,
    method: 'GET'


  }
);}





export const getCurrentUserQueryKey = () => {
    return [
    `${apiBaseUrl()}/v0.1/users/me`
    ] as const;
    }


export const getCurrentUserQueryOptions = <TData = Awaited<ReturnType<typeof currentUser>>, TError = ErrorType<unknown>>( options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof currentUser>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
) => {

const {query: queryOptions, request: requestOptions} = options ?? {};

  const queryKey =  queryOptions?.queryKey ?? getCurrentUserQueryKey();



    const queryFn: QueryFunction<Awaited<ReturnType<typeof currentUser>>> = ({ signal }) => currentUser({ signal, ...requestOptions });





   return  { queryKey, queryFn, ...queryOptions} as UseQueryOptions<Awaited<ReturnType<typeof currentUser>>, TError, TData> & { queryKey: DataTag<QueryKey, TData, TError> }
}

export type CurrentUserQueryResult = NonNullable<Awaited<ReturnType<typeof currentUser>>>
export type CurrentUserQueryError = ErrorType<unknown>


export function useCurrentUser<TData = Awaited<ReturnType<typeof currentUser>>, TError = ErrorType<unknown>>(
  options: { query:Partial<UseQueryOptions<Awaited<ReturnType<typeof currentUser>>, TError, TData>> & Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof currentUser>>,
          TError,
          Awaited<ReturnType<typeof currentUser>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useCurrentUser<TData = Awaited<ReturnType<typeof currentUser>>, TError = ErrorType<unknown>>(
  options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof currentUser>>, TError, TData>> & Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof currentUser>>,
          TError,
          Awaited<ReturnType<typeof currentUser>>
        > , 'initialData'
      >, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }
export function useCurrentUser<TData = Awaited<ReturnType<typeof currentUser>>, TError = ErrorType<unknown>>(
  options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof currentUser>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
  ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> }

export function useCurrentUser<TData = Awaited<ReturnType<typeof currentUser>>, TError = ErrorType<unknown>>(
  options?: { query?:Partial<UseQueryOptions<Awaited<ReturnType<typeof currentUser>>, TError, TData>>, request?: SecondParameter<typeof apiFetchData>}
 , queryClient?: QueryClient
 ):  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {

  const queryOptions = getCurrentUserQueryOptions(options)

  const query = useQuery(queryOptions, queryClient) as  UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

  return withQueryKey(query, queryOptions.queryKey);
}







