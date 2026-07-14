# Controle de Acesso e Regras de Negócio — SGC

## 1. Visão geral

As regras de acesso do SGC baseiam-se em dois eixos:

| Eixo            | O que controla         | Critério                           |
|-----------------|------------------------|------------------------------------|
| **Hierarquia**  | Visualização (Leitura) | Unidade responsável do subprocesso |
| **Localização** | Execução (Escrita)     | Localização atual do subprocesso   |

**Regra de ouro:** O usuário só pode executar ações de escrita em um subprocesso se este estiver **localizado na sua
unidade ativa** — incluindo o perfil ADMIN. Mas há algumas exceções, conforme descrito abaixo.

## 2. Perfis de Acesso

| Perfil       | Escopo de visualização                 | Responsabilidades principais                                                                                                  |
|--------------|----------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| **ADMIN**    | Todo o sistema                         | Criar/editar/iniciar processos, homologar cadastros, mapas e diagnósticos, criar admins, configurar sistema, gerar relatórios |
| **GESTOR**   | Sua unidade + subordinadas (recursivo) | Aceitar cadastros e mapas, devolver para ajustes, gerar relatórios selecionados de sua subarvores de unidades.                |
| **CHEFE**    | Apenas sua unidade                     | Cadastrar atividades e conhecimentos, disponibilizar cadastro, validar mapa, apresentar sugestões em mapa                     |
| **SERVIDOR** | Apenas sua unidade                     | Realizar a própria autoavaliação e aprovar a própria avaliação de consenso                                                    |

## 3. Regras de Visualização

Validadas no método `checkHierarquia` do `SgcPermissionEvaluator`:

- **ADMIN:** `return true` — acesso global.
- **GESTOR:** `hierarquiaService.ehMesmaOuSubordinada(unidadeAlvo, unidadeUsuario)` — vê sua unidade e todas as
  subordinadas.
- **CHEFE / SERVIDOR:** `usuario.getUnidadeAtivaCodigo() == unidadeAlvo.getCodigo()` — vê apenas sua própria unidade.

### Exceção: VERIFICAR_IMPACTOS

A ação `VERIFICAR_IMPACTOS` **não depende de localização** no Evaluator (apenas retorna `true` se o perfil for CHEFE,
GESTOR ou ADMIN). O controle fino de em quais situações cada perfil pode ver os impactos é feito no **serviço**
(`ImpactoMapaService`), não no Evaluator.

### Exceção: Alertas (Painel)

As regras de visualização de alertas **não seguem a hierarquia recursiva**. A visibilidade é determinada pelo perfil e
pelo destino do alerta (CDU-02):

- **Perfil SERVIDOR:**
    * Vê **apenas** os alertas exclusivos (**pessoais**) direcionados ao seu título de eleitor.
    * **Não vê** alertas da unidade, mesmo que seja sua unidade ativa.
- **Outros Perfis (ADMIN, GESTOR, CHEFE):**
    * Veem os alertas exclusivos (**pessoais**) direcionados a eles.
    * Veem **também** alertas coletivos da sua **unidade ativa** (alertas que não possuem um usuário de destino
      específico).

### Diagnóstico: leitura por hierarquia

`VISUALIZAR_DIAGNOSTICO` é uma permissão de **leitura** específica para contexto, equipe, unidade e histórico do
Diagnóstico. Ela segue a regra de hierarquia da unidade responsável:

- **ADMIN:** visualiza qualquer subprocesso de Diagnóstico.
- **GESTOR:** visualiza a própria unidade e todas as unidades subordinadas, recursivamente.
- **CHEFE / SERVIDOR:** visualiza somente o subprocesso da própria unidade.

Visualizar um subprocesso não concede permissão para executar ações nele. As ações de escrita continuam dependendo da
localização atual do subprocesso.

## 4. Regras de Execução (Escrita)

### 4.1 Ações dependentes de localização

Definidas em `AcaoPermissao` como ações do tipo `ESCRITA`. Para ações executadas sobre subprocesso, a regra é:

```
usuario.getUnidadeAtivaCodigo() == localizacaoAtual(subprocesso).getCodigo()
```

O `SgcPermissionEvaluator` verifica também se o perfil ativo é compatível com a ação:

| Ação                              | Perfil necessário | CDU        |
|-----------------------------------|-------------------|------------|
| `EDITAR_CADASTRO`                 | CHEFE             | 08         |
| `DISPONIBILIZAR_CADASTRO`         | CHEFE             | 09         |
| `EDITAR_REVISAO_CADASTRO`         | CHEFE             | 08         |
| `DISPONIBILIZAR_REVISAO_CADASTRO` | CHEFE             | 10         |
| `IMPORTAR_ATIVIDADES`             | CHEFE             | 08         |
| `DEVOLVER_CADASTRO`               | ADMIN, GESTOR     | 13         |
| `DEVOLVER_REVISAO_CADASTRO`       | ADMIN, GESTOR     | 14         |
| `ACEITAR_CADASTRO`                | GESTOR            | 13         |
| `ACEITAR_REVISAO_CADASTRO`        | GESTOR            | 14         |
| `HOMOLOGAR_CADASTRO`              | ADMIN             | 13         |
| `HOMOLOGAR_REVISAO_CADASTRO`      | ADMIN             | 14         |
| `EDITAR_MAPA`                     | ADMIN             | 15         |
| `DISPONIBILIZAR_MAPA`             | ADMIN             | 17         |
| `AJUSTAR_MAPA`                    | ADMIN             | 16         |
| `APRESENTAR_SUGESTOES`            | CHEFE             | 19         |
| `VALIDAR_MAPA`                    | CHEFE             | 19         |
| `DEVOLVER_MAPA`                   | ADMIN, GESTOR     | 20         |
| `ACEITAR_MAPA`                    | GESTOR            | 20         |
| `HOMOLOGAR_MAPA`                  | ADMIN             | 20         |
| `PREENCHER_AUTOAVALIACAO`         | SERVIDOR          | 44         |
| `CRIAR_CONSENSO`                  | CHEFE             | 45, 47, 48 |
| `CONCLUIR_DIAGNOSTICO`            | CHEFE             | 49         |
| `VALIDAR_DIAGNOSTICO`             | GESTOR            | 50         |
| `DEVOLVER_DIAGNOSTICO`            | ADMIN, GESTOR     | 50         |
| `HOMOLOGAR_DIAGNOSTICO`           | ADMIN             | 50         |

`VISUALIZAR_DIAGNOSTICO` é a ação de leitura usada pelas consultas do fluxo. As ações listadas acima são estruturais:
as de escrita exigem também que a unidade ativa do usuário seja a localização atual do subprocesso.

### 4.1.1 Ações de leitura e de processo do Evaluator

| Ação                           | Tipo     | Perfil / regra                                                                                                           |
|--------------------------------|----------|--------------------------------------------------------------------------------------------------------------------------|
| `VISUALIZAR_SUBPROCESSO`       | Leitura  | ADMIN global; GESTOR pela hierarquia; CHEFE e SERVIDOR na própria unidade                                                |
| `VISUALIZAR_DIAGNOSTICO`       | Leitura  | Mesma regra de `VISUALIZAR_SUBPROCESSO`                                                                                  |
| `CONSULTAR_PARA_IMPORTACAO`    | Leitura  | CHEFE; permite consultar subprocesso finalizado ou da própria hierarquia para importar atividades                        |
| `VERIFICAR_IMPACTOS`           | Leitura  | CHEFE, GESTOR ou ADMIN; situação e demais restrições são verificadas por `ImpactoMapaService`                            |
| `VISUALIZAR_PROCESSO`          | Leitura  | Qualquer perfil autenticado; os endpoints de detalhes aplicam a checagem complementar de acesso ao processo              |
| `FINALIZAR_PROCESSO`           | Processo | ADMIN; processo não pode estar finalizado. O serviço exige todos os subprocessos homologados conforme o tipo do processo |
| `ACEITAR_CADASTRO_EM_BLOCO`    | Processo | GESTOR                                                                                                                   |
| `HOMOLOGAR_CADASTRO_EM_BLOCO`  | Processo | ADMIN                                                                                                                    |
| `ACEITAR_MAPA_EM_BLOCO`        | Processo | GESTOR                                                                                                                   |
| `HOMOLOGAR_MAPA_EM_BLOCO`      | Processo | ADMIN                                                                                                                    |
| `DISPONIBILIZAR_MAPA_EM_BLOCO` | Processo | ADMIN                                                                                                                    |

### 4.2 Ações administrativas (sem dependência de localização)

Protegidas com `@PreAuthorize("hasRole('ADMIN')")` diretamente no controller:

| Endpoint                                                                           | Ação                                          | CDU   |
|------------------------------------------------------------------------------------|-----------------------------------------------|-------|
| `POST /api/processos`                                                              | Criar processo                                | 03    |
| `POST /api/processos/{codigo}/atualizar`                                           | Editar processo                               | 03    |
| `POST /api/processos/{codigo}/excluir`                                             | Excluir processo                              | 03    |
| `POST /api/processos/{codigo}/iniciar`                                             | Iniciar processo                              | 04/05 |
| `POST /api/processos/{codigo}/finalizar`                                           | Finalizar processo                            | 21    |
| `POST /api/processos/{codigo}/enviar-lembrete`                                     | Enviar lembrete de prazo                      | 34    |
| `POST /api/subprocessos/{codigo}/data-limite`                                      | Alterar data limite                           | 27    |
| `POST /api/subprocessos/{codigo}/reabrir-cadastro`                                 | Reabrir cadastro                              | 32    |
| `POST /api/subprocessos/{codigo}/reabrir-revisao-cadastro`                         | Reabrir revisão                               | 33    |
| `POST /api/subprocessos`                                                           | Criar subprocesso                             | —     |
| `POST /api/subprocessos/{codigo}/atualizar`                                        | Atualizar subprocesso                         | —     |
| `POST /api/subprocessos/{codigo}/excluir`                                          | Excluir subprocesso                           | —     |
| `GET /api/subprocessos`                                                            | Listar subprocessos                           | —     |
| `GET /api/configuracoes`                                                           | Listar configurações                          | 31    |
| `POST /api/configuracoes`                                                          | Atualizar configurações                       | 31    |
| `GET /api/usuarios/administradores`                                                | Listar administradores                        | 30    |
| `POST /api/usuarios/administradores`                                               | Adicionar administrador                       | 30    |
| `POST /api/usuarios/administradores/{titulo}/remover`                              | Remover administrador                         | 30    |
| `POST /api/unidades/{codigo}/atribuicoes-temporarias`                              | Criar atribuição temporária                   | 28    |
| `GET /api/unidades/atribuicoes`                                                    | Listar atribuições                            | 28    |
| `GET /api/unidades/{codigo}/atribuicoes-temporarias`                               | Consultar atribuições temporárias             | 28    |
| `POST /api/unidades/{codigo}/atribuicoes-temporarias/{codigoAtribuicao}/atualizar` | Atualizar atribuição temporária               | 28    |
| `POST /api/unidades/{codigo}/atribuicoes-temporarias/{codigoAtribuicao}/excluir`   | Excluir atribuição temporária                 | 28    |
| `POST /api/mapas`                                                                  | Criar mapa                                    | 15    |
| `POST /api/mapas/{codigo}/atualizar`                                               | Atualizar mapa                                | 15    |
| `POST /api/mapas/{codigo}/excluir`                                                 | Excluir mapa                                  | 15    |
| `GET /api/admin/notificacoes/listar`                                               | Listar notificações administrativas           | 38    |
| `POST /api/admin/notificacoes/{codigo}/reenviar`                                   | Reenfileirar notificação com falha definitiva | 38    |
| `GET /api/admin/notificacoes/leitor-email-testes`                                  | Consultar URL do leitor de e-mails de testes  | 38    |
| `POST /api/processos/{codigo}/excluir-completo`                                    | Excluir processo e seus dados relacionados    | —     |

### 4.2.1 Feedback contextual

O módulo de feedback só é disponibilizado nos perfis de ambiente `hom`, `e2e` e `test`; em produção os endpoints não
existem. Suas permissões são:

| Endpoint                                | Ação                          | Perfil / controle            | CDU |
|-----------------------------------------|-------------------------------|------------------------------|-----|
| `POST /api/feedback`                    | Registrar feedback contextual | Qualquer usuário autenticado | 39  |
| `GET /api/feedback/listar`              | Listar feedbacks enviados     | ADMIN                        | 40  |
| `GET /api/feedback/{codigo}/screenshot` | Exibir screenshot de feedback | ADMIN                        | 40  |

### 4.3 Ações de Relatórios (Acesso por ADMIN ou Hierarquia)

Estes endpoints permitem a geração e exportação de relatórios, com acesso flexibilizado para administradores, gestores
ou usuários da hierarquia do processo/unidade alvo:

| Endpoint                                                                 | Ação                                                   | Perfil / Controle                                     | CDU |
|--------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------------------------------|-----|
| `GET /api/relatorios/andamento/{codigo}`                                 | Visualizar andamento do processo em JSON               | ADMIN ou Hierarquia (`@processoService.checarAcesso`) | 35  |
| `GET /api/relatorios/andamento/{codigo}/exportar`                        | Exportar relatório de andamento em PDF                 | ADMIN ou Hierarquia (`@processoService.checarAcesso`) | 35  |
| `GET /api/relatorios/mapas`                                              | Visualizar consolidado de mapas em JSON                | `hasAnyRole('ADMIN', 'GESTOR')`                       | 36  |
| `GET /api/relatorios/mapas/exportar`                                     | Exportar relatório consolidado de mapas em PDF         | `hasAnyRole('ADMIN', 'GESTOR')`                       | 36  |
| `GET /api/relatorios/mapas/subprocessos/{codigo}`                        | Visualizar relatório do mapa atual do subprocesso      | `VISUALIZAR_SUBPROCESSO`                              | 18  |
| `GET /api/relatorios/mapas/subprocessos/{codigo}/exportar`               | Exportar relatório do mapa atual do subprocesso em PDF | `VISUALIZAR_SUBPROCESSO`                              | 18  |
| `GET /api/relatorios/mapas-vigentes/unidades/{codigo}`                   | Visualizar mapa vigente de uma unidade                 | ADMIN, GESTOR ou CHEFE                                | 18  |
| `GET /api/relatorios/mapas-vigentes/unidades/{codigo}/exportar`          | Exportar mapa vigente de uma unidade em PDF            | ADMIN, GESTOR ou CHEFE                                | 18  |
| `GET /api/relatorios/unidades-sem-mapas-vigentes/exportar`               | Exportar relatório de unidades sem mapa vigente em PDF | `hasRole('ADMIN')`                                    | 37  |
| `GET /api/relatorios/unidades-sem-mapas-vigentes`                        | Visualizar unidades sem mapa vigente em JSON           | `hasRole('ADMIN')`                                    | 37  |
| `GET /api/relatorios/diagnostico/gaps/{codigo}`                          | Visualizar relatório de gaps de Diagnóstico            | ADMIN ou GESTOR + acesso ao processo                  | 54  |
| `GET /api/relatorios/diagnostico/gaps/{codigo}/exportar`                 | Exportar relatório de gaps de Diagnóstico em PDF       | ADMIN ou GESTOR + acesso ao processo                  | 54  |
| `GET /api/relatorios/diagnostico/situacao-capacitacao/{codigo}`          | Visualizar situação de capacitação                     | ADMIN ou GESTOR + acesso ao processo                  | 55  |
| `GET /api/relatorios/diagnostico/situacao-capacitacao/{codigo}/exportar` | Exportar situação de capacitação em PDF                | ADMIN ou GESTOR + acesso ao processo                  | 55  |

Nos relatórios de Diagnóstico, o parâmetro de unidades não amplia o escopo autorizado: o **GESTOR** só pode selecionar a
própria unidade e suas subordinadas, recursivamente; o **ADMIN** pode selecionar todas as unidades participantes. Os
relatórios são agregados e não expõem nomes de servidores.

### 4.4 Consultas auxiliares de Unidades

| Endpoint                             | Ação                                                              | Perfil / Controle  | CDU |
|--------------------------------------|-------------------------------------------------------------------|--------------------|-----|
| `GET /api/unidades/sem-mapa-vigente` | Listar códigos de unidades sem mapa vigente para pré-visualização | `hasRole('ADMIN')` | 37  |

### 4.5 Consultas de apoio e visibilidade de dados

Os endpoints abaixo não executam transição de workflow. Quando o controlador não declara uma regra mais restritiva,
exigem apenas autenticação; a tela consumidora ainda deve respeitar as permissões de visualização do recurso exibido.

| Recurso                   | Endpoints / regra de acesso                                                                                                                                                                                                                  |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Processo                  | Detalhes, contexto completo e subprocessos de um processo: ADMIN ou `processoService.checarAcesso`; processos ativos/finalizados e unidades bloqueadas: autenticado; subprocessos elegíveis para ação em bloco: ADMIN ou GESTOR              |
| Subprocesso               | Detalhe, status, contexto de edição, permissões de UI, mapa, sugestões e históricos: `VISUALIZAR_SUBPROCESSO`; busca do código e busca de contexto por processo/unidade: autenticado, com validação do recurso no serviço ou pós-autorização |
| Cadastro                  | Contexto, cadastro, validações e histórico: `VISUALIZAR_SUBPROCESSO`; atividades disponíveis para importação: `CONSULTAR_PARA_IMPORTACAO`                                                                                                    |
| Mapas e atividades        | Listagem de mapas: ADMIN, GESTOR ou CHEFE; detalhe de mapa e leitura de atividade/conhecimentos: `VISUALIZAR_SUBPROCESSO`; alterações de atividade/conhecimento: CHEFE, com validação fina em `AtividadeService` |
| Unidades e usuários       | Consulta de usuários de unidade e de usuário por título: ADMIN, GESTOR ou CHEFE; demais consultas organizacionais e pesquisa de usuários: autenticado, salvo regra específica do endpoint                                                    |
| Configuração operacional  | `GET /api/configuracoes/dias-inativacao-processo`: autenticado; demais leitura e alteração de configurações: ADMIN                                                                                                                           |
| Painel, alertas e eventos | Autenticado; a resposta de alertas deve obedecer à regra específica do CDU-02 descrita nesta especificação                                                                                                                                   |

## 5. Ações em Bloco

### 5.1 Via endpoints dedicados no SubprocessoController

| Endpoint                          | Ação (Evaluator)                        | Perfil | CDU |
|-----------------------------------|-----------------------------------------|--------|-----|
| `POST /aceitar-cadastro-bloco`    | `ACEITAR_CADASTRO` (por subprocesso)    | GESTOR | 22  |
| `POST /homologar-cadastro-bloco`  | `HOMOLOGAR_CADASTRO` (por subprocesso)  | ADMIN  | 23  |
| `POST /disponibilizar-mapa-bloco` | `DISPONIBILIZAR_MAPA` (por subprocesso) | ADMIN  | 24  |
| `POST /aceitar-validacao-bloco`   | `ACEITAR_MAPA` (por subprocesso)        | GESTOR | 25  |
| `POST /homologar-validacao-bloco` | `HOMOLOGAR_MAPA` (por subprocesso)      | ADMIN  | 26  |

### 5.2 Via endpoint genérico no ProcessoController

`POST /api/processos/{codigo}/acao-em-bloco` — protegido com `hasAnyRole('ADMIN', 'GESTOR')`.

A `ProcessoService.executarAcaoEmBloco()` faz a verificação fina de permissão internamente via
`permissionEvaluator.verificarPermissao()`, categorizando cada subprocesso na ação correta. Para Diagnóstico, as ações
da UI `aceitar-diagnostico` e `homologar-diagnostico` usam o mesmo endpoint genérico e correspondem, respectivamente, a
`VALIDAR_DIAGNOSTICO` e `HOMOLOGAR_DIAGNOSTICO`.

### 5.3 Permissões no nível de Processo (checkProcesso)

O `checkProcesso` do Evaluator valida ações a nível de processo (não subprocesso):

| Ação                           | Perfil   | Condição extra                       |
|--------------------------------|----------|--------------------------------------|
| `VISUALIZAR_PROCESSO`          | Qualquer | —                                    |
| `FINALIZAR_PROCESSO`           | ADMIN    | Processo não pode estar `FINALIZADO` |
| `ACEITAR_CADASTRO_EM_BLOCO`    | GESTOR   | —                                    |
| `HOMOLOGAR_CADASTRO_EM_BLOCO`  | ADMIN    | —                                    |
| `ACEITAR_MAPA_EM_BLOCO`        | GESTOR   | —                                    |
| `HOMOLOGAR_MAPA_EM_BLOCO`      | ADMIN    | —                                    |
| `DISPONIBILIZAR_MAPA_EM_BLOCO` | ADMIN    | —                                    |

### 5.4 Finalização de processos

`POST /api/processos/{codigo}/finalizar` exige ADMIN e processo ainda não finalizado. Antes de alterar a situação, o
serviço valida o fluxo correspondente:

- **Mapeamento e revisão (CDU-21):** todos os subprocessos das unidades participantes devem estar com mapa homologado.
- **Diagnóstico (CDU-53):** todos os subprocessos das unidades participantes devem estar em
  `DIAGNOSTICO_HOMOLOGADO`.

A finalização é uma ação de processo; não depende da localização de um subprocesso individual.

---

## 6. Atividades e Conhecimentos

O `AtividadeController` usa `hasRole('CHEFE')` para CRUD de atividades e conhecimentos. A verificação fina é feita na
`AtividadeService.verificarPermissaoEdicao()`:

1. **Permissão de localização:** Checa `EDITAR_CADASTRO` via `permissionEvaluator` (CHEFE + localização).
2. **Validação de situação:** Permite edição apenas nas seguintes situações do subprocesso:
    - `NAO_INICIADO`
    - `MAPEAMENTO_CADASTRO_EM_ANDAMENTO`
    - `REVISAO_CADASTRO_EM_ANDAMENTO`
    - `MAPEAMENTO_MAPA_CRIADO`
    - `MAPEAMENTO_MAPA_COM_SUGESTOES`
    - `REVISAO_MAPA_AJUSTADO`
    - `REVISAO_MAPA_COM_SUGESTOES`

### 6.1 Exceção de UX para controles de edição

Mesmo quando o backend indicar que o perfil possui a ação estrutural de edição, o frontend deve **ocultar** os controles
de edição (em vez de apenas desabilitar) nos casos abaixo, quando a situação do subprocesso não permitir edição no
workflow atual:

- **Cadastro de atividades:** perfil **CHEFE**.
- **Cadastro de mapa:** perfil **ADMIN**.

Essa exceção é intencional e restrita às telas de cadastro/edição citadas acima.

## 7. Importação de Atividades

- `POST /subprocessos/{codigo}/importar-atividades` — protegido com
  `hasPermission(#id, 'Subprocesso', 'IMPORTAR_ATIVIDADES')`.
- `IMPORTAR_ATIVIDADES` exige perfil **CHEFE** + localização.
- `GET /subprocessos/{codigo}/atividades-importacao` — protegido com `isAuthenticated()`.
- A ação `CONSULTAR_PARA_IMPORTACAO` no Evaluator permite que CHEFE consulte subprocessos finalizados ou da sua
  hierarquia.

## 8. Situações do Subprocesso

### 8.1 Mapeamento

```
NAO_INICIADO → MAPEAMENTO_CADASTRO_EM_ANDAMENTO → MAPEAMENTO_CADASTRO_DISPONIBILIZADO
    → MAPEAMENTO_CADASTRO_HOMOLOGADO → MAPEAMENTO_MAPA_CRIADO → MAPEAMENTO_MAPA_DISPONIBILIZADO
    → MAPEAMENTO_MAPA_COM_SUGESTOES / MAPEAMENTO_MAPA_VALIDADO → MAPEAMENTO_MAPA_HOMOLOGADO
```

### 8.2 Revisão

```
NAO_INICIADO → REVISAO_CADASTRO_EM_ANDAMENTO → REVISAO_CADASTRO_DISPONIBILIZADA
    → REVISAO_CADASTRO_HOMOLOGADA → REVISAO_MAPA_AJUSTADO → REVISAO_MAPA_DISPONIBILIZADO
    → REVISAO_MAPA_COM_SUGESTOES / REVISAO_MAPA_VALIDADO → REVISAO_MAPA_HOMOLOGADO
```

### 8.3 Diagnóstico

```
NAO_INICIADO → DIAGNOSTICO_EM_ANDAMENTO → DIAGNOSTICO_CONCLUIDO → DIAGNOSTICO_HOMOLOGADO
```

#### 8.3.1 Ações de diagnóstico por etapa

As permissões finas de diagnóstico dependem de perfil, localização atual do subprocesso e situação do subprocesso:

- `PREENCHER_AUTOAVALIACAO`: SERVIDOR, para a própria avaliação, com subprocesso localizado na própria unidade.
- `CRIAR_CONSENSO`: CHEFE, para manter/concluir o consenso dos servidores da unidade, com subprocesso localizado na
  própria unidade.
- `CONCLUIR_DIAGNOSTICO`: CHEFE, com subprocesso em `DIAGNOSTICO_EM_ANDAMENTO` e localizado na própria unidade.
- `VALIDAR_DIAGNOSTICO`: GESTOR, com subprocesso em `DIAGNOSTICO_CONCLUIDO` e localizado na unidade ativa do gestor.
- `DEVOLVER_DIAGNOSTICO`: ADMIN ou GESTOR, com subprocesso em `DIAGNOSTICO_CONCLUIDO` e localizado na unidade ativa do
  usuário.
- `HOMOLOGAR_DIAGNOSTICO`: ADMIN, com subprocesso em `DIAGNOSTICO_CONCLUIDO` e localizado na unidade ativa do usuário.

#### Regras internas do Diagnóstico

- O SERVIDOR só aprova a própria avaliação de consenso. A aprovação não concede edição do consenso de outros servidores.
- O CHEFE pode manter o consenso, indicar ou reverter a impossibilidade de avaliação e preencher a situação de
  capacitação, sempre na própria unidade e enquanto o subprocesso estiver no fluxo de Diagnóstico.
- O card de `Situação de capacitação` só fica habilitado para CHEFE quando existir pelo menos uma avaliação de consenso
  aprovada. A edição só fica disponível para servidor cuja avaliação esteja aprovada.
- O Diagnóstico só pode ser concluído quando todos os servidores estiverem com consenso aprovado ou avaliação
  impossibilitada e as situações de capacitação exigidas estiverem preenchidas.
- A devolução retorna as avaliações elegíveis para `Autoavaliação concluída`, reabrindo a manutenção do consenso; uma
  avaliação impossibilitada permanece nessa situação.
- Após a homologação, o subprocesso fica em `DIAGNOSTICO_HOMOLOGADO`; nenhuma ação de escrita do fluxo fica habilitada.

#### Consultas e endpoints do fluxo

Os endpoints de consulta usam `VISUALIZAR_DIAGNOSTICO`. Os endpoints de escrita usam a ação correspondente:

| Operação                                                                                      | Permissão                 |
|-----------------------------------------------------------------------------------------------|---------------------------|
| Contexto, autoavaliação, consenso de outro servidor, diagnóstico da unidade e histórico       | `VISUALIZAR_DIAGNOSTICO`  |
| Salvar/concluir autoavaliação e aprovar consenso do próprio servidor                          | `PREENCHER_AUTOAVALIACAO` |
| Salvar/concluir consenso, impossibilitar/reverter avaliação e salvar situações de capacitação | `CRIAR_CONSENSO`          |
| Concluir unidade e validar pré-condição de conclusão                                          | `CONCLUIR_DIAGNOSTICO`    |
| Validar diagnóstico e validar a ação de aceite                                                | `VALIDAR_DIAGNOSTICO`     |
| Devolver diagnóstico e validar a devolução                                                    | `DEVOLVER_DIAGNOSTICO`    |
| Homologar diagnóstico e validar a homologação                                                 | `HOMOLOGAR_DIAGNOSTICO`   |

As permissões `pode...` indicam a permissão estrutural do perfil. As permissões `habilitar...` acrescentam situação do
workflow, localização e, quando aplicável, a existência de consenso aprovado. A UI deve esconder cards que não pertencem
ao perfil e desabilitar ações permitidas, mas momentaneamente indisponíveis.

Observação:

- Para diagnóstico, o `PermissionEvaluator` continua aplicando a regra geral de escrita por localização atual do
  subprocesso.
- O serviço de apresentação (`SubprocessoAcessoService`) faz o controle adicional de habilitação por situação do fluxo
  para exibir ou desabilitar ações na UI.
- Na UI, o card de Autoavaliação deve ser ocultado quando `podePreencherAutoavaliacao` for falso. Não é uma ação “sempre
  visível”; ela depende do perfil ativo ser `SERVIDOR`.

### 8.4 Situações do Processo

```
CRIADO → EM_ANDAMENTO → FINALIZADO
```

## 9. Regras de Frontend

### 9.1 Regra de apresentação

As ações devem seguir estas diretrizes na UI:

- **Esconder:** Se o perfil ativo **nunca** tem permissão para a ação (ex: botão "Criar processo" para CHEFE).

- **Desabilitar:** Se o perfil permite, mas a **situação** ou a **localização** atual impede (ex: botão "Disponibilizar"
  visível mas desabilitado quando o subprocesso não está na unidade do usuário — com tooltip explicativo). Algumas
  exceções se aplicam quando a UX seria comprometida com a aplicação das regras (ex. componentes de edição de mapas e
  atividades são escondidos quando edição não é permitida, ao invés de apenas desabilitados)

### 9.2 Disponibilidade contextual por fluxo

Os campos `pode...` expõem a permissão estrutural do perfil; os campos `habilitar...` combinam perfil, situação e, para
ações de subprocesso, localização atual. Eles são calculados por `SubprocessoAcessoService`.

| Fluxo                          | Ações estruturais                                                                                                  | Condição para habilitar                                                                                                                                                                                                                                 |
|--------------------------------|--------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Cadastro e revisão de cadastro | CHEFE edita/disponibiliza; GESTOR aceita; ADMIN homologa; ADMIN ou GESTOR devolve                                  | Edição: `NAO_INICIADO`, cadastro em andamento ou revisão em andamento. Disponibilização: cadastro/revisão em andamento. Análise: cadastro/revisão disponibilizada. Sempre na localização do usuário.                                                    |
| Mapa                           | ADMIN edita, disponibiliza e homologa; CHEFE valida ou apresenta sugestões; GESTOR aceita; ADMIN ou GESTOR devolve | Edição/disponibilização e análise seguem as situações específicas de mapeamento e revisão. A homologação exige mapa validado; devolução do ADMIN exige mapa com sugestões, e a do GESTOR aceita também mapa validado. Sempre na localização do usuário. |
| Reabertura                     | ADMIN                                                                                                              | Cadastro só após mapa homologado; revisão só após revisão de mapa homologada.                                                                                                                                                                           |
| Diagnóstico                    | Conforme seção 8.3.1                                                                                               | Conforme seção 8.3.1, incluindo consenso aprovado para habilitar o card de situação de capacitação.                                                                                                                                                     |

Em processo finalizado, as permissões estruturais podem continuar sendo retornadas para a composição da tela, mas todas
as ações de escrita ficam desabilitadas. A leitura continua sujeita às regras de hierarquia.

## 10. Implementação técnica

### Anotações `@PreAuthorize`

```java
// Ações de fluxo — validação completa (perfil + localização):
@PreAuthorize("hasPermission(#id, 'Subprocesso', 'ACEITAR_CADASTRO')")

// Ações administrativas — apenas perfil:
@PreAuthorize("hasRole('ADMIN')")

// Ações em bloco — validação por cada subprocesso:
@PreAuthorize("hasPermission(#request.subprocessos, 'Subprocesso', 'ACEITAR_MAPA')")

// Acesso a detalhes de processo — admin ou hierarquia:
@PreAuthorize("hasRole('ADMIN') or @processoService.checarAcesso(authentication, #codigo)")
```
