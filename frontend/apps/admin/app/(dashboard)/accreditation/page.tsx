import { Card, CardContent } from "@instello/ui/components/card"
import {
  Empty,
  EmptyDescription,
  EmptyHeader,
  EmptyMedia,
  EmptyTitle,
} from "@instello/ui/components/empty"
import { IconCertificate } from "@tabler/icons-react"

export default function AccreditationPage() {
  return (
    <div className="flex flex-1 flex-col gap-6 p-6">
      <div className="space-y-1">
        <h1 className="font-heading text-2xl font-medium tracking-tight">Accreditation</h1>
        <p className="text-sm text-muted-foreground">Review institution accreditation records.</p>
      </div>
      <Card className="min-h-80 flex-1">
        <CardContent className="flex flex-1 items-center justify-center">
          <Empty className="border-0">
            <EmptyHeader>
              <EmptyMedia variant="icon">
                <IconCertificate />
              </EmptyMedia>
              <EmptyTitle>No accreditations</EmptyTitle>
              <EmptyDescription>Accreditation records will show up here.</EmptyDescription>
            </EmptyHeader>
          </Empty>
        </CardContent>
      </Card>
    </div>
  )
}
