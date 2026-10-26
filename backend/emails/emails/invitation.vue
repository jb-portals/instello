<template>
  <Layout body-class="bg-slate-100">
    <Preheader>Your Instello access request has been approved.</Preheader>

    <Container class="max-w-md mx-auto px-4 py-10">
      <Section class="rounded-2xl bg-white px-8 py-10 text-center">
        <Img
          th:src="${logoUrl}"
          src="https://instello.in/instello.svg"
          width="160"
          alt="Instello"
          class="mx-auto"
        />

        <Text class="m-0 mt-8 text-xs font-semibold uppercase tracking-widest text-indigo-600">
          Invitation
        </Text>
        <Heading class="mt-2 text-2xl font-semibold text-slate-900">
          Welcome to Instello
        </Heading>
        <Text class="m-0 mt-4 text-sm text-slate-600">
          Hi <Text as="span" th:text="${firstName}">there</Text>, your request has been approved.
          Create your account to start managing
          <Text as="span" class="font-semibold text-slate-900" th:text="${organizationName}">your organization</Text>
          on Instello.
        </Text>

        <Section class="mt-8">
          <Button
            th:href="${inviteUrl}"
            href="https://instello.in/signup"
            class="rounded-lg bg-indigo-600 px-6 py-3 text-sm font-semibold text-white"
          >
            Create your account
          </Button>
        </Section>

        <Text class="m-0 mt-6 text-sm text-slate-500">
          This invitation expires in
          <Text as="span" class="font-semibold text-slate-900" th:text="${expiresInDays}">7</Text>
          days.
        </Text>
        <Text class="m-0 mt-8 text-xs text-slate-500">
          If you did not request access to Instello, you can ignore this email.
        </Text>
      </Section>

      <Text class="m-0 mt-6 text-center text-xs text-slate-400">
        Instello · contact@instello.in
      </Text>
    </Container>
  </Layout>
</template>
