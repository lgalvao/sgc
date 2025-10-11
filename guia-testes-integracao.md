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

- **Organização com Classes Aninhadas:** Para casos de uso complexos, utilize classes aninhadas (`@Nested`) com a anotação `@DisplayName` para agrupar os testes por funcionalidade (ex: Criação, Edição, Remoção, Importação). Isso melhora significativamente a legibilidade e a organização da suíte de testes.

- **Cenários de Sucesso:**
  - Crie um teste para o fluxo principal do caso de uso.
  - Use o `MockMvc` para realizar requisições HTTP para os endpoints da API.
  - Verifique o status da resposta (ex: `status().isOk()`, `status().isCreated()`).
  - Utilize `jsonPath` para fazer asserções sobre o corpo da resposta JSON, validando os dados retornados.
  - Use o `AssertJ` (`assertThat`) para fazer asserções no estado do banco de dados após a operação, garantindo que a persistência ocorreu como esperado.

- **Cenários de Falha:**
  - Crie testes para os fluxos alternativos e de erro.
  - Teste casos como a não localização de recursos (`status().isNotFound()`).
  - Teste a validação de entrada e as regras de negócio (`status().isBadRequest()`, `status().isUnprocessableEntity()`).

- **Diferentes Perfis de Usuário:**
  - Use `@WithMockUser` em nível de método para testar o comportamento da API para diferentes perfis (ex: `ADMIN` vs. `GESTOR`).

- **Cenários de Teste Complexos:** Para funcionalidades que exigem um conjunto de dados específico (como um fluxo de importação), você pode adicionar um método `@BeforeEach` dentro da classe `@Nested` correspondente. Isso permite criar dados de teste especializados apenas para aquele subconjunto de testes, mantendo o `setUp` principal mais limpo.

## 4. Endpoints e DTOs

- **Detalhes vs. Resumo:** Esteja ciente de que a API pode ter endpoints diferentes para obter uma visão resumida ou detalhada de um recurso. Por exemplo:
  - `GET /api/processos/{id}`: Retorna um `ProcessoDto` com informações básicas.
  - `GET /api/processos/{id}/detalhes`: Retorna um `ProcessoDetalheDto` com informações completas, incluindo listas de entidades relacionadas (como as unidades participantes).
- **Consulte o DTO:** Antes de escrever as asserções com `jsonPath`, verifique a definição do DTO retornado pelo endpoint para garantir que você está validando os campos corretos.

## 5. Dicas e Armadilhas Comuns

- **Convenção de Nomenclatura:** Lembre-se que os controladores do projeto seguem a convenção de nomenclatura `NomeDoRecursoControle.java` (ex: `SubprocessoControle.java`).

- **Parâmetros de Segurança:** Alguns endpoints podem exigir parâmetros adicionais para fins de autorização, mesmo quando se utiliza `@WithMockUser`. Por exemplo, um endpoint pode precisar de `?perfil=ADMIN` na URL para validar o acesso. Se receber um erro 403 (Forbidden) inesperado, verifique se a implementação do `Controller` ou do `Service` esperam algum parâmetro de perfil.

- **Testando Cenários de Erro:**
  - **Status HTTP:** Para erros que são tratados pelo `Controller` e retornam um status HTTP específico (como 404 Not Found), você pode usar `mockMvc` e esperar o status correspondente.
  - **Exceções de Serviço:** Para erros que ocorrem na camada de serviço e não são tratados de forma específica no `Controller` (resultando em um erro 500 Internal Server Error), uma abordagem mais robusta é testar diretamente a camada de serviço. Injete o `Service` no seu teste e use `assertThrows` para verificar se a exceção esperada (ex: `ErroDominioNaoEncontrado`) é lançada. Isso torna o teste mais preciso e menos acoplado à implementação do `Controller`.
- **Funcionalidade Existente:** Antes de adicionar novas funcionalidades, pesquise exaustivamente no código-fonte para garantir que algo semelhante ainda não exista. Reutilizar ou estender a lógica existente é preferível a criar duplicatas.
- **Verifique a Especificação:** Não assuma que uma funcionalidade existente com nome parecido faz exatamente o que você precisa. Sempre verifique a especificação do caso de uso (CDU) para garantir que todos os detalhes da implementação (alertas, notificações, mudanças de estado) estão corretos.

## 6. Tópicos Avançados

### Testando com Usuários Específicos

Às vezes, `@WithMockUser` não é suficiente, especialmente quando a lógica de negócios depende do objeto `Usuario` real (por exemplo, ao verificar se um usuário é o titular de uma unidade). Nesses casos, o `@AuthenticationPrincipal` no seu controlador retornará `null`.

Para resolver isso, você pode criar uma anotação de teste personalizada que configura um `SecurityContext` com um objeto `Usuario` real.

1.  **Crie a anotação:**

    ```java
    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = WithMockChefeSecurityContextFactory.class)
    public @interface WithMockChefe {
        String value() default "chefe";
    }
    ```

2.  **Crie a fábrica de contexto:** Esta fábrica encontrará o usuário no banco de dados (ou o criará se não existir) e o definirá como o principal de segurança.

    ```java
    public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {

        @Autowired
        private UsuarioRepo usuarioRepo;

        @Override
        public SecurityContext createSecurityContext(WithMockChefe annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            Usuario usuario = usuarioRepo.findByTitulo(annotation.value())
                .orElseGet(() -> {
                    Usuario newUser = new Usuario();
                    newUser.setTitulo(annotation.value());
                    return usuarioRepo.save(newUser);
                });

            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            context.setAuthentication(token);
            return context;
        }
    }
    ```
    *Observação: A entidade `Usuario` deve implementar `UserDetails`.*

3.  **Use no seu teste:** Substitua `@WithMockUser` pela sua nova anotação. Não se esqueça de importar a fábrica no seu teste com `@Import`.

    ```java
    @SpringBootTest
    @WithMockChefe // ou @WithMockChefe("outro_usuario")
    @Import({TestSecurityConfig.class, WithMockChefeSecurityContextFactory.class})
    public class SeuIntegrationTest { ... }
    ```

### Executando um Único Arquivo de Teste

Para acelerar o ciclo de desenvolvimento, você pode executar apenas o arquivo de teste em que está trabalhando em vez de toda a suíte de testes. Use o seguinte comando, substituindo `NomeDoSeuTeste`:

```bash
cd /app && ./gradlew :backend:test --tests sgc.NomeDoSeuTeste
```

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