# Constantes

Última atualização: 2025-12-04 14:18:38Z

Este diretório centraliza valores constantes utilizados em toda a aplicação frontend. O objetivo é evitar "números
mágicos" e strings repetidas, facilitando a manutenção e a consistência.

## Arquivos Principais

### `situacoes.ts`

- Define os enums e constantes para as situações das entidades de negócio, como:
    - `SituacaoProcesso` (ex: EM_ANDAMENTO, FINALIZADO)
    - `SituacaoSubprocesso` (ex: PENDENTE_CADASTRO, MAPA_HOMOLOGADO)
    - `SituacaoMapa`

### `textos.ts`

- Centraliza textos exibidos na interface, mensagens de erro padrão e rótulos. Útil para padronização e futura
  internacionalização.

### `index.ts`

- Ponto de entrada que exporta as constantes para facilitar a importação em outros arquivos.

## Detalhamento técnico (gerado em 2025-12-04T14:22:48Z)

Resumo detalhado dos artefatos, comandos e observações técnicas gerado automaticamente.
