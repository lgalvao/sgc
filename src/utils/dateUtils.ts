/**
 * Utilitários para manipulação de datas no projeto SGC
 */

/**
 * Converte uma string de data para objeto Date
 * @param dateString - String no formato ISO ou brasileiro
 * @returns Date object ou null se inválido
 */
export function parseDate(dateString: string | null | undefined): Date | null {
  if (!dateString) return null;

  try {
    // Tenta formato ISO primeiro
    const date = new Date(dateString);
    if (!isNaN(date.getTime())) {
      return date;
    }

    // Tenta formato brasileiro DD/MM/YYYY
    const brMatch = dateString.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (brMatch) {
      const [, day, month, year] = brMatch;
      return new Date(parseInt(year), parseInt(month) - 1, parseInt(day));
    }

    return null;
  } catch {
    return null;
  }
}

/**
 * Formata data para exibição em português brasileiro
 * @param date - Date object ou string
 * @param options - Opções de formatação
 * @returns String formatada ou 'Não informado' se null
 */
export function formatDateBR(
  date: Date | string | null | undefined,
  options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }
): string {
  if (!date) return 'Não informado';

  try {
    const dateObj = typeof date === 'string' ? parseDate(date) : date;
    if (!dateObj) return 'Data inválida';

    return dateObj.toLocaleDateString('pt-BR', options);
  } catch {
    return 'Data inválida';
  }
}

/**
 * Formata data para campos de input (YYYY-MM-DD)
 * @param date - Date object
 * @returns String no formato YYYY-MM-DD ou vazia se null
 */
export function formatDateForInput(date: Date | null | undefined): string {
  if (!date) return '';

  try {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch {
    return '';
  }
}

/**
 * Formata data e hora para exibição
 * @param date - Date object ou string
 * @returns String formatada com data e hora
 */
export function formatDateTimeBR(date: Date | string | null | undefined): string {
  return formatDateBR(date, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Verifica se uma data é válida e futura
 * @param date - Date object
 * @returns true se a data é hoje ou futura
 */
export function isDateValidAndFuture(date: Date | null | undefined): boolean {
  if (!date) return false;

  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    date.setHours(0, 0, 0, 0);
    return date >= today;
  } catch {
    return false;
  }
}

/**
 * Calcula diferença em dias entre duas datas
 * @param date1 - Primeira data
 * @param date2 - Segunda data
 * @returns Número de dias de diferença
 */
export function diffInDays(date1: Date, date2: Date): number {
  const diffTime = Math.abs(date2.getTime() - date1.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}