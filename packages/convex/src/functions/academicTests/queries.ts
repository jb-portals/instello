import { ERROR_CODES, throwAppError } from "#helpers/constants";
import { insQuery } from "#helpers/customFunctions";
import { vv } from "#schema";
import { AssessmentComponentListItemSchema } from "./validator/assessmentComponent";
import { AssessmentSchemaListItemSchema } from "./validator/assessmentSchema";

/** Lists all assessment schemas for the program-subject */
export const listAcademicSchemas = insQuery({
	args: {
		programSubjectId: vv.id("programSubjects"),
	},
	returns: vv.array(AssessmentSchemaListItemSchema),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** List all assessment components under assessment schema */
export const listAcademicComponents = insQuery({
	args: {
		assessmentSchemaId: vv.id("assessmentSchemas"),
	},
	returns: vv.array(AssessmentComponentListItemSchema),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});
