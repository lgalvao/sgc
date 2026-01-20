import 'vitest'

declare module 'vitest' {
  interface Assertion {
    toHaveNoViolations(): any
  }
  interface AsymmetricMatchersContaining {
    toHaveNoViolations(): any
  }
}
