import { ownerIdentity } from "../auth.setup";
import { createInstitution } from "../factories/institution.setup";
import { createOwner } from "../factories/owner.setup";
import type { AppTest } from "../types.setup";

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
