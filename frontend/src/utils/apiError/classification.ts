export function ehErroAxios(erro: unknown): erro is import('axios').AxiosError {
    if (erro && typeof erro === 'object' && 'isAxiosError' in erro) {
        const e = erro as Record<string, boolean | string | number | undefined>;
        return e.isAxiosError === true;
    }
    return false;
}
