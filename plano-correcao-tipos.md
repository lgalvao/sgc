# Plano de Correção de Tipos

Este plano detalha os passos para corrigir os erros de tipagem no frontend da aplicação.

## Nova Estratégia (Backend-First)

A abordagem inicial de corrigir arquivos um por um se mostrou ineficiente devido à natureza interconectada dos erros de tipo. Adotamos uma nova estratégia mais estruturada, começando pelo backend como a fonte da verdade para as estruturas de dados.

1.  **Analisar Modelos JPA:** Examinar as classes de entidade JPA em `backend/src/main/java/sgc` para entender as estruturas de dados canônicas.
2.  **Analisar DTOs:** Inspecionar as classes DTO (Data Transfer Objects) no backend para ver como as entidades são expostas para o frontend.
3.  **Auditar e Solidificar Tipos do Frontend:** Com base na análise do backend, revisar e corrigir o arquivo `frontend/src/types/tipos.ts` para que ele reflita com precisão os DTOs.
4.  **Corrigir Camada de Serviço:** Corrigir todos os erros de tipo nos arquivos em `frontend/src/services/`.
5.  **Corrigir Camada de Store:** Corrigir os stores Pinia em `frontend/src/stores/`.
6.  **Corrigir Arquivos de Teste:** Corrigir dados de mock e lógica em todos os arquivos de teste (`__tests__`).
7.  **Corrigir Camada de Componentes:** Corrigir os erros restantes nos componentes Vue (`.vue`).
8.  **Verificação Final:** Executar `npm run typecheck` uma última vez para confirmar que todos os erros foram resolvidos.

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
