import {CLASSES_BADGE_SITUACAO} from '@/constants/situacoes';

export function badgeClass(situacao: string): string {
    return CLASSES_BADGE_SITUACAO[situacao as keyof typeof CLASSES_BADGE_SITUACAO] || 'bg-secondary';
}
