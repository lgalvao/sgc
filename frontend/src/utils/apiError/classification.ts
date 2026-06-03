export function ehErroAxios(erro: unknown): erro is import('axios').AxiosError {
    return !!(erro && typeof erro === 'object' && Reflect.get(erro, 'isAxiosError') === true);
}
