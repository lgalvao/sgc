# Diagramas de Arquitetura SGC

**Data:** 2026-01-15  
**Versão:** 1.0

---

## 1. Dependências Entre Módulos (Atual)

```mermaid
graph TB
    subprocesso[subprocesso<br/>76 arquivos<br/>~6.100 linhas]
    processo[processo<br/>35 arquivos]
    mapa[mapa<br/>48 arquivos]
    organizacao[organizacao<br/>35 arquivos]
    seguranca[seguranca<br/>31 arquivos]
    analise[analise<br/>12 arquivos]
    notificacao[notificacao<br/>10 arquivos]
    comum[comum<br/>25 arquivos]
    
    %% Dependências de subprocesso
    subprocesso -->|56 imports| mapa
    subprocesso -->|43 imports| organizacao
    subprocesso -->|22 imports| analise
    subprocesso -->|7 imports| processo
    subprocesso -->|18 imports| comum
    
    %% Dependências de processo
    processo -->|20 imports| organizacao
    processo -->|17 imports| subprocesso
    processo -->|10 imports| comum
    
    %% Dependências de mapa
    mapa -->|11 imports| subprocesso
    mapa -->|7 imports| organizacao
    mapa -->|14 imports| comum
    
    %% Dependências de organizacao
    organizacao -->|7 imports| comum
    organizacao -->|2 imports| processo
    
    %% Outros módulos
    analise --> subprocesso
    notificacao --> subprocesso
    
    %% Estilo
    style subprocesso fill:#ff6b6b,stroke:#c92a2a,color:#fff
    style mapa fill:#fab005,stroke:#f08c00,color:#000
    style processo fill:#51cf66,stroke:#2f9e44,color:#000
    classDef normal fill:#339af0,stroke:#1c7ed6,color:#fff
    class organizacao,seguranca,analise,notificacao,comum normal
```

**Legenda:**
- 🔴 Vermelho: Módulo mais central (Subprocesso)
- 🟡 Amarelo: Módulo com acoplamento significativo (Mapa)
- 🟢 Verde: Módulo orquestrador (Processo)
- 🔵 Azul: Módulos de suporte

**Observação:** Subprocesso é o módulo mais dependido (59 arquivos importam dele).

---

## 2. Hierarquia de Agregados (DDD)

```mermaid
graph TB
    subgraph "Agregado PROCESSO"
        P[Processo<br/>Tipo: Mapeamento/Revisão/Diagnóstico<br/>Estados: Criado → Em andamento → Finalizado]
    end
    
    subgraph "Agregado SUBPROCESSO ⭐ (Raiz)"
        SP[Subprocesso<br/>9 estados de workflow<br/>Conecta tudo]
        SP_CAD[Cadastro de Atividades]
        SP_MAPA[Referência ao Mapa]
        SP_MOV[Movimentações]
        
        SP --> SP_CAD
        SP --> SP_MAPA
        SP --> SP_MOV
    end
    
    subgraph "Agregado MAPA"
        M[Mapa de Competências]
        M_COMP[Competências]
        M_ATIV[Atividades do Mapa]
        
        M --> M_COMP
        M --> M_ATIV
    end
    
    subgraph "Agregado UNIDADE"
        U[Unidade Organizacional]
        U_RESP[Responsáveis]
        U_SERV[Servidores]
        
        U --> U_RESP
        U --> U_SERV
    end
    
    subgraph "Entidades Independentes"
        A[Atividade]
        C[Conhecimento]
        COMP[Competência]
    end
    
    %% Relações entre agregados
    P -.1:N.-> SP
    SP -.N:1.-> U
    SP -.1:1.-> M
    SP -.1:N.-> A
    M -.N:M.-> COMP
    COMP -.N:M.-> A
    A -.N:M.-> C
    
    %% Estilo
    style SP fill:#ff6b6b,stroke:#c92a2a,color:#fff,stroke-width:4px
    style M fill:#fab005,stroke:#f08c00,color:#000
    style P fill:#51cf66,stroke:#2f9e44,color:#000
    style U fill:#339af0,stroke:#1c7ed6,color:#fff
```

**Legenda:**
- ⭐ **Agregado Raiz:** Subprocesso é o agregado central que conecta todos os outros
- Linhas pontilhadas: Relações entre agregados
- 1:N, N:1, N:M: Cardinalidade das relações

---

## 3. Fluxo de Dados Simplificado

```mermaid
sequenceDiagram
    participant SEDOC as SEDOC<br/>(ADMIN)
    participant Processo
    participant Subprocesso as Subprocesso<br/>(Agregado Raiz)
    participant CHEFE
    participant Mapa
    
    SEDOC->>Processo: 1. Criar Processo<br/>(Tipo: Mapeamento)
    Processo->>Subprocesso: 2. Criar Subprocessos<br/>(1 por unidade operacional)
    Note over Subprocesso: Estado: "Não iniciado"
    
    CHEFE->>Subprocesso: 3. Cadastrar Atividades
    Note over Subprocesso: Estado: "Cadastro em andamento"
    
    CHEFE->>Subprocesso: 4. Disponibilizar Cadastro
    Note over Subprocesso: Estado: "Cadastro disponibilizado"
    
    Note over Subprocesso: 5. Validação Hierárquica<br/>(GESTOR → SEDOC)
    Note over Subprocesso: Estado: "Cadastro homologado"
    
    SEDOC->>Mapa: 6. Criar Mapa<br/>(sintetizar atividades)
    Mapa-->>Subprocesso: Referência ao Mapa
    Note over Subprocesso: Estado: "Mapa criado"
    
    SEDOC->>Subprocesso: 7. Disponibilizar Mapa
    Note over Subprocesso: Estado: "Mapa disponibilizado"
    
    CHEFE->>Subprocesso: 8. Validar Mapa
    Note over Subprocesso: Estado: "Mapa validado"
    
    SEDOC->>Subprocesso: 9. Homologar Mapa
    Note over Subprocesso: Estado: "Mapa homologado"
    
    SEDOC->>Processo: 10. Finalizar Processo
    Note over Mapa: Mapa torna-se<br/>VIGENTE
```

**Observação:** Note como Subprocesso é central no fluxo - praticamente todas as operações passam por ele.

---

## 4. Arquitetura de Services - Subprocesso (Atual)

```mermaid
graph LR
    subgraph "Controllers (Public API)"
        CC[SubprocessoCrudController]
        CAD[SubprocessoCadastroController]
        MAP[SubprocessoMapaController]
        VAL[SubprocessoValidacaoController]
    end
    
    subgraph "Facade (Orquestração)"
        F[SubprocessoFacade<br/>363 linhas<br/>PUBLIC]
    end
    
    subgraph "Workflow Services"
        WFC[SubprocessoCadastroWorkflowService<br/>288 linhas<br/>PUBLIC ⚠️]
        WFM[SubprocessoMapaWorkflowService<br/>435 linhas<br/>PUBLIC ⚠️]
        TRA[SubprocessoTransicaoService<br/>166 linhas<br/>PUBLIC ⚠️]
    end
    
    subgraph "CRUD Services"
        CRUD[SubprocessoCrudService<br/>262 linhas<br/>PUBLIC ⚠️]
        DET[SubprocessoDetalheService<br/>175 linhas<br/>PUBLIC ⚠️]
        VAL2[SubprocessoValidacaoService<br/>136 linhas<br/>PUBLIC ⚠️]
    end
    
    subgraph "Support Services"
        CTX[SubprocessoContextoService<br/>~100 linhas<br/>PUBLIC ⚠️]
        MAPA[SubprocessoMapaService<br/>152 linhas<br/>PUBLIC ⚠️]
        EMAIL[SubprocessoEmailService<br/>138 linhas<br/>PUBLIC ⚠️]
        FAC[SubprocessoFactory<br/>145 linhas<br/>PUBLIC ⚠️]
    end
    
    %% Fluxo de chamadas
    CC --> F
    CAD --> F
    MAP --> F
    VAL --> F
    
    F --> WFC
    F --> WFM
    F --> TRA
    F --> CRUD
    F --> DET
    F --> VAL2
    F --> CTX
    F --> MAPA
    F --> EMAIL
    F --> FAC
    
    %% Estilo
    style F fill:#51cf66,stroke:#2f9e44,color:#000,stroke-width:3px
    style WFC fill:#ff6b6b,stroke:#c92a2a,color:#fff
    style WFM fill:#ff6b6b,stroke:#c92a2a,color:#fff
    style TRA fill:#ff6b6b,stroke:#c92a2a,color:#fff
    style CRUD fill:#fab005,stroke:#f08c00,color:#000
    style DET fill:#fab005,stroke:#f08c00,color:#000
    style VAL2 fill:#fab005,stroke:#f08c00,color:#000
```

**Problema Identificado:**
- ⚠️ Todos os 12 services são PUBLIC (deveriam ser package-private)
- ⚠️ 12 services quando 6-7 seriam suficientes

---

## 5. Arquitetura de Services - Subprocesso (Proposta)

```mermaid
graph LR
    subgraph "Controllers (Public API)"
        CC[SubprocessoCrudController]
        CAD[SubprocessoCadastroController]
        MAP[SubprocessoMapaController]
        VAL[SubprocessoValidacaoController]
    end
    
    subgraph "Facade (Orquestração)"
        F[SubprocessoFacade<br/>PUBLIC ✅]
    end
    
    subgraph "Workflow Services (Consolidado)"
        WF[SubprocessoWorkflowService<br/>package-private ✅<br/>← CadastroWorkflow<br/>← MapaWorkflow<br/>← Transicao]
    end
    
    subgraph "CRUD Services"
        CRUD[SubprocessoCrudService<br/>package-private ✅]
        VAL2[SubprocessoValidacaoService<br/>package-private ✅]
    end
    
    subgraph "Support Services"
        NOT[SubprocessoNotificacaoService<br/>package-private ✅<br/>← EmailService]
        FAC[SubprocessoFactory<br/>package-private ✅]
    end
    
    subgraph "Eliminados"
        E1[✗ DetalheService<br/>→ lógica movida para Facade]
        E2[✗ ContextoService<br/>→ lógica movida para Facade]
        E3[✗ MapaService<br/>→ lógica movida para MapaFacade]
    end
    
    %% Fluxo de chamadas
    CC --> F
    CAD --> F
    MAP --> F
    VAL --> F
    
    F --> WF
    F --> CRUD
    F --> VAL2
    F --> NOT
    F --> FAC
    
    %% Estilo
    style F fill:#51cf66,stroke:#2f9e44,color:#000,stroke-width:4px
    style WF fill:#51cf66,stroke:#2f9e44,color:#000
    style CRUD fill:#51cf66,stroke:#2f9e44,color:#000
    style VAL2 fill:#51cf66,stroke:#2f9e44,color:#000
    style NOT fill:#51cf66,stroke:#2f9e44,color:#000
    style FAC fill:#51cf66,stroke:#2f9e44,color:#000
    style E1 fill:#868e96,stroke:#495057,color:#fff
    style E2 fill:#868e96,stroke:#495057,color:#fff
    style E3 fill:#868e96,stroke:#495057,color:#fff
```

**Melhorias:**
- ✅ 12 services → 6 services (50% redução)
- ✅ Todos services package-private (exceto Facade)
- ✅ Lógica consolidada, menos duplicação

---

## 6. Comunicação por Eventos (Proposta)

```mermaid
sequenceDiagram
    participant SubprocessoFacade
    participant WorkflowService
    participant EventPublisher
    participant NotificacaoListener
    participant MapaListener
    participant AnaliseListener
    
    SubprocessoFacade->>WorkflowService: disponibilizarCadastro(id)
    WorkflowService->>WorkflowService: transição de estado
    WorkflowService->>EventPublisher: publish(EventoCadastroDisponibilizado)
    
    par Listeners Assíncronos
        EventPublisher--)NotificacaoListener: onCadastroDisponibilizado
        NotificacaoListener--)NotificacaoListener: enviar email para<br/>unidade superior
    and
        EventPublisher--)MapaListener: onCadastroDisponibilizado
        MapaListener--)MapaListener: preparar análise<br/>de impacto (se revisão)
    and
        EventPublisher--)AnaliseListener: onCadastroDisponibilizado
        AnaliseListener--)AnaliseListener: registrar métrica
    end
    
    Note over NotificacaoListener,AnaliseListener: Desacoplamento via eventos:<br/>Subprocesso não conhece<br/>Notificacao, Mapa, Analise
```

**Benefícios:**
- ✅ Desacoplamento entre módulos
- ✅ Extensibilidade (novos listeners sem alterar código)
- ✅ Processamento assíncrono

---

## 7. Organização de Sub-pacotes (Proposta)

```
subprocesso/
├── 📄 SubprocessoCrudController.java
├── 📄 SubprocessoCadastroController.java
├── 📄 SubprocessoMapaController.java
├── 📄 SubprocessoValidacaoController.java
│
├── 📁 dto/
│   ├── SubprocessoDto.java
│   ├── SubprocessoDetalheDto.java
│   └── [outros DTOs]
│
├── 📁 mapper/
│   └── SubprocessoMapper.java
│
├── 📁 model/
│   ├── Subprocesso.java (entidade JPA)
│   ├── SituacaoSubprocesso.java (enum)
│   └── [outros modelos]
│
├── 📁 eventos/
│   ├── EventoTransicaoSubprocesso.java
│   └── TipoTransicao.java
│
├── 📁 listener/
│   └── SubprocessoEventListener.java
│
├── 📁 erros/
│   └── SubprocessoErro.java
│
└── 📁 service/
    ├── 📄 SubprocessoFacade.java (PUBLIC ✅)
    │
    ├── 📁 workflow/
    │   ├── 🔒 SubprocessoWorkflowService.java
    │   └── 🔒 SubprocessoTransicaoService.java
    │
    ├── 📁 crud/
    │   ├── 🔒 SubprocessoCrudService.java
    │   └── 🔒 SubprocessoValidacaoService.java
    │
    ├── 📁 notificacao/
    │   └── 🔒 SubprocessoNotificacaoService.java
    │
    └── 📁 factory/
        └── 🔒 SubprocessoFactory.java
```

**Legenda:**
- 📄 Arquivo público (controllers, facade)
- 🔒 Arquivo package-private (services especializados)
- 📁 Diretório

**Benefícios:**
- ✅ Navegação clara por responsabilidade
- ✅ Separação lógica (workflow/ vs crud/ vs notificacao/)
- ✅ Facilita identificar services relacionados

---

## 8. Comparação de Métricas

### Estado Atual

| Métrica | Valor |
|---------|-------|
| **Arquivos no módulo** | 76 |
| **Services** | 12 |
| **Services públicos** | 12 ⚠️ |
| **Linhas em services** | ~2.500 |
| **Eventos implementados** | 3 (TransicaoSubprocesso) |
| **Arquivos importando módulo** | 59 |
| **Comunicação** | Majoritariamente síncrona |

### Estado Proposto

| Métrica | Valor | Melhoria |
|---------|-------|----------|
| **Arquivos no módulo** | ~65 | ⬇️ 15% (eliminar redundantes) |
| **Services** | 6 | ⬇️ 50% |
| **Services públicos** | 1 (Facade) | ⬇️ 92% |
| **Linhas em services** | ~1.800 | ⬇️ 28% |
| **Eventos implementados** | 10+ | ⬆️ 233% |
| **Arquivos importando módulo** | 45-50 | ⬇️ 15-24% |
| **Comunicação** | Mix síncrona/assíncrona | ⬆️ Desacoplamento |

---

## 9. Cronograma de Implementação

```mermaid
gantt
    title Roadmap de Melhorias - SGC Subprocesso
    dateFormat  YYYY-MM-DD
    section Fase 1
    Análise e Documentação           :done, f1, 2026-01-15, 5d
    
    section Fase 2
    Package-Private Services         :active, f2, 2026-01-20, 3d
    
    section Fase 3
    Implementar Eventos              :f3, 2026-01-23, 7d
    
    section Fase 4
    Organizar Sub-pacotes            :f4, 2026-01-30, 3d
    
    section Fase 5
    Consolidar Services              :f5, 2026-02-02, 10d
    
    section Fase 6
    Documentação Final               :f6, 2026-02-12, 3d
```

**Total estimado:** 31 dias úteis (~6 semanas)

---

## 10. Conclusão Visual

```
┌─────────────────────────────────────────────────────────────┐
│                   ARQUITETURA ATUAL                         │
│                                                             │
│  ✅ CORRETO: Organização por Agregados de Domínio         │
│                                                             │
│  ⚠️ MELHORAR:                                              │
│     • Consolidar services (12 → 6)                         │
│     • Tornar services package-private                      │
│     • Implementar eventos de domínio                       │
│     • Organizar sub-pacotes                                │
│                                                             │
│  ❌ NÃO FAZER:                                             │
│     • Reorganizar por tipo de processo (duplicação!)       │
│     • Reorganizar por camadas técnicas (perde coesão)      │
│                                                             │
│  📊 IMPACTO ESPERADO:                                      │
│     • -50% services                                         │
│     • -28% linhas de código                                │
│     • +233% eventos (desacoplamento)                       │
│     • +100% encapsulamento (package-private)               │
└─────────────────────────────────────────────────────────────┘
```

---

**Mantido por:** GitHub Copilot AI Agent  
**Data:** 2026-01-15  
**Relacionado:** proposta-arquitetura.md, ADR-006
