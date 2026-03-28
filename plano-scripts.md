# Plano de Reorganizacao dos Scripts do SGC

## Objetivo

Consolidar os scripts espalhados pelo repositorio em um toolkit unico, coerente, facil de usar e facil de manter.

O estado atual possui automacoes distribuidas em:

- `backend/etc/scripts`
- `frontend/etc/scripts`
- `etc/scripts`
- `etc/qa-dashboard/scripts`

Isso gera duplicacao, descoberta ruim, nomenclatura inconsistente, multiplos pontos de entrada e falta de governanca sobre artefatos e contratos de saida.

## Objetivos de resultado

Ao final da reorganizacao, o projeto deve ter:

- um unico ponto de entrada: `node etc/scripts/sgc.cjs`
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

- Scripts estao espalhados em quatro polos independentes.
- Nao existe uma CLI raiz do projeto.
- A documentacao esta descentralizada por area.
- Os scripts de backend ja avancaram em padronizacao, mas os de frontend, QA e raiz ainda estao fora do mesmo modelo.
- Ainda existem wrappers e utilitarios em shell, PowerShell, Python, `.js`, `.mjs` e `.cjs`.
- Os nomes dos scripts foram criados em momentos diferentes e sem criterio unico de dominio.

### Problemas operacionais

- Descobrir "qual comando usar" depende de conhecer o diretorio certo.
- Nao existe um comando de `doctor` para validar ambiente.
- Nao existe um comando de `limpar` artefatos.
- Nao existe um resumo consolidado do toolkit.
- Nem todos os comandos produzem saida estruturada.
- O dashboard de QA existe, mas ainda nao faz parte do fluxo principal do toolkit.

### Problemas de manutencao

- Bibliotecas compartilhadas ainda estao restritas ao backend.
- O frontend tem scripts utilitarios que nao conversam com a CLI consolidada.
- `etc/scripts` ja possui comandos transversais importantes, mas sem governanca central.
- O conjunto inteiro ainda nao esta organizado como plataforma de automacao do projeto.

## Direcao arquitetural

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

- migrar a CLI atual de `backend/etc/scripts` para `etc/scripts/backend`
- manter a base compartilhada de cobertura
- preservar a nomenclatura padronizada atual

Itens:

- mover scripts de cobertura
- mover scripts de testes
- mover scripts Java
- mover bibliotecas de apoio
- ajustar caminhos relativos
- atualizar documentacao

### Frontend

Objetivo:

- trazer `frontend/etc/scripts` para dentro da mesma arquitetura de CLI
- padronizar nomes e ajuda

Itens previstos:

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

- integrar `etc/qa-dashboard` ao toolkit principal
- transformar o snapshot em funcionalidade oficial da CLI

Comandos previstos:

- `qa snapshot coletar --perfil rapido`
- `qa snapshot coletar --perfil backend`
- `qa resumo`
- `qa dashboard servir`

Itens:

- adaptar `etc/qa-dashboard/scripts/coletar-snapshot.mjs`
- decidir se o `dashboard-servir` sera implementado ou apenas documentado
- padronizar saida JSON do resumo
- conectar o dashboard aos demais comandos de qualidade

### Projeto

Objetivo:

- absorver scripts transversais que hoje estao em `etc/scripts`
- separar comandos publicos de scripts internos

Comandos previstos:

- `projeto doctor`
- `projeto limpar`
- `projeto setup`
- `projeto qualidade`

Itens a consolidar:

- `setup-env.*`
- `quality-check.*`
- `qa-*.sh`
- `qa-*.ps1`
- `clean-*`
- `qualidade.js`

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

Objetivo:

- mover o toolkit atual do backend para `etc/scripts/backend`

Entregas:

- scripts backend funcionando pelo novo ponto de entrada
- caminhos ajustados
- README do backend absorvido pela documentacao raiz

### Fase 3: Migracao do frontend

Objetivo:

- incorporar `frontend/etc/scripts`

Entregas:

- subcomandos `frontend`
- padronizacao de nomes
- ajuda padronizada

### Fase 4: Integracao do QA dashboard

Objetivo:

- integrar `coletar-snapshot.mjs` ao namespace `qa`

Entregas:

- `qa snapshot coletar`
- `qa resumo`
- documentacao do dashboard dentro do toolkit

### Fase 5: Consolidacao de `etc/scripts`

Objetivo:

- absorver wrappers e scripts transversais existentes

Entregas:

- `projeto doctor`
- `projeto limpar`
- `projeto setup`
- `projeto qualidade`

### Fase 6: Limpeza final

Objetivo:

- remover caminhos antigos
- eliminar documentacao duplicada
- deixar apenas a superficie final

Entregas:

- referencias antigas removidas
- wrappers legados descontinuados
- documentacao final unificada

## Funcionalidades faltantes prioritarias

### Alta prioridade

- `projeto doctor`
- `projeto limpar`
- `qa snapshot coletar` dentro da CLI unificada
- namespace `frontend`
- padrao `--json`
- README unico do toolkit

### Media prioridade

- `qa resumo`
- comandos de comparacao/baseline
- resumir qualidade consolidada em um comando unico
- servir dashboard com comando dedicado

### Baixa prioridade

- smoke tests automatizados do toolkit
- dry-run para mais comandos
- colorizacao controlada de terminal
- exportadores adicionais

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
- os contratos de ajuda e saida estiverem padronizados

## Recomendacao imediata

A proxima execucao deve iniciar pela Fase 1:

1. criar `etc/scripts/sgc.cjs`
2. criar `etc/scripts/package.json`
3. criar `etc/scripts/lib`
4. mover a CLI consolidada do backend para o namespace `backend`
5. so depois plugar frontend, QA e comandos de projeto
