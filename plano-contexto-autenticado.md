# Plano de Contexto Autenticado do SGC

Documento operacional para unificar a obtenção e o uso do usuário autenticado no backend. Este plano não trata de simplificação genérica; trata de consistência arquitetural, contrato e segurança.

## Objetivo

Eliminar estilos concorrentes para obtenção do usuário atual e consolidar um único caminho de contexto autenticado no backend.

## Regra central

* o usuário autenticado atual deve ser obtido sempre por um ponto centralizado;
* controllers não devem usar `@AuthenticationPrincipal` para repassar o usuário atual;
* facades e services de aplicação não devem receber `Usuario` por parâmetro para representar o usuário atual;
* não pode existir fallback de usuário, perfil ou título;
* se algum fluxo realmente precisar de um usuário que não seja o autenticado atual, isso deve aparecer como dado de negócio explícito, com nome próprio, e não como reaproveitamento de `Usuario`.

## Fontes de verdade

Ordem de precedência:

* `AGENTS.md`
* `etc/docs/regras-acesso.md`
* `etc/reqs`
* este plano

## Situação atual confirmada

* o backend ainda mistura dois estilos:
  * leitura centralizada via `UsuarioFacade`;
  * repasse por `@AuthenticationPrincipal Usuario`.
* a convivência desses estilos aumenta:
  * acoplamento com Spring Security nos controllers;
  * superfície de contrato nos services;
  * risco de divergência entre controller, service, permissão e testes.
* a direção correta já está validada em partes do sistema:
  * `alerta`;
  * `painel`;
  * leituras/contextos em `processo`;
  * leituras/contextos em `subprocesso`.

## Alvos prioritários

## Frente 1 — Controllers com `@AuthenticationPrincipal`

Objetivo:

* remover `@AuthenticationPrincipal` como estilo padrão dos controllers.

Escopo inicial:

* `backend/src/main/java/sgc/organizacao/UsuarioController.java`
* `backend/src/main/java/sgc/processo/ProcessoController.java`
* `backend/src/main/java/sgc/subprocesso/SubprocessoController.java`

Critério de pronto:

* controller não repassa `Usuario` atual;
* obtenção do contexto atual ocorre em ponto centralizado;
* contratos HTTP permanecem intactos.

## Frente 2 — Services de workflow e escrita

Objetivo:

* remover `Usuario` como parâmetro de aplicação quando ele representa apenas o usuário autenticado atual.

Escopo inicial:

* `backend/src/main/java/sgc/processo/service/ProcessoService.java`
* `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`
* pontos auxiliares em `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java`, se surgirem durante a limpeza

Critério de pronto:

* services obtêm o contexto autenticado internamente;
* movimentação, análise, histórico e auditoria continuam corretos;
* sem fallback;
* sem coexistência de assinatura dupla para o mesmo fluxo.

## Frente 3 — Testes e fixtures

Objetivo:

* alinhar a base de testes ao contrato único de contexto autenticado.

Escopo inicial:

* WebMvc tests que ainda mockam `@AuthenticationPrincipal`;
* testes unitários e de integração que ainda stubbam apenas `usuarioAutenticado()` quando o fluxo já depende de `contextoAutenticado()`;
* helpers de teste que criam `Usuario` incompleto para contexto autenticado.

Critério de pronto:

* testes montam contexto autenticado válido de forma explícita;
* ausência de contexto passa a falhar de forma intencional;
* nenhum teste depende de perfil ou título inventado pelo código de produção.

## Restrições

* não criar camada nova só para esconder `@AuthenticationPrincipal`;
* não introduzir fallback de segurança por conveniência de teste;
* não misturar essa padronização com mudança de regra de negócio;
* não alterar contratos HTTP ou DTOs externos sem necessidade real;
* manter o menor privilégio como premissa de interpretação das regras de acesso.

## Sequência recomendada

1. Remover o caso restante mais simples em `UsuarioController`.
2. Atacar `ProcessoController` e `ProcessoService`.
3. Atacar `SubprocessoController` e `SubprocessoTransicaoService`.
4. Fechar a rodada ajustando testes e fixtures para o contrato único.

## Critério global de pronto

* um único estilo para obter o usuário atual no backend;
* nenhum repasse de `Usuario` atual por parâmetro em controller, facade ou service de aplicação;
* nenhuma compatibilidade dupla;
* nenhum fallback;
* testes alinhados ao contrato final.

## Validação mínima por rodada

### Backend

* `./gradlew :backend:compileTestJava`
* `./gradlew :backend:test`
