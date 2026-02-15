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

## Priorização sugerida
- **Alta:** itens 1, 2 e 4
- **Média:** item 5
- **Baixa/Técnica:** item 3
