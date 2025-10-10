# Plano de Refatoração de DTOs

Este documento detalha o plano para refatorar os Data Transfer Objects (DTOs) no módulo `backend` do projeto, visando padronizar o uso de `records` e do padrão Builder (com Lombok) para melhorar a legibilidade, imutabilidade e flexibilidade.

## 1. Objetivo

*   Padronizar a criação de DTOs no backend.
*   Melhorar a legibilidade e a clareza na instanciação de DTOs.
*   Promover a imutabilidade dos DTOs.
*   Reduzir a complexidade dos construtores.

## 2. Diretrizes de Escolha (`record` vs. `class` com `@Builder`)

Para cada DTO, a escolha entre `record` e `class` com `@Builder` será baseada na seguinte diretriz:

*   **Usar `record`:**
    *   Para DTOs mais simples, que são primariamente portadores de dados.
    *   Quando todos os campos são geralmente fornecidos no momento da criação.
    *   Quando não há necessidade de um builder fluente complexo (ex: muitos campos opcionais, validações elaboradas na construção).
    *   Exemplo: DTOs de resposta simples com poucos campos obrigatórios.

*   **Usar `class` com `@Builder` (Lombok):**
    *   Para DTOs mais complexos, com muitos campos.
    *   Quando há muitos campos opcionais, e a construção se beneficia de um builder fluente para definir apenas os campos necessários.
    *   Quando a lógica de construção é mais elaborada (ex: validações específicas, valores padrão complexos).
    *   Exemplo: DTOs de requisição com muitos parâmetros que podem ser opcionais.

## 3. Ferramentas Utilizadas

*   **Lombok:** Para geração automática de boilerplate (getters, setters, construtores, `@Builder`).
*   **Java `records`:** Para DTOs simples e imutáveis.

## 4. Etapas da Refatoração

### Etapa 4.1: Identificação dos DTOs

1.  **Localizar DTOs:** Realizar uma busca por arquivos `.java` dentro do pacote `sgc` do módulo `backend` que representem DTOs. Isso pode incluir classes em pacotes como `*.dto`ou classes com nomes que terminam em `Dto`.
    *   Comando de busca inicial: `glob 'backend/src/main/java/sgc/**/*.java'`

### Etapa 4.2: Análise e Decisão por DTO

1.  Para cada DTO identificado:
    *   **Ler o conteúdo do arquivo:** Entender a estrutura do DTO, seus campos e como ele é atualmente construído.
    *   **Analisar pontos de uso:** Verificar onde o DTO é instanciado para entender a complexidade dos construtores atuais e a frequência de campos opcionais.
    *   **Decidir:** Com base nas diretrizes do item 2, decidir se o DTO será convertido para um `record` ou para uma `class` com `@Builder`.

### Etapa 4.3: Implementação das Mudanças nos DTOs

1.  **Para DTOs que se tornarão `records`:**
    *   Converter a `class` para `record`.
    *   Remover getters, setters, `equals()`, `hashCode()`, `toString()` e construtores manuais, pois o `record` os gera automaticamente.
    *   Remover anotações do Lombok que se tornam redundantes (ex: `@Data`, `@Getter`, `@Setter`).

2.  **Para DTOs que se tornarão `class` com `@Builder`:**
    *   Adicionar as anotações `@Data` e `@Builder` à classe.
    *   Remover construtores manuais complexos.
    *   Considerar adicionar `@NoArgsConstructor` e `@AllArgsConstructor` se houver necessidade de construtores padrão para frameworks (ex: JPA, Jackson).

### Etapa 4.4: Refatoração dos Pontos de Uso

1.  Para cada DTO modificado:
    *   **Localizar todas as instâncias:** Encontrar todos os locais no código onde o DTO é instanciado (ex: `new MeuDto(...)`).
    *   **Atualizar a instanciação:**
        *   Se for um `record`: `new MeuRecord(campo1, campo2, ...);`
        *   Se for uma `class` com `@Builder`: `MeuClass.builder().campo1(valor1).campo2(valor2).build();`

### Etapa 4.5: Verificação e Testes

1.  **Compilação:** Garantir que todo o projeto compile sem erros após as mudanças.
2.  **Testes de Unidade do Backend:** Executar todos os testes de unidade do backend para verificar a funcionalidade.
    *   Comando: `gradle :backend:agentTest`
3.  **Testes de Integração/E2E:** Se aplicável, executar testes de integração ou end-to-end para garantir que as APIs continuam funcionando corretamente.
4.  **Análise Estática:** Rodar ferramentas de análise estática (linters) para garantir a conformidade com os padrões de código.

## 5. Cronograma (Estimativa)

*   **Etapa 4.1 (Identificação):** [Estimativa em horas/dias]
*   **Etapa 4.2 (Análise e Decisão):** [Estimativa em horas/dias por DTO]
*   **Etapa 4.3 (Implementação nos DTOs):** [Estimativa em horas/dias por DTO]
*   **Etapa 4.4 (Refatoração de Uso):** [Estimativa em horas/dias por DTO]
*   **Etapa 4.5 (Verificação e Testes):** [Estimativa em horas/dias]

---

Este plano será um guia. A execução será iterativa, focando em um DTO ou um grupo de DTOs por vez.

## 6. Status da Refatoração

Esta seção rastreia o progresso da refatoração dos DTOs.

### DTOs Concluídos

A maioria dos DTOs já foi convertida para `record` ou `class` com `@Builder`. Os seguintes DTOs foram revisados ou refatorados recentemente:

*   `sgc.subprocesso.dto.SubprocessoCadastroDto` (convertido para `record`)
*   `sgc.subprocesso.dto.SubprocessoDetalheDto` (convertido para `record`)
*   `sgc.subprocesso.dto.SubprocessoDto` (convertido para `class` com `@Builder`)

### DTOs Pendentes (Para Análise)

A lista a seguir contém DTOs que foram identificados como classes e ainda precisam ser analisados e potencialmente refatorados.

*(Nenhum DTO pendente no momento)*
