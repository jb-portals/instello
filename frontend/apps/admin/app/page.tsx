import { AppSmokeCard } from "@instello/ui/components/app-smoke-card"

export default function Page() {
  return (
    <AppSmokeCard
      app="admin"
      port={3000}
      title="Admin"
      description="Platform administration. Styles and components come from the shared UI package."
    />
  )
}
