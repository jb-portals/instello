import { redirect } from "next/navigation";

export default async function Page({
  params,
}: {
  params: Promise<{ alias: string; programSubjectId: string }>;
}) {
  const { alias, programSubjectId } = await params;
  redirect(`/p/${alias}/subjects/${programSubjectId}/schemas`);
}
