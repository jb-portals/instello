<template>
  <Layout body-class="bg-slate-100">
    <Preheader>Your Instello verification code is inside.</Preheader>

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
          Verification code
        </Text>
        <Heading class="mt-2 text-2xl font-semibold text-slate-900">
          Verify your email
        </Heading>
        <Text class="m-0 mt-4 text-sm text-slate-600">
          Enter this code to continue signing in to Instello.
        </Text>

        <Section class="mt-6 rounded-xl border border-solid border-slate-200 bg-slate-50 py-4">
          <Text class="m-0 font-mono text-3xl font-semibold tracking-widest text-slate-900" th:text="${code}">
            123456
          </Text>
        </Section>

        <Text class="m-0 mt-6 text-sm text-slate-500">
          This code expires in
          <Text as="span" class="font-semibold text-slate-900" th:text="${expiresInMinutes}">10</Text>
          minutes.
        </Text>
        <Text class="m-0 mt-8 text-xs text-slate-500">
          If you did not request this code, you can ignore this email.
        </Text>
      </Section>

      <Text class="m-0 mt-6 text-center text-xs text-slate-400">
        Instello · contact@instello.in
      </Text>
    </Container>
  </Layout>
</template>
