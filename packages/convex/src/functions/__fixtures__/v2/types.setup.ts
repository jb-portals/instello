import type { TestConvex } from "convex-test";
import type schema from "#schema";

/** Convex test harness — the only value provided by the v2 fixture context. */
export type AppTest = TestConvex<typeof schema>;
