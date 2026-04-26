# Tracking de Refatoração e Estabilização

## Status Geral
Fase 1 (Unificação Visual/Acessibilidade) concluída. Fase 2 em andamento com base já implantada no runtime do frontend: `useSubprocessoStore` virou o centro do contexto de subprocesso, `useFluxoSubprocesso` e `useInvalidacaoNavegacao` foram alinhados ao store, `npm run typecheck` voltou a passar e os restos de `useSubprocessos` ficaram restritos a testes legados.

---

## P0: Estabilização e Desempenho Crítico (Frontend)

### Gerenciamento de Requisições e Cache
- [x] **Base de Deduplication**: `frontend/src/stores/subprocesso.ts` já unifica requisições concorrentes por chave de edição/cadastro e mantém cache curto de contexto.
- [x] **Consolidação do Runtime**: `SubprocessoView`, `CadastroView`, `MapaView`, `useFluxoSubprocesso`, `SubprocessoCards` e `useInvalidacaoNavegacao` já deixaram de depender de `useSubprocessos`.
- [ ] **Auditoria de Requests**: Mapear onde `SubprocessoView`, `CadastroView` e `MapaView` ainda forçam recarga completa, resolução redundante por processo/unidade ou atualização integral após mutação local.
- [ ] **Invalidação Seletiva**: Reduzir a abrangência de `useInvalidacaoNavegacao`, que ainda invalida stores de painel/processo/subprocesso em conjunto em diversos fluxos.
- [ ] **Cancelamento Escopado**: Aproveitar a infraestrutura global de `AbortController` do `axios-setup` para cancelamento por rota/subprocesso; hoje o tratamento de cancelamento aparece no diagnóstico do contexto, mas não está consolidado ponta a ponta nos fluxos de subprocesso.
- [ ] **Remover Fachada Legada**: Migrar os testes restantes e apagar `frontend/src/composables/useSubprocessos.ts`.

### Suíte E2E e Alertas
- [ ] **Regressão Ampla**: Executar a suíte E2E completa configurada no repositório como gate de saída das mudanças de navegação/cache.
- [ ] **Limpeza de Alertas**: Confirmar estabilidade dos testes CDU-32/33 após alinhamento da RN-09.07.

---

## P1: Racionalização Estrutural (Hotspots)

### Backend: Quebra de God Classes
- [x] **`SubprocessoConsultaService`**: cálculo de contexto/permissões/visualização já foi parcialmente extraído para `SubprocessoContextoConsultaService`, `SubprocessoAcessoService` e `SubprocessoVisualizacaoService`.
- [ ] **`SubprocessoConsultaService`**: consolidar a fachada remanescente, remover dependências mantidas apenas por compatibilidade legada/reflexão em testes e fechar o recorte de responsabilidades.
- [ ] **`ProcessoService`**: Isolar responsabilidades de criação e lógica de finalização/bloqueio.
- [ ] **`SubprocessoTransicaoService`**: Isolar lógicas de movimentação específicas.

### Frontend: Modularização
- [x] **Modularização Parcial**: `MapaView.vue` já usa composables específicos, componentes dedicados e `defineAsyncComponent` para modais pesados.
- [x] **Modularização Parcial**: `CadastroView.vue` já delega partes do fluxo para composables e componentes, mas ainda concentra orquestração extensa de estado e workflow.
- [x] **Store de Subprocesso Consolidado**: contexto de edição/cadastro, erro de integração, dedupe e sincronização local foram absorvidos por `stores/subprocesso`.
- [ ] **Modularização de Views**: Extrair o bootstrap de contexto, a orquestração de modais e os fluxos de validação/aceite/devolução para reduzir o tamanho e o acoplamento local das views.

### Remoção de Código Defensivo
- [ ] **Limpeza de DTOs**: Reduzir a nulabilidade estrutural em `UnidadeDto`, `ProcessoDetalheDto` e DTOs correlatos onde o contrato já é estável; baseline atual do backend ainda gira em torno de ~285 usos de `@Nullable`.
- [ ] **Simplificação Frontend**: Eliminar guards redundantes somente onde o contrato backend/store já provou estabilidade em produção e testes.

---

## P2: Dívida Técnica e UX

### Qualidade e Tipagem
- [ ] **Validação Mobile**: Conferir capturas de tela (375px) para resiliência visual.
- [~] **Tipagem**: o runtime do frontend já está alinhado ao store novo; o trabalho remanescente está concentrado em specs antigos e helpers compartilhados de teste/E2E.
- [ ] **QA Dashboard**: Recuperar status verde para cobertura de branches.
