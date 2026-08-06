import { ERROR_CODES, throwAppError } from "#helpers/constants";
import { insMutation } from "#helpers/customFunctions";
import { vv } from "#schema";
import {
	CreateAssessmentComponentInput,
	PatchAssessmentComponentBody,
} from "./validator/assessmentComponent";
import {
	CreateAssessmentSchemaInput,
	PatchAssessmentSchemaBody,
} from "./validator/assessmentSchema";

/** Create assessment schema */
export const createAssessmentSchema = insMutation({
	args: CreateAssessmentSchemaInput,
	returns: vv.id("assessmentSchemas"),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** Create assessment component under schema */
export const createAssessmentComponent = insMutation({
	args: CreateAssessmentComponentInput,
	returns: vv.id("assessmentComponents"),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** Update assessment schema details */
export const updateAssessmentSchema = insMutation({
	args: {
		id: vv.id("assessmentSchemas"),
		body: PatchAssessmentSchemaBody,
	},
	returns: vv.null(),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** Update assessment component configuration */
export const updateAssessmentComponent = insMutation({
	args: {
		id: vv.id("assessmentComponents"),
		body: PatchAssessmentComponentBody,
	},
	returns: vv.null(),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** Re-position assessment component to the given order index */
export const reposAssessmentComponent = insMutation({
	args: {
		id: vv.id("assessmentComponents"),
		orderIdx: vv.number(),
	},
	returns: vv.null(),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** Remove the assessment schema and its components together */
export const removeAssessmentSchema = insMutation({
	args: {
		id: vv.id("assessmentSchemas"),
	},
	returns: vv.null(),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});

/** Remove the assessment component */
export const removeAssessmentComponent = insMutation({
	args: {
		id: vv.id("assessmentComponents"),
	},
	returns: vv.null(),
	handler: async () => {
		throwAppError(ERROR_CODES.BASE.METHOD_NOT_IMPLEMENTED);
	},
});
