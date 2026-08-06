import type { Id } from "@instello/convex/dataModel";
import { programPath } from "@/features/programs/program-path";

export type AssessmentTab = "schemas" | "schedule" | "marks";

export function assessmentPath(
	alias: string,
	programSubjectId: Id<"programSubjects"> | string,
	tab: AssessmentTab = "schemas",
) {
	return programPath(alias, `subjects/${programSubjectId}/${tab}`);
}

export function assessmentRootPath(
	alias: string,
	programSubjectId: Id<"programSubjects"> | string,
) {
	return programPath(alias, `subjects/${programSubjectId}`);
}
