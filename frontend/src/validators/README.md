# Diretório de Validadores (Validators)

Este diretório contém funções de validação reutilizáveis, projetadas para verificar a integridade e o formato dos dados, especialmente em formulários.

## Objetivo

O principal objetivo é centralizar toda a lógica de validação de regras de negócio em um único local. Isso promove a reutilização, a consistência e facilita os testes. Em vez de reescrever a mesma lógica de validação em múltiplos formulários, os desenvolvedores podem importar e compor funções de validação deste diretório.

As funções de validação são frequentemente usadas em conjunto com bibliotecas de gerenciamento de formulários, como [VeeValidate](https://vee-validate.logaretm.com/v4/) ou [Zod](https://zod.dev/), para fornecer feedback em tempo real ao usuário.

## Estrutura de uma Função de Validação

Uma função de validação típica segue um padrão simples:
- Recebe um valor (e.g., `string`, `number`) como entrada.
- Retorna `true` se o valor for válido.
- Retorna uma `string` com uma mensagem de erro se o valor for inválido.

### Exemplo

```typescript
// Em /validators/commonValidators.ts

export function isObrigatorio(valor: any): boolean | string {
  if (valor === undefined || valor === null || valor === '') {
    return 'Este campo é obrigatório.';
  }
  return true;
}

export function isEmail(valor: string): boolean | string {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(valor)) {
    return 'Por favor, insira um e-mail válido.';
  }
  return true;
}
```

## Casos de Uso

- Validar campos de formulário (obrigatoriedade, formato, comprimento).
- Compor múltiplas regras de validação para um único campo.
- Validar objetos de dados complexos antes de enviá-los para a API.

As funções podem ser agrupadas em arquivos com base no domínio de negócio (e.g., `usuarioValidators.ts` para regras específicas de usuário) ou em arquivos genéricos (`commonValidators.ts`) para regras aplicáveis em toda a aplicação.