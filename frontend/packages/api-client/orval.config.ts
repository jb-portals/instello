import { defineConfig, type OptionsExport } from "orval"

import { apiVersions } from "./versions"

function outputFor(versionId: string, kind: "fetch" | "query"): OptionsExport {
  const input = {
    target: `./openapi/${versionId}.json`,
  }

  if (kind === "fetch") {
    return {
      input,
      output: {
        mode: "split",
        target: `./src/${versionId}/client.ts`,
        schemas: `./src/${versionId}/models`,
        client: "fetch",
        clean: true,
        baseUrl: {
          runtime: "apiBaseUrl()",
          imports: [{ name: "apiBaseUrl", importPath: "../mutator" }],
        },
        override: {
          mutator: {
            path: "./src/mutator.ts",
            name: "apiFetch",
          },
          fetch: {
            forceSuccessResponse: true,
          },
        },
      },
    }
  }

  return {
    input,
    output: {
      mode: "split",
      target: `./src/${versionId}/query/index.ts`,
      schemas: `./src/${versionId}/models`,
      client: "react-query",
      httpClient: "fetch",
      clean: true,
      baseUrl: {
        runtime: "apiBaseUrl()",
        imports: [{ name: "apiBaseUrl", importPath: "../../mutator" }],
      },
      override: {
        header: () => "'use client'\n",
        mutator: {
          path: "./src/mutator.ts",
          name: "apiFetchData",
        },
        fetch: {
          forceSuccessResponse: true,
          includeHttpResponseReturnType: false,
        },
        query: {
          signal: true,
        },
      },
    },
  }
}

export default defineConfig(
  Object.fromEntries(
    apiVersions.flatMap((version) => [
      [version.id, outputFor(version.id, "fetch")],
      [`${version.id}-query`, outputFor(version.id, "query")],
    ]),
  ),
)
