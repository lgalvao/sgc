# Relatório de Segurança - SGC

## Escopo e metodologia

Esta análise combinou revisão manual de código com ferramentas automatizadas disponíveis no ambiente:

- **Semgrep** com regras locais em `/tmp/sgc-semgrep-rules.yml`, cobrindo:
  - segredo JWT padrão;
  - `ambiente-testes: true` na configuração principal;
  - exposição de stack trace na resposta;
  - confiança em `X-Forwarded-For`;
  - `permitAll` para `/e2e/**`;
  - logging de identificador sensível;
  - cookie sensível com `Secure` condicionado a flag de ambiente;
  - OpenAPI/Swagger habilitado por padrão.
- **SpotBugs** via `./gradlew :backend:spotbugsMain :backend:spotbugsTest`.
- **npm audit** na raiz e no `frontend`.
- **Validação de baseline do projeto**:
  - `JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64 ./gradlew :backend:test`
  - `npm run lint`
  - `npm run typecheck`
  - `npm run test:unit`
- **Leitura manual de código** em pontos sensíveis do backend e frontend.

### Limitações do ambiente

- O **Semgrep remoto** (`--config auto`) não conseguiu baixar regras de `semgrep.dev` por restrição de rede do sandbox.
- O **`codeql_checker` local** não executou porque ele só analisa mudanças de código, e esta tarefa não alterou código-fonte executável.
- A consulta aos alertas de **Code Scanning** e **Secret Scanning** do GitHub retornou **403 Resource not accessible by integration**.

Mesmo com essas limitações, os achados abaixo foram confirmados diretamente no código.

---

## Resumo executivo

### Principais riscos confirmados

1. **Fluxos inseguros habilitados por configuração padrão de teste** no backend.
2. **Vazamento de detalhes internos** por inclusão de stack trace em respostas de erro.
3. **Superfície de ataque ampliada** por endpoints E2E destrutivos e públicos dentro do profile `e2e`.
4. **Possível broken access control no painel**, pois parâmetros sensíveis são aceitos sem vínculo visível com o usuário autenticado.
5. **Descoberta excessiva da API**, com Swagger/OpenAPI potencialmente público.
6. **Riscos operacionais em autenticação**, incluindo confiança direta em `X-Forwarded-For`.
7. **Riscos de privacidade e impacto de XSS**, por exposição de PII e persistência de JWT em `localStorage`.
8. **Vulnerabilidades conhecidas em dependências de desenvolvimento do frontend**.

### Sinais positivos

- O backend já aplica **HSTS**, **CSP**, **X-Frame-Options** e **CSRF** com cookie para SPA em `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/config/ConfigSeguranca.java:71-91`.
- O `GerenciadorJwt` **bloqueia segredo padrão em produção**, embora ainda aceite em `default`, `local` e `hom`: `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/login/GerenciadorJwt.java:31-42`.
- Baseline funcional do projeto está saudável neste ambiente:
  - backend tests: **OK**
  - lint: **OK**
  - typecheck: **OK**
  - frontend unit tests: **120 arquivos / 1045 testes OK**
- `npm audit` da raiz não encontrou vulnerabilidades.
- SpotBugs executou com sucesso, sem falha de build.

---

## Achados detalhados

### 1. Configuração principal ativa modo de testes e enfraquece autenticação

- **Severidade:** Alta
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/resources/application.yml:46-60`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/LoginFacade.java:34-67`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/login/LimitadorTentativasLogin.java:66-74`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/login/LoginController.java:35-58`

**Evidência**

- A configuração principal traz `aplicacao.ambiente-testes: true`.
- Com essa flag ativa, `LoginFacade.autenticar(...)` retorna `true` sem consultar AD.
- O `LimitadorTentativasLogin` também é desabilitado quando `ambiente-testes` está ativo.
- O cookie `SGC_PRE_AUTH` recebe `Secure=false` quando `ambienteTestes` é `true`.

**Impacto**

Se a aplicação subir sem profile seguro ou com configuração indevida, o sistema pode:

- aceitar qualquer senha;
- perder proteção de rate limit no login;
- enviar cookie sensível sem flag `Secure`.

O profile `prod` redefine `ambiente-testes: false`, mas a configuração base atual é arriscada demais para homologação, execução local despreparada e cenários de deploy mal configurado.

**Sugestões**

- Mudar o default da configuração principal para `false`.
- Restringir bypass de autenticação apenas a profiles estritamente de teste (`test` e `e2e`), não por flag genérica no YAML principal.
- Forçar `cookie.setSecure(true)` fora de cenários explicitamente instrumentados de teste.

---

### 2. Stack trace é serializado e enviado ao cliente

- **Severidade:** Alta
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/comum/erros/RestExceptionHandler.java:44-49,65-76,150-190`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/comum/erros/ErroApi.java:20-27`
  - `/home/runner/work/sgc/sgc/frontend/src/axios-setup.ts:34-37`

**Evidência**

- O `RestExceptionHandler` chama `erroApi.setStackTrace(getStackTrace(ex))` em vários fluxos.
- `ErroApi` possui o campo `stackTrace` serializável.
- O frontend ainda registra `normalized.stackTrace` no logger global quando presente.

**Impacto**

Isso pode expor:

- nomes de classes internas;
- estrutura de pacotes;
- detalhes de validação e fluxo;
- mensagens internas úteis para engenharia reversa.

Além do vazamento direto ao cliente, o frontend amplia a exposição ao registrar essa informação no lado do navegador.

**Sugestões**

- Não incluir `stackTrace` na resposta HTTP em ambientes normais.
- Expor apenas `traceId` ao cliente e manter detalhes completos apenas no log do servidor.
- Revisar o logger global do frontend para nunca propagar stack traces vindas da API.

---

### 3. Endpoints E2E públicos e destrutivos sob o profile `e2e`

- **Severidade:** Alta
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/e2e/E2eSecurityConfig.java:62-88`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/e2e/E2eController.java:55-67,112-201`

**Evidência**

- `E2eSecurityConfig` libera `"/e2e/**"` com `permitAll()`.
- `E2eController` oferece operações destrutivas, como:
  - reset completo do banco;
  - limpeza total de processos;
  - criação acelerada de fixtures.

**Impacto**

Se o profile `e2e` for ativado fora do ambiente controlado de teste, um atacante ou usuário indevido pode:

- apagar dados;
- adulterar estado do sistema;
- contornar fluxos normais de negócio.

O uso de `@Profile("e2e")` reduz o risco, mas o impacto potencial é muito alto caso haja erro operacional.

**Sugestões**

- Garantir em pipeline/deploy que o profile `e2e` nunca seja ativado em ambientes expostos.
- Considerar proteção adicional por segredo temporário, allowlist de IP ou porta/admin app separada.
- Evitar `permitAll()` público mesmo em E2E, quando possível.

---

### 4. Painel aceita parâmetros sensíveis sem vínculo explícito ao contexto autenticado

- **Severidade:** Alta
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/processo/painel/PainelController.java:29-57`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/processo/painel/PainelFacade.java:41-57,73-109`

**Evidência**

- `GET /api/painel/processos` recebe `perfil` e `unidade` por query string.
- `GET /api/painel/alertas` recebe `usuarioTitulo` e `unidade` por query string.
- O facade usa esses valores diretamente para listar e até marcar alertas como lidos.
- Não há, nessas camadas, validação visível comparando esses parâmetros com o usuário autenticado.

**Impacto**

Há risco de:

- visualização de dados de outra unidade;
- marcação de alertas de outro usuário como lidos;
- enumeração de contexto organizacional indevido.

**Observação**

Ao contrário de suspeitas antigas em `ProcessoController`, aqui o risco permanece **plausível e consistente com o código atual**. Já em `ProcessoController`, a checagem `processoService.checarAcesso(...)` existe e não foi classificada neste relatório como IDOR confirmado.

**Sugestões**

- Derivar `perfil`, `unidade` e `usuarioTitulo` do `AuthenticationPrincipal` sempre que possível.
- Se parâmetros precisarem continuar existindo, validar explicitamente:
  - que a unidade pertence ao escopo do usuário;
  - que `usuarioTitulo` corresponde ao usuário logado, salvo exceção explícita e auditada.

---

### 5. Swagger/OpenAPI potencialmente público

- **Severidade:** Média
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/resources/application.yml:27-31`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/config/ConfigSeguranca.java:60-70`

**Evidência**

- `springdoc.api-docs.enabled: true`
- `springdoc.swagger-ui.enabled: true`
- A segurança autentica apenas `"/api/**"` e deixa `anyRequest().permitAll()`.

**Impacto**

Mesmo sem exploração direta, documentação pública:

- facilita enumeração de endpoints;
- expõe modelos e contratos;
- reduz esforço de reconhecimento para atacantes.

**Sugestões**

- Proteger `/api-docs` e `/swagger-ui.html` por autenticação ou restringir por profile.
- Desabilitar a documentação interativa fora de ambientes internos.

---

### 6. Confiança direta em `X-Forwarded-For` afeta rate limit e auditoria

- **Severidade:** Média
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/login/LoginController.java:127-140`

**Evidência**

- O método `extrairIp(...)` usa `X-Forwarded-For` diretamente e escolhe o primeiro valor.

**Impacto**

Sem validação de proxy confiável, um cliente pode forjar o cabeçalho e:

- contornar rate limit por IP;
- poluir auditoria;
- dificultar investigação de abuso.

**Sugestões**

- Confiar em `X-Forwarded-For` apenas quando o proxy reverso for conhecido e validado.
- Considerar uso de `ForwardedHeaderFilter`/configuração padronizada da infraestrutura.

---

### 7. Segredo JWT padrão conhecido é aceito em ambientes não produtivos

- **Severidade:** Média
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/resources/application.yml:55-57`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/login/GerenciadorJwt.java:27-40`

**Evidência**

- O YAML principal define fallback para `JWT_SECRET`.
- `GerenciadorJwt` bloqueia esse segredo em produção, mas o aceita em `test`, `e2e`, `local`, `hom` e `default`.

**Impacto**

O risco em produção está parcialmente mitigado, mas ainda há exposição em:

- homologação;
- ambientes locais compartilhados;
- qualquer execução sem profile produtivo adequado.

**Sugestões**

- Remover o valor default.
- Exigir segredo explícito em todos os ambientes não efêmeros.
- Validar presença e força do segredo em CI/CD.

---

### 8. Exposição de dados pessoais em endpoints de usuário

- **Severidade:** Média
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/UsuarioController.java:28-40`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/organizacao/model/Usuario.java:35-50,65-69`

**Evidência**

- A view pública expõe:
  - `tituloEleitoral`
  - `matricula`
  - `nome`
  - `email`
  - `unidadeCodigo`
- Os endpoints de busca/pesquisa exigem autenticação, mas não mostram filtragem adicional por necessidade mínima.

**Impacto**

Isso aumenta risco de:

- coleta massiva de PII por usuários autenticados;
- scraping de base organizacional;
- uso indevido de identificadores internos.

**Sugestões**

- Reduzir a `OrganizacaoViews.Publica`.
- Expor apenas campos mínimos para busca e seleção.
- Separar DTOs de busca de DTOs detalhados.

---

### 9. JWT armazenado em `localStorage` no frontend

- **Severidade:** Média
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/frontend/src/stores/perfil.ts:9-10,127-138`
  - `/home/runner/work/sgc/sgc/frontend/src/axios-setup.ts:43-49`

**Evidência**

- O token JWT é gravado em `localStorage` com a chave `jwtToken`.
- O `axios` lê o token de `localStorage` e injeta em `Authorization: Bearer`.

**Impacto**

Esse padrão aumenta muito o impacto de qualquer XSS, pois scripts executados no navegador conseguem extrair o token.

**Sugestões**

- Preferir cookie `HttpOnly` para o token principal, quando a arquitetura permitir.
- Se o design atual precisar permanecer, endurecer ainda mais políticas de CSP e reduzir superfícies de XSS.

---

### 10. Logging de identificador sensível

- **Severidade:** Baixa/Média
- **Arquivos:**
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/login/ClienteAcessoAd.java:25-46`
  - `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/seguranca/LoginFacade.java:53-67,107-108`

**Evidência**

- Há logs com título eleitoral/identificador do usuário.

**Impacto**

Em ambientes com retenção longa de logs ou múltiplos consumidores, isso amplia exposição de PII.

**Sugestões**

- Evitar logar identificadores completos.
- Usar mascaramento ou hash parcial.

---

### 11. Vulnerabilidades em dependências de desenvolvimento do frontend

- **Severidade:** Média
- **Ferramenta:** `npm audit` em `/home/runner/work/sgc/sgc/frontend`

**Resultado**

- **8 vulnerabilidades altas**, concentradas no ecossistema `vitest`/browser tooling:
  - `vitest`
  - `@vitest/browser`
  - `@vitest/browser-playwright`
  - `@vitest/browser-preview`
  - `@vitest/browser-webdriverio`
  - `@vitest/coverage-v8`
  - `@vitest/ui`
  - transitiva `flatted`

**Impacto**

O risco parece estar concentrado em dependências de desenvolvimento, não em runtime da aplicação entregue ao usuário final. Ainda assim, isso afeta:

- segurança da estação/CI;
- confiabilidade da cadeia de suprimentos;
- exposição de ferramentas internas se executadas em ambientes compartilhados.

**Sugestões**

- Planejar atualização coordenada do stack Vitest.
- Validar compatibilidade antes de aplicar `npm audit fix --force`, pois a correção disponível sugere mudança potencialmente disruptiva.

---

## Itens revisados sem evidência imediata de exploração

- **`ProcessoController`**: há verificação real de acesso em `processoService.checarAcesso(...)` em `/home/runner/work/sgc/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java:521-529`. Não foi classificado aqui como IDOR confirmado.
- **Headers de segurança**: o backend já aplica HSTS, CSP e `X-Frame-Options`, o que é um bom baseline.
- **CORS**: configurável por propriedades; não foi encontrada origem wildcard no profile principal, mas o tema merece revisão por ambiente.
- **Segredo hardcoded adicional**: não foram encontrados segredos reais de produção além do fallback conhecido de JWT e credenciais de teste.

---

## Priorização recomendada

### Prioridade 1

1. Desligar o modo de testes por default na configuração principal.
2. Remover stack trace das respostas HTTP.
3. Blindar operacionalmente o profile `e2e` e os endpoints `/e2e/**`.
4. Corrigir vínculo do painel com o usuário autenticado.

### Prioridade 2

5. Restringir Swagger/OpenAPI por ambiente.
6. Remover fallback de segredo JWT.
7. Corrigir confiança em `X-Forwarded-For`.
8. Reduzir exposição de PII em endpoints de usuário.

### Prioridade 3

9. Rever persistência de JWT em `localStorage`.
10. Reduzir logging de identificadores.
11. Atualizar dependências do stack Vitest.

---

## Conclusão

O sistema já possui uma base razoável de segurança em transporte e autenticação, mas há **pontos de configuração e exposição de dados** que merecem correção prioritária. O principal padrão observado é que recursos pensados para conveniência de desenvolvimento/teste continuam muito próximos do caminho de execução normal, o que aumenta o risco operacional.

Em termos práticos, as correções mais valiosas no curto prazo são:

- **desativar defaults inseguros**;
- **não devolver stack trace ao cliente**;
- **amarrar o painel ao contexto autenticado**;
- **blindar totalmente o profile E2E**.
