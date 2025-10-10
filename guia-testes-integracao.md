# Guia para Criação de Testes de Integração

Este guia fornece recomendações e um passo a passo para a criação de testes de integração no SGC.

## 1. Estrutura da Classe de Teste

- **Localização:** Os testes de integração para casos de uso (CDUs) devem ser criados na raiz do pacote de testes: `backend/src/test/java/sgc/`.
- **Nomeclatura:** Siga o padrão `CDUXXIntegrationTest.java`, onde `XX` é o número do caso de uso.
- **Anotações Essenciais:**
  - `@SpringBootTest(classes = Sgc.class)`: Carrega o contexto completo da aplicação Spring.
  - `@AutoConfigureMockMvc`: Configura o `MockMvc` para realizar requisições à API.
  - `@ActiveProfiles("test")`: Ativa o perfil de teste, que utiliza o banco de dados em memória (H2).
  - `@WithMockUser(username = "...", roles = {"..."})`: Simula um usuário autenticado com os papéis necessários para o teste. Pode ser usada na classe ou em métodos específicos.
  - `@Import(TestSecurityConfig.class)`: Importa a configuração de segurança de teste que desabilita CSRF e outras proteções para simplificar os testes.
  - `@Transactional`: Garante que cada teste seja executado em sua própria transação, que é revertida ao final. Isso isola os testes e evita que um afete o outro.

## 2. Configuração de Dados de Teste (`@BeforeEach`)

- Utilize um método anotado com `@BeforeEach` para preparar os dados necessários para cada teste.
- Crie e salve as entidades JPA (ex: `Processo`, `Unidade`, `Subprocesso`) usando os repositórios (`@Autowired`).
- Armazene as entidades principais como campos da classe de teste (ex: `private Processo processo;`) para que possam ser acessadas nos métodos de teste.

## 3. Escrevendo os Testes

- **Cenários de Sucesso:**
  - Crie um teste para o fluxo principal do caso de uso.
  - Use o `MockMvc` para realizar requisições HTTP para os endpoints da API.
  - Verifique o status da resposta (ex: `status().isOk()`, `status().isCreated()`).
  - Utilize `jsonPath` para fazer asserções sobre o corpo da resposta JSON, validando os dados retornados.
- **Cenários de Falha:**
  - Crie testes para os fluxos alternativos e de erro.
  - Teste casos como a não localização de recursos (`status().isNotFound()`).
  - Teste a validação de entrada e as regras de negócio (`status().isBadRequest()`, `status().isUnprocessableEntity()`).
- **Diferentes Perfis de Usuário:**
  - Use `@WithMockUser` em nível de método para testar o comportamento da API para diferentes perfis (ex: `ADMIN` vs. `GESTOR`).

## 4. Endpoints e DTOs

- **Detalhes vs. Resumo:** Esteja ciente de que a API pode ter endpoints diferentes para obter uma visão resumida ou detalhada de um recurso. Por exemplo:
  - `GET /api/processos/{id}`: Retorna um `ProcessoDto` com informações básicas.
  - `GET /api/processos/{id}/detalhes`: Retorna um `ProcessoDetalheDto` com informações completas, incluindo listas de entidades relacionadas (como as unidades participantes).
- **Consulte o DTO:** Antes de escrever as asserções com `jsonPath`, verifique a definição do DTO retornado pelo endpoint para garantir que você está validando os campos corretos.

## Exemplo de Esqueleto de Teste

```java
package sgc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Importar Repos e Modelos necessários

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = Sgc.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "admin", roles = {"ADMIN"})
@Import(TestSecurityConfig.class)
@Transactional
public class CDUXXIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Autowire os Repos necessários

    // Declare as entidades principais
    private Processo processo;
    private Unidade unidade;

    @BeforeEach
    void setUp() {
        // Crie e salve as entidades aqui
    }

    @Test
    void testCenarioDeSucesso() throws Exception {
        mockMvc.perform(get("/api/endpoint/{id}", processo.getCodigo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campo").value("valor_esperado"));
    }

    @Test
    void testCenarioDeFalha() throws Exception {
        mockMvc.perform(get("/api/endpoint/{id}", 999L)) // ID inexistente
                .andExpect(status().isNotFound());
    }
}
```