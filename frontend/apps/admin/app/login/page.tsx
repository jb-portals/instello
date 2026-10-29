import { redirect } from "next/navigation"
import { isAdmin } from "@/lib/auth"
import { getCurrentUser } from "@/lib/session"
import { LoginForm } from "./login-form"

export default async function LoginPage() {
  const user = await getCurrentUser()
  if (user && isAdmin(user.authorities)) redirect("/")
  return <LoginForm />
}
