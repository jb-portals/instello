import type { NextConfig } from "next"

const nextConfig: NextConfig = {
  transpilePackages: ["@instello/ui", "@instello/api-client"],
}

export default nextConfig
