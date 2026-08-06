import { components } from "#_generated/api";
import type { AppTest } from "../types.setup";

export type CreatedOwner = {
	_id: string;
	name: string;
	email: string;
};

let ownerSeq = 0;

export async function createOwner(
	t: AppTest,
	overrides?: Partial<{ name: string; email: string }>,
): Promise<CreatedOwner> {
	ownerSeq += 1;
	const name = overrides?.name ?? `Owner ${ownerSeq}`;
	const email =
		overrides?.email ?? `owner-${ownerSeq}-${Date.now()}@example.com`;

	const user = await t.run(async (ctx) => {
		return await ctx.runMutation(components.betterAuth.adapter.create, {
			input: {
				model: "user",
				data: {
					name,
					email,
					createdAt: Date.now(),
					emailVerified: true,
					updatedAt: Date.now(),
					role: "owner",
				},
			},
		});
	});

	return { _id: user._id, name, email };
}
