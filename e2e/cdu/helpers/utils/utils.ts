export function gerarNomeUnico(prefixo: string): string {
  return `${prefixo} ${Date.now()}`;
}
