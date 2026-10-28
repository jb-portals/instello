export const apiVersions = [
  {
    id: "v0.1",
    openApiPath: "/api-docs/v0.1",
  },
] as const

export type ApiVersionId = (typeof apiVersions)[number]["id"]

/** Default client re-exported from the package root. Must be one of `apiVersions`. */
export const latestApiVersion = "v0.1" satisfies ApiVersionId

export const openapiOrigin = process.env.OPENAPI_ORIGIN ?? "http://localhost:8080"
