# Diretório de Constantes

Armazena valores fixos e configurações estáticas que não mudam durante a execução da aplicação.

## Conteúdo

* **`situacoes.ts`**: Mapeamento de códigos de status para labels e cores (ex: `EM_ANDAMENTO` -> `primary`).
* **`textos.ts`**: Mensagens de erro padrão, labels fixas e conteúdos textuais estáticos.
* **`index.ts`**: Exporta constantes globais, como limites de paginação e timeouts.

## Por que usar constantes?

1. **Manutenibilidade**: Altere um valor em um único lugar em vez de fazer busca e substituição em todo o código.
2. **Auto-documentação**: Nomes de constantes (ex: `MAX_FILE_SIZE_MB`) são mais claros que "números mágicos" ou strings
   soltas.
