"use client";

import { api } from "@instello/convex/api";
import type { Id } from "@instello/convex/dataModel";
import { Button } from "@instello/ui/components/button";
import { Skeleton } from "@instello/ui/components/skeleton";
import { IconChevronLeft } from "@tabler/icons-react";
import Link from "next/link";
import { useParams, usePathname } from "next/navigation";
import type { ReactNode } from "react";
import Container from "@/components/common/container";
import { PageHeader, PageHeaderStart } from "@/components/common/page-header";
import { ProgramLink } from "@/components/sidebars/program-sidebar/program-link";
import { SubjectTypeBadge } from "@/features/program-subjects/components/subject-type-badge";
import { programPath } from "@/features/programs/program-path";
import { SubjectAvatar } from "@/features/subjects/components/subject-avatar";
import { useInsQuery } from "@/hooks/convex-react";
import { useProgramAlias } from "@/hooks/use-program-alias";
import { cn } from "@/lib/utils";
import { type AssessmentTab, assessmentPath } from "../assessment-path";
import { ASSESSMENT_TABS } from "../constants";

export function ProgramSubjectAssessmentLayout({
  children,
}: {
  children: ReactNode;
}) {
  const alias = useProgramAlias();
  const pathname = usePathname();
  const { programSubjectId } = useParams<{ programSubjectId: string }>();
  const allocation = useInsQuery(
    api.program.queries.getProgramSubject,
    programSubjectId
      ? { id: programSubjectId as Id<"programSubjects"> }
      : "skip",
  );

  const activeTab = (ASSESSMENT_TABS.find((tab) =>
    pathname.endsWith(`/${tab.id}`),
  )?.id ?? "schemas") as AssessmentTab;

  if (allocation === undefined) {
    return (
      <Container>
        <AssessmentLayoutSkeleton />
      </Container>
    );
  }

  if (allocation === null) {
    return (
      <Container>
        <PageHeader>
          <PageHeaderStart>
            <Button
              nativeButton={false}
              variant="ghost"
              size="sm"
              className="-ml-2 h-8 rounded-full px-2 text-muted-foreground"
              render={<ProgramLink segment="subjects" />}
            >
              <IconChevronLeft className="size-4" />
              Subjects
            </Button>
          </PageHeaderStart>
        </PageHeader>
        <p className="text-sm text-muted-foreground">
          This subject allocation was not found in this institution.
        </p>
      </Container>
    );
  }

  return (
    <Container>
      <PageHeader>
        <PageHeaderStart>
          <Button
            nativeButton={false}
            variant="ghost"
            size="sm"
            className="-ml-2 h-8 rounded-full px-2 text-muted-foreground"
            render={
              <Link
                href={programPath(
                  alias,
                  `subjects?stage=${allocation.academicStageId}`,
                )}
              />
            }
          >
            <IconChevronLeft className="size-4" />
            Subjects
          </Button>
        </PageHeaderStart>
      </PageHeader>

      <div className="mx-auto max-w-3xl space-y-6">
        <div className="flex items-start gap-4">
          <SubjectAvatar
            name={allocation.subject.name}
            color={allocation.subject.color}
            size="xl"
          />
          <div className="min-w-0 space-y-1.5">
            <div className="flex flex-row flex-wrap items-center gap-2">
              <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">
                {allocation.subject.name}
              </h1>
              <SubjectTypeBadge type={allocation.type} />
            </div>
            <p className="text-sm text-muted-foreground uppercase">
              {allocation.subject.code}
            </p>
          </div>
        </div>

        <nav className="flex gap-1 border-b border-border">
          {ASSESSMENT_TABS.map((tab) => (
            <Link
              key={tab.id}
              href={assessmentPath(alias, allocation._id, tab.id)}
              className={cn(
                "-mb-px border-b-2 px-3 py-2 text-sm font-medium transition-colors",
                activeTab === tab.id
                  ? "border-foreground text-foreground"
                  : "border-transparent text-muted-foreground hover:text-foreground",
              )}
            >
              {tab.label}
            </Link>
          ))}
        </nav>

        {children}
      </div>
    </Container>
  );
}

function AssessmentLayoutSkeleton() {
  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <Skeleton className="h-8 w-28" />
      <div className="flex items-start gap-4">
        <Skeleton className="size-16 rounded-lg" />
        <div className="space-y-2">
          <Skeleton className="h-8 w-56" />
          <Skeleton className="h-4 w-24" />
        </div>
      </div>
      <Skeleton className="h-10 w-full" />
      <Skeleton className="h-48 w-full" />
    </div>
  );
}
