import { components } from "#_generated/api";
import type { Id } from "#_generated/dataModel";
import type { AppMutationCtx, AppQueryCtx } from "#model/common.types";
import { splitUserName } from "../../attendance/helpers";
import type {
	MarkActivityChange,
	MarkActivityLogDto,
	MarksActivitySummary,
} from "../validator/assessmentMark";

const LIST_LIMIT = 100;

/** Append a new activity log for a sitting's marks. */
export async function appendLog(
	ctx: AppMutationCtx,
	args: {
		assessmentSittingId: Id<"assessmentSittings">;
		action: "created" | "updated" | "deleted";
		performedBy: string;
		performedAt: number;
		changes: MarkActivityChange[];
	},
) {
	if (args.changes.length === 0) {
		return;
	}

	await ctx.db.insert("assessmentMarkActivityLogs", {
		assessmentSittingId: args.assessmentSittingId,
		action: args.action,
		performedBy: args.performedBy,
		performedAt: args.performedAt,
		changes: args.changes,
	});
}

async function toDto(
	ctx: AppQueryCtx | AppMutationCtx,
	log: {
		_id: Id<"assessmentMarkActivityLogs">;
		assessmentSittingId: Id<"assessmentSittings">;
		action: "created" | "updated" | "deleted";
		performedBy: string;
		performedAt: number;
		changes: MarkActivityChange[];
	},
): Promise<MarkActivityLogDto> {
	const user = await ctx.runQuery(components.betterAuth.users.getById, {
		userId: log.performedBy,
	});
	const { firstName, lastName } = splitUserName(user.name);

	return {
		_id: log._id,
		assessmentSittingId: log.assessmentSittingId,
		action: log.action,
		performedBy: {
			_id: user._id,
			name: `${firstName} ${lastName}`.trim(),
			...(user.image ? { image: user.image } : {}),
		},
		performedAt: log.performedAt,
		changes: log.changes,
	};
}

/** Get the latest activity log for a sitting, or null. */
export async function getLatestForSitting(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentSittingId: Id<"assessmentSittings">,
) {
	const logs = await ctx.db
		.query("assessmentMarkActivityLogs")
		.withIndex("by_sitting", (q) =>
			q.eq("assessmentSittingId", assessmentSittingId),
		)
		.order("desc")
		.take(1);

	const latest = logs[0];
	if (!latest) return null;
	return await toDto(ctx, latest);
}

/** List activity logs for a sitting (newest first). */
export async function listBySitting(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentSittingId: Id<"assessmentSittings">,
): Promise<MarkActivityLogDto[]> {
	const logs = await ctx.db
		.query("assessmentMarkActivityLogs")
		.withIndex("by_sitting", (q) =>
			q.eq("assessmentSittingId", assessmentSittingId),
		)
		.order("desc")
		.take(LIST_LIMIT);

	return await Promise.all(logs.map((log) => toDto(ctx, log)));
}

/** Build the summary for an activity log shown on conducted sitting rows. */
export function toActivitySummary(
	log: MarkActivityLogDto,
): MarksActivitySummary {
	const changedCount = log.changes.length;
	const noun = changedCount === 1 ? "mark" : "marks";
	const description =
		log.action === "created"
			? `Created ${changedCount} ${noun}`
			: log.action === "updated"
				? `Updated ${changedCount} ${noun}`
				: `Deleted ${changedCount} ${noun}`;

	return {
		actor: log.performedBy,
		description,
		updatedAt: log.performedAt,
	};
}

/** Latest activity summary for a sitting, or undefined when none. */
export async function getLatestActivitySummary(
	ctx: AppQueryCtx | AppMutationCtx,
	assessmentSittingId: Id<"assessmentSittings">,
): Promise<MarksActivitySummary | undefined> {
	const latest = await getLatestForSitting(ctx, assessmentSittingId);
	if (!latest) return undefined;
	return toActivitySummary(latest);
}

/** Cascade helper — deletes all activity logs for a sitting. */
export async function removeAllBySitting(
	ctx: AppMutationCtx,
	assessmentSittingId: Id<"assessmentSittings">,
) {
	const logs = await ctx.db
		.query("assessmentMarkActivityLogs")
		.withIndex("by_sitting", (q) =>
			q.eq("assessmentSittingId", assessmentSittingId),
		)
		.take(LIST_LIMIT);

	for (const log of logs) {
		await ctx.db.delete("assessmentMarkActivityLogs", log._id);
	}
}
