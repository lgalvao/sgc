export function validarEmail(email: string | null | undefined): boolean {
  if (!email) return false;
  // Regex simples para email (HTML5 standard)
  const regex = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
  return regex.test(email);
}

export function validarSenha(senha: string): boolean {
    // Exemplo de regra: Mínimo 8 chars, 1 letra, 1 numero
    if (!senha) return false;
    if (senha.length < 8) return false;
    if (!/[A-Za-z]/.test(senha)) return false;
    if (!/[0-9]/.test(senha)) return false;
    return true;
}
