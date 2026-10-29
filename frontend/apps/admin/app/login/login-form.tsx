"use client"

import { useRequestOTP, useVerifyOTP } from "@instello/api-client/query"
import { Button } from "@instello/ui/components/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@instello/ui/components/card"
import { Field, FieldError, FieldLabel } from "@instello/ui/components/field"
import { Input } from "@instello/ui/components/input"
import { InputOTP, InputOTPGroup, InputOTPSlot } from "@instello/ui/components/input-otp"
import { useRouter } from "next/navigation"
import { SubmitEventHandler, useEffect, useState } from "react"
import { errorMessage } from "@/lib/auth"

const RESEND_COOLDOWN_SECONDS = 60
const otpSlotClass =
  "size-12 border-background bg-muted text-base first:rounded-l-xl first:border-l last:rounded-r-xl"

export function LoginForm() {
  const router = useRouter()
  const requestOtp = useRequestOTP()
  const verifyOtp = useVerifyOTP()
  const [step, setStep] = useState<"email" | "code">("email")
  const [email, setEmail] = useState("")
  const [code, setCode] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [secondsLeft, setSecondsLeft] = useState(0)

  useEffect(() => {
    if (secondsLeft <= 0) return
    const timeout = window.setTimeout(() => setSecondsLeft((current) => current - 1), 1000)
    return () => window.clearTimeout(timeout)
  }, [secondsLeft])

  const onRequestCode: SubmitEventHandler<HTMLFormElement> = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      await requestOtp.mutateAsync({ data: { email: email.trim() } })
      setStep("code")
      setSecondsLeft(RESEND_COOLDOWN_SECONDS)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  const onVerifyCode: SubmitEventHandler<HTMLFormElement> = async (event) => {
    event.preventDefault()
    setError(null)
    try {
      await verifyOtp.mutateAsync({ data: { email: email.trim(), code } })
      router.replace("/")
      router.refresh()
    } catch (verifyError) {
      setError(errorMessage(verifyError))
    }
  }

  async function onResend() {
    setError(null)
    try {
      await requestOtp.mutateAsync({ data: { email: email.trim() } })
      setSecondsLeft(RESEND_COOLDOWN_SECONDS)
    } catch (requestError) {
      setError(errorMessage(requestError))
    }
  }

  return (
    <main className="flex min-h-svh flex-col items-center justify-center gap-8 p-6">
      {step === "email" ? <img src="/instello.svg" alt="Instello" className="h-auto w-40" /> : null}

      {step === "email" ? (
        <form onSubmit={onRequestCode}>
          <Card className="w-full min-w-xs max-w-md">
            <CardHeader className="text-center">
              <CardTitle className="text-xl">Sign in</CardTitle>
              <CardDescription>Enter your admin email and we will send you a code.</CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col gap-4">
              <Field data-invalid={error ? true : undefined}>
                <FieldLabel htmlFor="email">Email</FieldLabel>
                <Input
                  id="email"
                  type="email"
                  autoComplete="email"
                  required
                  value={email}
                  aria-invalid={error ? true : undefined}
                  onChange={(event) => setEmail(event.target.value)}
                />
                <FieldError>{error}</FieldError>
              </Field>
              <Button type="submit" className="h-10 w-full" disabled={requestOtp.isPending}>
                {requestOtp.isPending ? "Sending code..." : "Continue"}
              </Button>
            </CardContent>
          </Card>
        </form>
      ) : (
        <form onSubmit={onVerifyCode}>
          <Card className="w-full min-w-xs max-w-md">
            <CardHeader className="text-center">
              <CardTitle className="text-xl">Account verification</CardTitle>
              <CardDescription>
                We have sent a code to your email. Check it and verify it here.
              </CardDescription>
            </CardHeader>
            <CardContent className="flex flex-col items-center gap-5">
              <Field className="items-center" data-invalid={error ? true : undefined}>
                <InputOTP
                  maxLength={6}
                  value={code}
                  aria-invalid={error ? true : undefined}
                  onChange={setCode}
                >
                  <InputOTPGroup>
                    <InputOTPSlot index={0} className={otpSlotClass} />
                    <InputOTPSlot index={1} className={otpSlotClass} />
                    <InputOTPSlot index={2} className={otpSlotClass} />
                    <InputOTPSlot index={3} className={otpSlotClass} />
                    <InputOTPSlot index={4} className={otpSlotClass} />
                    <InputOTPSlot index={5} className={otpSlotClass} />
                  </InputOTPGroup>
                </InputOTP>
                <FieldError className="text-center">{error}</FieldError>
              </Field>
              <p className="text-center text-sm text-muted-foreground">
                Didn't receive an email?{" "}
                {secondsLeft > 0 ? (
                  <span className="text-primary">Resend (in {secondsLeft}s)</span>
                ) : (
                  <Button
                    type="button"
                    variant="link"
                    className="h-auto px-0"
                    disabled={requestOtp.isPending}
                    onClick={() => void onResend()}
                  >
                    Resend
                  </Button>
                )}
              </p>
              <Button
                type="submit"
                className="h-10 w-full"
                disabled={verifyOtp.isPending || code.length !== 6}
              >
                {verifyOtp.isPending ? "Verifying..." : "Verify & Continue"}
              </Button>
              <Button
                type="button"
                variant="link"
                className="h-auto text-muted-foreground"
                onClick={() => {
                  setStep("email")
                  setCode("")
                  setError(null)
                  setSecondsLeft(0)
                }}
              >
                Use a different email
              </Button>
            </CardContent>
          </Card>
        </form>
      )}
    </main>
  )
}
