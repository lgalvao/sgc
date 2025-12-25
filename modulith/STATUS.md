# Status da Implementação Spring Modulith no SGC

**Data**: 24/12/2025
**Versão**: 2.1 - Teste de Verificação e Análise de Ciclos

## Resumo Executivo

A refatoração do projeto SGC para adotar o Spring Modulith revelou desafios estruturais significativos após a implementação dos testes de verificação. O sistema utiliza **Spring Modulith 2.0.1**.

### Status Geral

✅ **CONCLUÍDO** - Implementação do teste de verificação (`ModulithTests.java`)
❌ **FALHA** - Verificação estrutural (`modules.verify()`) falha devido a ciclos complexos.
⚠️ **EM PROGRESSO** - Refatoração para quebra de ciclos.

---

## Trabalho Realizado

### 1. Implementação de Testes

✅ **ModulithTests.java**
- Criado em `backend/src/test/java/sgc/ModulithTests.java`.
- Executa `modules.verify()` e gera documentação.
- O teste revelou um ciclo de dependência massivo envolvendo quase todos os módulos principais.

### 2. Análise Estrutural e Ciclos

A execução do teste `verifyModulithStructure` identificou o seguinte ciclo principal:

`alerta -> comum -> sgrh -> unidade -> mapa -> atividade -> processo -> subprocesso -> alerta`

#### Detalhamento das Violações:
1.  **Ciclo `comum <-> sgrh`**:
    - O módulo `comum` dependia de classes de segurança em `sgrh` (devido a configurações globais).
    - **Ação Tomada**: `ConfigSeguranca` e `FiltroAutenticacaoSimulado` foram movidos de `sgc.comum.config` para `sgc.sgrh.internal.config` para tentar mitigar a dependência inversa.
    - **Status**: A dependência direta foi removida, mas o Modulith ainda detecta violações complexas de visibilidade e dependências indiretas.

2.  **Violações de Visibilidade**:
    - Múltiplos módulos acessam classes `internal` de outros módulos (ex: `alerta` acessa `sgc.processo.internal.model.Processo`).
    - Isso indica que o encapsulamento proposto pelo Modulith (usar apenas classes do pacote `api`) não está sendo respeitado pela implementação atual.

3.  **Dependências Cruzadas de Entidades**:
    - As entidades JPA possuem relacionamentos fortes (ex: `Unidade` tem `Set<Processo>`, `Processo` tem `Unidade`). Isso cria ciclos naturais no nível de domínio que o Spring Modulith sinaliza como violações de arquitetura modular.

### 3. Ações Imediatas

- **Refinamento de Pacotes**: Movimentação de classes de configuração de segurança para o módulo `sgrh`, onde semanticamente pertencem.
- **Transparência**: Atualização deste status para refletir que, embora a estrutura de pacotes (api/internal) exista, o acoplamento lógico impede a validação estrita do Modulith neste momento.

---

## Pendências e Próximos Passos (Revisado)

### Prioridade ALTA (Bloqueante para "Estrutura Verde")

1.  **Refatoração de Entidades para DTOs**:
    - Para quebrar o acesso a classes `internal`, os módulos devem se comunicar exclusivamente via DTOs ou Interfaces publicadas no pacote `api`.
    - Exemplo: `AlertaService` não deve receber `Processo` (entidade interna), mas sim `ProcessoDto` ou apenas o ID.

2.  **Quebra de Ciclos de Domínio**:
    - Analisar a necessidade de referências bidirecionais fortes entre módulos (ex: `unidade <-> processo`).
    - Considerar o uso de Eventos de Domínio para desacoplar atualizações de estado entre módulos.

3.  **Definição Explícita de Módulos**:
    - Avaliar o uso de `package-info.java` com `@ApplicationModule(allowedDependencies = ...)` para permitir explicitamente certas dependências legadas enquanto a refatoração ocorre, ou usar "Open Modules" se o desacoplamento total for inviável a curto prazo.

### Prioridade MÉDIA

4.  **Ajuste de Visibilidade**:
    - Após a migração para comunicação via DTOs, reduzir a visibilidade das classes `internal` para `package-private`.

---

## Conclusão

A ferramenta de verificação do Spring Modulith cumpriu seu papel de expor o acoplamento oculto na arquitetura. Embora a organização de pastas sugira modularidade, o grafo de dependências real ainda é monolítico e cíclico. A solução requer uma refatoração profunda das interações entre os serviços de domínio, movendo-se de chamadas diretas de métodos/entidades para um modelo mais orientado a eventos ou interfaces segregadas.
