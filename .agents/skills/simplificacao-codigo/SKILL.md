---
name: simplificacao-codigo
description: Simplificar código do SGC sem romper contratos, requisitos ou regras de acesso. Use para reduzir duplicação, acoplamento, superfícies de services/composables/stores, efeitos colaterais e código morto em backend Java/Spring ou frontend Vue/TypeScript, incluindo auditorias de permissões e contratos entre camadas.
---

# Simplificação de Código no SGC

## Fontes de verdade

Antes de alterar código, leia o `AGENTS.md` e as especificações aplicáveis em `specs/`. Para permissões ou visibilidade,
leia também `specs/design/acesso.md` e `specs/design/ux.md`.

Preserve comportamento, contrato HTTP, DTOs, regras de acesso, textos de UI e testes. Não exponha entidades JPA, não
remova camadas que ainda concentram regra real e não crie compatibilidades silenciosas para tolerar contratos incorretos.

## Escolha do corte

1. Mapeie quem decide, quem consome e onde a regra está duplicada.
2. Classifique o problema: duplicação, contrato frouxo, superfície ampla, estado global desnecessário, efeito colateral
   oculto, código morto, baixa coesão ou deriva entre backend e frontend.
3. Escolha a menor fronteira segura. Prefira corrigir a fonte da regra a compensá-la no cliente.
4. Altere uma fronteira por vez e valide antes de prosseguir.

Prefira helper privado ou composição local antes de abstração compartilhada. Extraia uma camada somente quando ela
passar a ter contrato ou mais de um consumidor real.

## Permissões e contratos de UI

Para qualquer mudança de acesso, percorra esta sequência:

1. Confirme a regra em `acesso.md` e a apresentação exigida em `ux.md`.
2. Verifique a barreira HTTP (`@PreAuthorize`).
3. Verifique o alvo e a ação no `SgcPermissionEvaluator`; uma ação incompatível com o tipo do alvo deve ser negada.
4. Verifique regras adicionais no serviço: situação do workflow, dono do dado e seleção de unidade.
5. Faça o DTO refletir a decisão do backend. Não deixe `pode...` anunciar permissão estrutural para perfil que não a
   possui.
6. Preserve a semântica: `pode...` representa capacidade estrutural do perfil; `habilitar...` acrescenta situação,
   localização, carregamento e demais pré-condições contextuais.
7. Faça a UI esconder ações que não pertencem ao perfil e desabilitar ações permitidas, mas indisponíveis agora. Respeite
   as exceções documentadas para cadastro de atividades e mapa.
8. Cubra ao menos uma negação por perfil, uma por localização/hierarquia e uma por situação, além do cenário permitido.

Não reintroduza `Perfil`, `isAdmin`, `isChefe` ou equivalente no frontend quando o backend já entrega permissões.
Corrija o DTO ou a borda HTTP em vez de duplicar a regra na tela.

## Backend

- Mantenha controllers como borda HTTP; não os faça acessar repositórios diretamente quando houver regra, segurança ou
  transação.
- Mantenha DTOs separados do domínio e sem importação de `model.*` em contratos públicos.
- Centralize busca, validação ou montagem repetidas no service responsável.
- Não use permissões genéricas para tipos de alvo incompatíveis.
- Ao mudar uma dependência ou contrato interno, atualize mocks, testes e código órfão na mesma rodada.
- Se o Gradle falhar por cache ou saída obsoleta, repita a validação com `--no-configuration-cache` antes de concluir que
  existe regressão de código.

## Frontend

- Não crie store global ou composable compartilhado para estado de uma única tela.
- Não use fallback silencioso, `Partial`, casts ou defaults para mascarar resposta central incompleta.
- Mantenha visibilidade e habilitação como decisões distintas.
- Prefira corrigir o contrato backend quando a tela recompõe regra de workflow, elegibilidade ou autorização.
- Preserve acessibilidade, textos e navegação definidos em `specs/design/ux.md`.

## Validação

Execute a menor validação que prova a mudança e amplie conforme o risco:

```powershell
# Backend
./gradlew :backend:compileTestJava --no-configuration-cache
./gradlew :backend:test --tests "sgc.pacote.TesteAlvo" --no-configuration-cache
node toolkit/sgc.js backend contratos auditar

# Frontend — executar dentro de C:\sgc\frontend
npx vitest run <arquivos> --reporter=dot --no-color
npm run typecheck
npm run lint
npm run arquitetura:audit
npm run cruft:audit

# Auditorias transversais, quando aplicáveis
node toolkit/sgc.js codigo smells auditar
```

Use `git diff --check`. Para E2E, siga `e2e/regras-e2e.md`, rode o spec serial completo quando aplicável e redirecione
a saída para arquivo.

## Saída da rodada

Informe a simplificação feita, os contratos preservados, a validação executada e qualquer próximo alvo concreto. Quando
o problema for repetível, proponha uma auditoria no toolkit com comando, saída legível e, quando necessário, JSON.
