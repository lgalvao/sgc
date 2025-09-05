declare global {
  interface Window {
    generateUniqueId: () => number;
  }
}