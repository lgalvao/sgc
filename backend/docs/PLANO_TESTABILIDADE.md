# Plano de Melhoria de Testabilidade - SGC Backend

**Data:** 2026-01-06  
**Baseado em:** Análise do BACKLOG_TESTABILIDADE.md e cobertura atual

---

## Métricas Atuais

| Métrica | Valor | Meta |
|---------|-------|------|
| Cobertura de Linhas | ~99% | 100% ❌ |
| Cobertura de Branches | ~88% | 90% ❌ |
| Total de Testes | 1026+ | - |
| Linhas Perdidas | ~44 (Identificadas) | - |

---

## 📊 Linhas Perdidas Identificadas (Prioridade Imediata)

| Arquivo | Linhas | Contexto |
|---------|--------|----------|
| `sgc.organizacao.UsuarioService` | 353, 357 | Possíveis falhas em buscas ou validações de segurança |
| `sgc.painel.PainelService` | 88, 235 | Filtros de dashboard ou tratamento de erro |
| `sgc.notificacao.NotificacaoEmailAsyncExecutor` | 72, 73, 74, 78 | Logs de erro e retentativas em caso de exceção |
| `sgc.relatorio.service.RelatorioService` | 61, 62, 109, 110 | Tratamento de erro ao gerar PDF (catch blocks) |
| `sgc.notificacao.EventoProcessoListener` | 123, 124 | Exceções ao enviar e-mails em loop |
| `sgc.subprocesso.service.SubprocessoMapaService` | 167 | Validação específica ou branch raro |
| `sgc.subprocesso.SubprocessoCadastroController` | 326 | Tratamento de erro ou validação |
| `sgc.subprocesso.SubprocessoCrudController` | 35, 36, 50 | Endpoints menores não testados |
| `sgc.subprocesso.service.SubprocessoFactory` | 133, 134 | Tratamento de erro na criação de subprocessos |
| `sgc.subprocesso.service.SubprocessoCadastroWorkflowService` | 203 | Condição de borda em workflow |
| `sgc.organizacao.ValidadorDadosOrganizacionais` | 118 | Validação de dados inválidos |
| `sgc.seguranca.GerenciadorJwt` | 84, 85 | Validação de ambiente de produção |
| `sgc.alerta.AlertaController` | 25, 26 | Construtor ou método utilitário |
| `sgc.comum.erros.ErroInterno` | 54, 55 | Construtor secundário |
| `sgc.seguranca.FiltroAutenticacaoMock` | 56 | Log de erro ou condição de filtro |
| `sgc.alerta.AlertaService` | 187 | Validação de alerta não encontrado |
| `sgc.mapa.service.AtividadeFacade` | 39 | Método não coberto |
| `sgc.mapa.service.AnalisadorCompetenciasService` | 145 | Branch complexo de análise |
| `sgc.processo.ProcessoController` | 174 | Endpoint de busca ou validação |

---

## 🎯 Plano de Execução

1. **UsuarioService & PainelService**: Criar testes unitários focados nas linhas específicas (provavelmente `catch` blocks ou validações de `null`).
2. **Notificacao & Relatorio**: Garantir cobertura dos blocos `catch` através de mocks que lançam exceções.
3. **Controladores**: Adicionar testes `@WebMvcTest` ou unitários para os endpoints faltantes.
4. **Classes Utilitárias e Erros**: Criar testes simples para construtores ou métodos estáticos não utilizados.
5. **Limpeza**: Se algum código for inalcançável ou inútil, remover.

---

### Comandos para Verificação

```bash
# Executar testes e gerar relatório
cd /app && ./gradlew :backend:test :backend:jacocoTestReport > test_output.log 2>&1

# Verificar linhas perdidas detalhadas
cd /app/backend && python3 scripts/list_missed_lines.py
```
