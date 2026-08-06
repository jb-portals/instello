import { ownerIdentity } from "../auth";
import { createInstitution } from "../factories/institution";
import { createOwner } from "../factories/owner";
import type { AppTest } from "../types";

/**
 * Owner who owns an institution — the common auth boundary for insMutation/insQuery.
 */
export async function arrangeOwnerInstitution(t: AppTest) {
	const owner = await createOwner(t);
	const institution = await createInstitution(t, { ownerId: owner._id });

	const asOwner = () =>
		t.withIdentity(ownerIdentity(owner._id, institution._id));

	return { owner, institution, asOwner };
}
