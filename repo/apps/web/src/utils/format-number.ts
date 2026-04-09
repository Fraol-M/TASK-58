/**
 * Formats a number with locale-aware thousands separators.
 */
export function formatNumber(
  value: number,
  options?: Intl.NumberFormatOptions,
): string {
  return new Intl.NumberFormat('en-US', options).format(value)
}

/**
 * Formats a decimal value as a percentage string.
 * e.g. formatPercent(0.856) => "85.6%"
 */
export function formatPercent(
  value: number,
  decimals: number = 1,
): string {
  return new Intl.NumberFormat('en-US', {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value)
}

/**
 * Formats a number as USD currency.
 */
export function formatCurrency(
  value: number,
  currency: string = 'USD',
): string {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(value)
}
