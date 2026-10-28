import { AppSmokeCard } from "@instello/ui/components/app-smoke-card"
import { institutionUrl } from "@/lib/tenant"

const DEMO_SLUGS = ["acme", "globex"]

export default function Page() {
  return (
    <AppSmokeCard
      app="institution"
      port={3002}
      title="Institution"
      description="No institution detected. Open an institution on its own subdomain."
    >
      <ul className="flex flex-col gap-1">
        {DEMO_SLUGS.map((slug) => (
          <li key={slug}>
            <a className="underline underline-offset-4" href={institutionUrl(slug)}>
              {institutionUrl(slug).replace(/^https?:\/\//, "")}
            </a>
          </li>
        ))}
      </ul>
    </AppSmokeCard>
  )
}
