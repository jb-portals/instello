import { AppSmokeCard } from "@instello/ui/components/app-smoke-card"

export default function Page() {
  return (
    <AppSmokeCard
      app="organization"
      port={3001}
      title="Organization"
      description="Organization workspace. Styles and components come from the shared UI package."
    />
  )
}
