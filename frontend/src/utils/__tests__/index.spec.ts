import {describe, expect, it} from 'vitest';
import {
    badgeClass,
    diffInDays,
    ensureValidDate,
    formatDateBR,
    formatDateForInput,
    formatDateTimeBR,
    generateUniqueId,
    iconeTipo,
    isDateValidAndFuture,
    parseDate
} from '@/utils';

describe('utilitários', () => {
    describe('generateUniqueId', () => {
        it('deve gerar IDs únicos', () => {
            const id1 = generateUniqueId();
            const id2 = generateUniqueId();
            expect(id1).not.toBe(id2);
            expect(typeof id1).toBe('number');
            expect(typeof id2).toBe('number');
        });
    });

    describe('badgeClass', () => {
        it('deve retornar a classe de badge correta para situações conhecidas', () => {
            expect(badgeClass('Finalizado')).toBe('bg-success');
            expect(badgeClass('Em andamento')).toBe('bg-warning text-dark');
        });

        it('deve retornar a classe padrão para situações desconhecidas', () => {
            expect(badgeClass('any')).toBe('bg-secondary');
        });
    });

    describe('iconeTipo', () => {
        it('deve retornar os ícones corretos para tipos de notificação', () => {
            expect(iconeTipo('success')).toBe('bi bi-check-circle-fill text-success');
            expect(iconeTipo('error')).toBe('bi bi-exclamation-triangle-fill text-danger');
            expect(iconeTipo('warning')).toBe('bi bi-exclamation-triangle-fill text-warning');
            expect(iconeTipo('info')).toBe('bi bi-info-circle-fill text-info');
            expect(iconeTipo('email')).toBe('bi bi-envelope-fill text-primary');
        });

        it('deve retornar o ícone padrão para tipos desconhecidos', () => {
            expect(iconeTipo('any' as 'success')).toBe('bi bi-bell-fill');
        });
    });

    describe('parseDate', () => {
        it('deve retornar null para entrada nula ou indefinida', () => {
            expect(parseDate(null)).toBeNull();
            expect(parseDate(undefined)).toBeNull();
            expect(parseDate('')).toBeNull();
        });

        it('deve analisar strings de data ISO', () => {
            const date = parseDate('2024-03-15');
            expect(date).toBeInstanceOf(Date);
            expect(date?.getFullYear()).toBe(2024);
            expect(date?.getMonth()).toBe(2);
            expect([14, 15]).toContain(date?.getDate());
        });

        it('deve analisar o formato de data brasileiro DD/MM/YYYY', () => {
            const date = parseDate('15/03/2024');
            expect(date).toEqual(new Date(2024, 2, 15));
        });

        it('deve retornar null para strings de data inválidas', () => {
            expect(parseDate('invalid')).toBeNull();
            expect(parseDate('99/99/9999')).toBeNull();
            expect(parseDate('00/01/2024')).toBeNull();
        });
    });

    describe('formatDateBR', () => {
        it('deve retornar "Não informado" para nulo ou indefinido', () => {
            expect(formatDateBR(null)).toBe('Não informado');
            expect(formatDateBR(undefined)).toBe('Não informado');
        });

        it('deve formatar o objeto Date para o formato brasileiro', () => {
            const date = new Date(2024, 2, 15);
            expect(formatDateBR(date)).toBe('15/03/2024');
        });

        it('deve retornar "Data inválida" para datas inválidas', () => {
            expect(formatDateBR('invalid')).toBe('Data inválida');
            expect(formatDateBR(new Date('invalid'))).toBe('Data inválida');
        });
    });

    describe('formatDateForInput', () => {
        it('deve retornar string vazia para nulo ou indefinido', () => {
            expect(formatDateForInput(null)).toBe('');
            expect(formatDateForInput(undefined)).toBe('');
        });

        it('deve formatar a data para o formato YYYY-MM-DD', () => {
            const date = new Date(2024, 2, 15);
            expect(formatDateForInput(date)).toBe('2024-03-15');
        });

        it('deve preencher meses e dias de um único dígito', () => {
            const date = new Date(2024, 0, 5);
            expect(formatDateForInput(date)).toBe('2024-01-05');
        });
    });

    describe('formatDateTimeBR', () => {
        it('deve formatar data e hora no formato brasileiro', () => {
            const date = new Date(2024, 2, 15, 14, 30, 0);
            const result = formatDateTimeBR(date);
            expect(result).toContain('15/03/2024');
            expect(result).toContain('14:30');
        });

        it('deve lidar com nulo e indefinido', () => {
            expect(formatDateTimeBR(null)).toBe('Não informado');
            expect(formatDateTimeBR(undefined)).toBe('Não informado');
        });
    });

    describe('isDateValidAndFuture', () => {
        it('deve retornar false para nulo ou indefinido', () => {
            expect(isDateValidAndFuture(null)).toBe(false);
            expect(isDateValidAndFuture(undefined)).toBe(false);
        });

        it('deve retornar true para hoje', () => {
            const today = new Date();
            expect(isDateValidAndFuture(today)).toBe(true);
        });

        it('deve retornar true para datas futuras', () => {
            const tomorrow = new Date();
            tomorrow.setDate(tomorrow.getDate() + 1);
            expect(isDateValidAndFuture(tomorrow)).toBe(true);
        });

        it('deve retornar false para datas passadas', () => {
            const yesterday = new Date();
            yesterday.setDate(yesterday.getDate() - 1);
            expect(isDateValidAndFuture(yesterday)).toBe(false);
        });
    });

    describe('diffInDays', () => {
        it('deve calcular a diferença positiva entre as datas', () => {
            const date1 = new Date(2024, 0, 1);
            const date2 = new Date(2024, 0, 5);
            expect(diffInDays(date1, date2)).toBe(4);
        });

        it('deve calcular a diferença independentemente da ordem', () => {
            const date1 = new Date(2024, 0, 5);
            const date2 = new Date(2024, 0, 1);
            expect(diffInDays(date1, date2)).toBe(4);
        });

        it('deve retornar 0 para a mesma data', () => {
            const date1 = new Date(2024, 0, 1);
            const date2 = new Date(2024, 0, 1);
            expect(diffInDays(date1, date2)).toBe(0);
        });
    });

    describe('ensureValidDate', () => {
        it('deve retornar null para nulo ou indefinido', () => {
            expect(ensureValidDate(null)).toBeNull();
            expect(ensureValidDate(undefined)).toBeNull();
        });

        it('deve retornar um objeto Date válido para uma data válida', () => {
            const validDate = new Date(2024, 2, 15);
            const result = ensureValidDate(validDate);
            expect(result).toBe(validDate);
        });

        it('deve retornar null para objeto Date inválido', () => {
            const invalidDate = new Date('invalid');
            expect(ensureValidDate(invalidDate)).toBeNull();
        });

        it('deve retornar null para Date com tempo NaN', () => {
            const nanDate = new Date(NaN);
            expect(ensureValidDate(nanDate)).toBeNull();
        });

        it('deve lidar com objetos Date com tempo válido, mas componentes inválidos', () => {
            // Cria uma data que seria inválida (como 30 de fevereiro)
            // JavaScript corrige automaticamente isso para 1º de março, então este teste valida o comportamento corrigido
            const edgeCaseDate = new Date(2024, 1, 30); // 30 de fevereiro se torna 1º de março
            expect(ensureValidDate(edgeCaseDate)).toBeInstanceOf(Date);
            expect(ensureValidDate(edgeCaseDate)?.getMonth()).toBe(2); // Março
            expect(ensureValidDate(edgeCaseDate)?.getDate()).toBe(1); // 1º
        });
    });
     describe('tratamento de erro em formatDateBR', () => {
         it('deve retornar "Data inválida" para strings de data inválidas', () => {
             // Testa com uma string de data inválida que fará com que parseDate falhe
             const result = formatDateBR('invalid-date-string');
             expect(result).toBe('Data inválida');
         });
     });
});
     

     describe('tratamento de erro em formatDateForInput', () => {
         it('deve retornar string vazia quando as operações de data geram um erro', () => {
             // Cria uma data que fará com que getFullYear() gere um erro
             const invalidDate = new Date('invalid');

             // Isso deve acionar o bloco catch e retornar ''
             const result = formatDateForInput(invalidDate);
             expect(result).toBe('');
         });
     });

     describe('tratamento de erro em isDateValidAndFuture', () => {
         it('deve retornar false quando as operações de data geram um erro', () => {
             // Cria uma data que fará com que setHours() gere um erro
             const invalidDate = new Date('invalid');

             // Isso deve acionar o bloco catch e retornar false
             const result = isDateValidAndFuture(invalidDate);
             expect(result).toBe(false);
         });
     });