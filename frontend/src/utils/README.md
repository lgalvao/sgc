# Utils (Utilitários Gerais)

Última atualização: 2025-12-13

Este diretório contém funções utilitárias de propósito geral, que não dependem de estado da aplicação (Pinia) ou do
framework (Vue). São funções puras de JavaScript/TypeScript.

## Arquivos

### `apiError.ts`

- Contém a infraestrutura de tratamento de erros padronizado.
- **`normalizeError(error: unknown): NormalizedError`**: Converte qualquer erro (Axios, JS, etc.) para um formato `NormalizedError` previsível, contendo `message`, `kind`, `code` e `details`.
- **`notifyError(normalized: NormalizedError): void`**: Exibe um toast global (via `FeedbackStore`) para erros que exigem atenção global (ex: erro de rede, 500).
- **`shouldNotifyGlobally(normalized: NormalizedError): boolean`**: Helper que decide se um erro deve ser notificado globalmente ou tratado inline.
- **Helpers de API**:
    - `existsOrFalse(apiCall)`: Retorna `true` se a chamada der sucesso, `false` se der 404.
    - `getOrNull(apiCall)`: Retorna o dado se sucesso, `null` se der 404.

### `index.ts`

- Coleção de funções utilitárias diversas, como formatação de datas, manipulação de strings, cálculos simples, etc.

### `logger.ts`

- Uma abstração simples para logar mensagens no console. Permite controlar o nível de log (debug, info, error) e pode
  ser estendido para enviar logs para um serviço externo no futuro.
