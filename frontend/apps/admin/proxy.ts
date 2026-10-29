import { type NextRequest, NextResponse } from "next/server"

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl
  if (pathname === "/login" || request.cookies.has("JSESSIONID")) return NextResponse.next()

  const url = request.nextUrl.clone()
  url.pathname = "/login"
  return NextResponse.redirect(url)
}

export const config = {
  matcher: ["/((?!_next|v0.1|.*\\..*).*)"],
}
