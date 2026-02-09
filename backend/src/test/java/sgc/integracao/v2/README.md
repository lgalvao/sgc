# Testes de Integração V2 - SGC

## Visão Geral

Este diretório contém a **nova suíte de testes de integração** do SGC, criada para garantir **fidelidade total aos requisitos** definidos em `/etc/reqs`.

## Diferenças da Suíte Antiga

| Aspecto | Testes Antigos (`/sgc/integracao`) | Testes Novos (`/sgc/integracao/v2`) |
|---------|-------------------------------------|--------------------------------------|
| **Fonte de Verdade** | Implementação existente | Requisitos em `/etc/reqs` |
| **Abordagem** | Alguns usam mocks, atalhos | Simulação real de usuário |
| **Endpoints** | Nem sempre testam via REST | Sempre testam via controllers REST |
| **Organização** | Um arquivo por CDU | Um arquivo por CDU, organizados por módulo |
| **Independência** | Alguns dependem de `data.sql` | Totalmente independentes, dados programáticos |
| **Cobertura** | Parcial, divergiu dos requisitos | Completa, alinhada aos requisitos |

## Princípios

### 1. Fidelidade aos Requisitos
Cada teste mapeia diretamente para um CDU (Caso de Uso) e valida **exatamente** o que está especificado nos requisitos.

### 2. Fluxo Real de Usuário
- Chamar endpoints REST dos controllers
- **NÃO** usar mocks de services ou repositories  
- **NÃO** acessar repositories diretamente nos testes (exceto para setup)
- Seguir o fluxo: Controller → Service Facade → Services → Repository

### 3. Independência
- Cada teste cria seus próprios dados programaticamente
- Usa `@Transactional` para rollback automático
- Não depende de `data.sql` ou outras fixtures estáticas
- Pode executar em qualquer ordem

### 4. Cobertura Completa
- Testa todos os perfis (ADMIN, GESTOR, CHEFE, SERVIDOR)
- Valida regras de autorização e hierarquia
- Verifica efeitos colaterais (notificações, alertas, movimentações)
- Testa casos de erro e validações

## Estrutura

```
v2/
├── BaseIntegrationTestV2.java          # Classe base com métodos helper
├── autenticacao/                       # CDU-01
├── processo/                           # CDU-03, 04, 05, 06, 21
├── cadastro/                           # CDU-08, 09, 10, 11, 13, 14
├── mapa/                               # CDU-12, 15, 16, 17, 18, 19, 20
├── operacoes_bloco/                    # CDU-22, 23, 24, 25, 26
├── administracao/                      # CDU-27, 28, 30, 31, 32, 33, 34
├── painel/                             # CDU-02, 07
├── relatorios/                         # CDU-29, 35, 36
└── fluxos_completos/                   # Testes end-to-end
```

## Como Escrever um Teste

### Template Básico

```java
@DisplayName("CDU-XX: Nome do Caso de Uso")
class CDUXXNomeCasoUsoIntegrationTest extends BaseIntegrationTestV2 {
    
    private static final String API_ENDPOINT = "/api/recurso";
    
    @Nested
    @DisplayName("Cenário Principal")
    class CenarioPrincipal {
        
        @Test
        @WithMockAdmin  // ou @WithMockUser
        @DisplayName("Descrição clara do que está sendo testado")
        void testNomeDescritivo() throws Exception {
            // ARRANGE - Preparar contexto
            Unidade unidade = criarUnidadeOperacional("Nome");
            Usuario usuario = criarChefeParaUnidade(unidade);
            setupSecurityContext(usuario);
            
            String requestBody = """
                {
                    "campo": "valor"
                }
                """;
            
            // ACT - Executar ação via API REST
            ResultActions result = mockMvc.perform(
                post(API_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody));
            
            // ASSERT - Verificar resultados
            result.andExpect(status().isOk())
                  .andExpect(jsonPath("$.campo").value("valor"));
            
            // Verificar efeitos colaterais via API
            mockMvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());
        }
    }
}
```

### Métodos Helper Disponíveis

A classe `BaseIntegrationTestV2` fornece:

```java
// Criar unidades
Unidade criarUnidadeOperacional(String nome)
Unidade criarHierarquiaUnidades(String nomeRaiz, String... nomesFilhas)

// Criar processos
Processo criarProcesso(String descricao, TipoProcesso tipo, List<Unidade> unidades)

// Criar usuários
Usuario criarUsuarioComPerfil(String titulo, Unidade unidade, String... perfis)
Usuario criarChefeParaUnidade(Unidade unidade)
Usuario criarGestorParaUnidade(Unidade unidade)
Usuario criarAdmin()

// Configurar segurança
void setupSecurityContext(Usuario usuario)
void setupSecurityContext(Usuario usuario, Unidade unidade, String... perfis)
```

## Padrão AAA (Arrange-Act-Assert)

Todos os testes seguem o padrão:

1. **ARRANGE**: Criar dados de teste, configurar contexto
2. **ACT**: Executar a ação via API REST
3. **ASSERT**: Verificar resultados e efeitos colaterais

## Executar os Testes

### Todos os testes V2
```bash
./gradlew :backend:test --tests "sgc.integracao.v2.*"
```

### Um CDU específico
```bash
./gradlew :backend:test --tests "sgc.integracao.v2.processo.CDU03ManterProcessoIntegrationTest"
```

### Com tag
```bash
./gradlew :backend:test -DincludeTags=integration-v2
```

## Checklist para Novo Teste

- [ ] Ler o CDU correspondente em `/etc/reqs`
- [ ] Identificar todos os cenários (principal + alternativos)
- [ ] Criar arquivo no módulo apropriado
- [ ] Estender `BaseIntegrationTestV2`
- [ ] Adicionar tag `@DisplayName` com número e nome do CDU
- [ ] Organizar testes em `@Nested` classes por cenário
- [ ] Seguir padrão AAA (Arrange-Act-Assert)
- [ ] Testar via endpoints REST (não acessar repositories)
- [ ] Validar regras de autorização
- [ ] Verificar efeitos colaterais (alertas, notificações)
- [ ] Atualizar `integration-test-tracking.md`

## Perguntas Frequentes

### Por que não remover os testes antigos?

Os testes antigos ainda fornecem cobertura valiosa. Esta nova suíte é **complementar** e serve como fonte de verdade alinhada aos requisitos. Futuramente, podemos migrar ou consolidar.

### Por que criar dados programaticamente em vez de usar fixtures?

Fixtures estáticas (`data.sql`) criam dependências ocultas e tornam os testes frágeis. Dados programáticos garantem que cada teste seja **auto-contido** e **independente**.

### Como lidar com testes longos?

Se um teste está muito longo:
1. Dividir em múltiplos testes menores
2. Usar `@Nested` classes para agrupar cenários relacionados
3. Extrair lógica comum para métodos helper privados

### Como testar notificações/emails?

Como estamos testando sem mocks:
1. Verificar que alertas foram criados via API `/api/alertas`
2. Se houver endpoint de histórico de emails, usar para verificar
3. Para testes que não conseguem verificar emails diretamente, documentar como limitação conhecida

## Progresso

Veja `integration-test-tracking.md` no diretório raiz para acompanhar o progresso de implementação.

**Status Atual**: 2/42 CDUs implementados (4.8%)

## Contribuir

Ao adicionar novos testes:
1. Seguir os princípios e padrões documentados aqui
2. Atualizar `integration-test-tracking.md`
3. Documentar quaisquer descobertas ou divergências dos requisitos
4. Manter testes independentes e auto-contidos
