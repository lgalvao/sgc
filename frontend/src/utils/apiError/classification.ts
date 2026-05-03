import type {ErroSimples} from "./types";

export function ehErroAxios(erro: unknown): erro is import('axios').AxiosError {
    return (erro !== null &&
        typeof erro === 'object' &&
        'isAxiosError' in erro && (erro as ErroSimples).isAxiosError === true);
}
