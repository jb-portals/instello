import type { CreatedAssessmentComponent } from "../factories/assessmentComponent";
import { createAssessmentComponent } from "../factories/assessmentComponent";
import type { CreatedAssessmentSchema } from "../factories/assessmentSchema";
import { createAssessmentSchema } from "../factories/assessmentSchema";
import type { AppTest } from "../types";
import {
	arrangeProgramSubjectReady,
	type ProgramSubjectReady,
} from "./programSubjectReady";

type AssessmentSchemaReadyBase = ProgramSubjectReady & {
	assessmentSchema: CreatedAssessmentSchema;
};

export type AssessmentSchemaReady = AssessmentSchemaReadyBase & {
	components: CreatedAssessmentComponent[];
};

export type AssessmentSchemaWithComponents = AssessmentSchemaReadyBase & {
	/** First arranged component — present when `componentCount` > 0. */
	component: CreatedAssessmentComponent;
	components: CreatedAssessmentComponent[];
};

/**
 * Program-subject with an assessment schema and optional components already arranged.
 */
export async function arrangeAssessmentSchemaReady(
	t: AppTest,
): Promise<AssessmentSchemaReady>;
export async function arrangeAssessmentSchemaReady(
	t: AppTest,
	options: {
		schemaName?: string;
		componentCount: number;
	},
): Promise<AssessmentSchemaWithComponents>;
export async function arrangeAssessmentSchemaReady(
	t: AppTest,
	options?: {
		schemaName?: string;
		componentCount?: number;
	},
): Promise<AssessmentSchemaReady | AssessmentSchemaWithComponents> {
	const base = await arrangeProgramSubjectReady(t);
	const assessmentSchema = await createAssessmentSchema(t, {
		programSubjectId: base.programSubject._id,
		name: options?.schemaName ?? "CIE 1",
		description: "Continuous internal evaluation",
	});

	const componentCount = options?.componentCount ?? 0;
	const components: CreatedAssessmentComponent[] = [];
	for (let i = 0; i < componentCount; i++) {
		components.push(
			await createAssessmentComponent(t, {
				assessmentSchemaId: assessmentSchema._id,
				name: `IA ${i + 1}`,
				totalAllotedMarks: 20,
				passingMarks: 8,
				orderIdx: i,
			}),
		);
	}

	if (componentCount > 0) {
		const component = components[0];
		if (!component) {
			throw new Error("Expected at least one assessment component");
		}

		return {
			...base,
			assessmentSchema,
			component,
			components,
		};
	}

	return {
		...base,
		assessmentSchema,
		components,
	};
}
