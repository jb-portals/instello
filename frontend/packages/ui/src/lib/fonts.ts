import { Geist_Mono, Roboto, Roboto_Slab } from "next/font/google"

const fontHeading = Roboto_Slab({ subsets: ["latin"], variable: "--font-heading" })
const fontSans = Roboto({ subsets: ["latin"], variable: "--font-sans" })
const fontMono = Geist_Mono({ subsets: ["latin"], variable: "--font-mono" })

export const fontVariables = [fontMono.variable, fontSans.variable, fontHeading.variable].join(" ")
