# Plano de Refatoração de Acesso e Segurança - SGC

Este documento detalha os problemas identificados na arquitetura de segurança e o plano de ação para alinhar o backend ao requisito de **par único perfil-unidade por sessão**.

## 1. Diagnóstico de Problemas Identificados

### 1.1 Perda de Contexto no JWT (FiltroJwt)
Embora o `GerenciadorJwt` inclua corretamente o `perfil` e a `unidadeCodigo` nos claims do token no momento do login, o `FiltroJwt` descarta essas informações ao validar o token. Ele recarrega o usuário do banco e atribui **todas** as permissões (authorities) que o usuário possui globalmente, ignorando qual perfil ele selecionou para aquela sessão.

### 1.2 Inconsistência nas Políticas de Acesso (AbstractAccessPolicy)
As políticas de acesso (ex: `SubprocessoAccessPolicy`) buscam todos os perfis do usuário no banco de dados para cada verificação. Isso permite que um usuário "logado como Servidor na Unidade A" consiga executar ações de "Chefe na Unidade B" se ele possuir esse perfil no banco, pois o sistema não filtra a autoridade ativa da sessão.

### 1.3 Redundância no Cálculo de Atribuições
A view `VW_USUARIO_PERFIL_UNIDADE` no banco de dados já consolida as atribuições permanentes, substituições e atribuições temporárias (via `VW_RESPONSABILIDADE`). O método `Usuario.getTodasAtribuicoes()` tenta recalcular essa lógica em memória no Java, o que é redundante e propenso a inconsistências.

### 1.4 Débitos Técnicos no ProcessoConsultaService
* **Hacks de Unidade**: O método `subprocessosElegiveis` usa `perfis.stream().findFirst()` para tentar adivinhar em qual unidade o usuário está operando.
* **Tipagem Fraca**: O método `unidadesBloqueadasPorTipo` aceita `String` em vez do enum `TipoProcesso`.
* **Nomenclatura Confusa**: O método `codUnidadesProcessosAndamento` tem um nome que não descreve claramente sua intenção.
* **Mapeamento Manual**: O uso de `paraSubprocessoElegivelDto` viola a ADR-004, que exige o uso de Mappers (MapStruct).

---

## 2. Proposta Técnica de Refatoração

### Fase 1: Ajuste da Infraestrutura de Segurança e Limpeza do Modelo
1.  **Limpeza do Usuario.java**: Remover o campo `atribuicoesTemporarias` e o método `getTodasAtribuicoes`, confiando exclusivamente na view consolidada.
2.  **Contexto de Autenticação**: Garantir que o `Usuario` armazene explicitamente o `perfilAtivo` e a `unidadeAtivaCodigo` extraídos do JWT.
3.  **FiltroJwt**: Alterar o filtro para preencher o contexto com apenas a autoridade do perfil que veio no token.
4.  **AbstractAccessPolicy**: Modificar os métodos `temPerfil` e `verificaHierarquia` para utilizarem exclusivamente os dados da sessão (Authentication).

---

## 3. Cronograma de Implementação

1.  **[Infra]** Atualização do `SubprocessoRepo` e `ConsultasSubprocessoService`. ✅
2.  **[Mapper]** Configuração do `SubprocessoMapper` para DTOs de elegibilidade. ✅
3.  **[Security]** Refatoração do `FiltroJwt` e `GerenciadorJwt`. ✅
4.  **[Cleanup]** Remoção de lógica redundante de atribuições no `Usuario.java` e `UsuarioPerfilService`.
5.  **[Policy]** Atualização da `AbstractAccessPolicy` para usar o contexto de sessão. ✅
6.  **[Business Logic]** Refatoração final do `ProcessoConsultaService` e `ProcessoFacade`.
7.  **[Tests]** Correção massiva dos testes unitários e de integração para suportar o novo contexto de sessão.

---
**Status:** Fase 1 e 2 concluídas (Segurança e Políticas). Iniciando Limpeza e Lógica de Negócio.
