import type { Id } from "@instello/convex/dataModel";

export const QUESTION_PAPER_MAX_BYTES = 10 * 1024 * 1024;

export async function uploadQuestionPaperPdf(
	generateUploadUrl: () => Promise<string>,
	file: File,
): Promise<Id<"_storage">> {
	if (file.type !== "application/pdf") {
		throw new Error("Please select a PDF file");
	}

	if (file.size > QUESTION_PAPER_MAX_BYTES) {
		throw new Error("Question paper must be 10MB or smaller");
	}

	const uploadUrl = await generateUploadUrl();
	const result = await fetch(uploadUrl, {
		method: "POST",
		headers: { "Content-Type": file.type },
		body: file,
	});

	if (!result.ok) {
		throw new Error("Failed to upload question paper");
	}

	const { storageId } = (await result.json()) as {
		storageId: Id<"_storage">;
	};
	return storageId;
}
