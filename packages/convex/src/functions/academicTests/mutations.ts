import { insMutation } from "#helpers/customFunctions";
import { vv } from "#schema";
import * as AcademicComponent from "./model/academicComponent";
import * as AcademicSchema from "./model/academicSchema";
import * as Access from "./model/access";
import * as AssessmentSitting from "./model/assessmentSitting";
import {
	CreateAssessmentComponentInput,
	PatchAssessmentComponentBody,
} from "./validator/assessmentComponent";
import {
	CreateAssessmentSchemaInput,
	PatchAssessmentSchemaBody,
} from "./validator/assessmentSchema";
import {
	PatchAssessmentSittingBody,
	ScheduleAssessmentSittingInput,
} from "./validator/assessmentSitting";

/** Create assessment schema */
export const createAssessmentSchema = insMutation({
	args: CreateAssessmentSchemaInput,
	returns: vv.id("assessmentSchemas"),
	handler: async (ctx, args) => {
		await Access.requireProgramSubjectInInstitution(
			ctx,
			args.programSubjectId,
			ctx.institution._id,
		);

		return await AcademicSchema.create(ctx, {
			programSubjectId: args.programSubjectId,
			name: args.name,
			description: args.description,
		});
	},
});

/** Create assessment component under schema */
export const createAssessmentComponent = insMutation({
	args: CreateAssessmentComponentInput,
	returns: vv.id("assessmentComponents"),
	handler: async (ctx, args) => {
		await Access.requireSchemaInInstitution(
			ctx,
			args.assessmentSchemaId,
			ctx.institution._id,
		);

		return await AcademicComponent.create(ctx, {
			assessmentSchemaId: args.assessmentSchemaId,
			name: args.name,
			totalAllotedMarks: args.totalAllotedMarks,
			passingMarks: args.passingMarks,
		});
	},
});

/** Update assessment schema details */
export const updateAssessmentSchema = insMutation({
	args: {
		id: vv.id("assessmentSchemas"),
		body: PatchAssessmentSchemaBody,
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireSchemaInInstitution(ctx, args.id, ctx.institution._id);

		await AcademicSchema.patch(ctx, args.id, args.body);
		return null;
	},
});

/** Update assessment component configuration */
export const updateAssessmentComponent = insMutation({
	args: {
		id: vv.id("assessmentComponents"),
		body: PatchAssessmentComponentBody,
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireComponentInInstitution(
			ctx,
			args.id,
			ctx.institution._id,
		);

		await AcademicComponent.patch(ctx, args.id, args.body);
		return null;
	},
});

/** Re-position assessment component to the given order index */
export const reposAssessmentComponent = insMutation({
	args: {
		id: vv.id("assessmentComponents"),
		orderIdx: vv.number(),
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireComponentInInstitution(
			ctx,
			args.id,
			ctx.institution._id,
		);

		await AcademicComponent.repos(ctx, args.id, args.orderIdx);
		return null;
	},
});

/** Remove the assessment schema and its components together */
export const removeAssessmentSchema = insMutation({
	args: {
		id: vv.id("assessmentSchemas"),
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireSchemaInInstitution(ctx, args.id, ctx.institution._id);

		await AcademicSchema.remove(ctx, args.id);
		return null;
	},
});

/** Remove the assessment component */
export const removeAssessmentComponent = insMutation({
	args: {
		id: vv.id("assessmentComponents"),
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireComponentInInstitution(
			ctx,
			args.id,
			ctx.institution._id,
		);

		await AcademicComponent.remove(ctx, args.id);
		return null;
	},
});

/** Short-lived URL for uploading a PDF question paper */
export const generateQuestionPaperUploadUrl = insMutation({
	permissions: ["program:update"],
	args: {},
	returns: vv.string(),
	handler: async (ctx) => {
		return await ctx.storage.generateUploadUrl();
	},
});

/** Schedule a per-class sitting for an assessment schema */
export const scheduleAssessmentSitting = insMutation({
	permissions: ["program:update"],
	args: ScheduleAssessmentSittingInput,
	returns: vv.id("assessmentSittings"),
	handler: async (ctx, args) => {
		await Access.requireSchemaInInstitution(
			ctx,
			args.assessmentSchemaId,
			ctx.institution._id,
		);

		const programSubjectId = await Access.resolveProgramSubjectIdForSchema(
			ctx,
			args.assessmentSchemaId,
		);

		return await AssessmentSitting.create(ctx, {
			assessmentSchemaId: args.assessmentSchemaId,
			classId: args.classId,
			programSubjectId,
			sessionDate: args.sessionDate,
			sessionStartTime: args.sessionStartTime,
			sessionEndTime: args.sessionEndTime,
			questionPaperStorageId: args.questionPaperStorageId,
			questionPaperFileName: args.questionPaperFileName,
		});
	},
});

/** Update a scheduled sitting (date / times / question paper) */
export const updateAssessmentSitting = insMutation({
	permissions: ["program:update"],
	args: {
		id: vv.id("assessmentSittings"),
		body: PatchAssessmentSittingBody,
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireSittingInInstitution(ctx, args.id, ctx.institution._id);

		await AssessmentSitting.patch(ctx, args.id, args.body);
		return null;
	},
});

/** Mark a scheduled sitting as conducted */
export const markAssessmentSittingConducted = insMutation({
	permissions: ["program:update"],
	args: {
		id: vv.id("assessmentSittings"),
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireSittingInInstitution(ctx, args.id, ctx.institution._id);

		await AssessmentSitting.markConducted(ctx, args.id);
		return null;
	},
});

/** Remove a scheduled sitting */
export const removeAssessmentSitting = insMutation({
	permissions: ["program:update"],
	args: {
		id: vv.id("assessmentSittings"),
	},
	returns: vv.null(),
	handler: async (ctx, args) => {
		await Access.requireSittingInInstitution(ctx, args.id, ctx.institution._id);

		await AssessmentSitting.remove(ctx, args.id);
		return null;
	},
});
