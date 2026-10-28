import { AppSmokeCard } from "@instello/ui/components/app-smoke-card"
import { Button } from "@instello/ui/components/button"
import Link from "next/link"

type PageProps = { params: Promise<{ institutionSlug: string }> }

export default async function Page({ params }: PageProps) {
  const { institutionSlug } = await params

  return (
    <AppSmokeCard
      app="institution"
      port={3002}
      title={`Institution: ${institutionSlug}`}
      description="The slug was read from the subdomain by the proxy and passed as a route param."
    >
      <Button size="sm" render={<Link href="/about" />}>
        About
      </Button>
    </AppSmokeCard>
  )
}
