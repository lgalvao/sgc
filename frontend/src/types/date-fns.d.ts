declare module "date-fns" {
    export interface FormatOptions {
        locale?: unknown;
        weekStartsOn?: 0 | 1 | 2 | 3 | 4 | 5 | 6;
        firstWeekContainsDate?: number;
        useAdditionalWeekYearTokens?: boolean;
        useAdditionalDayOfYearTokens?: boolean;
    }

    export function addDays(date: Date | number, amount: number): Date;

    export function format(date: Date | number, formatString: string, options?: FormatOptions): string;

    export function isValid(date: unknown): boolean;

    export function parse(dateString: string, formatString: string, referenceDate: Date | number, options?: FormatOptions): Date;

    export function parseISO(argument: string, options?: { additionalDigits?: 0 | 1 | 2 }): Date;

    export function startOfDay(date: Date | number): Date;
}

declare module "date-fns/locale" {
    export interface Locale {
        code?: string;
        formatDistance?: unknown;
        formatRelative?: unknown;
        localize?: unknown;
        formatLong?: unknown;
        match?: unknown;
        options?: {
            weekStartsOn?: 0 | 1 | 2 | 3 | 4 | 5 | 6;
            firstWeekContainsDate?: 1 | 2 | 3 | 4 | 5 | 6 | 7;
        };
    }

    export const ptBR: Locale;
}
