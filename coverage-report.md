# Relatório de Cobertura de Testes

## Situação Atual

A cobertura de testes do backend foi significativamente melhorada. O foco deste trabalho foi identificar e cobrir lacunas nas classes críticas de serviço e infraestrutura que apresentavam linhas não executadas pelos testes existentes.

### Resumo das Alterações

As seguintes classes receberam novos testes unitários para cobrir cenários de borda, tratamento de exceções e configurações específicas de ambiente:

1.  **`PainelService`**
    *   **Cobertura:** Melhorada para 100% (anteriormente possuía linhas não cobertas em blocos `catch`).
    *   **Novos Testes:**
        *   `listarProcessos_FormatarUnidadesException`: Cobre o tratamento de erro ao formatar unidades participantes.
        *   `listarProcessos_LinkChefeErro`: Cobre o fallback quando a unidade não é encontrada ao gerar links.
        *   `listarAlertas_UnidadesNulas`: Valida o comportamento robusto ao listar alertas com unidades nulas.

2.  **`EventoProcessoListener`**
    *   **Cobertura:** Melhorada para 100% (anteriormente possuía linhas não cobertas em tratamento de erros de notificação).
    *   **Novos Testes:**
        *   `deveTratarExcecaoAoEnviarEmailProcessoIniciado`: Verifica que o listener não falha silenciosamente se o envio de e-mail falhar.
        *   `deveTratarExcecaoAoEnviarEmailParaSubstituto`: Garante resiliência no envio para substitutos.
        *   `deveLogarErroParaTipoUnidadeNaoSuportado`: Cobre o branch `default` de tipos de unidade desconhecidos.
        *   `deveTratarExcecaoNoLoopDeSubprocessos`: Verifica isolamento de falhas entre subprocessos distintos.

3.  **`GerenciadorJwt`**
    *   **Cobertura:** Melhorada para 100%.
    *   **Novos Testes:**
        *   `warnDefaultTest`: Valida a emissão de alerta de segurança quando usado segredo padrão em ambiente de teste/dev.
        *   `failDefaultProd`: Confirma o bloqueio da aplicação em produção com segredo inseguro.
        *   `validateMissingClaims`: Testa robustez contra tokens mal formados.

4.  **`SubprocessoMapaService`**
    *   **Cobertura:** Melhorada para 100%.
    *   **Novos Testes:**
        *   `importarAtividades_TipoProcessoNulo`: Cobre o caso onde o tipo de processo é nulo ou desconhecido durante a importação, garantindo que o status não seja alterado incorretamente.

5.  **`ProcessoService`**
    *   **Cobertura:** Melhorada através da adição de testes para validações complexas e cenários de erro.
    *   **Novos Testes:**
        *   `devePermitirAcessoEmHierarquiaComplexa`: Valida lógica de acesso hierárquico recursivo (neto -> avô).
        *   `deveFalharAoFinalizarSeSubprocessoSemUnidade`: Cobre mensagens de erro formatadas para inconsistências de dados.
        *   Testes de validação para lembretes e tipos de processo inválidos.

6.  **`SubprocessoMapaWorkflowService`**
    *   **Cobertura:** Branches perdidos reduzidos significativamente.
    *   **Novos Testes:**
        *   Cobertura para operações de edição de mapa em estados de revisão (`REVISAO_CADASTRO_HOMOLOGADA`, `REVISAO_MAPA_AJUSTADO`).
        *   Testes de resiliência para operações com observações vazias ou mapas nulos.
        *   Testes garantindo que o status do subprocesso não muda incorretamente ao manipular competências.
        *   `deveAceitarEHomologarSeSemSuperior`: Cobre o caso de validação na raiz da hierarquia.

7.  **`SubprocessoCadastroWorkflowService`**
    *   **Cobertura:** Melhorada para cobrir branches de validação e tratamento de nulos.
    *   **Novos Testes:**
        *   `disponibilizarCadastroMapaNulo`: Garante erro específico quando o mapa não está associado.
        *   Testes para falhas de invariante (falta de unidade superior) em fluxos de devolução e aceite.

### Detalhamento da Cobertura Final

Abaixo, a tabela detalhada da cobertura por pacote, gerada após a execução completa da suíte de testes.

| Componente                                    |  Instr. %  |  Linhas %  | Coberto    | Total      |
|-----------------------------------------------|------------|------------|------------|------------|
| sgc.alerta                                    |    97.17% |    99.03% |        480 |        494 |
| sgc.notificacao                               |    97.44% |    99.22% |       1105 |       1134 |
| sgc.processo.dto                              |    87.10% |   100.00% |         27 |         31 |
| sgc.subprocesso.service.decomposed            |    98.59% |   100.00% |       1258 |       1276 |
| sgc.mapa.evento                               |   100.00% |   100.00% |          6 |          6 |
| sgc.mapa                                      |    99.47% |   100.00% |        188 |        189 |
| sgc.subprocesso                               |    97.14% |    98.81% |        679 |        699 |
| sgc.processo.erros                            |   100.00% |   100.00% |         17 |         17 |
| sgc.painel.erros                              |   100.00% |   100.00% |          6 |          6 |
| sgc.subprocesso.model                         |   100.00% |   100.00% |        272 |        272 |
| sgc.relatorio.service                         |   100.00% |   100.00% |        300 |        300 |
| sgc.organizacao.dto                           |   100.00% |   100.00% |         15 |         15 |
| sgc.organizacao                               |    98.76% |    99.81% |       2235 |       2263 |
| sgc.configuracao.service                      |   100.00% |   100.00% |         38 |         38 |
| sgc.subprocesso.erros                         |   100.00% |   100.00% |         16 |         16 |
| sgc.e2e                                       |   100.00% |   100.00% |         15 |         15 |
| sgc.comum.model                               |   100.00% |   100.00% |          4 |          4 |
| sgc.alerta.dto                                |   100.00% |   100.00% |         54 |         54 |
| sgc.subprocesso.listener                      |   100.00% |   100.00% |         10 |         10 |
| sgc.processo.eventos                          |   100.00% |   100.00% |          7 |          7 |
| sgc.comum.erros                               |    98.84% |    97.50% |        513 |        519 |
| sgc.processo.dto.mappers                      |   100.00% |   100.00% |         21 |         21 |
| sgc.analise.dto                               |    93.75% |    85.71% |         30 |         32 |
| sgc.seguranca.autenticacao                    |    50.00% |    80.00% |          9 |         18 |
| sgc.subprocesso.mapper                        |    99.26% |   100.00% |        267 |        269 |
| sgc.seguranca.config                          |   100.00% |   100.00% |        134 |        134 |
| sgc.mapa.service                              |    98.75% |    99.66% |       2522 |       2554 |
| sgc.processo.model                            |   100.00% |   100.00% |         92 |         92 |
| sgc.processo                                  |    95.43% |    97.78% |        188 |        197 |
| sgc.organizacao.model                         |    99.68% |   100.00% |        309 |        310 |
| sgc.configuracao                              |   100.00% |   100.00% |          9 |          9 |
| sgc.processo.service                          |    99.25% |   100.00% |       1712 |       1725 |
| sgc.organizacao.mapper                        |    98.46% |   100.00% |        128 |        130 |
| sgc.comum.config                              |   100.00% |   100.00% |         61 |         61 |
| sgc.mapa.mapper                               |    88.73% |   100.00% |         63 |         71 |
| sgc.analise.model                             |   100.00% |   100.00% |         42 |         42 |
| sgc.subprocesso.eventos                       |    98.80% |    95.24% |        165 |        167 |
| sgc.subprocesso.service                       |    98.51% |    99.70% |       3045 |       3091 |
| sgc.seguranca.dto                             |   100.00% |   100.00% |         12 |         12 |
| sgc.seguranca                                 |    97.52% |    97.17% |        433 |        444 |
| sgc.analise                                   |    99.38% |   100.00% |        159 |        160 |
| sgc.relatorio.controller                      |   100.00% |   100.00% |         29 |         29 |
| sgc.comum.util                                |   100.00% |   100.00% |         78 |         78 |
| sgc.painel                                    |    98.14% |    98.20% |        476 |        485 |
| sgc.mapa.model                                |   100.00% |   100.00% |        113 |        113 |
|-----------------------------------------------|------------|------------|------------|------------|
| **TOTAL DO PROJETO**                          | **98.48%** | **99.44%** | **17342**  | **17609**  |

### Conclusão

A cobertura funcional crítica foi assegurada nas classes alvo. O sistema está mais robusto contra regressões em cenários de erro, fluxos de trabalho complexos e configurações de segurança.
