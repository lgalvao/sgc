# Plano de Simplificação do Backend SGC

> Sistema de intranet, 5-10 usuários simultâneos. Objetivo: remover abstrações desnecessárias sem perder funcionalidade.

---

## 1. Eliminar Eventos Assíncronos → Chamadas Diretas

**Por quê:** Async complica debugging, testes e tratamento de erros. Desnecessário para 5-10 usuários.

### 1a. EventoTransicaoSubprocesso → chamada direta

- `SubprocessoComunicacaoListener.handle()` faz duas coisas: cria alerta e envia e-mail.
- **Ação:** Mover essa lógica para dentro de `SubprocessoTransicaoService.registrar()`, chamando `AlertaFacade` e `SubprocessoEmailService` diretamente.
- Remover: `SubprocessoComunicacaoListener.java`, `EventoTransicaoSubprocesso.java`, `subprocesso/eventos/TipoTransicao.java` (se TipoTransicao só é usado pelo evento, mantê-lo como enum auxiliar do service).
- No `SubprocessoTransicaoService.registrar()`: substituir `eventPublisher.publishEvent(evento)` por chamadas diretas ao alerta/email.

### 1b. EventoProcessoIniciado / EventoProcessoFinalizado → chamada direta

- `EventoProcessoListener` processa início e finalização (alertas + e-mails).
- **Ação:** Mover a lógica de `EventoProcessoListener.aoIniciarProcesso()` para `ProcessoFacade.iniciarProcesso()` (ou `ProcessoInicializador`), chamando diretamente.
- Mover `aoFinalizarProcesso()` para `ProcessoFacade.finalizar()` (ou `ProcessoFinalizador`).
- Remover: `EventoProcessoListener.java`, `EventoProcessoIniciado.java`, `EventoProcessoFinalizado.java`.
- Manter try/catch + log.error para falhas de e-mail (como já existe no listener).

### 1c. EventoMapaAlterado → chamada direta

- `SubprocessoMapaListener` escuta este evento.
- **Ação:** Identificar quem publica `EventoMapaAlterado` e substituir por chamada direta ao método do listener.
- Remover: `EventoMapaAlterado.java`, `SubprocessoMapaListener.java`.

### 1d. EventoImportacaoAtividades → chamada direta

- `MapaImportacaoListener` escuta este evento.
- **Ação:** Identificar quem publica e substituir por chamada direta.
- Remover: `EventoImportacaoAtividades.java`, `MapaImportacaoListener.java`.

### 1e. Limpar infraestrutura async

- Remover `@EnableAsync` da configuração (se não houver mais nenhum uso de `@Async`).
- Remover pacotes `eventos/` e `listener/` vazios.

---

## 2. Eliminar Facades Pass-Through

**Por quê:** Facades que apenas delegam adicionam indireção sem valor.

### Critério

Manter Facade **somente** se ela orquestra 2+ services numa mesma operação. Se apenas delega 1:1, eliminar.

### Ação para cada Facade

| Facade | Métodos | Decisão |
|--------|---------|---------|
| `SubprocessoFacade` | 62 | **Manter** — orquestra crud, workflow, query, atividade, contexto |
| `ProcessoFacade` | ~25 | **Manter** — orquestra consulta, manutenção, inicialização, finalização |
| `MapaFacade` | ? | Avaliar — se só delega, eliminar |
| `AtividadeFacade` | ? | Avaliar — se só delega, eliminar |
| `AlertaFacade` | ? | Avaliar — se só delega, eliminar |
| `AnaliseFacade` | ? | Avaliar — se só delega, eliminar |
| `OrganizacaoFacade` | ? | Avaliar — se só delega, eliminar |
| `UnidadeFacade` | ? | Avaliar — se só delega, eliminar |
| `UsuarioFacade` | ? | Avaliar — se só delega, eliminar |
| `PainelFacade` | ? | Avaliar — se só delega, eliminar |
| `LoginFacade` | ? | Avaliar — se só delega, eliminar |
| `RelatorioFacade` | ? | Avaliar — se só delega, eliminar |

**Procedimento para eliminar uma Facade:**
1. Verificar se todos os métodos delegam 1:1 para um único service.
2. Nos controllers/consumers que usam a Facade, injetar o service diretamente.
3. Remover a Facade.

---

## 3. Achatar Sub-Pacotes de `subprocesso/service/`

**Por quê:** 6 sub-pacotes para 15 classes é fragmentação excessiva.

**Ação:** Mover todas as classes de `crud/`, `factory/`, `notificacao/`, `query/`, `workflow/` para `subprocesso/service/`. Ajustar imports.

**Estrutura final:**
```
subprocesso/service/
  SubprocessoFacade.java
  SubprocessoCrudService.java
  SubprocessoValidacaoService.java
  SubprocessoFactory.java
  SubprocessoEmailService.java
  ConsultasSubprocessoService.java
  SubprocessoCadastroWorkflowService.java
  SubprocessoMapaWorkflowService.java
  SubprocessoAdminWorkflowService.java
  SubprocessoTransicaoService.java
  SubprocessoAjusteMapaService.java
  SubprocessoAtividadeService.java
  SubprocessoContextoService.java
  SubprocessoPermissaoCalculator.java
```

---

## 4. Consolidar Classes de Erro

**Por quê:** 19 exceções customizadas é excessivo.

**De → Para:**

| Manter | Absorve |
|--------|---------|
| `ErroNegocio` | `ErroNegocioBase` (tornar `ErroNegocio` a base) |
| `ErroValidacao` | (já existe) |
| `ErroEntidadeNaoEncontrada` | (já existe) |
| `ErroAcessoNegado` | `ErroAutenticacao` (usar HTTP 401 vs 403 no handler) |
| `ErroInterno` | `ErroConfiguracao` (erro de config é erro interno) |
| `ErroApi` | Manter se for usado para respostas REST padronizadas |

**Remover:** `ErroNegocioBase`, `ErroAutenticacao`, `ErroConfiguracao`, `ErroSubApi` (avaliar uso antes).

---

## Ordem de Execução

1. **Etapa 1 — Eventos** (maior impacto, menor risco se feito com cuidado)
2. **Etapa 2 — Facades** (avaliar cada uma, eliminar as pass-through)
3. **Etapa 3 — Sub-pacotes** (refatoração mecânica, baixo risco)
4. **Etapa 4 — Erros** (menor prioridade, pode quebrar muitos testes)

> Após cada etapa: rodar `./gradlew test` para garantir que nada quebrou.
