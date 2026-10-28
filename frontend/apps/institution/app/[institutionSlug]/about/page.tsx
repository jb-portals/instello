import { AppSmokeCard } from "@instello/ui/components/app-smoke-card"
import type { Metadata } from "next"

type PageProps = { params: Promise<{ institutionSlug: string }> }

export const metadata: Metadata = { title: "About" }

export default async function Page({ params }: PageProps) {
  const { institutionSlug } = await params

  return (
    <AppSmokeCard
      app="institution"
      port={3002}
      title={`About ${institutionSlug}`}
      description="Nested route under the institution slug."
    />
  )
}
