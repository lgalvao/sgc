# Plano de Execução: Melhorias Técnicas do Frontend (Fase 1)

Este plano aborda os primeiros três passos do backlog validado.
**Diretriz Crucial:** Não ficar preso aos testes legados. Testes que validam comportamento defasado, acoplamento
profundo a detalhes de implementação (como via `defineExpose`) ou que codificam situações impossíveis devem ser
reescritos ou, se necessário, apagados.

## Escopo & Impacto

Esta fase foca em remover chamadas redundantes (redução de carga na API), separar responsabilidades em composables (
melhoria de arquitetura e coesão) e forçar a rigorosidade do domínio (remoção de mitigação de nulidade indevida).

## Passos de Implementação

### Passo 1: Remover carregamento sobreposto (`MapaView`)

- **Alvo:** `frontend/src/composables/useMapaOrquestracao.ts`, `frontend/src/views/MapaView.vue`,
  `frontend/src/components/mapa/MapaSomenteLeitura.vue`, `frontend/src/services/subprocessoService.ts`.
- **Ações:**
    1. Apagar a função `obterMapaVisualizacao` de `subprocessoService.ts`.
    2. Em `useMapaOrquestracao.ts`, remover o método `carregarDadosMapaSomenteLeitura` e a variável passada por
       referência `mapaSomenteLeitura`. O hook passa a apenas garantir e expor o contexto de edição.
    3. No `MapaView.vue`, criar a computada `const mapaSomenteLeitura = computed(() => mapasStore.mapaCompleto.value);`
    4. Ajustar `MapaSomenteLeitura.vue` para tipar sua *prop* como `MapaCompleto` em vez de `MapaVisualizacao`.
    5. Ajustar os testes de `MapaView` e do service para remover a expectativa por chamadas mockadas a
       `obterMapaVisualizacao`. Testes acoplados demais serão limpos.

### Passo 2: Extrair `useMapaSugestoes`

- **Alvo:** Criação de `frontend/src/composables/useMapaSugestoes.ts` e edição de `frontend/src/views/MapaView.vue`.
- **Ações:**
    1. Mover toda a lógica de sugestões (variáveis reativas `sugestoes`, `sugestoesVisualizacao`,
       `mostrarModalSugestoes`, methods de carga, edição, visualização e submissão com o service `apresentarSugestoes`).
    2. Importar o composable em `MapaView.vue` delegando a reatividade e as ações para a View de forma transparente.
    3. Reduzir as exposições de propriedades internas via `defineExpose` e corrigir testes atrelados a essas
       implementações onde fizer sentido, limpando os que perderam validade.

### Passo 3: Remover vazamento de regra (`etapaAtual ?? 1`)

- **Alvo:** `frontend/src/services/subprocessoService.ts`.
- **Ações:**
    1. No método `mapSubprocessoDetalheResponseParaModel`, remover o fallback `?? 1` do campo `etapaAtual`. O campo deve
       refletir exatamente o que vem da API, confiando no contrato.
    2. Ajustar os testes unitários (`subprocessoService.spec.ts`) que dependam desse comportamento defensivo.

## Verificação

- A compilação TypeScript passará sem erros.
- A interface de Mapa manterá a capacidade de exibir e submeter sugestões (confirmado testando a renderização dos
  componentes).
- A bateria de testes do Vue será mantida sadia; falhas derivadas de quebra de mock de implementações internas ou
  defasadas serão resolvidas refatorando ou deletando o teste afetado.
