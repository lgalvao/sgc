/**
 * Utilitários de data para testes E2E
 * Copiado de frontend/src/utils/index.ts para evitar dependência do código fonte
 */

/**
 * Converte uma data em string, número ou objeto Date para um objeto Date.
 * @param dateInput A data a ser convertida.
 * @returns Um objeto Date ou nulo se a conversão falhar.
 */
export function parseDate(dateInput: string | number | Date | null | undefined): Date | null {
  if (dateInput === null || dateInput === undefined || dateInput === '') return null;

  // Se já for Date
  if (dateInput instanceof Date) {
    return isNaN(dateInput.getTime()) ? null : dateInput;
  }

  // Se for número (timestamp)
  if (typeof dateInput === 'number') {
    const d = new Date(dateInput);
    return isNaN(d.getTime()) ? null : d;
  }

  // Se for string
  if (typeof dateInput === 'string') {
    const s = dateInput.trim();
    if (!s) return null;

    // Detecta ISO com tempo (ex.: 2023-10-01T00:00:00Z) ou somente data (YYYY-MM-DD)
    // Para strings somente-data (YYYY-MM-DD) devemos construir a Date em horário local
    // para evitar deslocamento de fuso horário que causa `getMonth()` diferente do esperado.
    const isoDateWithOptionalTimeRe = /^\d{4}-\d{2}-\d{2}(T.*)?$/;
    if (isoDateWithOptionalTimeRe.test(s)) {
      // Se for somente data (sem 'T'), construir em horário local
      if (!s.includes('T')) {
        const parts = s.split('-');
        const year = Number(parts[0]);
        const month = Number(parts[1]);
        const day = Number(parts[2]);
        const d = new Date(year, month - 1, day);
        if (!isNaN(d.getTime())) return d;
      } else {
        const d = new Date(s);
        if (!isNaN(d.getTime())) return d;
      }
    }

    // Detecta timestamp numérico em string
    const numericRe = /^\d{10,}$/;
    if (numericRe.test(s)) {
      const n = Number(s);
      const d = new Date(n);
      if (!isNaN(d.getTime())) return d;
    }

    // Detecta DD/MM/YYYY
    const ddmmyyyy = /^(\d{2})\/(\d{2})\/(\d{4})$/;
    const m = ddmmyyyy.exec(s);
    if (m) {
      const day = parseInt(m[1], 10);
      const month = parseInt(m[2], 10);
      const year = parseInt(m[3], 10);
      if (year >= 1000 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
        const d = new Date(year, month -1, day);
        // Verifica componentes para evitar 31/02 etc.
        if (!isNaN(d.getTime()) && d.getUTCFullYear() === year && d.getUTCMonth() === month -1 && d.getUTCDate() === day) {
          return d;
        }
      }
    }

    // Falha ao parsear
    return null;
  }

  return null;
}
