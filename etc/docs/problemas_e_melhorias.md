# Problemas Identificados e Sugestões de Melhorias — Catálogo de Endpoints SGC

Este documento consolida os problemas de segurança, consistência, redundância e performance identificados durante a análise do **Catálogo de Endpoints do SGC** (Sistema de Gestão de Competências), acompanhados das respectivas recomendações e sugestões de melhorias arquiteturais.

---

## 🔍 1. Problemas e Melhorias Identificadas nos Endpoints

Abaixo estão listados os problemas específicos encontrados nos controllers da API REST do SGC, ordenados por severidade.

> [!IMPORTANT]
> Em conformidade com a convenção de nomenclatura do SGC, todas as chaves primárias e referências a identificadores foram descritas utilizando o termo `codigo` (em vez de `id`).

| # | Severidade | Endpoint(s) Afetado(s) | Descrição do Problema | Recomendação / Sugestão de Melhoria |
|---|------------|------------------------|-----------------------|------------------------------------|
| 1 | **Média** | `GET /api/processos/{codigo}/unidades-importacao` | Restrito a perfis `CHEFE` e `ADMIN`. O perfil `GESTOR` permanece bloqueado para buscar unidades de importação. | Avaliar com a equipe de negócios se o perfil `GESTOR` deve ter acesso a esse endpoint para acompanhar e auditar o histórico de importações de unidades. |
| 2 | **Média** | `POST /api/usuarios/logout` | **Ausência de invalidação server-side do JWT.** Como não há uma *blacklist* de tokens, o JWT continua ativo e tecnicamente válido no servidor até expirar pelo seu tempo de vida padrão (24h). | Implementar um mecanismo de *blacklist* de tokens revogados utilizando um cache compartilhado (como **Redis**), ou reduzir o tempo de expiração do JWT de acesso utilizando a estratégia de *Refresh Tokens*. |
| 3 | **Média** | `GET /api/usuarios/{titulo}` | **Exposição de dados sensíveis.** O endpoint expõe dados detalhados do usuário (nome, matrícula, unidade) para qualquer usuário autenticado no sistema, independentemente do perfil. | Restringir o acesso a este endpoint apenas para perfis com permissões elevadas (`ADMIN`, `GESTOR`, `CHEFE`), ou limitar os campos retornados para servidores normais. |
| 4 | **Baixa** | `GET /api/mapas` | **Falta de paginação.** O endpoint retorna a lista completa de todos os mapas cadastrados. Com o crescimento do banco de dados, isso causará problemas graves de performance e consumo de memória. | Adicionar suporte à paginação (parâmetros `page`, `size` e `sort`) e filtros básicos de busca. |
| 5 | **Baixa** | `GET /api/processos/finalizados`<br>`GET /api/processos/para-importacao` | **Duplicação de endpoints semelhantes.** O endpoint `/para-importacao` é apenas um subconjunto de `/finalizados` filtrando por processos de tipo `MAPEAMENTO`. | Considerar a unificação dos endpoints na rota `/api/processos/finalizados` aceitando um parâmetro de consulta para filtragem, por exemplo: `?elegivelImportacao=true`. |
| 6 | **Baixa** | `GET /api/unidades/{codigo}/mapa-vigente` | **Redundância de dados.** O endpoint retorna apenas um booleano (`{"temMapaVigente": true/false}`), sendo que o endpoint `/api/unidades/{codigo}/mapa-vigente/referencia` já traz essa informação acompanhada do código e data da referência. | Avaliar se existem consumidores que utilizam exclusivamente o retorno booleano simples. Caso contrário, descontinuar e remover este endpoint redundante para simplificar a API. |
| 7 | **Baixa** | `POST /api/usuarios/login` | **Redundância na resposta.** O campo booleano `requerSelecaoPerfil` retornado na resposta do primeiro passo do login é redundante, pois essa condição já é inferida logicamente quando o objeto `sessao` é nulo. | Simplificar o payload de resposta do login, eliminando o campo redundante. |
| 8 | **Baixa** | `POST /api/subprocessos/aceitar-cadastro-bloco`<br>`POST /api/subprocessos/homologar-cadastro-bloco`<br>`POST /api/subprocessos/disponibilizar-mapa-bloco`<br>`POST /api/subprocessos/aceitar-validacao-bloco`<br>`POST /api/subprocessos/homologar-validacao-bloco` | **Duplicação de regras de permissão.** Os endpoints que realizam ações em lote (bloco) duplicam manualmente a lógica de validação de permissões que já é feita nos endpoints individuais de cada subprocesso. | Avaliar a criação de um padrão de design ou interceptador genérico para processamento em lote que reutilize a lógica de verificação granular dos endpoints individuais. |

---

## 🛠 2. Pontos de Atenção e Segurança (Ambientes Isolados)

> [!WARNING]
> Os endpoints descritos abaixo realizam ações destrutivas ou de bypass de segurança necessárias para testes automatizados e homologação rápida. É de **extrema importância** garantir que os perfis correspondentes nunca sejam expostos em ambiente produtivo.

### A. Exclusão em Cascata (`POST /api/processos/{codigo}/excluir-completo`)
* **Uso:** Exclui um processo e todos os seus dependentes (subprocessos, mapas, atividades, análises, alertas, notificações).
* **Mitigação Atual:** Restrito ao perfil de Spring `hom` (Homologação).
* **Recomendação:** Assegurar em pipeline de CI/CD que o profile `hom` seja desabilitado por padrão nas builds de produção.

### B. Fixtures e Resets E2E (`/api/e2e/**`)
* **Uso:** Permite resetar o banco de dados para o estado do `seed.sql` (`POST /api/e2e/reset`) e criar dados falsos (fixtures) diretamente. Realiza operações no banco por meio de JDBC ignorando regras normais de segurança (`executeAsAdmin()`).
* **Mitigação Atual:** Restrito ao perfil de Spring `e2e` (Testes End-to-End).
* **Recomendação:** Garantir por meio de configurações ambientais e automação que o profile `e2e` sob nenhuma circunstância possa ser ativado fora do pipeline local ou de integração contínua (CI).

---

## 🚀 3. Oportunidades de Evolução Arquitetural

Com base no catálogo de endpoints analisado, identificaram-se as seguintes oportunidades para modernização e otimização do sistema:

### 1. Expansão do SSE (Server-Sent Events)
* **Cenário Atual:** O sistema possui o endpoint `/api/eventos` para assinar eventos SSE, mas atualmente transmite apenas o sinal `org-cache-refreshed` para limpar/recarregar caches organizacionais no Vue.
* **Oportunidade:** Utilizar esse canal estabelecido para enviar atualizações de status de subprocessos em tempo real e notificações/alertas *in-app* instantâneos, reduzindo o tráfego de rede gerado por polling de status no frontend.

### 2. Endpoint de Inicialização Global (BFF Geral)
* **Cenário Atual:** Ao abrir o sistema, o frontend faz 3 a 4 chamadas de rede paralelas para obter configurações, árvore organizacional de unidades, dados do usuário e contadores iniciais.
* **Oportunidade:** Assim como existe um excelente padrão BFF no `/api/painel/bootstrap`, criar um endpoint global de inicialização (ex: `/api/bootstrap-app`) que consolide as configurações do sistema, dados do usuário e metadados comuns em uma única requisição.

### 3. Versionamento da API REST
* **Cenário Atual:** A API do SGC não adota nenhum controle de versão explícito nos caminhos de URI (ex: `/api/v1/`).
* **Oportunidade:** Implementar versionamento de API para facilitar futuras refatorações estruturais ou quebras de contrato de payload (breaking changes) sem prejudicar a comunicação com o frontend ou possíveis integrações externas.

### 4. Padronização Sistemática de Paginação
* **Cenário Atual:** Alguns endpoints oferecem paginação robusta via Spring Data, enquanto outros que também consultam listas dinâmicas que tendem a crescer (como mapas, configurações, feedbacks, notificações e logs) retornam listas cheias.
* **Oportunidade:** Definir um padrão corporativo na arquitetura do backend para que qualquer listagem dinâmica de dados utilize a abstração `Pageable` do Spring de forma homogênea.
