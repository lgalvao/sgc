# Plano de Reorganizacao dos Scripts do SGC

## Status atual

Consolidacao em andamento com base ja operacional em `etc/scripts`.

Ja concluido:

- CLI raiz em `node etc/scripts/sgc.js`
- subprojeto isolado com `etc/scripts/package.json`
- comandos transversais de `projeto` e `qa`
- scripts operacionais centralizados em `etc/scripts/backend` e `etc/scripts/frontend`
- remocao dos wrappers `.sh` e `.ps1` substituidos
- testes automatizados iniciais do toolkit

Pendente:

- reduzir uso de `CommonJS` nos scripts herdados de `backend/` e `frontend/`
- normalizar saida, ajuda e logging dos scripts herdados que ainda usam `console.*`
- ampliar cobertura de testes do toolkit
- consolidar melhor a camada de bibliotecas compartilhadas entre backend, frontend e QA
- revisar se ainda faz sentido manter scripts `*.cjs` ou se ja compensa migrar o toolkit inteiro para ESM puro

## Objetivo

Consolidar os scripts espalhados pelo repositorio em um toolkit unico, coerente, facil de usar e facil de manter.

O estado original possuia automacoes distribuidas em:

- scripts locais de backend
- scripts locais de frontend
- `etc/scripts`
- `etc/qa-dashboard/scripts`

Isso gerava duplicacao, descoberta ruim, nomenclatura inconsistente, multiplos pontos de entrada e falta de governanca sobre artefatos e contratos de saida.

## Objetivos de resultado

Ao final da reorganizacao, o projeto deve ter:

- um unico ponto de entrada: `node etc/scripts/sgc.js`
- uma estrutura unica de subcomandos por dominio
- um padrao unico de linguagem: `Node.js` com ESM
- nomes consistentes em portugues e `kebab-case`
- ajuda padronizada
- saida humana e, quando fizer sentido, saida estruturada em JSON
- documentacao unica do toolkit
- separacao clara entre comandos publicos e bibliotecas internas
- integracao do dashboard de QA como parte do toolkit oficial

## Diagnostico atual

### Problemas estruturais

- Scripts estavam espalhados em quatro polos independentes.
- Nao existia uma CLI raiz do projeto.
- A documentacao estava descentralizada por area.
- Havia multiplos wrappers e utilitarios em shell, PowerShell, Python, `.js`, `.mjs` e `.cjs`.
- Os nomes dos scripts foram criados em momentos diferentes e sem criterio unico de dominio.

### Problemas operacionais

- Nem todos os comandos produzem saida estruturada.
- Parte dos scripts herdados ainda usa ajuda minima, `console.*` e contratos de saida irregulares.
- A suite automatizada do toolkit ainda cobre smoke tests e fluxos simples, mas nao os cenarios mais caros de integracao.

### Problemas de manutencao

- Ainda existe duplicacao de utilitarios entre scripts herdados.
- Boa parte da superficie migrada ainda esta em `CommonJS`.
- Falta decidir o ponto de equilibrio entre manter wrappers herdados e reescrever os scripts mais importantes em ESM.

## Direcao arquitetural

### Estado alvo ajustado

O toolkit ja esta centralizado em `etc/scripts`, com a seguinte organizacao operacional:

```text
etc/scripts/
  sgc.js
  package.json
  README.md
  backend/
  frontend/
  qa/
  projeto/
  codigo/
  e2e/
  lib/
  test/
```

O backlog restante nao e mais de consolidacao estrutural. Agora ele e de:

- acabamento
- padronizacao
- remocao de herancas tecnicas
- ampliacao de testes

### Ponto de entrada unico

Criar e adotar como oficial:

```bash
node etc/scripts/sgc.js
```

Esse comando deve ser a forma recomendada para uso humano e para documentacao.

### Estrutura proposta

```text
etc/scripts/
  package.json
  sgc.js
  README.md
  lib/
    cli-ajuda.js
    caminhos.js
    execucao.js
    json-saida.js
    filtros.js
  backend/
    cobertura-analisar.js
    cobertura-priorizar.js
    cobertura-complexidade.js
    cobertura-lacunas.js
    cobertura-plano.js
    cobertura-verificar.js
    cobertura-jornada.js
    testes-analisar.js
    testes-priorizar.js
    testes-gerar-stub.js
    java-corrigir-fqn.js
    java-auditar-null.js
    java-instalar-certificados.js
    lib/
      cobertura-base.js
  frontend/
    cobertura-verificar.js
    cobertura-impacto.js
    cobertura-linhas-sem-cobertura.js
    mensagens-extrair.js
    mensagens-analisar.js
    validacoes-auditar.js
    views-auditar-validacoes.js
    test-ids-listar.js
    test-ids-duplicados.js
    telas-capturar.js
  qa/
    snapshot-coletar.js
    dashboard-servir.js
    resumo.js
  projeto/
    doctor.js
    limpar.js
    setup.js
    qualidade.js
  codigo/
    comentarios-limpar-ai.js
    comentarios-limpar-generico.js
    id-legado-identificar.js
    title-case-identificar.js
    title-case-corrigir.js
  e2e/
    limpar.js
  test/
    sgc.test.js
```

### Modelo de subcomandos

O toolkit final deve seguir esta ideia:

```bash
node etc/scripts/sgc.js backend cobertura verificar
node etc/scripts/sgc.js backend testes analisar
node etc/scripts/sgc.js frontend cobertura verificar
node etc/scripts/sgc.js frontend mensagens analisar
node etc/scripts/sgc.js qa snapshot coletar --perfil rapido
node etc/scripts/sgc.js qa resumo
node etc/scripts/sgc.js projeto doctor
node etc/scripts/sgc.js projeto limpar
```

## Escopo por dominio

### Backend

Objetivo:

- consolidado em `etc/scripts/backend`
- manter a base compartilhada de cobertura
- reduzir a heranca `CommonJS` e padronizar logging/ajuda

Itens:

- revisar `console.*` e saídas manuais
- migrar utilitarios compartilhados do backend para `etc/scripts/lib` quando fizer sentido
- decidir quais scripts de backend merecem reescrita ESM completa

### Frontend

Objetivo:

- consolidado em `etc/scripts/frontend`
- padronizar ajuda, saida e logging
- avaliar migracao gradual de `CommonJS` para ESM

Itens ainda relevantes:

- `verificar-cobertura.cjs` -> `frontend/cobertura-verificar.cjs`
- `analisar-impacto-cobertura.cjs` -> `frontend/cobertura-impacto.cjs`
- `mostrar-linhas-sem-cobertura.cjs` -> `frontend/cobertura-linhas-sem-cobertura.cjs`
- `extrair-mensagens.cjs` -> `frontend/mensagens-extrair.cjs`
- `analisar-mensagens.cjs` -> `frontend/mensagens-analisar.cjs`
- `audit-frontend-validations.cjs` -> `frontend/validacoes-auditar.cjs`
- `audit-view-validations.cjs` -> `frontend/views-auditar-validacoes.cjs`
- `listar-test-ids.cjs` -> `frontend/test-ids-listar.cjs`
- `listar-test-ids-duplicados.cjs` -> `frontend/test-ids-duplicados.cjs`
- `capturar-telas.cjs` -> `frontend/telas-capturar.cjs`

### QA Dashboard

Objetivo:

- integrado ao toolkit principal
- reforcar seu papel como fonte de verdade de QA

Comandos previstos:

- `qa snapshot coletar --perfil rapido`
- `qa snapshot coletar --perfil backend`
- `qa resumo`
- `qa dashboard servir`

Itens ainda relevantes:

- adicionar testes do wrapper `qa snapshot coletar`
- avaliar um comando `qa validar` orientado a CI
- decidir se o dashboard deve ganhar mais comandos de resumo/comparacao

### Projeto

Objetivo:

- absorvido em boa parte
- separar melhor comandos publicos de bibliotecas internas

Comandos previstos:

- `projeto doctor`
- `projeto limpar`
- `projeto setup`
- `projeto qualidade`

Itens ainda relevantes:

- refinar `projeto setup`
- avaliar se `git-hooks/pre-push` deve ser absorvido por um comando ou mantido como artefato auxiliar

## Padroes obrigatorios

### Linguagem

- Padrao principal: `Node.js` com ESM
- toolkit isolado em `etc/scripts/package.json`
- extensao preferencial: `.js`
- `package.json` local com `"type": "module"`
- Evitar novos scripts em `Python`, `bash`, `PowerShell` ou `CommonJS`

### Nomenclatura

- portugues
- `kebab-case`
- nome orientado a dominio e acao
- exemplos:
  - `backend/cobertura-verificar.js`
  - `frontend/mensagens-analisar.js`
  - `qa/snapshot-coletar.js`
  - `projeto/doctor.js`

### UX

Todo comando publico deve ter:

- ajuda padronizada
- uso recomendado via `sgc.js`
- exemplos reais
- mensagens curtas e objetivas
- codigo de saida coerente

### Saida

Padroes desejados:

- saida humana por padrao
- `--json` quando o comando fizer sentido para automacao
- arquivos gerados com localizacao previsivel
- distincao clara entre artefato permanente e temporario

### Bibliotecas internas

As bibliotecas internas devem sair dos diretorios de dominio quando forem compartilhadas e ir para:

- `etc/scripts/lib`

Exemplos:

- ajuda padronizada
- execucao de comandos
- resolvedor de caminhos
- serializacao JSON
- filtros

Bibliotecas de dominio especifico podem ficar em:

- `etc/scripts/backend/lib`
- `etc/scripts/frontend/lib`
- `etc/scripts/qa/lib`

## Fases de implementacao

### Fase 1: Fundacao da CLI raiz

Status: concluida

Objetivo:

- criar `etc/scripts/package.json`
- criar `etc/scripts/sgc.js`
- criar `etc/scripts/lib`
- definir despacho por grupo e acao com bibliotecas externas

Entregas:

- `etc/scripts/package.json`
- `etc/scripts/sgc.js`
- `etc/scripts/README.md`
- `etc/scripts/lib/cli-ajuda.js`
- `etc/scripts/lib/caminhos.js`

### Fase 2: Migracao do backend

Status: concluida em estrutura

Objetivo:

- mover o toolkit atual do backend para `etc/scripts/backend`

Entregas:

- scripts backend funcionando pelo novo ponto de entrada
- caminhos ajustados
- README do backend absorvido pela documentacao raiz

### Fase 3: Migracao do frontend

Status: concluida em estrutura

Objetivo:

- incorporar os scripts locais de frontend

Entregas:

- subcomandos `frontend`
- padronizacao de nomes
- ajuda padronizada

### Fase 4: Integracao do QA dashboard

Status: concluida em superficie publica

Objetivo:

- integrar `coletar-snapshot.mjs` ao namespace `qa`

Entregas:

- `qa snapshot coletar`
- `qa resumo`
- documentacao do dashboard dentro do toolkit

### Fase 5: Consolidacao de `etc/scripts`

Status: em andamento

Objetivo:

- absorver wrappers e scripts transversais existentes
- portar utilitarios restantes para o dominio correto
- remover legados desnecessarios

Entregas:

- `projeto doctor`
- `projeto limpar`
- `projeto setup`
- `projeto qualidade`
- `codigo id-legado identificar`
- `codigo title-case identificar`
- `codigo title-case corrigir`
- `qa dashboard servir`

### Fase 6: Limpeza final

Status: em andamento

Objetivo:

- remover caminhos antigos
- eliminar documentacao duplicada
- deixar apenas a superficie final

Entregas:

- referencias antigas removidas
- wrappers legados descontinuados
- documentacao final unificada

Restante desta fase:

- reduzir `console.*` residual nos scripts herdados
- uniformizar `--help`, `--json` e codigos de saida
- decidir o destino final de `CommonJS`

## Funcionalidades faltantes prioritarias

### Alta prioridade

- ampliar testes do toolkit
- normalizar logging e saida dos scripts herdados
- revisar quais comandos precisam de `--json`
- consolidar bibliotecas compartilhadas em `etc/scripts/lib`

### Media prioridade

- comandos de comparacao/baseline para QA
- comando orientado a CI, como `qa validar`
- reduzir `CommonJS` em backend/frontend
- aumentar profundidade da documentacao por dominio

### Baixa prioridade

- testes de integracao mais caros
- `dry-run` para mais comandos
- exportadores adicionais
- revisar se vale trocar mais scripts herdados por bibliotecas terceiras

## Riscos e cuidados

- mover scripts entre diretorios exigira revisar caminhos relativos com cuidado
- o dashboard de QA tem contrato proprio e nao deve passar a consumir artefatos transientes diretamente
- wrappers `.sh` e `.ps1` podem estar sendo usados informalmente por pessoas ou CI; a remocao deve ser planejada
- nomes e comandos publicos devem estabilizar antes de atualizar documentacao externa

## Criterios de conclusao

O plano sera considerado concluido quando:

- existir um unico ponto de entrada oficial em `etc/scripts/sgc.js`
- backend, frontend, QA e projeto estiverem acessiveis por essa CLI
- a documentacao central estiver em `etc/scripts/README.md`
- os scripts antigos espalhados deixarem de ser a forma recomendada de uso
- os scripts herdados principais tiverem logging, ajuda e saida padronizados
- a suite do toolkit cobrir os fluxos principais com seguranca razoavel
- os contratos de ajuda e saida estiverem padronizados

## Recomendacao imediata

A proxima execucao deve atacar o **acabamento da Fase 6** com foco em valor rapido:

1. padronizar ajuda e codigos de saida dos comandos mais usados (`backend cobertura verificar`, `frontend cobertura verificar`, `qa snapshot coletar`)
2. concluir uma primeira trilha de migracao de `CommonJS` para ESM com baixa dependencia cruzada
3. ampliar os testes do toolkit cobrindo `--help`, `--json` e cenarios de erro comuns
4. revisar o `README.md` de `etc/scripts` para garantir exemplos reais e consistentes com o estado atual
5. decidir formalmente o destino de `git-hooks/pre-push` (comando oficial vs artefato auxiliar)

## Plano de execucao detalhado (curto prazo)

### Ciclo A - Padronizacao da experiencia de uso

Objetivo:

- tornar previsivel o comportamento da CLI independentemente do dominio

Entregas:

- contrato minimo de ajuda para todos os comandos publicos
- contrato de saida com `--json` nos comandos candidatos
- padrao unico de mensagens de erro para operacao e validacao de argumentos

Checklist sugerido:

- [ ] mapear comandos sem `--help` consistente
- [ ] mapear comandos que ja suportam JSON e documentar formato
- [ ] implementar camada compartilhada de ajuda/saida para reduzir duplicacao
- [ ] atualizar snapshots/testes do toolkit para refletir contratos

### Ciclo B - Reducao de heranca CommonJS

Objetivo:

- reduzir risco tecnico sem reescrever todo o toolkit de uma vez

Estrategia:

- priorizar scripts com baixa dependencia interna
- migrar primeiro os que ja usam utilitarios modernos de `etc/scripts/lib`
- manter compatibilidade de CLI durante a transicao

Checklist sugerido:

- [ ] inventariar scripts `.cjs` por criticidade e frequencia de uso
- [ ] migrar lote inicial de frontend (mais isolados)
- [ ] migrar lote inicial de backend sem quebrar `backend/lib/cobertura-base`
- [ ] remover adaptacoes temporarias quando todos os consumidores estiverem em ESM

### Ciclo C - Confiabilidade e governanca

Objetivo:

- aumentar confianca para evolucao continua do toolkit

Entregas:

- suite de testes com foco em contratos publicos
- regra clara de deprecacao e remocao de wrappers remanescentes
- orientacao de CI para validacao minima obrigatoria

Checklist sugerido:

- [ ] criar matriz de testes por grupo (`backend`, `frontend`, `qa`, `projeto`, `codigo`)
- [ ] cobrir cenarios de falha de argumentos e falha de execucao de subprocesso
- [ ] avaliar comando `qa validar` como agregador para pipeline
- [ ] publicar politica de deprecacao no README da CLI

## Backlog executivo priorizado

### Bloco 1 - Fazer agora

- padronizar `--help` e codigos de saida dos comandos com maior uso diario
- cobrir em teste os comandos `qa snapshot coletar` e `qa resumo`
- definir padrao unico de logging interno (sem `console.log` em producao)

### Bloco 2 - Fazer na sequencia

- migrar lote inicial de scripts frontend `.cjs` para ESM
- extrair utilitarios duplicados para `etc/scripts/lib`
- revisar naming de comandos para manter consistencia semantica

### Bloco 3 - Fazer depois da estabilizacao

- avaliar comando de comparacao de snapshots de QA
- expandir modos de saida estruturada para analise externa
- revisar custo/beneficio de migrar 100% dos scripts legados de uma vez
