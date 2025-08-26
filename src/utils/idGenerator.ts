let counter = 0

export function generateUniqueId(): number {
  return Date.now() * 1000 + (counter++ % 1000)
}