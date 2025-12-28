import { test, expect } from 'vitest';
import fc from 'fast-check';
import { formatarCpf, limparCpf } from './formatters';

test('formatarCpf deve sempre retornar uma string', () => {
  fc.assert(
    fc.property(fc.string(), (texto) => {
      const resultado = formatarCpf(texto);
      return typeof resultado === 'string';
    })
  );
});

test('formatarCpf deve retornar string formatada para inputs numéricos de 11 dígitos', () => {
  // Gerador de string numérica de 11 dígitos
  const cpfArbitrary = fc.array(fc.constantFrom(...'0123456789'), { minLength: 11, maxLength: 11 })
                         .map(arr => arr.join(''));

  fc.assert(
    fc.property(cpfArbitrary, (cpfNumerico) => {
      const resultado = formatarCpf(cpfNumerico);
      // Deve ter o formato 000.000.000-00 (length 14)
      return resultado.length === 14 &&
             resultado[3] === '.' &&
             resultado[7] === '.' &&
             resultado[11] === '-';
    })
  );
});

test('limparCpf deve remover todos os não-dígitos', () => {
  fc.assert(
    fc.property(fc.string(), (texto) => {
      const limpo = limparCpf(texto);
      // Regex verifica se só tem dígitos
      return /^\d*$/.test(limpo);
    })
  );
});

test('Propriedade: formatarCpf(limparCpf(x)) deve preservar os números originais', () => {
    fc.assert(
        fc.property(fc.string(), (texto) => {
            const limpoOriginal = limparCpf(texto);
            const formatado = formatarCpf(texto);
            const limpoDepois = limparCpf(formatado);

            // Se o original tinha 11 digitos, a formatação acontece, mas a "limpeza" deve recuperar os números
            // Se não tinha 11, a formatação retorna o limpo, então limpar de novo dá na mesma.
            return limpoOriginal === limpoDepois;
        })
    );
});
