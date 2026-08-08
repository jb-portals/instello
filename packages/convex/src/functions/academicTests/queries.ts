import { insQuery } from "#helpers/customFunctions";
import { vv } from "#schema";
import * as AcademicComponent from "./model/academicComponent";
import * as AcademicSchema from "./model/academicSchema";
import * as Access from "./model/access";
import * as AssessmentSitting from "./model/assessmentSitting";
import {
	AssessmentComponentListItemSchema,
	AssessmentComponentWithSchemaListItemSchema,
} from "./validator/assessmentComponent";
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
