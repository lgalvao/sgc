# Plano de Correção dos Testes

Este arquivo descreve as alterações necessárias para corrigir os erros de compilação nos testes após a refatoração para usar enums de `Perfil` e `TipoProcesso`.

## Arquivos a serem corrigidos:

1.  **`backend/src/test/java/sgc/integracao/CDU03IntegrationTest.java`**
    *   **Problema:** A criação dos DTOs `CriarProcessoReq` e `AtualizarProcessoReq` está usando `TipoProcesso.MAPEAMENTO.name()` em vez do próprio enum.
    *   **Correção:** Substituir `TipoProcesso.MAPEAMENTO.name()` por `TipoProcesso.MAPEAMENTO` nas chamadas do construtor.

2.  **`backend/src/test/java/sgc/integracao/CDU07IntegrationTest.java`**
    *   **Problema:** A chamada para `subprocessoDtoService.obterDetalhes` está passando o perfil como uma `String` (`"ADMIN"`).
    *   **Correção:** Substituir a string `"ADMIN"` pelo enum `Perfil.ADMIN` e importar a classe `sgc.sgrh.modelo.Perfil`.

3.  **`backend/src/test/java/sgc/subprocesso/dto/SubprocessoDtoServiceTest.java`**
    *   **Problema:** As chamadas para `service.obterDetalhes` estão passando os perfis como `String` (`"INVALIDO"`, `"GESTOR"`).
    *   **Correção:** Substituir os literais de string pelos enums correspondentes (`Perfil.GESTOR`, etc.). O perfil `"INVALIDO"` deve ser tratado como um `null` ou um enum inválido, se aplicável ao teste.

4.  **`backend/src/test/java/sgc/service/ProcessoServiceTest.java`**
    *   **Problema:** Semelhante ao `ProcessoServiceTest`, a instanciação de `CriarProcessoReq` usa `TipoProcesso.MAPEAMENTO.name()`.
    *   **Correção:** Substituir `TipoProcesso.MAPEAMENTO.name()` por `TipoProcesso.MAPEAMENTO`.
