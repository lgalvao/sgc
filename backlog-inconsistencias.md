# Backlog de Inconsistências - SGC

## Objetivo
Consolidar inconsistências identificadas durante a execução do plano de melhorias de UX para correção incremental.

## Itens mapeados

1. **Nomenclatura divergente entre plano e código real**
   - Exemplos: referências a `ModalMapaDisponibilizar` e `ModalRelatorioAndamento` enquanto os arquivos reais eram `DisponibilizarMapaModal.vue` e `ModalAndamentoGeral.vue`.
   - Impacto: aumenta risco de execução incorreta por agentes e humanos.
   - Ação sugerida: normalizar nomes na documentação e concluir refatoração UX-010.

2. **Trechos de plano com caminhos de arquivos desatualizados**
   - Exemplo histórico: `assets/styles/_tokens.scss` vs implementação alinhada em `assets/css/tokens.css`.
   - Impacto: retrabalho e instruções conflitantes.
   - Ação sugerida: revisão de consistência em todo ciclo de atualização do plano/tracking.

3. **Uso de tipagens frouxas em pontos específicos de componentes**
   - Exemplo: refs de componentes com `any` por limitação de inferência de tipos de componentes genéricos do BootstrapVueNext.
   - Impacto: reduz segurança de tipos em partes localizadas.
   - Ação sugerida: avaliar criação de wrappers tipados para componentes genéricos ou helper de tipagem.

4. **Cobertura de validação inline ainda parcial no frontend**
   - Escopo atual: aplicado em `ProcessoFormFields` e `CadAtividadeForm`.
   - Impacto: experiência de validação ainda heterogênea em formulários não revisados.
   - Ação sugerida: executar o item `ux-002-formularios-restantes` antes de iniciar novos blocos amplos de UX.

5. **Dependência de documentação para refletir estado real de execução**
   - Tracking e plano precisam de sincronização frequente após cada ciclo de implementação.
   - Impacto: risco de leitura de status defasado.
   - Ação sugerida: checklist obrigatório de atualização de `ux-improvement-tracking.md` e `plan.md` ao final de cada entrega.

6. **Nomenclatura inconsistente de partes de views sem sufixo `Section`**
   - Evidência: componentes/seções equivalentes com nomes mistos (com e sem `Section`).
   - Impacto: reduz previsibilidade de navegação e manutenção.
   - Ação sugerida: auditoria de nomenclatura e correção incremental no escopo do UX-010.

7. **Diretório `components` com organização e nomes inconsistentes**
   - Evidência: componente com sufixo de view (`TreeTableView.vue`) dentro de `components` e mistura de domínios sem agrupamento consistente.
   - Impacto: dificulta descoberta/reuso e aumenta custo de manutenção.
   - Ação sugerida: executar UX-011 com auditoria, reorganização por domínio/camada e migração de imports.

## Priorização sugerida
- **Alta:** itens 1, 2, 4 e 6
- **Média:** item 5
- **Baixa/Técnica:** item 3

## Andamento de tratamento

- ✅ **Item 1 (parcial):** documentação de tracking alinhada com nomes reais de arquivos atuais; renomeações formais permanecem no escopo do UX-010.
- ✅ **Item 2 (parcial):** caminhos principais de tokens e referências de UX já sincronizados no plano/tracking.
- ✅ **Item 4 (parcial):** formulários mapeados em UX-002 revisados com validação inline e foco no primeiro erro.
- 🔄 **Item 3 (pendente):** manter acompanhamento de tipagens frouxas para redução incremental.
- 🔄 **Item 5 (contínuo):** manter atualização de `ux-improvement-tracking.md` e `plan.md` a cada ciclo e incluir execução periódica de `npm run test:e2e:captura` como auditoria visual de integração.
- ✅ **Item 6:** padronização de seções concluída no escopo UX-010.
- ✅ **Item 7:** UX-011 concluído com renomeação de `TreeTableView.vue` para `TreeTable.vue`, unificação de `common` em `comum` e reorganização completa por domínio/camada (`processo`, `mapa`, `unidade`, `atividades`, `layout`, `comum`), eliminando `.vue` residuais na raiz de `components` (com captura E2E periódica validada em 18/18).
- ✅ **Item 8:** confusão entre `layout`, `comum` e `ui` tratada no UX-012 com unificação de `ui` em `comum` (`LoadingButton`) e atualização da documentação de componentes.
