# Roteiro de Teste Manual Completo: Ciclo de Vida do Processo de Mapeamento

**Objetivo:** Validar o fluxo completo de um processo de **Mapeamento**, desde sua criação até a finalização, envolvendo múltiplos perfis e ações, com base nos Casos de Uso (CDUs) do sistema.

---

## 🔐 Atores e Credenciais

Este roteiro utiliza a hierarquia da **Secretaria 2**. A senha padrão para todos os usuários é **`senha`**.

| Papel no Teste | Perfil | Usuário (Login) | Nome | Unidade |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | ADMIN | `191919` | Admin Único | Administração |
| **Chefe de Seção** | CHEFE | `141414` | Tina Turner | Seção 221 |
| **Gestor (Coord.)**| GESTOR | `131313` | Mick Jagger | Coordenação 22 |
| **Gestor (Sec.)** | GESTOR | `212121` | George Harrison | Secretaria 2 |

---

## 📋 Cenário de Teste End-to-End

### 1. Criação e Início do Processo (ADMIN)

**Ator:** Administrador (`191919`)

1.  **Login:** Faça login como `191919`.
2.  **Criar Processo:**
    *   Acesse **Processos** > **Criar processo**.
    *   Preencha os dados:
        *   **Descrição:** `Teste E2E - Mapeamento Completo [Seu Nome]`
        *   **Tipo:** `MAPEAMENTO`
        *   **Unidade Responsável:** `Seção 221`
        *   **Dias Limite:** `30`
    *   Clique em **Salvar**.
    *   ✅ **Resultado Esperado:** O processo é criado e aparece na lista de processos com a situação "CRIADO".
3.  **Iniciar Processo:**
    *   Clique no processo recém-criado.
    *   Clique em **Iniciar** e confirme.
    *   ✅ **Resultado Esperado:** O processo muda para a situação "EM_ANDAMENTO". O processo desaparece da visão do ADMIN (pois agora está com o CHEFE) e um alerta de início de processo é gerado para a `Seção 221`.

### 2. Mapeamento de Atividades e Conhecimentos (CHEFE)

**Ator:** Chefe de Seção (`141414`)

1.  **Login:** Faça login como `141414` (Tina Turner).
2.  **Acessar Subprocesso:**
    *   No Painel, o novo processo deve estar visível. Clique nele.
    *   Você será direcionado para a tela de "Detalhes do subprocesso".
3.  **Cadastrar Atividades e Conhecimentos:**
    *   Acesse a aba/card **Atividades e Conhecimentos**.
    *   **Teste de Erro (Validação):**
        *   Adicione uma atividade: `Atividade Teste 1` (sem conhecimentos).
        *   Clique em **Disponibilizar**.
        *   ✅ **Resultado Esperado:** O sistema deve exibir uma mensagem de erro informando que todas as atividades precisam de conhecimentos, e a operação deve ser bloqueada.
    *   **Cadastro Correto:**
        *   Edite a `Atividade Teste 1` e adicione o conhecimento `Conhecimento A`.
        *   Adicione uma nova atividade: `Atividade Teste 2` com o conhecimento `Conhecimento B`.
4.  **Disponibilizar Cadastro:**
    *   Clique em **Disponibilizar**.
    *   Na modal de confirmação, clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel com uma mensagem de sucesso. O processo não está mais na sua caixa de entrada. A situação do subprocesso muda para "Cadastro disponibilizado".

### 3. Análise e Devolução do Cadastro (GESTOR)

**Ator:** Gestor da Coordenação (`131313`)

1.  **Login:** Faça login como `131313` (Mick Jagger).
2.  **Acessar Processo para Análise:**
    *   No Painel, o processo `Teste E2E...` deve estar visível. Clique nele.
    *   Na tela de "Detalhes do processo", clique na `Seção 221`.
3.  **Analisar e Devolver:**
    *   Acesse a aba/card **Atividades e Conhecimentos**.
    *   Clique no botão **Devolver para ajustes**.
    *   Na modal, preencha a **Observação**: `É necessário adicionar uma atividade sobre relatórios.` e clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel com uma mensagem de sucesso. O processo desaparece da sua caixa de entrada.

### 4. Ajuste e Reenvio do Cadastro (CHEFE)

**Ator:** Chefe de Seção (`141414`)

1.  **Login:** Faça login como `141414` (Tina Turner).
2.  **Verificar Histórico:**
    *   Acesse o subprocesso devolvido.
    *   Clique no botão **Histórico de Análise**.
    *   ✅ **Resultado Esperado:** O modal deve exibir um registro de "Devolução" feito por `Mick Jagger` com a observação `É necessário adicionar uma atividade sobre relatórios.`.
3.  **Realizar Ajuste:**
    *   Feche o modal.
    *   Adicione a nova atividade: `Elaboração de Relatórios Gerenciais` com o conhecimento `Análise de Dados`.
4.  **Reenviar Cadastro:**
    *   Clique em **Disponibilizar** e confirme.
    *   ✅ **Resultado Esperado:** Sucesso. O processo é enviado novamente para o Gestor.

### 5. Aceite do Cadastro (GESTOR)

**Ator:** Gestor da Coordenação (`131313`)

1.  **Login:** Faça login como `131313` (Mick Jagger).
2.  **Acessar e Analisar:**
    *   Acesse o processo e o subprocesso da `Seção 221` novamente.
    *   Verifique se a nova atividade foi incluída.
3.  **Registrar Aceite:**
    *   Clique em **Registrar aceite**.
    *   Na modal, clique em **Confirmar** (sem observação).
    *   ✅ **Resultado Esperado:** Sucesso. O processo é enviado para o próximo nível hierárquico (ADMIN, neste caso, pois a Secretaria não tem um gestor intermediário configurado para homologar).

### 6. Homologação do Cadastro (ADMIN)

**Ator:** Administrador (`191919`)

1.  **Login:** Faça login como `191919`.
2.  **Acessar e Homologar:**
    *   Acesse o processo e o subprocesso da `Seção 221`.
    *   Acesse **Atividades e Conhecimentos**.
    *   Clique em **Homologar**.
    *   Na modal, confirme a homologação.
    *   ✅ **Resultado Esperado:** Sucesso. A tela é atualizada e a situação do subprocesso muda para "Cadastro homologado".

### 7. Criação e Disponibilização do Mapa (ADMIN)

**Ator:** Administrador (`191919`)

1.  **Acessar Mapa:**
    *   Ainda na tela de detalhes do subprocesso, acesse o card **Mapa de Competências**.
2.  **Criar Mapa:**
    *   Crie uma competência: `Gestão de Projetos`.
    *   Associe as atividades `Atividade Teste 1` e `Elaboração de Relatórios Gerenciais` a esta competência.
    *   Crie outra competência: `Desenvolvimento`.
    *   Associe a `Atividade Teste 2` a esta competência.
    *   Salve as alterações.
3.  **Disponibilizar Mapa:**
    *   Volte para a tela de "Detalhes do processo".
    *   O botão **Disponibilizar mapas em bloco** deve estar visível. Clique nele.
    *   Na modal, defina uma **Data limite** para a validação.
    *   Clique em **Disponibilizar**.
    *   ✅ **Resultado Esperado:** Sucesso. O mapa é disponibilizado para a `Seção 221`. A situação do subprocesso muda para "Mapa disponibilizado".

### 8. Análise e Validação do Mapa (CHEFE e GESTOR)

**Atores:** Chefe de Seção (`141414`) e Gestor da Coordenação (`131313`)

1.  **Login (CHEFE):** Faça login como `141414` (Tina Turner).
2.  **Validar Mapa (CHEFE):**
    *   Acesse o subprocesso e o **Mapa de Competências**.
    *   O mapa estará em modo de visualização.
    *   Clique em **Validar Mapa** (ou uma ação similar como "Apresentar Sugestões" e depois "Validar").
    *   Confirme a ação.
    *   ✅ **Resultado Esperado:** Sucesso. O mapa é enviado para o próximo nível (Gestor da Coordenação).
3.  **Login (GESTOR):** Faça login como `131313` (Mick Jagger).
4.  **Aceitar Validação (GESTOR):**
    *   Acesse o processo, o subprocesso da `Seção 221` e o **Mapa de Competências**.
    *   Clique em **Aceitar Validação** (ou ação equivalente).
    *   Confirme a ação.
    *   ✅ **Resultado Esperado:** Sucesso. O mapa é enviado para o próximo nível (ADMIN).

### 9. Homologação do Mapa (ADMIN)

**Ator:** Administrador (`191919`)

1.  **Login:** Faça login como `191919`.
2.  **Acessar e Homologar Mapa:**
    *   Acesse o processo, o subprocesso da `Seção 221` e o **Mapa de Competências**.
    *   Clique em **Homologar Mapa**.
    *   Confirme a homologação.
    *   ✅ **Resultado Esperado:** Sucesso. A situação do subprocesso muda para "Mapa homologado".

### 10. Finalização do Processo (ADMIN)

**Ator:** Administrador (`191919`)

1.  **Acessar Processo:**
    *   Volte para a tela de "Detalhes do processo".
2.  **Finalizar Processo:**
    *   Clique no botão **Finalizar processo**.
    *   ✅ **Validação:** Se houvesse outros subprocessos não homologados, o sistema deveria exibir um erro. Como só temos um e ele está homologado, a operação deve prosseguir.
    *   Na modal de confirmação, clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel. O processo agora tem a situação "FINALIZADO". O mapa de competências da `Seção 221` é definido como vigente. Notificações por e-mail são enviadas.

---
**Fim do Teste.**
Este roteiro cobre o "caminho feliz" e alguns cenários de desvio (devolução, validação de erro), garantindo uma cobertura abrangente do ciclo de vida do processo de mapeamento.
