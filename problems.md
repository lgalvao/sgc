# Frontend Unit Test Failures after BootstrapVueNext Migration

After migrating the frontend modal components to use the `BootstrapVueNext` library, a significant number of unit tests began to fail. This document outlines the nature of the failures and the attempts made to resolve them.

## Problem Description

The primary issue is that the unit tests for the modal components are unable to find and interact with the elements of the `<b-modal>` component from `BootstrapVueNext`. This leads to two main types of errors:

1.  **`Cannot call ... on an empty DOMWrapper`**: This error occurs when the test attempts to find an element (e.g., a button or a title) that is no longer rendered in the same way as it was in the manual modal implementation.

2.  **`expected false to be true`**: This error arises from the fact that `<b-modal>` components are not rendered in the DOM when they are hidden (`v-if="false"`). The tests were written with the expectation that the modal's root element would always exist, even when hidden.

## Attempts to Resolve

### 1. Using `findComponent`

The initial approach was to use `@vue/test-utils`'s `findComponent` to locate the `<b-modal>` instance and then assert on its props. This failed with the error `Cannot call props on an empty VueWrapper`, indicating that the component could not be found. This is likely due to `BModal` being a transitive dependency that is not properly stubbed in the test environment.

### 2. Using `b-modal-stub`

The second approach was to look for the `b-modal-stub` component, which is what `@vue/test-utils` should render in place of the actual component. This approach also failed, but with a different error: `expected false to be true`. This indicates that the stub is not being rendered at all when the modal is hidden, which is consistent with the behavior of the real component.

### 3. File Path Issues

In addition to the testing issues, I also encountered a file path issue where a test file was placed in an incorrect directory (`frontend/srcs` instead of `frontend/src`). I have since corrected this.

## Conclusion

Despite multiple attempts to fix the unit tests, the issues persist. Given the time constraints and the user's request to proceed, I am submitting the pull request with the failing tests. Further investigation is needed to determine the correct way to test components that use `BootstrapVueNext` in this environment. It's possible that a global configuration in `vitest.setup.ts` is required to properly stub the `BootstrapVueNext` components.
