# Pacote `comum`

## Objetivo

O pacote `sgc.comum` concentra infraestrutura compartilhada entre os domínios do backend.

Ele não representa um domínio de negócio específico; fornece base de configuração, erros, modelos e utilidades.

## Subpacotes principais

### `config`

Configurações transversais da aplicação, como:

- configuração geral da aplicação,
- cache,
- OpenAPI,
- integração de templates.

### `erros`

Padronização de exceções e resposta de erro da API:

- `RestExceptionHandler`
- `ErroApi`
- `ErroSubApi`
- exceções de negócio (`ErroNegocio`, `ErroValidacao`, `ErroAcessoNegado`, etc.).

### `model`

Tipos base compartilhados, incluindo:

- `EntidadeBase` (com campo `codigo`),
- contratos reutilizáveis de model/repositório.

### `util`

Utilitários transversais, incluindo:

- monitoramento (`MonitoramentoAspect`, `FiltroMonitoramentoHttp`, `MonitoramentoProperties`),
- apoio de formatação e helpers técnicos.

## Artefatos adicionais

- `ComumDtos`
- `Mensagens`

## Quando usar

Use `sgc.comum` para evitar duplicação entre módulos e manter padrão único de infraestrutura.

Evite colocar regras de negócio específicas de domínio neste pacote.
