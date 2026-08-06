import { components } from "#_generated/api";
import type { AppTest } from "../types.setup";

export type CreatedInstitution = {
	_id: string;
	name: string;
	slug: string;
	code: string;
	addressLine: string;
	district: string;
	state: string;
	country: string;
	zipCode: string;
};

let institutionSeq = 0;

export async function createInstitution(
	t: AppTest,
	args: {
		ownerId: string;
		name?: string;
		slug?: string;
		code?: string;
	},
): Promise<CreatedInstitution> {
	institutionSeq += 1;
	const name = args.name ?? `Test College ${institutionSeq}`;
	const slug = args.slug ?? `test-college-${institutionSeq}-${Date.now()}`;
	const code = args.code ?? String(100 + institutionSeq);

	const institution = await t.run(async (ctx) => {
		const created = await ctx.runMutation(
			components.betterAuth.adapter.create,
			{
				input: {
					model: "institution",
					data: {
						name,
						slug,
						code,
						addressLine: "123 Main Street",
						district: "Bangalore Urban",
						state: "Karnataka",
						country: "India",
						zipCode: "560001",
						createdAt: Date.now(),
					},
				},
			},
		);

		await ctx.runMutation(components.betterAuth.adapter.create, {
			input: {
				model: "institutionMember",
				data: {
					organizationId: created._id,
					userId: args.ownerId,
					role: "owner",
					createdAt: Date.now(),
				},
			},
		});

		return created;
	});

	return {
		_id: institution._id,
		name: institution.name,
		slug: institution.slug,
		code: institution.code,
		addressLine: institution.addressLine,
		district: institution.district,
		state: institution.state,
		country: institution.country,
		zipCode: institution.zipCode,
	};
}
