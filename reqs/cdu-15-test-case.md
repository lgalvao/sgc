# Especificação de Casos de Teste - CDU-15

**Caso de Uso:** Manter mapa de competências
**Ator:** ADMIN
**Referência:** [CDU-15](./cdu-15.md)

## Cenários de Teste

### CT-00: Fluxo Completo do Login à Edição de Mapa
**Pré-condições:**
- Processo de mapeamento existente.
- Unidade com subprocesso em 'Cadastro homologado' ou 'Mapa criado'.
- Usuário ADMIN cadastrado.

**Passos:**
1. Acessar a aplicação e realizar login como ADMIN.
2. No Painel ADMIN, selecionar um processo na lista de processos ativos.
3. Na tela "Detalhes do processo", localizar e clicar em uma unidade operacional ou interoperacional (que tenha subprocesso em situação válida).
4. Na tela "Detalhes do subprocesso", clicar no card "Mapa de Competências".

**Resultado Esperado:**
- O sistema exibe a tela "Edição de mapa" (corroborando o início do CT-01).

---

### CT-01: Visualizar Edição de Mapa e Elementos Visuais
**Pré-condições:**
- Processo de mapeamento existente.
- Unidade com subprocesso em 'Cadastro homologado' ou 'Mapa criado'.
- Usuário ADMIN autenticado.
- Existência de competências previamente cadastradas com atividades e conhecimentos associados (para verificação completa).

**Passos:**
1. Navegar para a tela "Detalhes do subprocesso" da unidade selecionada.
2. Clicar no card "Mapa de Competências".
3. Observar a estrutura visual da tela.
4. Passar o mouse sobre o badge de número de conhecimentos de uma atividade.

**Resultado Esperado:**
- O sistema exibe a tela "Edição de mapa".
- Exibe blocos para cada competência, com título sendo a descrição.
- Botões de ação (editar/excluir) visíveis ao lado da descrição da competência.
- Atividades associadas listadas dentro do bloco da competência.
- Badge com quantidade de conhecimentos ao lado de cada atividade.
- Tooltip exibe lista de conhecimentos ao passar o mouse no badge.
- Botões "Criar competência" e "Disponibilizar" no canto superior direito.

---

### CT-02: Criar Competência e Atualizar Situação do Subprocesso
**Pré-condições:**
- Subprocesso na situação 'Cadastro homologado'.
- Estar na tela "Edição de mapa".

**Passos:**
1. Clicar no botão "Criar competência".
2. Preencher "Descrição da competência" na modal.
3. Selecionar uma ou mais atividades na lista.
4. Clicar em "Salvar".

**Resultado Esperado:**
- A modal fecha.
- A nova competência é exibida na lista.
- A situação do subprocesso é atualizada para 'Mapa criado'.
- Dados persistidos corretamente no backend.

---

### CT-03: Editar Competência Existente
**Pré-condições:**
- Competência cadastrada.
- Estar na tela "Edição de mapa".

**Passos:**
1. Clicar no botão de ação "Editar" de uma competência.
2. Verificar se a modal abre preenchida com os dados atuais (descrição e atividades selecionadas).
3. Alterar a descrição.
4. Alterar a seleção de atividades (adicionar ou remover).
5. Clicar em "Salvar".

**Resultado Esperado:**
- A modal fecha.
- O bloco da competência reflete as alterações (nova descrição e/ou atividades).
- Dados atualizados persistidos no backend.

---

### CT-04: Excluir Competência com Confirmação
**Pré-condições:**
- Competência cadastrada.
- Estar na tela "Edição de mapa".

**Passos:**
1. Clicar no botão de ação "Excluir" de uma competência.
2. Verificar o diálogo de confirmação:
    - Título: 'Exclusão de competência'.
    - Mensagem: 'Confirma a exclusão da competência [DESCRICAO_COMPETENCIA]?'.
3. Confirmar a exclusão.

**Resultado Esperado:**
- O diálogo fecha.
- A competência é removida da tela.
- A competência e seus vínculos são removidos do backend.

---

### CT-05: Validar Cancelamento da Exclusão
**Pré-condições:**
- Competência cadastrada.
- Estar na tela "Edição de mapa".

**Passos:**
1. Clicar no botão de ação "Excluir".
2. No diálogo de confirmação, clicar em "Cancelar".

**Resultado Esperado:**
- O diálogo fecha.
- A competência permanece na tela inalterada.

---

### CT-06: Navegar para Disponibilização
**Pré-condições:**
- Estar na tela "Edição de mapa".

**Passos:**
1. Clicar no botão "Disponibilizar".

**Resultado Esperado:**
- O sistema redireciona para o fluxo de "Disponibilizar mapa de competências" (CDU-16).
