"use client";

import { api } from "@instello/convex/api";
import {
	Empty,
	EmptyDescription,
	EmptyHeader,
	EmptyMedia,
	EmptyTitle,
} from "@instello/ui/components/empty";
import { Skeleton } from "@instello/ui/components/skeleton";
import { IconReportAnalytics } from "@tabler/icons-react";
import { notFound } from "next/navigation";
import { useMemo } from "react";
import Container from "@/components/common/container";
import {
	PageHeader,
	PageHeaderDescription,
	PageHeaderStart,
	PageHeaderTitle,
} from "@/components/common/page-header";
import { ConductedSittingListForAssigned } from "@/features/program-subject-assessments/components/conducted-sitting-list";
import { useInsQuery } from "@/hooks/convex-react";
import { useAssignedSubjectKey } from "../hooks/use-assigned-subject-key";

export function AssignedSubjectPage() {
	const key = useAssignedSubjectKey();
	const groups = useInsQuery(api.class.queries.listMyAssignedSubjects, {});

	const match = useMemo(() => {
		if (!key || groups === undefined) return null;
		for (const group of groups) {
			if (
				group.programAlias !== key.programAlias ||
				group.classSlug !== key.classSlug
			) {
				continue;
			}
			const subject = group.subjects.find(
				(row) => row.alias === key.subjectAlias,
			);
			if (!subject) continue;
			return {
				classId: group.classId,
				className: group.className,
				programName: group.programName,
				subjectName: subject.name,
				programSubjectId: subject.programSubjectId,
			};
		}
		return null;
	}, [groups, key]);

	if (!key) {
		notFound();
	}

	if (groups === undefined) {
		return (
			<Container>
				<div className="space-y-3">
					<Skeleton className="h-10 w-56" />
					<Skeleton className="h-6 w-80" />
					<Skeleton className="h-48 w-full" />
				</div>
			</Container>
		);
	}

	if (!match) {
		return (
			<Container>
				<Empty className="min-h-72 border border-dashed border-border">
					<EmptyMedia variant="icon">
						<IconReportAnalytics />
					</EmptyMedia>
					<EmptyHeader>
						<EmptyTitle>Assignment not found</EmptyTitle>
						<EmptyDescription>
							This class subject is not assigned to you, or it is no longer
							available.
						</EmptyDescription>
					</EmptyHeader>
				</Empty>
			</Container>
		);
	}

	return (
		<Container>
			<PageHeader>
				<PageHeaderStart>
					<PageHeaderTitle>{match.subjectName}</PageHeaderTitle>
					<PageHeaderDescription>
						{match.className} · {match.programName}
					</PageHeaderDescription>
				</PageHeaderStart>
			</PageHeader>

			<ConductedSittingListForAssigned
				classId={match.classId}
				programSubjectId={match.programSubjectId}
			/>
		</Container>
	);
}
