import type { Doc, Id } from "#_generated/dataModel";
import * as OwnerOrganization from "#model/ownerOrganization";
import type { AppTest } from "../types.setup";

export type CreatedOwnerOrganization = Doc<"ownerOrganizations">;

let orgSeq = 0;

export async function createOwnerOrganization(
	t: AppTest,
	args: {
		ownerId: string;
		name?: string;
		slug?: string;
	},
): Promise<CreatedOwnerOrganization> {
	orgSeq += 1;
	const name = args.name ?? `Owner Org ${orgSeq}`;
	const slug = args.slug ?? `owner-org-${orgSeq}-${Date.now()}`;

	const orgId = await t.run(async (ctx) => {
		return await OwnerOrganization.create(ctx, {
			ownerId: args.ownerId,
			name,
			slug,
			addressLine: "1 Test Lane",
			city: "Bangalore",
			state: "Karnataka",
			postalCode: "560001",
			country: "India",
		});
	});

	const org = await t.run(async (ctx) => {
		return await ctx.db.get(
			"ownerOrganizations",
			orgId as Id<"ownerOrganizations">,
		);
	});

	if (!org) {
		throw new Error("Failed to create owner organization");
	}

	return org;
}
