/**
 * Utilitários centralizados do projeto SGC
 */

import {CLASSES_BADGE_SITUACAO} from '@/constants/situacoes';
import {TipoNotificacao} from '@/stores/notificacoes';

// ===== GERAÇÃO DE IDs =====
let counter = 0;
export function generateUniqueId(): number {
  return Date.now() * 1000 + (counter++ % 1000);
}

// ===== CLASSES DE BADGE =====
export function badgeClass(situacao: string): string {
  return CLASSES_BADGE_SITUACAO[situacao as keyof typeof CLASSES_BADGE_SITUACAO] || 'bg-secondary';
}

// ===== ÍCONES DE NOTIFICAÇÃO =====
export const iconeTipo = (tipo: TipoNotificacao): string => {
  switch (tipo) {
    case 'success': return 'bi bi-check-circle-fill text-success';
    case 'error': return 'bi bi-exclamation-triangle-fill text-danger';
    case 'warning': return 'bi bi-exclamation-triangle-fill text-warning';
    case 'info': return 'bi bi-info-circle-fill text-info';
    case 'email': return 'bi bi-envelope-fill text-primary';
    default: return 'bi bi-bell-fill';
  }
};

// ===== UTILITÁRIOS DE DATA =====
export function parseDate(dateString: string | null | undefined): Date | null {
  if (!dateString) return null;

  try {
    // Formato brasileiro DD/MM/YYYY
    const brMatch = dateString.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (brMatch) {
      const [, day, month, year] = brMatch;
      const dayNum = parseInt(day);
      const monthNum = parseInt(month) - 1;
      const yearNum = parseInt(year);

      if (dayNum < 1 || dayNum > 31 || monthNum < 0 || monthNum > 11 || yearNum < 1900 || yearNum > 2100) {
        return null;
      }

      return new Date(yearNum, monthNum, dayNum);
    }

    // Formato ISO
    const date = new Date(dateString + 'T00:00:00.000Z');
    if (!isNaN(date.getTime())) {
      return date;
    }
    return null;
  } catch {
    return null;
  }
}

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
    if (!dateObj || isNaN(dateObj.getTime())) return 'Data inválida';

    return dateObj.toLocaleDateString('pt-BR', options);
  } catch {
    return 'Data inválida';
  }
}

export function formatDateForInput(date: Date | null | undefined): string {
  if (!date || isNaN(date.getTime())) return '';

  try {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  } catch {
    return '';
  }
}

export function formatDateTimeBR(date: Date | string | null | undefined): string {
  return formatDateBR(date, {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

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

export function diffInDays(date1: Date, date2: Date): number {
  const diffTime = Math.abs(date2.getTime() - date1.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

export function ensureValidDate(date: Date | null | undefined): Date | null {
  if (!date) return null;
  if (date instanceof Date && !isNaN(date.getTime())) {
    return date;
  }
  return null;
}