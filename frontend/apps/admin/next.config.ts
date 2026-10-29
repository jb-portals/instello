import type { NextConfig } from "next"

const apiOrigin = (process.env.API_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "")

const nextConfig: NextConfig = {
  transpilePackages: ["@instello/ui", "@instello/api-client"],
  async rewrites() {
    return [
      {
        source: "/v0.1/:path*",
        destination: `${apiOrigin}/v0.1/:path*`,
      },
    ]
  },
}

export default nextConfig
