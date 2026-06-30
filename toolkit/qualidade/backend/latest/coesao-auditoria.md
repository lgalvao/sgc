# Auditoria de coesão do backend

Gerado em: 2026-06-01T01:29:13.797Z

> Services com 3+ categorias de responsabilidade: consulta, mutação, workflow, notificação, permissão.

## Resumo

- Analisados: 33
- Críticos (4+ categorias): 1
- Alertas (3 categorias): 0
- OK: 32

## Hotspots

| Arquivo                | Métodos | Categorias | Distribuição                                                                                    | Severidade |
|------------------------|---------|------------|-------------------------------------------------------------------------------------------------|------------|
| `ProcessoService.java` | 21      | 4          | consulta/leitura (14), mutação/escrita (3), workflow/transição (3), notificação/comunicação (1) | 🔴 crítico |

## Detalhes dos hotspots

### ProcessoService.java

- Pacote: `sgc.processo.service`
- Total de métodos públicos: 21
- Categorias detectadas: 4

**consulta/leitura** (14): `buscarPorCodigo`, `buscarPorCodigoComParticipantes`, `buscarOpt`, `listarFinalizados`,
`listarParaImportacao`, `listarAtivos`, `listarTodos`, `listarIniciadosPorParticipantes`,
`listarIniciadosPorSubprocessos`, `listarUnidadesBloqueadasPorTipo`, `buscarIdsUnidadesComProcessosAtivos`,
`listarSubprocessosElegiveis`, `obterDetalhesCompleto`, `checarAcesso`
**mutação/escrita** (3): `criar`, `atualizar`, `apagar`
**workflow/transição** (3): `iniciar`, `finalizar`, `executarAcaoEmBloco`
**notificação/comunicação** (1): `enviarLembrete`

## Por que isso importa

Um service com muitas categorias de responsabilidade:

- acumula dependências de domínios diferentes;
- torna difícil testar cada responsabilidade em isolamento;
- aumenta o risco de efeito colateral entre fluxos distintos;
- dificulta fatiamento futuro por caso de uso.

## Primeiro corte sugerido

Começar por `ProcessoService.java`. Separar os métodos por categoria e verificar quais dependências cada grupo realmente
precisa. Extrair apenas quando a fronteira representar um conceito real — não por contagem de linhas.