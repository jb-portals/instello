import { insQuery } from "#helpers/customFunctions";
import { vv } from "#schema";
import * as AcademicComponent from "./model/academicComponent";
import * as AcademicSchema from "./model/academicSchema";
import * as Access from "./model/access";
import { AssessmentComponentListItemSchema } from "./validator/assessmentComponent";
import { AssessmentSchemaListItemSchema } from "./validator/assessmentSchema";

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
