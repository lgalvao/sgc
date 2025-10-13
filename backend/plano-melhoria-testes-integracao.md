# Plano de Melhoria para Testes de Integração

Este documento detalha um plano passo a passo para aprimorar a consistência e a manutenibilidade dos testes de integração existentes na pasta `sgc.integracao`.

## 1. Padronização da Configuração de Segurança

**Problema:** Atualmente, algumas classes de teste utilizam uma configuração de segurança aninhada (`@TestConfiguration` com `TestSecurityConfig`), enquanto outras importam a configuração centralizada (`sgc.integracao.mocks.TestSecurityConfig`).

**Objetivo:** Centralizar e padronizar a configuração de segurança para todos os testes de integração.

**Passos:**
1.  **Remover `TestSecurityConfig` aninhadas:** Em todas as classes de teste que possuem uma `TestSecurityConfig` interna, remover essa classe.
2.  **Importar `sgc.integracao.mocks.TestSecurityConfig`:** Adicionar a anotação `@Import(TestSecurityConfig.class)` no cabeçalho de todas as classes de teste de integração que ainda não a utilizam.
    *   **Exemplo de alteração:**
        ```java
        // Antes
        @SpringBootTest
        @AutoConfigureMockMvc
        @ActiveProfiles("test")
        @DisplayName("CDU-03: Manter processo")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        public class CDU03IntegrationTest {
            @TestConfiguration
            @SuppressWarnings("PMD.TestClassWithoutTestCases")
            static class TestSecurityConfig {
                @Bean
                SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
                    http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                            .csrf(AbstractHttpConfigurer::disable);
                    return http.build();
                }
            }
            // ...
        }

        // Depois
        import sgc.integracao.mocks.TestSecurityConfig; // Adicionar import
        // ...
        @SpringBootTest
        @AutoConfigureMockMvc
        @ActiveProfiles("test")
        @DisplayName("CDU-03: Manter processo")
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        @Import(TestSecurityConfig.class) // Adicionar anotação
        public class CDU03IntegrationTest {
            // ... (remover a classe TestSecurityConfig aninhada)
        }
        ```

## 2. Padronização do Uso de Anotações de Mock de Usuário

**Problema:** Alguns testes utilizam a anotação `@WithMockUser` do Spring Security diretamente, enquanto outros utilizam as anotações personalizadas (`@WithMockAdmin`, `@WithMockChefe`, `@WithMockGestor`).

**Objetivo:** Utilizar as anotações personalizadas sempre que possível para simular usuários com perfis específicos, melhorando a legibilidade e a consistência.

**Passos:**
1.  **Substituir `@WithMockUser` por `@WithMockAdmin`:** Em classes como `CDU03IntegrationTest`, onde o usuário mockado é um administrador, substituir `@WithMockUser(username = "admin", roles = {"ADMIN"})` por `@WithMockAdmin`.
    *   **Exemplo de alteração:**
        ```java
        // Antes
        @WithMockUser(username = "admin", roles = {"ADMIN"})
        public class CDU03IntegrationTest { ... }

        // Depois
        import sgc.integracao.mocks.WithMockAdmin; // Adicionar import
        // ...
        @WithMockAdmin
        public class CDU03IntegrationTest { ... }
        ```
2.  **Garantir `WithMockChefeSecurityContextFactory` e `WithMockGestorSecurityContextFactory` são `@Component`:** Verificar se as factories para `@WithMockChefe` e `@WithMockGestor` estão anotadas com `@Component` para que o Spring as encontre automaticamente. (Já estão, mas é bom verificar).

## 3. Revisão da Limpeza de Dados no `setUp()`

**Problema:** Em `CDU17IntegrationTest`, há chamadas explícitas a `deleteAll()` em vários repositórios dentro do método `setUp()`. Embora possa ser intencional para cenários específicos, o uso de `@Transactional` já deveria garantir um estado limpo para cada teste.

**Objetivo:** Avaliar a necessidade de `deleteAll()` explícitos e, se possível, removê-los para confiar mais no comportamento transacional do Spring.

**Passos:**
1.  **Analisar `CDU17IntegrationTest`:**
    *   Verificar se a lógica de `deleteAll()` é realmente necessária para o cenário de teste (e.g., testar a limpeza de análises antigas).
    *   Se não for estritamente necessária para o cenário, remover as chamadas a `deleteAll()`.
    *   Executar os testes para garantir que a remoção não introduza falhas devido a dados residuais.
2.  **Documentar a Justificativa:** Se a manutenção dos `deleteAll()` for considerada essencial, adicionar um comentário explicando o *porquê* dessa limpeza explícita, mesmo com `@Transactional`.

## 4. Aprimoramento da Verificação de Mensagens de Erro

**Problema:** Em alguns testes de falha, apenas o status HTTP é verificado, sem validar a mensagem de erro específica retornada pela API.

**Objetivo:** Adicionar asserções para as mensagens de erro em todos os testes de falha, garantindo que a validação correta foi acionada e que a mensagem é amigável/esperada.

**Passos:**
1.  **Identificar testes de falha:** Percorrer todos os testes que esperam um status de erro (e.g., `isBadRequest()`, `isUnprocessableEntity()`, `isConflict()`).
2.  **Adicionar `jsonPath("$.message")` ou `jsonPath("$.subErrors[0].message")`:** Incluir asserções para verificar o conteúdo da mensagem de erro.
    *   **Exemplo de alteração (já presente em alguns testes, mas generalizar):**
        ```java
        // Exemplo de teste de falha
        @Test
        void testCriarProcesso_descricaoVazia_falha() throws Exception {
            // ...
            mockMvc.perform(post(API_PROCESSOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.subErrors[0].message").value("Preencha a descrição")); // Adicionar esta linha
        }
        ```

## 5. Revisão da Nomenclatura de Métodos de Teste

**Problema:** Embora a nomenclatura seja geralmente boa, pode haver oportunidades para torná-la ainda mais descritiva e consistente.

**Objetivo:** Garantir que os nomes dos métodos de teste descrevam claramente o cenário que está sendo testado (condição inicial, ação e resultado esperado).

**Passos:**
1.  **Revisar nomes de métodos:** Percorrer todos os métodos de teste e verificar se o nome segue o padrão `deve[Ação]Quando[Condição]` ou `naoDeve[Ação]Quando[Condição]`.
2.  **Ajustar quando necessário:** Renomear métodos que possam ser mais claros.

Ao seguir este plano, os testes de integração se tornarão ainda mais robustos, fáceis de entender e manter, contribuindo significativamente para a qualidade do projeto.
