# Relatório de Análise de Endpoints: Redundância de Dados de Contexto (JWT)

A análise focada nos controllers, DTOs e requests do SGC identificou pontos críticos onde o sistema exige o envio manual de informações contextuais (ID de usuário, ID de perfil ou ID de unidade), embora esses mesmos dados estejam garantidos e validados pelo Spring Security por meio do token JWT (`@AuthenticationPrincipal Usuario`).

O sistema atual atrela as regras de permissões ao "Local" (unidade ativa) e à "Hierarquia" (perfil do usuário). Todo usuário que opera no sistema possui uma unidade e um perfil já definidos na sessão e no contexto de segurança. Exigir que o cliente informe esses valores no payload configura uma redundância técnica, aumenta a superfície de ataque (caso a validação não confira se o valor do payload bate com o do token) e prejudica a experiência de desenvolvimento do frontend.

Abaixo, detalhamos os endpoints afetados e propomos soluções.

---

## 1. Problemas Identificados

### 1.1 `CriarAnaliseRequest` e Endpoint de Análises (Cadastro e Validação)
- **Onde ocorre**: `SubprocessoController.criarAnaliseCadastro` e `SubprocessoController.criarAnaliseValidacao`.
- **A redundância**: O DTO `CriarAnaliseRequest` exige a passagem explícita de `tituloUsuario` e `siglaUnidade`.
  ```java
  public record CriarAnaliseRequest(
      @TituloEleitoral String tituloUsuario,
      @NotBlank @Size(max = 20) String siglaUnidade,
      // ...
  )
  ```
- **Contexto Detalhado**: Quando o `SubprocessoController` chama os endpoints para registrar que um determinado ator analisou o cadastro ou a validação de um mapa, o frontend deve preencher qual é o CPF (título de eleitor) do analista e a sigla da unidade que ele representa. Isso é uma falha de design, pois a ação está sendo executada pelo usuário logado, cujo contexto (`@AuthenticationPrincipal Usuario usuario`) já contém tanto seu `tituloEleitoral` quanto sua `unidadeAtiva` e `perfilAtivo`.
- **Risco**: Se o Controller não cruza os dados do payload com os do token, um usuário autenticado poderia, em tese, forjar uma análise em nome de outro usuário ou de outra unidade, ferindo as regras de negócio de auditoria estritas documentadas no `regras-acesso.md`.

### 1.2 `EntrarRequest` / `AutorizarRequest` e Fluxo de Login
- **Onde ocorre**: `LoginController.entrar` e `LoginController.autorizar`.
- **A redundância**: Ambos exigem o `tituloEleitoral` em seus respectivos payloads:
  ```java
  public record AutorizarRequest(@TituloEleitoral String tituloEleitoral) {}
  public record EntrarRequest(@TituloEleitoral String tituloEleitoral, /* ... */) {}
  ```
- **Contexto Detalhado**: O login do sistema acontece em etapas. O método `/autenticar` gera um "Pre-Auth Token" (um JWT de curta duração) gravado no cookie `SGC_PRE_AUTH`. As próximas requisições de autorização (para descobrir quais unidades/perfis o usuário pode acessar) e entrar (selecionando-os para pegar o JWT de sessão final) necessitam dessa prova de autenticação. O código atual do `LoginController` possui um método privado `verificarTokenPreAuth` que decodifica o cookie e extrai o "sujeito" do token.
- **Risco**: Apesar de o método verificar se o "sujeito" do token corresponde ao `tituloEsperado` passado no JSON, pedir que o cliente envie esse dado no payload é inútil e confuso. O backend deveria extrair a identidade exclusivamente do token seguro, blindando o sistema.

---

## 2. DTOs Não Afetados (Sem Redundância)

Ao longo da análise, identificou-se DTOs que pedem explicitamente IDs de unidade, usuário e perfil, mas que **estão corretos** por se tratarem de "alvos" da operação e não do "autor":

*   **`CriarAtribuicaoRequest`**: O payload pede o `tituloEleitoralUsuario`. Isso é usado pelo administrador para atribuir responsabilidade temporária a um *outro* usuário. Logo, não pode ser tirado do JWT do administrador.
*   **`AcaoEmBlocoRequest` / `EnviarLembreteRequest`**: Pedem uma lista de IDs de unidades (`unidadeCodigos`). Isso designa quais unidades de negócio são os *alvos* do lembrete ou da ação em bloco (executada por GESTOR ou ADMIN), e não quem está executando a ação.

---

## 3. Passo a Passo para Solução e Recomendações

### Fase A: Refatoração do fluxo de Análises de Subprocesso

1.  **Limpar DTO (`CriarAnaliseRequest.java`)**:
    *   Remova os campos `tituloUsuario` e `siglaUnidade` da definição do `record`.

2.  **Expor o Contexto no Controller (`SubprocessoController.java`)**:
    *   Injete `@AuthenticationPrincipal Usuario usuario` na assinatura dos métodos `@PostMapping("/{codSubprocesso}/analises-cadastro")` e `@PostMapping("/{codSubprocesso}/analises-validacao")`.
    *   Altere o método privado interno `criarAnalise(...)` para aceitar o objeto `Usuario`.

3.  **Ajustar a Regra de Negócio (`SubprocessoTransicaoService.java`)**:
    *   No método `criarAnalise(...)`, em vez de ler `request.siglaUnidade()` para buscar a unidade no banco e associar à análise, leia `usuario.getUnidadeAtivaCodigo()`.
    *   De forma equivalente, no builder de `Analise`, substitua `request.tituloUsuario()` por `usuario.getTituloEleitoral()`.
    *   Corrija as instâncias programáticas que usam o builder do `CriarAnaliseRequest` (como em `registrarAnalise`), removendo os `sets` desses atributos expurgados.

### Fase B: Refatoração do fluxo de Login Pre-Auth

1.  **Limpar DTOs (`AutorizarRequest.java`, `EntrarRequest.java`)**:
    *   Remova completamente a propriedade `@TituloEleitoral String tituloEleitoral` de ambos os records.
    *   *(Nota: `AutorizarRequest` se tornará um record vazio `public record AutorizarRequest() {}` - avalie se seu uso pelo frontend justifica mantê-lo ou transformá-lo em `GET`)*.

2.  **Isolar Leitura de Token (`LoginController.java`)**:
    *   Atualize o método `verificarTokenPreAuth` para se chamar `extrairTokenPreAuth`. Ele não receberá mais `tituloEsperado`, mas apenas retornará a `String` extraída do token após a validação `gerenciadorJwt.validarTokenPreAuth`.
    *   Nos métodos `autorizar(...)` e `entrar(...)`, chame este método para armazenar a identidade em uma variável e passá-la para a Fachada.

3.  **Atualizar a Fachada de Autenticação (`LoginFacade.java`)**:
    *   Altere o método `entrar(EntrarRequest)` para aceitar a identidade separadamente: `entrar(EntrarRequest, String tituloEleitoral)`.

---

## 4. Próximos Passos
Esta refatoração impactará as requisições geradas pelo frontend (que deve parar de enviar os campos expurgados) e possivelmente os testes End-to-End (`E2eController.java`) que forjam esses `Requests` com DTOs artificiais. É mandatório que o ajuste na API Backend seja sincronizado com as chamadas feitas pelo Axios/Vue no repositório `frontend`.
