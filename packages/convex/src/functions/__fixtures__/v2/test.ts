import { convexTest } from "convex-test";
import { test as vitestTest } from "vitest";
import betterAuthSchema from "#betterAuth/schema";
import schema from "#schema";
import { betterAuthModules, modules } from "#test.config";

/**
 * Minimal Vitest fixture: only the Convex test harness (`t`).
 * Arrange all domain data via factory / scenario helpers instead of fixture context.
 */
const test = vitestTest.extend("t", async () => {
	const t = convexTest(schema, modules);
	t.registerComponent("betterAuth", betterAuthSchema, betterAuthModules);
	return t;
});

export function baseTest() {
	return test;
}
