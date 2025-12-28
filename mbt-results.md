# Relatório de Testes de Mutação (MBT)

## 1. Introdução
Este documento resume os resultados da introdução de *Mutation-Based Testing* (MBT) no backend do SGC, utilizando a ferramenta **PITest**.

O objetivo foi identificar lacunas na cobertura de testes que as métricas tradicionais (como line coverage) não detectam, garantindo que os testes realmente validem a lógica de negócio.

## 2. Configuração
- **Ferramenta:** PITest (via plugin Gradle `info.solidsoft.pitest` v1.19.0-rc.1)
- **Escopo Inicial:** Pacote `sgc.unidade`
- **Mutadores:** Padrão do PITest (ConditionalsBoundary, NegateConditionals, VoidMethodCall, etc.)

## 3. Resultados da Execução Piloto
A execução inicial no pacote `sgc.unidade` revelou diversos mutantes sobreviventes, indicando áreas onde a lógica de teste pode ser fortalecida.

### Exemplo de Mutante Corrigido
Um exemplo claro foi encontrado na classe `sgc.unidade.model.Unidade`:

*   **Método:** `setTitular(Usuario usuario)`
*   **Lógica Original:**
    ```java
    public void setTitular(Usuario usuario) {
        if (usuario != null) {
            this.tituloTitular = usuario.getTituloEleitoral();
            this.matriculaTitular = usuario.getMatricula();
        }
    }
    ```
*   **Mutante Gerado:** `NegateConditionalsMutator` (inverteu `if (usuario != null)` para `if (usuario == null)`).
*   **Estado Inicial:** O mutante sobrevivia porque os testes existentes (se houvesse) não verificavam explicitamente o comportamento quando `usuario` não é nulo versus quando é nulo, ou simplesmente a classe não tinha cobertura direta.
*   **Ação Corretiva:** Foi criado um novo teste unitário `sgc.unidade.model.UnidadeTest` com dois cenários:
    1.  `deveAtualizarTitularQuandoUsuarioNaoNulo`: Garante que os campos são atualizados.
    2.  `naoDeveAtualizarTitularQuandoUsuarioNulo`: Garante que os campos *não* são alterados se o usuário for nulo.
*   **Resultado Final:** O mutante foi "morto" (KILLED), confirmando que o teste agora protege essa lógica condicional.

## 4. Próximos Passos
1.  **Expandir Cobertura:** Analisar os relatórios HTML gerados em `backend/build/reports/pitest/index.html` para identificar outros mutantes críticos em `sgc.unidade` e demais pacotes.
2.  **Integração CI:** Configurar o PITest para rodar em *pull requests* críticos ou em *nightly builds*, focando nas classes alteradas.
3.  **Refinamento:** Ajustar a configuração de exclusão (`excludedClasses`) para ignorar código gerado (Lombok, MapStruct) que gera ruído nos relatórios.
