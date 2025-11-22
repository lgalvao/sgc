/**
 * Utilitários centralizados do projeto SGC (Português BR)
 */

import {CLASSES_BADGE_SITUACAO, LABELS_SITUACAO} from "@/constants/situacoes";
import type {TipoNotificacao} from "@/stores/notificacoes";

// ===== CLASSES DE BADGE =====
export function badgeClass(situacao: string): string {
    return (
        CLASSES_BADGE_SITUACAO[situacao as keyof typeof CLASSES_BADGE_SITUACAO] ||
        "bg-secondary"
    );
}

// ===== LABELS DE SITUAÇÃO =====
export function situacaoLabel(situacao?: string | null): string {
    if (!situacao) return "Não disponibilizado";
  return LABELS_SITUACAO[situacao as keyof typeof LABELS_SITUACAO] || situacao;
}

// ===== ÍCONES DE NOTIFICAÇÃO =====
export const iconeTipo = (tipo: TipoNotificacao): string => {
  switch (tipo) {
      case "success":
          return "bi bi-check-circle-fill text-success";
      case "error":
          return "bi bi-exclamation-triangle-fill text-danger";
      case "warning":
          return "bi bi-exclamation-triangle-fill text-warning";
      case "info":
          return "bi bi-info-circle-fill text-info";
      case "email":
          return "bi bi-envelope-fill text-primary";
      default:
          return "bi bi-bell-fill";
  }
};

// ===== UTILITÁRIOS DE DATA =====
export function parseDate(
    dateInput: string | number | Date | null | undefined,
): Date | null {
    if (dateInput === null || dateInput === undefined || dateInput === "")
        return null;

  // Se já for Date
  if (dateInput instanceof Date) {
    return isNaN(dateInput.getTime()) ? null : dateInput;
  }

  // Se for número (timestamp)
    if (typeof dateInput === "number") {
    const d = new Date(dateInput);
    return isNaN(d.getTime()) ? null : d;
  }

  // Se for string
    if (typeof dateInput === "string") {
    const s = dateInput.trim();
    if (!s) return null;

    // Detecta ISO com tempo (ex.: 2023-10-01T00:00:00Z) ou somente data (YYYY-MM-DD)
    // Para strings somente-data (YYYY-MM-DD) devemos construir a Date em horário local
    // para evitar deslocamento de fuso horário que causa `getMonth()` diferente do esperado.
    const isoDateWithOptionalTimeRe = /^\d{4}-\d{2}-\d{2}(T.*)?$/;
    if (isoDateWithOptionalTimeRe.test(s)) {
      // Se for somente data (sem 'T'), construir em horário local
        if (!s.includes("T")) {
            const parts = s.split("-");
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
          const d = new Date(year, month - 1, day);
        // Verifica componentes para evitar 31/02 etc.
          if (
              !isNaN(d.getTime()) &&
              d.getUTCFullYear() === year &&
              d.getUTCMonth() === month - 1 &&
              d.getUTCDate() === day
          ) {
          return d;
        }
      }
    }

    // Falha ao parsear
    return null;
  }

  return null;
}

export function formatDateBR(
  date: Date | string | number | null | undefined,
  options: Intl.DateTimeFormatOptions = {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
  },
): string {
    if (!date) return "Não informado";
    const dateObj =
        typeof date === "string" || typeof date === "number"
            ? parseDate(date as any)
            : date;
    if (!dateObj || isNaN(dateObj.getTime())) return "Data inválida";
  try {
      return dateObj.toLocaleDateString("pt-BR", options);
  } catch {
      return "Data inválida";
  }
}

export function formatDateForInput(date: Date | null | undefined): string {
    if (!date || isNaN(date.getTime())) return "";
  try {
    const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
  } catch {
      return "";
  }
}

export function formatDateTimeBR(
    date: Date | string | number | null | undefined,
): string {
    if (!date) return "Não informado";
    const dateObj =
        typeof date === "string" || typeof date === "number"
            ? parseDate(date as any)
            : date;
    if (!dateObj || isNaN(dateObj.getTime())) return "Data inválida";
  return formatDateBR(dateObj, {
      year: "numeric",
      month: "2-digit",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit",
  });
}

export function isDateValidAndFuture(date: Date | null | undefined): boolean {
  if (!date) return false;
  try {
    const today = new Date();
      today.setHours(0, 0, 0, 0);
      const d =
          typeof date === "string" || typeof date === "number"
              ? parseDate(date as any)
              : date;
    if (!d) return false;
      d.setHours(0, 0, 0, 0);
    return d >= today;
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
