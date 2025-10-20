# Plano de Correção de Tipos

Este plano detalha os passos para corrigir os erros de tipagem no frontend da aplicação.

## Nova Estratégia (Backend-First)

A abordagem inicial de corrigir arquivos um por um se mostrou ineficiente devido à natureza interconectada dos erros de tipo. Adotamos uma nova estratégia mais estruturada, começando pelo backend como a fonte da verdade para as estruturas de dados.

1.  **Analisar Modelos JPA:** (Concluído) Examinei as classes de entidade JPA em `backend/src/main/java/sgc` para entender as estruturas de dados canônicas.
2.  **Analisar DTOs:** (Concluído) Inspecionei as classes DTO (Data Transfer Objects) no backend para ver como as entidades são expostas para o frontend.
3.  **Auditar e Solidificar Tipos do Frontend:** (Concluído) Com base na análise do backend, revisei e corrigi o arquivo `frontend/src/types/tipos.ts` para que ele reflita com precisão os DTOs.
4.  **Corrigir Camada de Serviço:** (Concluído) Corrigi todos os erros de tipo nos arquivos em `frontend/src/services/`.
5.  **Corrigir Camada de Store:** (Concluído) Corrigi os stores Pinia em `frontend/src/stores/`.
6.  **Corrigir Arquivos de Teste:** (Em andamento) Corrigindo dados de mock e lógica em todos os arquivos de teste (`__tests__`).
7.  **Corrigir Camada de Componentes:** (Concluído) Corrigi os erros restantes nos componentes Vue (`.vue`).
8.  **Verificação Final:** (Pendente) Executar `npm run typecheck` uma última vez para confirmar que todos os erros foram resolvidos.

## Etapas Anteriores (Parcialmente Concluídas)

### Etapa 1: Correção das Definições de Tipos (Concluído)

1.  **Adicionar tipos de requisição ausentes:** (Concluído)
2.  **Adicionar tipos do store de mapas ausentes:** (Concluído)
3.  **Corrigir interfaces existentes:** (Concluído)

### Etapa 2: Atualização dos Stores Pinia (Concluído)

1.  **Adicionar getter `getMapaByUnidadeId` ao store `mapas`:** (Concluído)
2.  **Adicionar actions de validação ao store `subprocessos`:** (Concluído)

### Etapa 3: Correção dos Componentes Vue (Parcialmente concluído)

- Vários componentes foram corrigidos, mas novos erros surgiram devido a inconsistências de tipo mais profundas. O trabalho continuará seguindo a nova estratégia.
