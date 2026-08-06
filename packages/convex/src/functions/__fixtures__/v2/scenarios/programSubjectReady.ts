import { adoptEngineeringPattern } from "../factories/academicPattern";
import { createOwnerOrganization } from "../factories/ownerOrganization";
import { createProgram } from "../factories/program";
import { createProgramSubject } from "../factories/programSubject";
import { createSubject } from "../factories/subject";
import type { AppTest } from "../types";
import { arrangeOwnerInstitution } from "./ownerInstitution";

/**
 * Full stack needed to work with assessment schemas:
 * owner + institution + adopted pattern + program + subject + programSubject.
 */
export async function arrangeProgramSubjectReady(t: AppTest) {
	const base = await arrangeOwnerInstitution(t);
	const ownerOrg = await createOwnerOrganization(t, {
		ownerId: base.owner._id,
	});
	const { pattern, firstStage, secondStage } = await adoptEngineeringPattern(
		t,
		{
			ownerOrganizationId: ownerOrg._id,
			institutionId: base.institution._id,
		},
	);
	const program = await createProgram(t, {
		institutionId: base.institution._id,
		createdBy: base.owner._id,
	});
	const subject = await createSubject(t, {
		institutionId: base.institution._id,
	});
	const programSubject = await createProgramSubject(t, {
		programId: program._id,
		subjectId: subject._id,
		academicStageId: firstStage._id,
	});

	return {
		...base,
		ownerOrg,
		pattern,
		firstStage,
		secondStage,
		program,
		subject,
		programSubject,
	};
}

export type ProgramSubjectReady = Awaited<
	ReturnType<typeof arrangeProgramSubjectReady>
>;
