export function ehErroAxios(erro: Error | object | null | undefined): erro is import('axios').AxiosError {
    if (erro && typeof erro === 'object' && 'isAxiosError' in erro) {
        const e = erro as { isAxiosError: boolean | string | number | undefined };
        return e.isAxiosError === true;
    }
    return false;
}
