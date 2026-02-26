# Roteiro de Teste Manual Completo: Ciclo de Vida do Processo de Mapeamento

**Objetivo:** Validar o fluxo completo de um processo de **Mapeamento**, desde sua criação até a finalização, envolvendo múltiplos perfis e ações, com base nos Casos de Uso (CDUs) do sistema.

---

## 🔐 Atores e Credenciais

Este roteiro utiliza a hierarquia da **Secretaria 2**, especificamente a `Seção 221`. A senha padrão para todos os usuários é **`senha`**.

A hierarquia percorrida pelo subprocesso é:
`SECAO_221 → COORD_22 → SECRETARIA_2 → ADMIN`

| Papel no Teste | Perfil usado | Usuário (Login) | Nome | Unidade |
| :--- | :--- | :--- | :--- | :--- |
| **Administrador** | ADMIN | `191919` | Admin Único | Administração |
| **Chefe de Seção** | CHEFE | `141414` | Tina Turner | Seção 221 (SECAO_221) |
| **Gestor (Coord.)** | GESTOR | `131313` | Mick Jagger | Coordenadoria 22 (COORD_22) |
| **Gestor (Sec.)** | GESTOR | `212121` | George Harrison | Secretaria 2 (SECRETARIA_2) |

> **Obs.:** George Harrison (`212121`) é o responsável pela `SECRETARIA_2` (unidade do tipo INTEROPERACIONAL) e possui múltiplos perfis. Para este roteiro, ele deve selecionar **`GESTOR - SECRETARIA_2`** na tela de login.

---

## 📋 Cenário de Teste End-to-End

### 1. Criação e Início do Processo (ADMIN)

**Ator:** Administrador (`191919`) · CDU-03, CDU-04

1.  **Login:** Faça login como `191919`.
2.  **Criar Processo:**
    *   No Painel, clique em **Criar processo**.
    *   Preencha os dados:
        *   **Descrição:** `Teste E2E - Mapeamento Completo [Seu Nome]`
        *   **Tipo:** `MAPEAMENTO`
        *   **Data limite etapa 1:** (uma data futura, ex.: 30 dias a partir de hoje)
    *   Na árvore **Unidades participantes**, expanda `Secretaria 2` → `Coordenadoria 22` e marque o checkbox da `Seção 221`.
    *   Clique em **Salvar**.
    *   ✅ **Resultado Esperado:** O processo é criado e aparece no Painel com a situação "Criado".
3.  **Iniciar Processo:**
    *   Clique na linha do processo recém-criado (situação "Criado") para abrir a tela **Cadastro de processo**.
    *   Clique em **Iniciar processo** e confirme no diálogo.
    *   ✅ **Resultado Esperado:** O processo muda para a situação "Em andamento". O ADMIN continua vendo o processo no Painel. Um alerta de início de processo é gerado para a `Seção 221` e e-mails de notificação são enviados.

### 2. Cadastrar Atividades e Conhecimentos (CHEFE)

**Ator:** Chefe de Seção (`141414`) · CDU-08, CDU-09

1.  **Login:** Faça login como `141414` (Tina Turner).
2.  **Acessar Subprocesso:**
    *   No Painel, o processo deve estar visível (situação "Em andamento"). Clique nele.
    *   O sistema exibe a tela **Detalhes do subprocesso** da `Seção 221` diretamente (CHEFE vai direto ao seu subprocesso).
3.  **Cadastrar Atividades e Conhecimentos:**
    *   Clique no card **Atividades e conhecimentos**.
    *   **Teste de Erro (Validação):**
        *   Adicione a atividade: `Atividade Teste 1` (sem adicionar conhecimento).
        *   Clique em **Disponibilizar**.
        *   ✅ **Resultado Esperado:** O sistema exibe um erro inline na atividade indicando que é necessário pelo menos um conhecimento; a operação é bloqueada.
    *   **Cadastro Correto:**
        *   Adicione o conhecimento `Conhecimento A` à `Atividade Teste 1`.
        *   Adicione a atividade `Atividade Teste 2` com o conhecimento `Conhecimento B`.
4.  **Disponibilizar Cadastro:**
    *   Clique em **Disponibilizar**.
    *   No diálogo de confirmação (título "Disponibilização do cadastro"), clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel com a mensagem "Cadastro de atividades disponibilizado". A situação do subprocesso muda para "Cadastro disponibilizado".

### 3. Análise/Devolução do Cadastro (GESTOR da Coord. 22)

**Ator:** Gestor da Coordenadoria 22 (`131313`) · CDU-13

1.  **Login:** Faça login como `131313` (Mick Jagger).
2.  **Acessar Subprocesso para Análise:**
    *   No Painel, clique no processo.
    *   O sistema exibe a tela **Detalhes do processo**. Clique na `Seção 221` na árvore de unidades.
3.  **Analisar e Devolver:**
    *   Na tela **Detalhes do subprocesso**, clique no card **Atividades e conhecimentos**.
    *   Clique em **Devolver para ajustes**.
    *   No diálogo (título "Devolução"), preencha a **Observação**: `É necessário adicionar uma atividade sobre relatórios.` e clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel com a mensagem "Devolução realizada". A situação do subprocesso volta para "Cadastro em andamento".

### 4. Ajuste e Nova Disponibilização do Cadastro (CHEFE)

**Ator:** Chefe de Seção (`141414`) · CDU-08, CDU-09

1.  **Login:** Faça login como `141414` (Tina Turner).
2.  **Verificar Histórico de Análise:**
    *   Acesse o subprocesso no Painel e clique no card **Atividades e conhecimentos**.
    *   O botão **Histórico de análise** estará visível (pois houve uma devolução). Clique nele.
    *   ✅ **Resultado Esperado:** O modal exibe um registro de "Devolução" com a unidade `COORD_22`, a data/hora e a observação `É necessário adicionar uma atividade sobre relatórios.`.
    *   Feche o modal.
3.  **Realizar Ajuste:**
    *   Adicione a atividade `Elaboração de Relatórios Gerenciais` com o conhecimento `Análise de Dados`.
4.  **Reenviar Cadastro:**
    *   Clique em **Disponibilizar** e confirme.
    *   ✅ **Resultado Esperado:** Mensagem "Cadastro de atividades disponibilizado". O subprocesso é enviado novamente para `COORD_22`.

### 5. Aceite do Cadastro (GESTOR da Coord. 22)

**Ator:** Gestor da Coordenadoria 22 (`131313`) · CDU-13

1.  **Login:** Faça login como `131313` (Mick Jagger).
2.  **Acessar e Analisar:**
    *   Acesse o processo e clique na `Seção 221`.
    *   Clique no card **Atividades e conhecimentos** e verifique se a nova atividade foi incluída.
3.  **Registrar Aceite:**
    *   Clique em **Registrar aceite**.
    *   No diálogo (título "Aceite"), clique em **Confirmar** (sem observação).
    *   ✅ **Resultado Esperado:** Mensagem "Aceite registrado". O subprocesso é enviado para a `SECRETARIA_2` (próximo nível hierárquico).

### 6. Aceite do Cadastro (GESTOR da Secretaria 2)

**Ator:** Gestor da Secretaria 2 (`212121`) · CDU-13

1.  **Login:** Faça login como `212121` (George Harrison). Na tela de login, selecione o perfil **`GESTOR - SECRETARIA_2`**.
2.  **Acessar e Analisar:**
    *   No Painel, clique no processo.
    *   Na tela **Detalhes do processo**, clique na `Seção 221`.
    *   Clique no card **Atividades e conhecimentos**.
3.  **Registrar Aceite:**
    *   Clique em **Registrar aceite**.
    *   No diálogo, clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** Mensagem "Aceite registrado". O subprocesso é enviado para o `ADMIN` (próximo nível hierárquico).

### 7. Homologação do Cadastro (ADMIN)

**Ator:** Administrador (`191919`) · CDU-13

1.  **Login:** Faça login como `191919`.
2.  **Acessar e Homologar:**
    *   No Painel, clique no processo.
    *   Na tela **Detalhes do processo**, clique na `Seção 221`.
    *   Clique no card **Atividades e conhecimentos**.
    *   Clique em **Homologar**.
    *   No diálogo de confirmação (título "Homologação do cadastro de atividades e conhecimentos"), clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para a tela **Detalhes do subprocesso** com a mensagem "Homologação efetivada". A situação do subprocesso muda para "Cadastro homologado".

### 8. Criação e Disponibilização do Mapa (ADMIN)

**Ator:** Administrador (`191919`) · CDU-15, CDU-17

1.  **Acessar Mapa:**
    *   Na tela **Detalhes do subprocesso** da `Seção 221`, clique no card **Mapa de Competências**.
2.  **Criar Competências:**
    *   Clique em **Criar competência**.
    *   Informe a descrição `Gestão de Processos` e associe as atividades `Atividade Teste 1` e `Elaboração de Relatórios Gerenciais`. Clique em **Salvar**.
    *   Clique em **Criar competência** novamente.
    *   Informe `Conhecimento Técnico` e associe a `Atividade Teste 2`. Clique em **Salvar**.
3.  **Disponibilizar Mapa:**
    *   Clique no botão **Disponibilizar** (no canto superior direito da tela de Edição de mapa).
    *   No modal de disponibilização (título "Disponibilização do mapa de competências"), preencha a **Data limite para validação** e clique em **Disponibilizar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel com a mensagem "Disponibilização do mapa de competências efetuada". A situação do subprocesso muda para "Mapa disponibilizado".

### 9. Validação do Mapa (CHEFE)

**Ator:** Chefe de Seção (`141414`) · CDU-19

1.  **Login:** Faça login como `141414` (Tina Turner).
2.  **Acessar Mapa:**
    *   No Painel, clique no processo e, na tela **Detalhes do subprocesso**, clique no card **Mapa de Competências**.
    *   O sistema exibe a tela **Visualização de mapa** com os botões **Apresentar sugestões** e **Validar**.
3.  **Validar Mapa:**
    *   Clique em **Validar**.
    *   No diálogo de confirmação (título "Validação do mapa de competências"), clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** Mensagem "Mapa validado e submetido para análise à unidade superior". A situação do subprocesso muda para "Mapa validado". O mapa é enviado para a `COORD_22`.

### 10. Aceite da Validação do Mapa (GESTOR da Coord. 22)

**Ator:** Gestor da Coordenadoria 22 (`131313`) · CDU-20

1.  **Login:** Faça login como `131313` (Mick Jagger).
2.  **Acessar Mapa:**
    *   No Painel, clique no processo.
    *   Na tela **Detalhes do processo**, clique na `Seção 221`.
    *   Clique no card **Mapa de Competências**.
3.  **Registrar Aceite:**
    *   Clique em **Registrar aceite**.
    *   No diálogo (título "Aceite"), clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** Mensagem "Aceite registrado". O mapa é enviado para a `SECRETARIA_2`.

### 11. Aceite da Validação do Mapa (GESTOR da Secretaria 2)

**Ator:** Gestor da Secretaria 2 (`212121`) · CDU-20

1.  **Login:** Faça login como `212121` (George Harrison), selecionando o perfil **`GESTOR - SECRETARIA_2`**.
2.  **Acessar Mapa:**
    *   No Painel, clique no processo.
    *   Na tela **Detalhes do processo**, clique na `Seção 221`.
    *   Clique no card **Mapa de Competências**.
3.  **Registrar Aceite:**
    *   Clique em **Registrar aceite**.
    *   No diálogo, clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** Mensagem "Aceite registrado". O mapa é enviado para o `ADMIN`.

### 12. Homologação do Mapa (ADMIN)

**Ator:** Administrador (`191919`) · CDU-20

1.  **Login:** Faça login como `191919`.
2.  **Acessar e Homologar Mapa:**
    *   No Painel, clique no processo.
    *   Na tela **Detalhes do processo**, clique na `Seção 221`.
    *   Clique no card **Mapa de Competências**.
    *   Clique em **Homologar**.
    *   No diálogo (título "Homologação"), clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** Mensagem "Homologação efetivada". A situação do subprocesso muda para "Mapa homologado".

### 13. Finalização do Processo (ADMIN)

**Ator:** Administrador (`191919`) · CDU-21

1.  **Acessar Processo:**
    *   No Painel, clique no processo. O sistema exibe a tela **Detalhes do processo**.
2.  **Finalizar Processo:**
    *   Clique no botão **Finalizar processo**.
    *   ✅ **Validação:** O sistema verifica se todos os subprocessos estão com situação "Mapa homologado". Como a `Seção 221` já foi homologada, a operação prossegue.
    *   No diálogo de confirmação (título "Finalização de processo"), clique em **Confirmar**.
    *   ✅ **Resultado Esperado:** O sistema redireciona para o Painel com a mensagem "Processo finalizado". A situação do processo muda para "Finalizado". O mapa de competências da `Seção 221` torna-se vigente. Notificações por e-mail são enviadas a todas as unidades participantes.

---
**Fim do Teste.**

Este roteiro cobre o "caminho feliz" e um cenário de desvio (devolução e reenvio do cadastro), garantindo uma cobertura abrangente do ciclo de vida completo de um processo de mapeamento, incluindo todos os níveis hierárquicos da cadeia de análise: `SECAO_221 → COORD_22 → SECRETARIA_2 → ADMIN`.
