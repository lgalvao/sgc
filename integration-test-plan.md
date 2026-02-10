# Plano de Testes de Integração - SGC

## Objetivo

Criar uma nova suíte de testes de integração baseada nos requisitos definidos em `/etc/reqs`, simulando fluxos reais de usuários através dos controllers, sem uso de mocks ou atalhos.

## Contexto

Os testes de integração existentes em `backend/src/test/java/sgc/integracao/` divergiram dos requisitos originais ao longo do tempo. Este plano estabelece uma nova suíte de testes que serve como fonte de verdade alinhada aos requisitos.

## Documentação Relacionada

- **[Rastreamento de Progresso](integration-test-tracking.md)**: Status de implementação e próximos passos
- **[Aprendizados](integration-test-learnings.md)**: Lições aprendidas, desafios e soluções durante a implementação
- **[README dos Testes V2](backend/src/test/java/sgc/integracao/v2/README.md)**: Guia prático para escrever testes

## Princípios dos Novos Testes

### 1. Fidelidade aos Requisitos
- Cada teste deve mapear diretamente para um ou mais CDUs (Casos de Uso)
- Validar todos os fluxos principais e alternativos descritos nos requisitos
- Verificar regras de negócio explícitas e implícitas

### 2. Simulação de Fluxo Real de Usuário
- Chamar endpoints REST dos controllers
- **NÃO** usar mocks de services ou repositories
- **NÃO** usar atalhos como acesso direto a repositories
- Seguir o fluxo completo: Controller → Service Facade → Services especializados → Repository

### 3. Isolamento e Independência
- Cada teste deve ser independente e poder executar em qualquer ordem
- Usar `@Transactional` para rollback automático
- Criar dados de teste programaticamente no `@BeforeEach`
- Não depender de `data.sql` ou fixtures estáticas

### 4. Cobertura Completa
- Testar todos os perfis de usuário (ADMIN, GESTOR, CHEFE, SERVIDOR)
- Verificar regras de autorização e hierarquia
- Validar notificações, alertas e movimentações
- Testar casos de erro e validações

## Estrutura dos Testes

### Diretório
```
backend/src/test/java/sgc/integracao/v2/
├── BaseIntegrationTestV2.java          # Classe base com setup comum
├── autenticacao/
│   └── CDU01LoginIntegrationTest.java
├── processo/
│   ├── CDU03ManterProcessoIntegrationTest.java
│   ├── CDU04IniciarMapeamentoIntegrationTest.java
│   ├── CDU05IniciarRevisaoIntegrationTest.java
│   ├── CDU06DetalharProcessoIntegrationTest.java
│   └── CDU21FinalizarProcessoIntegrationTest.java
├── cadastro/
│   ├── CDU08ManterCadastroIntegrationTest.java
│   ├── CDU09DisponibilizarCadastroIntegrationTest.java
│   ├── CDU10DisponibilizarRevisaoIntegrationTest.java
│   ├── CDU11VisualizarCadastroIntegrationTest.java
│   ├── CDU13AnalisarCadastroIntegrationTest.java
│   └── CDU14AnalisarRevisaoIntegrationTest.java
├── mapa/
│   ├── CDU12VerificarImpactosIntegrationTest.java
│   ├── CDU15ManterMapaIntegrationTest.java
│   ├── CDU16AjustarMapaIntegrationTest.java
│   ├── CDU17DisponibilizarMapaIntegrationTest.java
│   ├── CDU18VisualizarMapaIntegrationTest.java
│   ├── CDU19ValidarMapaIntegrationTest.java
│   └── CDU20AnalisarValidacaoMapaIntegrationTest.java
├── operacoes_bloco/
│   ├── CDU22AceitarCadastrosBlocoIntegrationTest.java
│   ├── CDU23HomologarCadastrosBlocoIntegrationTest.java
│   ├── CDU24DisponibilizarMapasBlocoIntegrationTest.java
│   ├── CDU25AceitarValidacaoMapasBlocoIntegrationTest.java
│   └── CDU26HomologarValidacaoMapasBlocoIntegrationTest.java
├── administracao/
│   ├── CDU27AlterarDataLimiteIntegrationTest.java
│   ├── CDU28ManterAtribuicaoTemporariaIntegrationTest.java
│   ├── CDU30ManterAdministradoresIntegrationTest.java
│   ├── CDU31ConfigurarSistemaIntegrationTest.java
│   ├── CDU32ReabrirCadastroIntegrationTest.java
│   ├── CDU33ReabrirRevisaoIntegrationTest.java
│   └── CDU34EnviarLembretePrazoIntegrationTest.java
├── painel/
│   ├── CDU02VisualizarPainelIntegrationTest.java
│   └── CDU07DetalharSubprocessoIntegrationTest.java
├── relatorios/
│   ├── CDU29ConsultarHistoricoIntegrationTest.java
│   ├── CDU35GerarRelatorioAndamentoIntegrationTest.java
│   └── CDU36GerarRelatorioMapasIntegrationTest.java
└── fluxos_completos/
    ├── FluxoCompletoMapeamentoIntegrationTest.java
    ├── FluxoCompletoRevisaoIntegrationTest.java
    └── FluxoCompletoDiagnosticoIntegrationTest.java
```

## Metodologia de Teste

### Setup Padrão (BaseIntegrationTestV2)

```java
@SpringBootTest
@Transactional
@Import(TestConfig.class)
@AutoConfigureMockMvc
public abstract class BaseIntegrationTestV2 {
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    // Repositórios para setup de dados (não para atalhos nos testes)
    @Autowired
    protected ProcessoRepo processoRepo;
    @Autowired
    protected UnidadeRepo unidadeRepo;
    // ... outros repos necessários
    
    // Métodos helper para criar estruturas hierárquicas
    protected Unidade criarUnidadeComHierarquia(...) {...}
    protected Processo criarProcessoComSubprocessos(...) {...}
    protected Usuario criarUsuarioComPerfil(...) {...}
}
```

### Padrão de Teste

Cada teste deve seguir o padrão AAA (Arrange-Act-Assert):

```java
@Test
@DisplayName("CHEFE deve conseguir disponibilizar cadastro com todas as atividades preenchidas")
void testDisponibilizarCadastroCompleto() throws Exception {
    // ARRANGE - Preparar contexto
    Unidade unidade = criarUnidadeOperacional();
    Processo processo = criarProcessoMapeamento(List.of(unidade));
    Usuario chefe = criarChefeParaUnidade(unidade);
    
    // Simular login do CHEFE
    setupSecurityContext(chefe);
    
    // Criar atividades via API (como usuário faria)
    AtividadeRequest atividade1 = new AtividadeRequest(...);
    mockMvc.perform(post("/api/atividades")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(atividade1)))
        .andExpect(status().isCreated());
    
    // ACT - Executar ação
    ResultActions result = mockMvc.perform(
        post("/api/subprocessos/{id}/disponibilizar", subprocessoId)
            .contentType(MediaType.APPLICATION_JSON));
    
    // ASSERT - Verificar resultados
    result.andExpect(status().isOk())
          .andExpect(jsonPath("$.situacao").value("Cadastro disponibilizado"));
    
    // Verificar efeitos colaterais via API
    mockMvc.perform(get("/api/alertas")
        .param("unidade", unidadeSuperior.getCodigo().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.descricao =~ /.*disponibilizou.*/i)]").exists());
}
```

## Cenários de Teste por CDU

### CDU-01: Login e Estrutura de Telas
- [ ] Login com credenciais válidas - um perfil/unidade
- [ ] Login com credenciais válidas - múltiplos perfis
- [ ] Login com credenciais inválidas
- [ ] Determinação de perfil ADMIN
- [ ] Determinação de perfil GESTOR
- [ ] Determinação de perfil CHEFE
- [ ] Determinação de perfil SERVIDOR
- [ ] Exibição de barra de navegação por perfil
- [ ] Logout

### CDU-02: Visualizar Painel
- [ ] ADMIN vê todos os processos (incluindo Criado)
- [ ] GESTOR vê processos da unidade e subordinadas (exceto Criado)
- [ ] CHEFE vê processos da própria unidade (exceto Criado)
- [ ] Exibição de alertas para usuário
- [ ] Marcação de alertas como visualizados
- [ ] Ordenação de processos e alertas

### CDU-03: Manter Processo
- [ ] ADMIN cria processo de mapeamento
- [ ] ADMIN cria processo de revisão
- [ ] ADMIN cria processo de diagnóstico
- [ ] ADMIN edita processo criado
- [ ] ADMIN exclui processo criado
- [ ] Validação: descrição obrigatória
- [ ] Validação: ao menos uma unidade
- [ ] Validação: unidades sem processo ativo do mesmo tipo
- [ ] Validação: revisão/diagnóstico requer mapas existentes

### CDU-04: Iniciar Processo de Mapeamento
- [ ] ADMIN inicia processo de mapeamento
- [ ] Criação de subprocessos para todas as unidades
- [ ] Preservação da hierarquia de unidades
- [ ] Criação de mapas vazios
- [ ] Envio de notificações por email
- [ ] Criação de alertas
- [ ] Registro de movimentações

### CDU-05: Iniciar Processo de Revisão
- [ ] ADMIN inicia processo de revisão
- [ ] Cópia de mapas de competências vigentes
- [ ] Criação de subprocessos apenas para unidades com mapas
- [ ] Notificações e alertas apropriados

### CDU-06: Detalhar Processo
- [ ] ADMIN visualiza detalhes completos do processo
- [ ] GESTOR visualiza detalhes do processo
- [ ] Exibição da árvore de unidades
- [ ] Exibição de situações dos subprocessos
- [ ] Botão de finalização disponível para ADMIN
- [ ] Operações em bloco habilitadas conforme situação

### CDU-07: Detalhar Subprocesso
- [ ] CHEFE visualiza detalhes do subprocesso
- [ ] SERVIDOR visualiza detalhes do subprocesso
- [ ] Exibição de informações da unidade
- [ ] Exibição de movimentações
- [ ] Cards diferenciados por tipo de processo

### CDU-08: Manter Cadastro de Atividades e Conhecimentos
- [ ] CHEFE adiciona atividade
- [ ] CHEFE adiciona conhecimento a atividade
- [ ] CHEFE edita atividade
- [ ] CHEFE remove atividade
- [ ] CHEFE remove conhecimento
- [ ] CHEFE importa atividades de outro processo
- [ ] Validação: atividade requer ao menos um conhecimento
- [ ] Auto-save após cada ação

### CDU-09: Disponibilizar Cadastro
- [ ] CHEFE disponibiliza cadastro completo
- [ ] Validação: todas as atividades têm conhecimentos
- [ ] Bloqueio de edições após disponibilização
- [ ] Limpeza do histórico de análises
- [ ] Registro da data de conclusão da etapa 1
- [ ] Criação de movimentação
- [ ] Envio de notificações

### CDU-10: Disponibilizar Revisão
- [ ] CHEFE disponibiliza revisão de cadastro
- [ ] Validações equivalentes ao CDU-09
- [ ] Mensagens específicas para contexto de revisão

### CDU-11: Visualizar Cadastro
- [ ] Todos os perfis visualizam cadastro após disponibilização
- [ ] Exibição somente leitura
- [ ] Organização por hierarquia

### CDU-12: Verificar Impactos no Mapa
- [ ] CHEFE verifica impactos
- [ ] GESTOR verifica impactos
- [ ] ADMIN verifica impactos
- [ ] Identificação de atividades adicionadas
- [ ] Identificação de atividades removidas
- [ ] Identificação de atividades modificadas
- [ ] Vinculação com competências impactadas

### CDU-13: Analisar Cadastro
- [ ] GESTOR aceita cadastro
- [ ] GESTOR devolve cadastro com justificativa
- [ ] ADMIN homologa cadastro
- [ ] Visualização de análises anteriores
- [ ] Registro de movimentações
- [ ] Envio de notificações

### CDU-14: Analisar Revisão de Cadastro
- [ ] Fluxo similar ao CDU-13 para revisão
- [ ] Botão de verificação de impactos disponível
- [ ] Dois caminhos de homologação baseado em impactos

### CDU-15: Manter Mapa de Competências
- [ ] ADMIN cria competência
- [ ] ADMIN edita competência
- [ ] ADMIN remove competência
- [ ] ADMIN associa atividades a competências
- [ ] Validação: competência requer ao menos uma atividade
- [ ] Atualização de status para "Mapa criado"

### CDU-16: Ajustar Mapa de Competências
- [ ] ADMIN visualiza impactos
- [ ] ADMIN ajusta mapa baseado em impactos
- [ ] Validação: todas as atividades devem estar associadas

### CDU-17: Disponibilizar Mapa
- [ ] ADMIN disponibiliza mapa
- [ ] Definição de prazo limite
- [ ] Validação: todas as competências têm atividades
- [ ] Validação: todas as atividades têm competências
- [ ] Bloqueio para validação
- [ ] Limpeza de sugestões e análises

### CDU-18: Visualizar Mapa
- [ ] Todos os perfis visualizam mapa
- [ ] Exibição em blocos de competências
- [ ] Atividades e conhecimentos associados

### CDU-19: Validar Mapa
- [ ] CHEFE sugere melhorias
- [ ] CHEFE valida sem sugestões
- [ ] Rastreamento de sugestões
- [ ] Registro da data de conclusão da etapa 2
- [ ] Notificações apropriadas

### CDU-20: Analisar Validação de Mapa
- [ ] GESTOR aceita validação
- [ ] GESTOR devolve validação
- [ ] ADMIN homologa mapa
- [ ] Visualização de sugestões quando "Mapa com sugestões"

### CDU-21: Finalizar Processo
- [ ] ADMIN finaliza processo de mapeamento
- [ ] ADMIN finaliza processo de revisão
- [ ] Validação: todos os mapas homologados
- [ ] Atualização dos mapas vigentes
- [ ] Notificação de todas as unidades

### CDU-22: Aceitar Cadastros em Bloco
- [ ] GESTOR seleciona múltiplas unidades
- [ ] GESTOR aceita em bloco
- [ ] Registro de análises múltiplas
- [ ] Movimentações para próximo nível

### CDU-23: Homologar Cadastros em Bloco
- [ ] ADMIN seleciona múltiplas unidades
- [ ] ADMIN homologa em bloco
- [ ] Atualização de status de todos os subprocessos

### CDU-24: Disponibilizar Mapas em Bloco
- [ ] ADMIN valida todos os mapas
- [ ] ADMIN define prazo comum
- [ ] ADMIN disponibiliza em bloco
- [ ] Notificações agrupadas por hierarquia

### CDU-25: Aceitar Validação de Mapas em Bloco
- [ ] GESTOR aceita múltiplas validações
- [ ] Movimentações para próximo nível

### CDU-26: Homologar Validação de Mapas em Bloco
- [ ] ADMIN homologa múltiplas validações
- [ ] Atualização de todos os mapas para homologado

### CDU-27: Alterar Data Limite
- [ ] ADMIN altera prazo de subprocesso
- [ ] Notificação da unidade

### CDU-28: Manter Atribuição Temporária
- [ ] ADMIN cria atribuição temporária
- [ ] Validação de campos obrigatórios
- [ ] Notificação do usuário designado
- [ ] Criação de alerta

### CDU-29: Consultar Histórico
- [ ] ADMIN consulta processos finalizados
- [ ] GESTOR consulta processos finalizados
- [ ] CHEFE consulta processos finalizados
- [ ] Agregação de unidades por subárvore completa

### CDU-30: Manter Administradores
- [ ] ADMIN adiciona administrador
- [ ] ADMIN remove administrador
- [ ] Validação: não remover a si mesmo
- [ ] Validação: não remover único admin
- [ ] Listagem de administradores

### CDU-31: Configurar Sistema
- [ ] ADMIN atualiza dias de inativação
- [ ] ADMIN atualiza dias de novidade de alertas
- [ ] Validação: valores >= 1
- [ ] Efeito imediato nas configurações

### CDU-32: Reabrir Cadastro
- [ ] ADMIN reabre cadastro com justificativa
- [ ] Mudança de status para "em andamento"
- [ ] Limpeza de data de conclusão
- [ ] Notificações a todos os níveis

### CDU-33: Reabrir Revisão
- [ ] ADMIN reabre revisão com justificativa
- [ ] Similar ao CDU-32 para contexto de revisão

### CDU-34: Enviar Lembrete de Prazo
- [ ] ADMIN seleciona unidades com pendências
- [ ] Confirmação do template de email
- [ ] Envio de lembretes
- [ ] Indicação de unidades próximas/vencidas

### CDU-35: Gerar Relatório de Andamento
- [ ] ADMIN gera relatório de processo
- [ ] Exibição de informações de subprocessos
- [ ] Exportação em PDF

### CDU-36: Gerar Relatório de Mapas
- [ ] ADMIN gera relatório consolidado
- [ ] Filtro opcional por unidades
- [ ] Estrutura hierárquica
- [ ] Exportação em PDF

### Fluxos Completos
- [ ] Fluxo completo de mapeamento (início ao fim)
- [ ] Fluxo completo de revisão (início ao fim)
- [ ] Fluxo completo de diagnóstico (início ao fim)

## Estimativa de Esforço

- **Total de CDUs**: 36
- **Cenários de teste estimados**: ~250
- **Fluxos completos**: 3
- **Tempo estimado por teste**: 30-60 minutos
- **Esforço total**: 150-250 horas (3-6 semanas com 1 desenvolvedor)

## Critérios de Sucesso

1. ✅ Todos os 36 CDUs têm testes correspondentes
2. ✅ Testes cobrem fluxos principais e alternativos
3. ✅ Testes validam regras de negócio e autorizações
4. ✅ Testes não usam mocks de services/repositories
5. ✅ Testes são independentes e podem executar em qualquer ordem
6. ✅ Cobertura de código >= 80% para controllers e facades
7. ✅ Todos os testes passam consistentemente
8. ✅ Documentação clara de cada cenário testado

## Próximos Passos

1. Criar estrutura de diretórios
2. Implementar `BaseIntegrationTestV2`
3. Começar com CDUs críticos (CDU-01, 03, 04, 08, 15)
4. Expandir gradualmente para todos os CDUs
5. Implementar fluxos completos
6. Revisar e refatorar testes
7. Documentar descobertas e divergências dos requisitos
