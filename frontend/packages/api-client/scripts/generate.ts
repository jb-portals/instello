import { spawnSync } from "node:child_process"
import { appendFile, mkdir, writeFile } from "node:fs/promises"
import { dirname, join } from "node:path"
import { fileURLToPath } from "node:url"

import { apiVersions, latestApiVersion, openapiOrigin } from "../versions"

const packageRoot = join(dirname(fileURLToPath(import.meta.url)), "..")

for (const version of apiVersions) {
  const url = `${openapiOrigin}${version.openApiPath}`
  let response: Response
  try {
    response = await fetch(url)
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error)
    throw new Error(`Could not reach OpenAPI for ${version.id} at ${url}: ${message}`)
  }

  if (!response.ok) {
    throw new Error(`OpenAPI for ${version.id} at ${url} returned ${response.status}`)
  }

  const spec = (await response.json()) as unknown
  const destination = join(packageRoot, "openapi", `${version.id}.json`)
  await mkdir(dirname(destination), { recursive: true })
  await writeFile(destination, `${JSON.stringify(spec, null, 2)}\n`)
}

// Orval's bin uses a Node shebang. Node 20 lacks Map.groupBy, which Orval 8 needs.
const orval = spawnSync("bun", ["--bun", "x", "orval", "--config", "./orval.config.ts"], {
  cwd: packageRoot,
  stdio: "inherit",
})

if (orval.status !== 0) {
  process.exit(orval.status ?? 1)
}

for (const version of apiVersions) {
  await appendFile(
    join(packageRoot, "src", version.id, "client.ts"),
    `\nexport * from "./models"\n`,
  )
}

await writeFile(
  join(packageRoot, "src", "index.ts"),
  `export { ApiError } from "./mutator"\nexport * from "./${latestApiVersion}/client"\n`,
)

await writeFile(
  join(packageRoot, "src", "query.ts"),
  `export * from "./${latestApiVersion}/query"\n`,
)
