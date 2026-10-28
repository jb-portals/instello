import { type NextRequest, NextResponse } from "next/server"
import { getInstitutionSlug, institutionUrl, isValidSlug } from "@/lib/tenant"

export function proxy(request: NextRequest) {
  const { pathname, search } = request.nextUrl
  const host = request.headers.get("x-forwarded-host") ?? request.headers.get("host")
  const slug = getInstitutionSlug(host)

  if (slug) {
    const url = request.nextUrl.clone()
    url.pathname = `/${slug}${pathname === "/" ? "" : pathname}`
    return NextResponse.rewrite(url)
  }

  // Bare root domain: the slug must come from the host, never from the path,
  // so `/acme/about` is redirected to `acme.<root-domain>/about`.
  if (pathname !== "/") {
    const [, firstSegment = "", ...rest] = pathname.split("/")
    if (isValidSlug(firstSegment)) {
      const protocol = request.headers.get("x-forwarded-proto") ?? request.nextUrl.protocol
      const restPath = rest.length > 0 ? `/${rest.join("/")}` : "/"
      return NextResponse.redirect(
        institutionUrl(firstSegment, `${restPath}${search}`, `${protocol.replace(/:$/, "")}:`),
      )
    }
  }

  return NextResponse.next()
}

export const config = {
  matcher: ["/((?!api|_next|.*\\..*).*)"],
}
