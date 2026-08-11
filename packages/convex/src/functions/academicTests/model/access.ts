import type { Doc, Id } from "#_generated/dataModel";
import * as ClassSubjectFaculty from "#class/model/classSubjectFaculty";
import * as Faculty from "#faculty/model/faculty";
import { ERROR_CODES, throwAppError } from "#helpers/constants";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import * as Program from "#program/model/program";
import * as ProgramFaculty from "#program/model/programFaculty";
import * as ProgramSubject from "#program/model/programSubject";
import type { InsRole } from "../../../better-auth/ins-permissions";
import * as insPermissions from "../../../better-auth/ins-permissions";
import * as AcademicComponent from "./academicComponent";
import * as AcademicSchema from "./academicSchema";
import * as AssessmentSitting from "./assessmentSitting";

type Ctx = AppQueryCtx | AppMutationCtx;

/**
 * Ensures the program-subject allocation belongs to the caller's institution.
 * Used when the public API takes a `programSubjectId`.
 */
export async function requireProgramSubjectInInstitution(
	ctx: Ctx,
	programSubjectId: Id<"programSubjects">,
	institutionId: string,
): Promise<Doc<"programSubjects">> {
	const programSubject = await ProgramSubject.getById(ctx, programSubjectId);

	if (!programSubject) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.INVALID_PROGRAM_SUBJECT);
	}

	const program = await Program.getById(
		ctx,
		programSubject.programId,
		institutionId,
	);

	if (!program) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.INVALID_PROGRAM_SUBJECT);
	}

	return programSubject;
}

/**
 * Ensures the assessment schema (via its program subject) belongs to the institution.
 */
export async function requireSchemaInInstitution(
	ctx: Ctx,
	assessmentSchemaId: Id<"assessmentSchemas">,
	institutionId: string,
): Promise<Doc<"assessmentSchemas">> {
	const schema = await AcademicSchema.getById(ctx, assessmentSchemaId);

	if (!schema) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND);
	}

	const programSubject = await ProgramSubject.getById(
		ctx,
		schema.programSubjectId,
	);

	if (!programSubject) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND);
	}

	const program = await Program.getById(
		ctx,
		programSubject.programId,
		institutionId,
	);

	if (!program) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND);
	}

	return schema;
}

/**
 * Ensures the assessment component belongs to a schema in the institution.
 * Wrong-institution / missing parent always surfaces as component not found.
 */
export async function requireComponentInInstitution(
	ctx: Ctx,
	componentId: Id<"assessmentComponents">,
	institutionId: string,
): Promise<Doc<"assessmentComponents">> {
	const component = await AcademicComponent.getById(ctx, componentId);

	if (!component) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const schema = await AcademicSchema.getById(
		ctx,
		component.assessmentSchemaId,
	);

	if (!schema) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const programSubject = await ProgramSubject.getById(
		ctx,
		schema.programSubjectId,
	);

	if (!programSubject) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const program = await Program.getById(
		ctx,
		programSubject.programId,
		institutionId,
	);

	if (!program) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	return component;
}

/**
 * Ensures the assessment sitting belongs to a schema in the institution.
 * Wrong-institution / missing parent always surfaces as sitting not found.
 */
export async function requireSittingInInstitution(
	ctx: Ctx,
	sittingId: Id<"assessmentSittings">,
	institutionId: string,
): Promise<Doc<"assessmentSittings">> {
	const sitting = await AssessmentSitting.getById(ctx, sittingId);

	if (!sitting) {
		throwAppError(ERROR_CODES.ASSESSMENT_SITTING.NOT_FOUND);
	}

	await requireSchemaInInstitution(
		ctx,
		sitting.assessmentSchemaId,
		institutionId,
	);

	return sitting;
}

/**
 * Resolves programSubjectId for a schema after institution access is confirmed.
 */
export async function resolveProgramSubjectIdForSchema(
	ctx: Ctx,
	assessmentSchemaId: Id<"assessmentSchemas">,
): Promise<Id<"programSubjects">> {
	const schema = await AcademicSchema.getById(ctx, assessmentSchemaId);
	if (!schema) {
		throwAppError(ERROR_CODES.ASSESSMENT_SCHEMA.NOT_FOUND);
	}

	return schema.programSubjectId;
}

/**
 * Resolves programSubjectId for a component after institution access is confirmed.
 */
export async function resolveProgramSubjectIdForComponent(
	ctx: Ctx,
	componentId: Id<"assessmentComponents">,
): Promise<Id<"programSubjects">> {
	const component = await AcademicComponent.getById(ctx, componentId);
	if (!component) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	const schema = await AcademicSchema.getById(
		ctx,
		component.assessmentSchemaId,
	);
	if (!schema) {
		throwAppError(ERROR_CODES.ASSESSMENT_COMPONENT.NOT_FOUND);
	}

	return schema.programSubjectId;
}

/**
 * Whether the caller's role (with HoP elevation) includes assessment:delete.
 */
export async function canDeleteMarks(
	ctx: Ctx,
	args: {
		role: InsRole;
		institutionId: string;
		userId: string;
	},
): Promise<boolean> {
	const required = insPermissions.toPermissionObject(["assessment:delete"]);
	const roleStatements = insPermissions.insRoles[args.role].statements;

	if (insPermissions.hasPermission(roleStatements, required)) {
		return true;
	}

	if (args.role === "faculty") {
		const isHop = await ProgramFaculty.isHeadOfProgramForUser(
			ctx,
			args.institutionId,
			args.userId,
		);
		if (
			isHop &&
			insPermissions.hasPermission(
				insPermissions.insRoles.principal.statements,
				required,
			)
		) {
			return true;
		}
	}

	return false;
}

/**
 * Regular faculty (non-HoP) must be assigned to the sitting's class + program subject.
 * Owner / principal / HoP skip this check.
 */
export async function requireMarksAssignmentAccess(
	ctx: Ctx,
	args: {
		sitting: Doc<"assessmentSittings">;
		role: InsRole;
		institutionId: string;
		userId: string;
		userEmail: string;
	},
) {
	if (args.role === "owner" || args.role === "principal") {
		return;
	}

	if (args.role === "faculty") {
		const isHop = await ProgramFaculty.isHeadOfProgramForUser(
			ctx,
			args.institutionId,
			args.userId,
		);
		if (isHop) {
			return;
		}
	}

	const faculty = await Faculty.findByEmail(
		ctx,
		args.institutionId,
		args.userEmail,
	);
	if (!faculty) {
		throwAppError(ERROR_CODES.BASE.ACCESS_DENIED);
	}

	const assignment =
		await ClassSubjectFaculty.findByClassProgramSubjectAndFaculty(ctx, {
			classId: args.sitting.classId,
			programSubjectId: args.sitting.programSubjectId,
			facultyId: faculty._id,
		});

	if (!assignment) {
		throwAppError(ERROR_CODES.BASE.ACCESS_DENIED);
	}
}

/**
 * Same assignment gate for list queries scoped by class + program subject.
 */
export async function requireAssignedClassSubjectAccess(
	ctx: Ctx,
	args: {
		classId: Id<"classes">;
		programSubjectId: Id<"programSubjects">;
		role: InsRole;
		institutionId: string;
		userId: string;
		userEmail: string;
	},
) {
	if (args.role === "owner" || args.role === "principal") {
		return;
	}

	if (args.role === "faculty") {
		const isHop = await ProgramFaculty.isHeadOfProgramForUser(
			ctx,
			args.institutionId,
			args.userId,
		);
		if (isHop) {
			return;
		}
	}

	const faculty = await Faculty.findByEmail(
		ctx,
		args.institutionId,
		args.userEmail,
	);
	if (!faculty) {
		throwAppError(ERROR_CODES.BASE.ACCESS_DENIED);
	}

	const assignment =
		await ClassSubjectFaculty.findByClassProgramSubjectAndFaculty(ctx, {
			classId: args.classId,
			programSubjectId: args.programSubjectId,
			facultyId: faculty._id,
		});

	if (!assignment) {
		throwAppError(ERROR_CODES.BASE.ACCESS_DENIED);
	}
}
