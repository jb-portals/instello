import type { Metadata } from "next"
import type { ReactNode } from "react"
import "@instello/ui/globals.css"
import { ThemeProvider } from "@instello/ui/components/theme-provider"
import { fontVariables } from "@instello/ui/lib/fonts"
import { cn } from "@instello/ui/lib/utils"
import { Providers } from "./providers"

export const metadata: Metadata = {
  title: { default: "Instello Admin", template: "%s | Instello Admin" },
}

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="en" suppressHydrationWarning className={cn("font-sans antialiased", fontVariables)}>
      <body>
        <ThemeProvider>
          <Providers>{children}</Providers>
        </ThemeProvider>
      </body>
    </html>
  )
}
