# Relatório de Auditoria de Controle de Acesso (SGC)

Este relatório apresenta um batimento entre os requisitos de controle de acesso definidos em `/etc/reqs` (CDUs) e a implementação atual no backend do sistema SGC.

## 1. Metodologia

A análise foi realizada através de duas abordagens:
1.  **Análise Estática:** Revisão do código fonte, especificamente dos Controllers, Facades, Services e Policies (`SubprocessoAccessPolicy`, `AbstractAccessPolicy`).
2.  **Análise Dinâmica (Auditoria):** Execução de um script de auditoria (`backend/etc/scripts/audit-access.sh`) que utiliza reflexão Java para extrair a matriz exata de regras configuradas na aplicação em tempo de execução.

## 2. Visão Geral da Arquitetura de Segurança

O sistema implementa a arquitetura centralizada definida no **ADR-003**:
*   **Controllers:** Utilizam `@PreAuthorize` para verificações básicas de Role (ex: `hasRole('ADMIN')`).
*   **Services:** A maioria das operações de escrita passa pelo `AccessControlService`, que delega para policies específicas (`SubprocessoAccessPolicy`, etc.).
*   **Policies:** Definem regras granulares baseadas em Perfil, Situação do Recurso e Hierarquia da Unidade.

A adesão a este padrão é alta, garantindo segurança robusta na maioria dos fluxos. No entanto, foram encontradas exceções críticas.

## 3. Vulnerabilidades Críticas Encontradas

### 3.1. Falha de Controle de Acesso na Importação de Atividades (IDOR)

**Gravidade:** 🔴 **CRÍTICA**
**Local:** `SubprocessoAtividadeService.importarAtividades` e `SubprocessoCadastroController.importarAtividades`
**Requisito:** CDU-08 (Manter cadastro de atividades e conhecimentos)

**Descrição:**
O endpoint de importação de atividades (`POST /api/subprocessos/{codigo}/importar-atividades`) está protegido apenas por `@PreAuthorize("hasRole('CHEFE')")`.
A implementação no serviço `SubprocessoAtividadeService` **não realiza nenhuma chamada** ao `AccessControlService.verificarPermissao`.

**Consequência:**
Um usuário com perfil `CHEFE` de qualquer unidade pode importar atividades para **qualquer outro subprocesso** do sistema, bastando conhecer ou adivinhar o ID do subprocesso destino. Isso permite:
1.  Alterar dados de subprocessos de outras unidades (violação de integridade e confidencialidade).
2.  Alterar dados de subprocessos que já estão finalizados ou homologados (violação de regra de negócio), pois não há verificação de estado.

**Recomendação:**
Adicionar imediatamente a verificação de permissão no método `importarAtividades`:
```java
accessControlService.verificarPermissao(usuario, Acao.EDITAR_CADASTRO, spDestino);
```
Nota: O sistema deve usar `EDITAR_CADASTRO` ou `EDITAR_REVISAO_CADASTRO` dependendo do tipo do processo.

---

## 4. Matriz de Regras Implementadas (Subprocesso)

A tabela abaixo reflete as regras **atualmente ativas** no código (`SubprocessoAccessPolicy`).

| Ação | Perfis Permitidos | Situações Permitidas | Requisito Hierarquia |
|---|---|---|---|
| Enviar lembrete de processo | ADMIN | TODAS | NENHUM |
| Listar subprocessos | ADMIN | TODAS | NENHUM |
| Visualizar subprocesso | ADMIN, GESTOR, CHEFE, SERVIDOR | TODAS | MESMA_OU_SUBORDINADA |
| Criar subprocesso | ADMIN | TODAS | NENHUM |
| Editar subprocesso | ADMIN | TODAS | NENHUM |
| Excluir subprocesso | ADMIN | TODAS | NENHUM |
| Alterar data limite | ADMIN | TODAS | NENHUM |
| Reabrir cadastro | ADMIN | TODAS | NENHUM |
| Reabrir revisão | ADMIN | TODAS | NENHUM |
| Editar cadastro de atividades | CHEFE | NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO | MESMA_UNIDADE |
| Disponibilizar cadastro | CHEFE | MAPEAMENTO_CADASTRO_EM_ANDAMENTO | TITULAR_UNIDADE |
| Devolver cadastro | ADMIN, GESTOR | MAPEAMENTO_CADASTRO_DISPONIBILIZADO | SUPERIOR_IMEDIATA |
| Aceitar cadastro | ADMIN, GESTOR | MAPEAMENTO_CADASTRO_DISPONIBILIZADO | SUPERIOR_IMEDIATA |
| Homologar cadastro | ADMIN | MAPEAMENTO_CADASTRO_DISPONIBILIZADO | NENHUM |
| Editar revisão de cadastro | CHEFE | NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO | MESMA_UNIDADE |
| Disponibilizar revisão de cadastro | CHEFE | REVISAO_CADASTRO_EM_ANDAMENTO | TITULAR_UNIDADE |
| Devolver revisão de cadastro | ADMIN, GESTOR | REVISAO_CADASTRO_DISPONIBILIZADA | SUPERIOR_IMEDIATA |
| Aceitar revisão de cadastro | ADMIN, GESTOR | REVISAO_CADASTRO_DISPONIBILIZADA | SUPERIOR_IMEDIATA |
| Homologar revisão de cadastro | ADMIN | REVISAO_CADASTRO_DISPONIBILIZADA | NENHUM |
| Visualizar mapa | ADMIN, GESTOR, CHEFE, SERVIDOR | TODAS | MESMA_OU_SUBORDINADA |
| Editar mapa | ADMIN | NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES, DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO | MESMA_UNIDADE |
| Disponibilizar mapa | ADMIN | MAPEAMENTO_CADASTRO_HOMOLOGADO, MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES | NENHUM |
| Verificar impactos no mapa | ADMIN, GESTOR, CHEFE | NAO_INICIADO, REVISAO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_DISPONIBILIZADA, REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO | MESMA_UNIDADE |
| Apresentar sugestões ao mapa | CHEFE | MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO | MESMA_UNIDADE |
| Validar mapa | CHEFE | MAPEAMENTO_MAPA_DISPONIBILIZADO, REVISAO_MAPA_DISPONIBILIZADO | MESMA_UNIDADE |
| Devolver mapa | ADMIN, GESTOR | MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO, REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO | SUPERIOR_IMEDIATA |
| Aceitar mapa | ADMIN, GESTOR | MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO, REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO | SUPERIOR_IMEDIATA |
| Homologar mapa | ADMIN | MAPEAMENTO_MAPA_COM_SUGESTOES, MAPEAMENTO_MAPA_VALIDADO, REVISAO_MAPA_COM_SUGESTOES, REVISAO_MAPA_VALIDADO | NENHUM |
| Ajustar mapa | ADMIN | REVISAO_CADASTRO_HOMOLOGADA, REVISAO_MAPA_AJUSTADO | NENHUM |
| Visualizar diagnóstico | ADMIN, GESTOR, CHEFE, SERVIDOR | TODAS | MESMA_OU_SUBORDINADA |
| Realizar autoavaliação | ADMIN, GESTOR, CHEFE | DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO | MESMA_UNIDADE |

## 5. Outras Observações e Discrepâncias

### 5.1. Restrição de `TITULAR_UNIDADE` para Substitutos
**Local:** `AbstractAccessPolicy.verificaHierarquia` (Case `TITULAR_UNIDADE`)
**Regra:** `Disponibilizar cadastro` exige `TITULAR_UNIDADE`.
**Observação:** A verificação compara estritamente o título eleitoral do usuário com o campo `tituloTitular` da unidade.
**Risco:** Usuários com perfil `CHEFE` que sejam **substitutos** (e não titulares oficiais no cadastro da unidade) serão bloqueados de disponibilizar o cadastro, mesmo tendo permissão para editá-lo.
**Recomendação:** Confirmar se o modelo de dados suporta substituição ou se a regra deve ser relaxada para `MESMA_UNIDADE` combinado com o perfil `CHEFE`.

### 5.2. Ação `VERIFICAR_IMPACTOS`
A ação possui lógica customizada complexa (`canExecuteVerificarImpactos`) que depende do perfil (ADMIN vs GESTOR vs CHEFE) e da situação.
*   **ADMIN:** Pode em situações avançadas de revisão.
*   **GESTOR:** Apenas se `REVISAO_CADASTRO_DISPONIBILIZADA`.
*   **CHEFE:** Apenas no início (`NAO_INICIADO`, `REVISAO_CADASTRO_EM_ANDAMENTO`) e na mesma unidade.
Esta lógica parece consistente com a necessidade de análise de impacto em diferentes fases, mas é uma exceção ao padrão da tabela declarativa e deve ser mantida com cuidado.

### 5.3. Inconsistência de Nomenclatura nas Rotas
*   `/api/subprocessos/{codigo}/cadastro/disponibilizar`
*   `/api/subprocessos/{codigo}/disponibilizar-revisao`
Embora funcional, a inconsistência nos padrões de URL dificulta a configuração de regras de segurança baseadas em caminho (se fossem usadas, ex: Spring Security matcher), embora o SGC use segurança em método.

## 6. Conclusão

A arquitetura de segurança do SGC é sólida, mas a implementação contém uma falha crítica na funcionalidade de "Importar Atividades" que precisa ser corrigida urgentemente. As demais regras estão, em geral, aderentes aos requisitos e garantem o isolamento entre unidades e perfis.

Recomenda-se a correção imediata do item 3.1.
