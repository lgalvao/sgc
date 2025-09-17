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
// Removendo import { parse, isValid } from 'date-fns';

export function parseDate(dateString: string | null | undefined): Date | null {
    if (!dateString) {
        return null;
    }

    // Tentar parsear como ISO 8601 primeiro
    let date = new Date(dateString);
    if (!isNaN(date.getTime())) {
        return date;
    }

    // Tentar parsear como DD/MM/YYYY manualmente
    const parts = dateString.match(/^(\d{2})\/(\d{2})\/(\d{4})$/);
    if (parts) {
        const day = parseInt(parts[1], 10);
        const month = parseInt(parts[2], 10); // Mês (1-12)
        const year = parseInt(parts[3], 10);

        // Validação básica de dia, mês e ano
        if (year < 1000 || year > 9999 || month < 1 || month > 12 || day < 1 || day > 31) {
            return null;
        }

        date = new Date(year, month - 1, day); // <-- Mudar para 'date'

        // Verificar se a data criada é válida e se os componentes correspondem
        // Isso é crucial para pegar datas como 31/02 ou meses inválidos
        if (!isNaN(date.getTime()) && // Mudar parsedDate para date
            date.getFullYear() === year && // Mudar parsedDate para date
            date.getMonth() === (month - 1) && // Mudar parsedDate para date
            date.getDate() === day) { // Mudar parsedDate para date
            return date; // Mudar parsedDate para date
        } // <-- Adicionado o fechamento do if (parts)
    } // <-- Adicionado o fechamento do if (!isNaN(date.getTime()))

    return null; // Retornar null se não conseguir parsear
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