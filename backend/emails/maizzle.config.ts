import { defineConfig } from '@maizzle/framework'

export default defineConfig({
  content: ['emails/**/*.vue'],
  output: { path: '../target/classes/templates/email', extension: 'html' },
})