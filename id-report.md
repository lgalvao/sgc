# Relatório de Investigação: Uso de "id" vs "codigo"

Este relatório detalha os locais no projeto onde o identificador `id` (ou variações compostas como `usuarioId`) é utilizado, em contraste com o padrão arquitetural definido `codigo`.

A investigação cobriu Backend (Java/Spring) e Frontend (Vue/TypeScript).

## 1. Backend (Java)

### 1.1. DTOs (Data Transfer Objects)
Vários DTOs estão expondo campos com o sufixo `Id` ou simplesmente `id`, violando o contrato de usar `codigo`.

*   **`sgc/unidade/dto/AtribuicaoTemporariaDto.java`**: Campo `private Long id;`.
*   **`sgc/subprocesso/dto/ConhecimentoAjusteDto.java`**: Campo `private final Long conhecimentoId;`.
*   **`sgc/subprocesso/dto/SubprocessoCadastroDto.java`**: Campo `private final Long subprocessoId;`.
*   **`sgc/subprocesso/dto/ErroValidacaoDto.java`**: Campo `private Long atividadeId;`.
*   **`sgc/subprocesso/dto/MapaAjusteDto.java`**: Builder usa `.conhecimentoId(...)`.
*   **`sgc/processo/eventos/EventoRevisaoSubprocessoDisponibilizada.java`**: Campo `private Long subprocessoId;`.

### 1.2. Parâmetros de Métodos e Variáveis Locais
O uso de parâmetros nomeados como `id` ou `...Id` é frequente em Services e Controllers.

*   **`sgc/alerta/AlertaService.java`**: Método `marcarComoLido(..., Long alertaId)`.
*   **`sgc/subprocesso/service/MovimentacaoListener.java`**: Método `buscarSubprocesso(Long id)`.
*   **`sgc/comum/erros/ErroEntidadeNaoEncontrada.java`**: Construtor aceita `Object id`.
*   **`sgc/subprocesso/service/SubprocessoDtoService.java`**: Uso extensivo de builders com `.subprocessoId(...)` e `.atividadeId(...)`.
*   **`sgc/painel/PainelService.java`**: Métodos como `encontrarMaiorIdVisivel`.

### 1.3. Repositórios e Entidades
*   **`sgc/alerta/model/AlertaUsuario.java`**: Usa `@EmbeddedId private Chave id;`. **Observação:** Em JPA, o nome do campo da chave composta embutida é frequentemente mantido como `id` por convenção, mas pode ser refatorado para `chave` ou similar se desejado, embora exija cuidado com `@AttributeOverrides`.
*   **`sgc/alerta/model/AlertaUsuarioRepo.java`**: Queries JPQL referenciam `au.id.alertaCodigo`.

## 2. Frontend (Vue/TypeScript)

### 2.1. Interfaces e Tipos (`frontend/src/types/tipos.ts`)
As definições de tipo estão inconsistentes com o backend (que deveria enviar `codigo`).

*   Interface `Unidade`: Define `id: number;`.
*   Interface `Conhecimento`: Define `id: number;` e `conhecimentos: { id: number ... }[]`.
*   Interface `TreeItem` (implícita em `TreeTableView`): Define `id: number | string;`.

### 2.2. Componentes Vue (Props e Variáveis)
Muitos componentes esperam props nomeadas `id` ou manipulam variáveis locais com esse nome.

*   **`frontend/src/views/UnidadeView.vue`**: Prop `id: number`.
*   **`frontend/src/views/ProcessoView.vue`**: Variável `id`.
*   **`frontend/src/views/CadAtividades.vue`**: Funções `salvarEdicaoAtividade(id...)`, variáveis `atividadeId`, `conhecimentoId`.
*   **`frontend/src/views/VisAtividades.vue`**: Variável `unidadeId`.
*   **`frontend/src/components/CompetenciaCard.vue`**: Variáveis `atvId`, `atividadeId`.
*   **`frontend/src/components/TreeTableView.vue`** e **`TreeRowItem.vue`**: Fortemente acoplados à propriedade `id` para manipulação da árvore.

### 2.3. Mappers
Camada de adaptação que explicitamente converte `codigo` para `id`, perpetuando o uso incorreto no frontend.

*   **`frontend/src/mappers/unidades.ts`**: Lógica de fallback `codigo: obj.id ?? obj.codigo`.
*   **`frontend/src/mappers/atividades.ts`**: Mapeia `id: dto.codigo`.

### 2.4. Rotas e URLs
*   **`frontend/src/router`**: Parâmetros de rota definidos como `:id` (ex: `/subprocessos/:id`).
*   **Chamadas de API**: Algumas chamadas axios usam variáveis `id` na construção da URL (ex: `apiClient.post(..., {id: ...})`).

## 3. Exceções Válidas (Falsos Positivos)

Estes casos **não devem ser alterados**, pois seguem padrões de infraestrutura ou bibliotecas externas:

*   **HTML Attributes:** Atributos `id="..."` em tags HTML (ex: `<input id="senha">`, `<main id="main-content">`) são semanticamente corretos e necessários para acessibilidade e CSS/JS.
*   **JPA Repository Methods:** Métodos padrão do Spring Data como `findById(ID id)`, `deleteById(ID id)`, `existsById(ID id)`. O *nome do método* não deve ser alterado, mas as variáveis passadas para eles devem ser `codigo`.
*   **Observabilidade:** Campos como `traceId` em `ErroApi` e `RestExceptionHandler` referem-se a identificadores de rastreamento de infraestrutura, não de domínio.
*   **Bibliotecas de Terceiros:** Se uma biblioteca de UI (como um componente de TreeView externo) *exigir* uma propriedade `id`, o frontend deve manter essa propriedade apenas na camada de apresentação (View Model), mas usar `codigo` em todas as camadas de serviço e store.

## 4. Recomendações de Ação

1.  **Padronização do Backend:** Renomear campos em DTOs (`subprocessoId` -> `subprocessoCodigo`) e parâmetros de métodos. Atualizar Mappers (MapStruct) correspondentes.
2.  **Padronização do Frontend:** Refatorar interfaces em `tipos.ts` para usar `codigo`. Atualizar stores e services.
3.  **Refatoração de Componentes:** Ajustar componentes para consumir `codigo`. Para componentes genéricos (como `TreeTableView`) que exigem `id`, considerar criar uma interface genérica que estende a original ou manter a conversão apenas dentro do componente, sem vazar para o domínio.
