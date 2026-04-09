/**
 * Simulates network latency by waiting a random duration between min and max milliseconds.
 */
export function simulateDelay(min: number = 200, max: number = 500): Promise<void> {
  const ms = Math.floor(Math.random() * (max - min + 1)) + min
  return new Promise((resolve) => setTimeout(resolve, ms))
}
