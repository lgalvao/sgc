# Plano de Implementação e Correção para CDU-13

Este documento detalha os desvios encontrados na implementação do Caso de Uso 13 (Analisar cadastro de atividades e conhecimentos) e apresenta um plano de ação para alinhar o código com a especificação.

## 1. Sumário dos Desvios Encontrados

A análise do código-fonte revelou que a implementação da CDU-13 está incompleta e apresenta os seguintes desvios em relação ao documento de requisitos (`reqs/cdu-13.md`):

### 1.1. Fluxo "Devolver para ajustes" (Item 9)
- **Desvio:** A gravação da análise de devolução está incorreta. O sistema não armazena o resultado (`DEVOLUCAO`) de forma estruturada no campo `acao` da entidade `AnaliseCadastro`. Em vez disso, concatena o motivo e as observações no campo `observacoes`.
- **Impacto:** Impede a consulta e a geração de relatórios estruturados sobre os resultados das análises.

### 1.2. Fluxo "Registrar aceite" (Item 10)
- **Desvio:** O conteúdo das notificações (e-mail e alerta) enviadas à unidade superior não corresponde ao especificado. O texto implementado ("...aceito e aguardando homologação") difere do texto requerido ("...submetido para análise").
- **Impacto:** A comunicação com o usuário é inconsistente com o fluxo de trabalho definido, podendo gerar confusão sobre a próxima ação a ser tomada.

### 1.3. Fluxo "Homologar" (Item 11)
- **Desvio:** O registro de movimentação (`Movimentacao`) para a homologação utiliza a unidade superior como origem e destino, em vez da unidade "SEDOC" conforme exigido pela especificação.
- **Impacto:** A trilha de auditoria do processo não reflete corretamente a responsabilidade da unidade homologadora.

### 1.4. Funcionalidade "Histórico de análise" (Item 7)
- **Desvio:** A funcionalidade está completamente ausente. Não existe um endpoint na API nem um método no serviço para consultar o histórico de análises de cadastro de um subprocesso.
- **Impacto:** O usuário não tem como consultar as análises prévias, uma funcionalidade essencial para subsidiar a tomada de decisão, conforme descrito no requisito.

---

## 2. Plano de Ação Detalhado

O plano a seguir descreve os passos necessários para corrigir os desvios e implementar as funcionalidades faltantes.

### Passo 1: Corrigir o Fluxo de Devolução
1.  **Modificar `SubprocessoService.devolverCadastro`:**
    *   Alterar a criação da entidade `AnaliseCadastro` para usar os campos corretos.
    *   Definir a ação: `analise.setAcao(TipoAcaoAnalise.DEVOLUCAO);`
    *   Definir o motivo: `analise.setMotivo(motivo);`
    *   Definir as observações: `analise.setObservacoes(observacoes);`
2.  **Criar Teste de Integração:**
    *   Adicionar um teste em `CDU13IntegrationTest.java` (ou criar o arquivo se não existir) que execute a devolução e verifique se o registro `AnaliseCadastro` foi salvo no banco de dados com os campos `acao` e `motivo` preenchidos corretamente.

### Passo 2: Corrigir o Fluxo de Aceite
1.  **Modificar `SubprocessoService.notificarAceiteCadastro`:**
    *   Atualizar o `assunto` e o `corpo` do e-mail para corresponder exatamente ao texto do requisito 10.7.
    *   Atualizar a `descricao` do `Alerta` para corresponder exatamente ao texto do requisito 10.8.
2.  **Atualizar Teste de Integração:**
    *   Localizar o teste existente para o aceite e atualizar as asserções para verificar o novo conteúdo das notificações.

### Passo 3: Corrigir o Fluxo de Homologação
1.  **Injetar `UnidadeRepo` em `SubprocessoService`:**
    *   Adicionar `private final UnidadeRepo unidadeRepo;` ao serviço.
2.  **Modificar `SubprocessoService.homologarCadastro`:**
    *   Antes de criar a `Movimentacao`, buscar a unidade SEDOC: `Unidade sedoc = unidadeRepo.findBySigla("SEDOC").orElseThrow(...);`.
    *   Utilizar a `sedoc` como `unidadeOrigem` e `unidadeDestino` na criação da `Movimentacao`.
3.  **Atualizar Teste de Integração:**
    *   Garantir que a unidade "SEDOC" exista nos dados de teste.
    *   Atualizar o teste de homologação para verificar se a `Movimentacao` foi criada com a unidade de origem e destino corretas.

### Passo 4: Implementar a Funcionalidade de Histórico
1.  **Criar DTO `AnaliseCadastroDto`:**
    *   Criar um novo record ou classe `AnaliseCadastroDto` no pacote `sgc.analise.dto` com os campos: `LocalDateTime dataHora`, `String unidadeSigla`, `String resultado`, `String observacoes`.
2.  **Adicionar Método no Repositório `AnaliseCadastroRepo`:**
    *   Adicionar a seguinte assinatura de método na interface: `List<AnaliseCadastro> findBySubprocessoCodigoOrderByDataHoraDesc(Long subprocessoCodigo);`
3.  **Implementar Lógica no `SubprocessoService`:**
    *   Criar um novo método público `getHistoricoAnaliseCadastro(Long subprocessoId)`.
    *   Dentro do método, chamar o novo método do repositório.
    *   Mapear a lista de entidades `AnaliseCadastro` para uma lista de `AnaliseCadastroDto`. O campo `resultado` do DTO deve ser populado a partir do `analise.getAcao().name()`.
4.  **Expor Endpoint no `SubprocessoControle`:**
    *   Criar um novo endpoint `GET /api/subprocessos/{id}/historico-cadastro`.
    *   O método do controlador deve chamar `subprocessoService.getHistoricoAnaliseCadastro(id)` e retornar `ResponseEntity.ok()` com a lista de DTOs.
5.  **Criar Teste de Integração para o Histórico:**
    *   Adicionar um novo teste em `CDU13IntegrationTest.java`.
    *   No teste:
        a. Criar um subprocesso e movê-lo para o estado `CADASTRO_DISPONIBILIZADO`.
        b. Realizar uma ação de "devolver".
        c. Realizar uma ação de "aceitar".
        d. Chamar o novo endpoint `GET /api/subprocessos/{id}/historico-cadastro`.
        e. Verificar se a resposta tem status 200 e contém dois itens no corpo, com os dados corretos para a devolução e o aceite.