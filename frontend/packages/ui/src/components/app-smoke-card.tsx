import { Badge } from "@instello/ui/components/badge"
import { Button } from "@instello/ui/components/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@instello/ui/components/card"
import type { ReactNode } from "react"

type AppSmokeCardProps = {
  app: string
  port: number
  title: string
  description: string
  children?: ReactNode
}

function AppSmokeCard({ app, port, title, description, children }: AppSmokeCardProps) {
  return (
    <main className="flex min-h-svh items-center justify-center p-6">
      <Card className="w-full max-w-md">
        <CardHeader>
          <div className="flex items-center gap-2">
            <Badge>{app}</Badge>
            <Badge variant="outline">:{port}</Badge>
          </div>
          <CardTitle className="font-heading text-xl">{title}</CardTitle>
          <CardDescription>{description}</CardDescription>
        </CardHeader>
        {children ? <CardContent className="text-sm">{children}</CardContent> : null}
        <CardFooter className="justify-between text-muted-foreground text-xs">
          <span>
            Shared from <code>@instello/ui</code>
          </span>
          <Button size="sm" variant="outline" render={<a href="https://ui.shadcn.com" />}>
            shadcn/ui
          </Button>
        </CardFooter>
      </Card>
    </main>
  )
}

export { AppSmokeCard }
