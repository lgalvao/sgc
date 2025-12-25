# Relatório de Diagnóstico: Refatoração Spring Modulith

**Data:** 01/03/2024
**Autor:** Jules (Agente de IA)
**Assunto:** Análise dos problemas introduzidos pela refatoração para Spring Modulith e recomendação de correção.

---

## 1. Diagnóstico do Problema

A refatoração recente, destinada a adotar a arquitetura **Spring Modulith**, excedeu drasticamente seu escopo original, resultando em um estado instável da base de código. A análise revelou que as alterações não se limitaram à reorganização de pacotes, mas modificaram a estrutura fundamental do modelo de domínio e do mapeamento Objeto-Relacional (ORM/JPA).

### 1.1 Violação do Modelo de Domínio
O problema mais crítico identificado é a alteração da modelagem de entidades JPA para tentar forçar um desacoplamento via IDs, quebrando a integridade referencial e a navegabilidade do ORM.

*   **Exemplo Crítico (`Mapa` vs `Subprocesso`):**
    *   **Antes:** A entidade `Mapa` possuía uma relação `@OneToOne` direta com `Subprocesso`.
    *   **Depois:** A entidade `Mapa` foi alterada para conter apenas um campo `Long subprocessoCodigo`.
    *   **Consequência:** A entidade `Subprocesso` manteve o mapeamento `@OneToOne(mappedBy = "subprocesso")`, referenciando um campo que **não existe mais** na classe `Mapa`. Isso coloca o Hibernate em um estado inválido, impedindo a inicialização do contexto de persistência e causando erros em tempo de execução.

### 1.2 Quebra Massiva de Testes
A mudança no modelo de objetos invalidou centenas de testes existentes.
*   **Erro Comum:** Métodos como `mapa.setSubprocesso(subprocesso)` não existem mais, causando falhas de compilação em classes de teste (`CDU12IntegrationTest`, `AtividadeFluxoIntegrationTest`, `MapaFixture`).
*   **Volume:** Embora a compilação principal (`compileJava`) passe (pois Java não valida strings de mapeamento JPA), a compilação de testes (`compileTestJava`) falha, e a execução da aplicação é impossível devido à inconsistência do mapeamento JPA.

### 1.3 Desvio das Instruções
O documento `modulith/guia-refatoracao.md` instrui explicitamente sobre a **movimentação de arquivos** (`git mv`) e ajustes de `package`/`import`. Não há instruções para substituir relacionamentos de objeto por IDs em entidades JPA. A execução da refatoração inventou regras não solicitadas, violando o princípio de "refatoração sem alteração de comportamento".

---

## 2. Impacto

*   **Arquivos Afetados:** 753 arquivos foram modificados em um único merge.
*   **Estabilidade:** O sistema está inoperante (não inicia devido a erros de mapeamento JPA).
*   **Testes:** A infraestrutura de testes está quebrada, impedindo qualquer verificação de segurança para correções incrementais.

---

## 3. Recomendação: Revert Completo

Dada a gravidade e a extensão dos danos ao modelo de domínio, a tentativa de "consertar para frente" (fix-forward) é **altamente desaconselhada**. Restaurar os relacionamentos JPA manualmente em centenas de arquivos, enquanto se tenta manter a nova estrutura de pacotes, é um processo propenso a erros e demorado.

**Recomendação:**
Proceder com um **Revert Completo** das alterações relacionadas ao "Spring Modulith" (Merge `fdfeb88e` e associados).

### Justificativa:
1.  **Segurança:** Restaura o sistema para o último estado conhecido estável e funcional.
2.  **Integridade:** Recupera o modelo de domínio correto e validado pelos requisitos originais.
3.  **Eficiência:** É mais rápido reverter e planejar uma nova refatoração (seguindo estritamente o guia) do que depurar centenas de erros estruturais espalhados por 753 arquivos.

### Próximos Passos Sugeridos (Pós-Revert):
1.  Realizar o revert via git.
2.  Garantir que a pipeline de testes (build e test) esteja verde.
3.  Reiniciar a adoção do Spring Modulith **módulo por módulo**, em PRs pequenos e isolados, garantindo que apenas a estrutura de pacotes seja alterada, sem tocar na lógica interna das entidades JPA num primeiro momento.

---
