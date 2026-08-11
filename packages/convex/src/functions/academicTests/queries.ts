import { insQuery } from "#helpers/customFunctions";
import { vv } from "#schema";
import type { InsRole } from "../../better-auth/ins-permissions";
import * as AcademicComponent from "./model/academicComponent";
import * as AcademicSchema from "./model/academicSchema";
import * as Access from "./model/access";
import * as AssessmentMark from "./model/assessmentMark";
import * as AssessmentMarkActivityLog from "./model/assessmentMarkActivityLog";
import * as AssessmentSitting from "./model/assessmentSitting";
import {
	AssessmentComponentListItemSchema,
	AssessmentComponentWithSchemaListItemSchema,
} from "./validator/assessmentComponent";
import {
	ConductedSittingListItemSchema,
	MarkActivityLogDtoSchema,
	MarksSheetSchema,
} from "./validator/assessmentMark";
import { AssessmentSchemaListItemSchema } from "./validator/assessmentSchema";
import {
	AssessmentSittingListItemSchema,
	EligibleClassForSittingSchema,
} from "./validator/assessmentSitting";

/** Lists all assessment schemas for the program-subject */
export const listAcademicSchemas = insQuery({
	args: {
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(AssessmentSchemaListItemSchema),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		return await AcademicSchema.listByProgramSubject(
			ctx,
			args.programSubjectId,
		);
	},
});

/** List all assessment components under assessment schema */
export const listAcademicComponents = insQuery({
	args: {
		assessmentSchemaId: vv.id("assessmentSchemas"),
	},
	returns: vv.array(AssessmentComponentListItemSchema),
	handler: async (ctx, args) => {
		await Access.requireSchemaInInstitution(
			ctx,
			args.assessmentSchemaId,
			ctx.institution._id,
		);

		return await AcademicComponent.listBySchema(ctx, args.assessmentSchemaId);
	},
});

/** List all components for a program-subject (with schema labels) */
export const listAcademicComponentsForProgramSubject = insQuery({
	permissions: ["program:view"],
	args: {
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(AssessmentComponentWithSchemaListItemSchema),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		return await AcademicComponent.listByProgramSubject(
			ctx,
			args.programSubjectId,
		);
	},
});

/** List scheduled/conducted sittings for a program-subject allocation */
export const listAssessmentSittings = insQuery({
	permissions: ["program:view"],
	args: {
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(AssessmentSittingListItemSchema),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		return await AssessmentSitting.listByProgramSubject(
			ctx,
			args.programSubjectId,
		);
	},
});

/** Classes eligible to sit this program-subject's assessments */
export const listEligibleClassesForSitting = insQuery({
	permissions: ["program:view"],
	args: {
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(EligibleClassForSittingSchema),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		return await AssessmentSitting.listEligibleClasses(
			ctx,
			args.programSubjectId,
		);
	},
});

/** Conducted sittings for the program Marks tab */
export const listConductedSittingsForProgramSubject = insQuery({
	permissions: ["assessment:view"],
	args: {
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(ConductedSittingListItemSchema),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		return await AssessmentSitting.listConductedByProgramSubject(
			ctx,
			args.programSubjectId,
		);
	},
});

/** Conducted sittings for a faculty-assigned class subject */
export const listConductedSittingsForAssigned = insQuery({
	permissions: ["assessment:view"],
	args: {
		classId: vv.id("classes"),
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(ConductedSittingListItemSchema),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		await Access.requireAssignedClassSubjectAccess(ctx, {
			classId: args.classId,
			programSubjectId: args.programSubjectId,
			role: ctx.membership.role as InsRole,
			institutionId: ctx.institution._id,
			userId: ctx.session.userId,
			userEmail: ctx.session.user.email,
		});

		return await AssessmentSitting.listConductedByClassAndProgramSubject(ctx, {
			classId: args.classId,
			programSubjectId: args.programSubjectId,
		});
	},
});

/** Full marks sheet for a conducted sitting */
export const getMarksSheet = insQuery({
	permissions: ["assessment:view"],
	args: {
		assessmentSittingId: vv.id("assessmentSittings"),
	},
	returns: MarksSheetSchema,
	handler: async (ctx, args) => {
		const sitting = await Access.requireSittingInInstitution(
			ctx,
			args.assessmentSittingId,
			ctx.institution._id,
		);

		await Access.requireMarksAssignmentAccess(ctx, {
			sitting,
			role: ctx.membership.role as InsRole,
			institutionId: ctx.institution._id,
			userId: ctx.session.userId,
			userEmail: ctx.session.user.email,
		});

		const canDeleteMarks = await Access.canDeleteMarks(ctx, {
			role: ctx.membership.role as InsRole,
			institutionId: ctx.institution._id,
			userId: ctx.session.userId,
		});

		return await AssessmentMark.getMarksSheet(ctx, {
			sitting,
			canDeleteMarks,
		});
	},
});

/** Audit trail for marks on a sitting */
export const listMarkActivity = insQuery({
	permissions: ["assessment:view"],
	args: {
		assessmentSittingId: vv.id("assessmentSittings"),
	},
	returns: vv.array(MarkActivityLogDtoSchema),
	handler: async (ctx, args) => {
		const sitting = await Access.requireSittingInInstitution(
			ctx,
			args.assessmentSittingId,
			ctx.institution._id,
		);

		await Access.requireMarksAssignmentAccess(ctx, {
			sitting,
			role: ctx.membership.role as InsRole,
			institutionId: ctx.institution._id,
			userId: ctx.session.userId,
			userEmail: ctx.session.user.email,
		});

		return await AssessmentMarkActivityLog.listBySitting(
			ctx,
			args.assessmentSittingId,
		);
	},
});
