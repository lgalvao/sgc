# Plano de Otimizacao de Views no Backend

Atualizado em 2026-04-05.

## Contexto

Este plano trata da otimizacao do acesso as views organizacionais complexas do Oracle no backend, sem depender de alteracoes no banco. As views envolvidas:

- `VW_UNIDADE`
- `VW_USUARIO`
- `VW_RESPONSABILIDADE`
- `VW_USUARIO_PERFIL_UNIDADE`

## O que ja foi entregue

As acoes abaixo foram implementadas e nao requerem mais atencao:

- projecao `UnidadeHierarquiaLeitura` record substituiu `listarTodasComHierarquia()` com `join fetch` triplo
- `listarTodasComHierarquia()` removido por completo — sem referencia restante no backend
- cache Caffeine em 5 pontos: arvore de unidades, mapa de hierarquia, unidade ADMIN (12h), unidades com mapa, diagnostico organizacional
- responsabilidades migradas para 3 records de projecao: `ResponsabilidadeLeitura`, `ResponsabilidadeUnidadeLeitura`, `ResponsabilidadeUnidadeResumoLeitura`
- `buscarResponsaveisUnidades()` consolidado em projecao unica via `listarResumosPorCodigosUnidade()` — sem segunda leitura em `VW_USUARIO`
- atribuicoes temporarias carregadas em lote via `carregarUsuariosPorTitulo()`
- autocomplete de usuarios migrado para `UsuarioPesquisaDto` record com `PageRequest.of(0, 20)`
- endpoints `/usuarios/{titulo}` e `/unidades/{codigo}/usuarios` migrados para `UsuarioConsultaLeitura` → `UsuarioConsultaDto.fromLeitura()`
- `@Immutable` aplicado em todas as entidades de view
- cache agressivo da unidade ADMIN com `buscarAdmin()` e TTL de 12h
- perfis por usuario migrados para `UsuarioPerfilAutorizacaoLeitura` record
- contratos HTTP e DTOs preservados
- codigo morto `UsuarioService.buscarTodos()` (JPA `findAll` em view) removido
- codigo morto `UsuarioConsultaDto.fromEntity()` (navegacao lazy herdada) removido
- refatorado `Predicate<Unidade>` na elegibilidade (uso do record `UnidadeElegibilidadeInfo`, eliminando `criarUnidadeLeve()`)

## Diretriz Principal

Views complexas devem ser tratadas como modelos de leitura, nao como entidades ricas com relacionamentos navegaveis.

- manter `@Immutable` para reforcar somente leitura
- preferir projecao ou DTO por caso de uso
- carregar apenas colunas necessarias
- mover composicao para a camada de servico
- nao usar associacoes bidirecionais entre views


## Tarefas Pendentes

### 1. Eliminar `ComumRepo` de `ResponsavelUnidadeService`

**Arquivo:** [ResponsavelUnidadeService.java](backend/src/main/java/sgc/organizacao/service/ResponsavelUnidadeService.java)

O servico depende de `ComumRepo` para 7 chamadas genericas que escondem o custo real do acesso:

| Linha | Chamada | Problema |
|---|---|---|
| 88 | `repo.buscar(Unidade.class, codUnidade)` | Carrega entidade JPA completa de `VW_UNIDADE` |
| 91 | `repo.buscar(Usuario.class, titulo)` | Carrega entidade JPA completa de `VW_USUARIO` |
| 119 | `repo.buscar(Usuario.class, ...)` | Idem |
| 137 | `repo.buscar(Usuario.class, ...)` | Idem |
| 154 | `repo.buscar(Usuario.class, ...)` | Idem |
| 155 | `repo.buscar(Usuario.class, ...)` | Idem |
| 268 | `repo.buscarPorSigla(Unidade.class, ...)` | Fallback generico |

**Acao:**

- L88: substituir por `unidadeRepo.findById()` (entidade simples necessaria para criar `AtribuicaoTemporaria`)
- L91: substituir por `usuarioRepo.buscarPorTitulo()`
- L119, 137: os metodos `buscarResponsavelAtual` e `buscarResponsabilidadeDetalhadaAtual` so precisam de `nome`, `titulo`, `matricula` do responsavel — avaliar se um record leve basta
- L154-155: `buscarResponsavelUnidade` faz 2 buscas em `VW_USUARIO` por chamada — considerar projecao enxuta
- L268: o fallback `repo.buscarPorSigla` e improvavel (a consulta por codigo acima ja cobre o caso normal) — avaliar remocao
- Remover campo `ComumRepo repo` ao final

### 2. Implementar observabilidade de queries

Nao ha nenhuma instrumentacao de tempo ou contagem de queries por request. Sem isso, nao da para:

- comprovar o ganho das otimizacoes ja feitas
- detectar regressoes futuras
- priorizar proximos alvos

**Acao minima:**

- ativar `datasource-proxy` ou `spring.jpa.properties.hibernate.generate_statistics=true` no perfil de dev
- logar `queryExecutionCount` e `queryExecutionMaxTime` por request em endpoints organizacionais
- considerar budget assertions nos testes: "este endpoint nao deve gerar mais que N queries"

**Acao opcional (fase posterior):**

- expor metricas via Micrometer (Actuator)
- separar statements que tocam `VW_*` de consultas em tabelas normais

### 3. Navegacao `getUnidadeSuperior()` fora do modulo organizacional

A associacao `Unidade.unidadeSuperior` e acessada via navegacao lazy em **varios servicos** fora do modulo organizacional:

| Arquivo | Contexto |
|---|---|
| `SubprocessoTransicaoService` (L604, 618, 790, 793) | Navegacao encadeada para subir niveis hierarquicos |
| `SubprocessoNotificacaoService` (L82, 87, 99) | Sobe hierarquia para notificar destinatarios |
| `ProcessoService` (L431, 437) | Navegacao encadeada para localizar gestor |
| `AlertaFacade` (L106, 111) | Sobe hierarquia para encontrar superior |
| `HierarquiaService` (L31, 42) | Verifica hierarquia por navegacao |
| `ProcessoDetalheDto` (L62-63) | Extrai codigo da superior para DTO |
| `UnidadeProcesso` (L81) | Similar |

Esses acessos sao navegacoes lazy sobre `VW_UNIDADE` que podem gerar queries adicionais. Muitos deles fazem loop de subida (`while (superior != null) { superior = superior.getUnidadeSuperior() }`) que gera N queries.

**Acao:** os servicos que sobem hierarquia devem usar o mapa cacheado `buscarMapaHierarquia()` (ja disponivel e cacheado no `UnidadeHierarquiaService`) ou um mapa invertido `filho → pai`. Isso eliminaria todas as queries de navegacao encadeada.

**Risco:** essa e a maior mudanca estrutural restante. Migrar por fluxo, nao por classe.

### 4. Navegacao `getUnidadeLotacao()` em `UsuarioFacade`

**Arquivo:** [UsuarioFacade.java#L150](backend/src/main/java/sgc/organizacao/UsuarioFacade.java#L150)

O metodo `toAdministradorDto` navega `usuario.getUnidadeLotacao()` para extrair `codigo` e `sigla`. O usuario vem de `buscarOpt(titulo)` que naofaz fetch join — risco de lazy loading em `VW_UNIDADE`.

**Acao:** trocar para `buscarPorTituloComUnidadeLotacao()` (que ja existe no `UsuarioRepo`) ou criar projecao leve `AdministradorLeitura`.

### 5. Reduzir associacoes JPA entre views

As associacoes abaixo ainda existem nas entidades mas a maioria ja nao e usada nos fluxos principais:

**`Responsabilidade.java`**
- `@OneToOne Unidade unidade` — usada apenas em `listarPorCodigosUnidade()` (`JOIN FETCH`). Os fluxos principais ja usam projecao.
- `@ManyToOne Usuario usuario` — acessada em `UnidadeDto.fromEntityObrigatoria()`. Os fluxos de arvore ja nao passam por aqui.

**`UsuarioPerfil.java`**
- `@ManyToOne Usuario usuario` — verificar se ha chamador que navega
- `@ManyToOne Unidade unidade` — verificar se ha chamador que navega

**`Usuario.java`**
- `@ManyToOne Unidade unidadeLotacao` — usada por varios fluxos (ver tarefa 7 e achados de navegacao). Nao pode ser removida agora.
- `@ManyToOne Unidade unidadeCompetencia` — verificar se ha chamador

**`Unidade.java`**
- `@ManyToOne Unidade unidadeSuperior` — usada extensivamente (ver tarefa 6). Nao pode ser removida agora.
- `@OneToMany List<Unidade> subunidades` — provavelmente nao usada em producao (os fluxos de arvore montam em memoria). Verificar e remover.
- `@OneToOne Responsabilidade responsabilidade` — usada em `possuiResponsavelEfetivo()` e `UnidadeDto.fromEntityObrigatoria()`. Migrar para projecao.

**Acao:** remover as associacoes comprovadamente orfas agora. As demais ficam para depois da tarefa 6.

## Ordem de Execucao Sugerida

| Prioridade | Tarefa | Risco | Esforco |
|---|---|---|---|
| 1 | Eliminar `ComumRepo` de `ResponsavelUnidadeService` (tarefa 1) | Baixo | Medio |
| 2 | Corrigir lazy de `getUnidadeLotacao()` em `UsuarioFacade` (tarefa 4) | Baixo | Minimo |
| 3 | Remover associacoes orfas (tarefa 5 — parte facil) | Baixo | Medio |
| 4 | Observabilidade de queries (tarefa 2) | Baixo | Medio |
| 5 | Migrar navegacao `getUnidadeSuperior()` para mapa cacheado (tarefa 3) | Medio | Alto |
| 6 | Remover associacoes restantes (tarefa 5 — parte dificil) | Alto | Alto |

## Riscos e Cuidados

- refactors parciais podem manter parte do custo se servicos continuarem navegando em relacionamentos antigos
- cache sem invalidacao clara pode gerar leitura desatualizada
- a navegacao encadeada de `getUnidadeSuperior()` e o ponto mais sensivel — migrar por fluxo funcional, testando cada servico antes de mover para o proximo
- testes E2E devem passar apos cada tarefa para garantir que contratos HTTP nao foram alterados

## Definicao de Sucesso

A otimizacao estara completa quando:

- nenhum servico navegar pelo grafo ORM para consultar dados de views
- todas as leituras de views usarem projecao ou DTO especifico
- a navegacao de hierarquia usar o mapa cacheado em vez de queries encadeadas
- houver instrumentacao minima para medir queries por request nos endpoints organizacionais
- `ComumRepo` nao for mais usado no modulo organizacional para acessar views
