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
*   **`src/utils/index.ts` (God Object)**:
    *   **Problema**: Este arquivo mistura responsabilidades distintas: formatação de datas, classes CSS para badges, labels de status e utilitários de árvore.
    *   **Impacto**: Dificulta a manutenção e testes.
    *   **Recomendação**: Separar em arquivos específicos: `src/utils/dateUtils.ts`, `src/utils/statusUtils.ts`, `src/utils/styleUtils.ts`.

*   **Duplicidade de Formatação**:
    *   Existe lógica de parsing de datas customizada em `utils/index.ts` (`parseStringDate`) que tenta adivinhar formatos. Isso pode ser arriscado. O ideal é padronizar a comunicação da API (ISO 8601) e usar `date-fns` estritamente.

### 2.3. Inconsistências
*   **Convenção de Nomes**:
    *   **Services**: Maioria no singular (`atividadeService.ts`, `processoService.ts`), mas `unidadesService.ts` está no plural.
    *   **Stores**: Maioria no plural (`processos.ts`, `atividades.ts`), mas `feedback.ts` e `perfil.ts` no singular.
    *   **Recomendação**: Padronizar (sugestão: Services no singular, Stores no plural).

*   **Padrões HTTP (RESTful)**:
    *   Em `atividadeService.ts`, rotas de atualização e exclusão utilizam o verbo `POST` com sufixos na URL (ex: `/atividades/{id}/excluir`).
    *   **Padrão REST**: Deveria usar `DELETE /atividades/{id}` e `PUT` ou `PATCH` para atualizações.
    *   **Recomendação**: Verificar se o backend suporta os verbos corretos e refatorar.

*   **Construção de Payload**:
    *   Em `atividadeService.ts`, o payload de atualização é construído manualmente objeto-a-objeto, ignorando o uso de mappers reversos (Model -> DTO), o que contradiz o padrão usado em outras partes do sistema.

### 2.4. Reinvenção da Roda
*   **`utils/treeUtils.ts`**: Implementa `flattenTree` manualmente. Embora simples e funcional, é uma lógica comum que já existe em bibliotecas utilitárias, mas dado o tamanho do projeto, manter localmente é aceitável, desde que testado.

## 3. Conclusão
O frontend do SGC está em um bom estado geral, mas sofre de "vazamento de regras de negócio". A complexidade de decidir "qual endpoint chamar" baseada em estados complexos está sobrecarregando as Stores e Composables.

**Ações Prioritárias Recomendadas:**
1.  **Refatorar `utils/index.ts`**: Quebrar em arquivos menores.
2.  **Centralizar Ações de Bloco**: Mover a lógica de decisão de `executarAcaoBloco` para o Backend.
3.  **Padronizar Services**: Corrigir nomes de arquivos e uso de Mappers vs Objetos manuais.
