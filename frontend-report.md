# Relatório de Análise do Frontend - Projeto SGC

Este relatório detalha a análise profunda realizada no código do frontend, focando em arquitetura, qualidade de código, consistências e integração com o backend.

## 1. Visão Geral
O projeto utiliza um stack moderno e robusto:
- **Framework**: Vue 3 (Composition API)
- **Linguagem**: TypeScript
- **State Management**: Pinia
- **UI**: BootstrapVueNext
- **HTTP**: Axios

A estrutura de pastas segue boas práticas (`services`, `stores`, `composables`, `views`, `components`), e o uso de **Mappers** para desacoplar os DTOs do Backend dos Modelos do Frontend é um ponto muito positivo.

## 2. Problemas Identificados

### 2.1. Lógica de Negócio no Frontend (Heavy Logic)
O frontend está assumindo responsabilidades de decisão que idealmente pertenceriam ao backend ou a uma camada de orquestração mais simples.

*   **ProcessosStore - `executarAcaoBloco`**:
    *   **Problema**: Existe uma árvore de decisão complexa (lines 173-234 em `stores/processos.ts`) que determina qual endpoint chamar com base no estado do subprocesso (`MAPEAMENTO_CADASTRO_DISPONIBILIZADO`, `REVISAO_MAPA_VALIDADO`, etc.) e na ação desejada (`aceitar`, `homologar`).
    *   **Impacto**: Se as regras de negócio mudarem no backend (ex: novos status), o frontend quebrará.
    *   **Recomendação**: Criar um endpoint unificado no backend (ex: `POST /processos/acao-em-bloco`) que receba a lista de IDs e a intenção, deixando o backend decidir qual transição de estado executar para cada item.

*   **Composables - `useVisMapaLogic`**:
    *   **Problema**: A função `confirmarAceitacao` contém lógica condicional baseada em strings mágicas (`perfil === "ADMIN"`) para decidir se chama `homologarValidacao` ou `aceitarValidacao`.
    *   **Impacto**: Insegurança e fragilidade. O perfil do usuário não deve ser a única checagem para determinar qual serviço chamar no frontend.
    *   **Recomendação**: O backend deve expor as ações disponíveis via HATEOAS ou permissões explícitas, ou um único endpoint que execute a "proxima etapa" baseada no usuário logado.

### 2.2. Repetições e Organização de Código
*   **`src/utils/index.ts` (God Object)** (**Resolvido**):
    *   O arquivo foi quebrado em módulos específicos: `dateUtils.ts`, `statusUtils.ts`, `styleUtils.ts`.

*   **Duplicidade de Formatação**:
    *   Existe lógica de parsing de datas customizada em `utils/dateUtils.ts` (`parseStringDate`) que tenta adivinhar formatos. Isso pode ser arriscado. O ideal é padronizar a comunicação da API (ISO 8601) e usar `date-fns` estritamente.

### 2.3. Inconsistências
*   **Convenção de Nomes** (**Resolvido**):
    *   O service `unidadesService.ts` foi renomeado para `unidadeService.ts` para manter a consistência de nomenclaturas no singular.

*   **Padrões HTTP**:
    *   O sistema utiliza exclusivamente `POST` e `GET` por regras de negócios corporativas. Embora não estritamente RESTful nos verbos, mantém consistência interna.

*   **Construção de Payload** (**Resolvido**):
    *   A construção manual de DTOs em `atividadeService.ts` foi substituída por novos mappers em `mappers/atividades.ts`, padronizando a camada de transformação de dados.

### 2.4. Reinvenção da Roda
*   **`utils/treeUtils.ts`**: Implementa `flattenTree` manualmente. Embora simples e funcional, é uma lógica comum que já existe em bibliotecas utilitárias, mas dado o tamanho do projeto, manter localmente é aceitável, desde que testado.

## 3. Conclusão
O frontend do SGC está em um bom estado geral, e as correções recentes melhoraram significativamente a consistência e manutenibilidade.

**Melhorias Implementadas:**
1.  **Refatoração do `utils/index.ts`**: Quebrado em arquivos menores e mais coesos.
2.  **Padronização de Services**: Renomeação de `unidadesService` e uso de mappers em `atividadeService`.
3.  **Mappers de Atualização**: Criação de `mapAtualizarAtividadeToDto` e `mapAtualizarConhecimentoToDto`.

**Próximos Passos (Recomendados):**
1.  **Backend**: Implementar endpoint unificado para ações em bloco para remover a lógica complexa de `ProcessosStore`.
