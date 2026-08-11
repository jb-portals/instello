"use client";

import { api } from "@instello/convex/api";
import type { Id } from "@instello/convex/dataModel";
import { useParams } from "next/navigation";
import { useInsQuery } from "@/hooks/convex-react";
import { ConductedSittingListForProgramSubject } from "../components/conducted-sitting-list";

export function AssessmentMarksPage() {
	const { programSubjectId } = useParams<{ programSubjectId: string }>();
	const allocation = useInsQuery(
		api.program.queries.getProgramSubject,
		programSubjectId
			? { id: programSubjectId as Id<"programSubjects"> }
			: "skip",
	);

	if (allocation === undefined || allocation === null) {
		return null;
	}

	return (
		<ConductedSittingListForProgramSubject programSubjectId={allocation._id} />
	);
}
