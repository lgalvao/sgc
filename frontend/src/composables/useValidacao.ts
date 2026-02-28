export function useValidacao() {
    function obterPrimeiroCampoComErro<T extends object>(erros: T): keyof T | null {
        const chave = Object.keys(erros).find((item) => Boolean((erros as any)[item]));
        return (chave as keyof T) ?? null;
    }

    function possuiErros<T extends object>(erros: T): boolean {
        return Object.values(erros as any).some(Boolean);
    }

    return {
        obterPrimeiroCampoComErro,
        possuiErros,
    };
}
