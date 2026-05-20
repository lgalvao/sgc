# Auditoria de consistencia de nomenclatura

Gerado em: 2026-05-20T18:46:37.412Z
Base: /home/runner/work/sgc/sgc

## Indicadores

- Arquivos analisados: 1128
- Tipos fora do padrao PascalCase: 0
- Membros fora do padrao camelCase: 594
- Parametros fora do padrao camelCase: 767
- Parametros com uso de 'id': 4
- Pacotes Java fora de lowercase.dotted: 0

## Formatos de arquivos por extensao

| Extensao | Formatos encontrados |
|---|---|
| .java | PascalCase: 556, kebab-case: 25 |
| .ts | outro: 247, minusculo: 58, kebab-case: 21, camelCase: 73 |
| .js | minusculo: 13, outro: 3, kebab-case: 37 |
| .vue | PascalCase: 95 |

## Exemplos de divergencias

### Tipos fora de PascalCase
- Nenhum encontrado

### Membros fora de camelCase
- AlertaDto(Long codigo,
        Long codProcesso,
        String processo,
        String origem,
        String unidadeDestino,
        String descricao,
        String mensagem,
        LocalDateTime dataHora,
        LocalDateTime dataHoraLeitura) (PascalCase) em backend/src/main/java/sgc/alerta/dto/AlertaDto.java
- NotificacaoDto(Long codigo,
        Long subprocessoCodigo,
        String unidadeSigla,
        String processoDescricao,
        TipoNotificacao tipoNotificacao,
        String usuarioDestinoTitulo,
        String destinatario,
        String assunto,
        String corpoHtml,
        sgc.alerta.model.SituacaoNotificacao situacao,
        int tentativas,
        LocalDateTime dataHoraCriacao,
        LocalDateTime dataHoraEnvio,
        LocalDateTime proximaTentativaEm,
        String ultimoErro) (PascalCase) em backend/src/main/java/sgc/alerta/dto/NotificacaoDto.java
- NotificacaoReenvioDto(Long codigo,
        int reenfileiradas) (PascalCase) em backend/src/main/java/sgc/alerta/dto/NotificacaoReenvioDto.java
- NotificacaoSubprocessoResumoDto(Long subprocessoCodigo,
        Long processoCodigo,
        String processoDescricao,
        String unidadeSigla,
        SituacaoSubprocesso situacaoSubprocesso,
        long totalNotificacoes,
        long pendentes,
        long enviando,
        long enviadas,
        long falhasTemporarias,
        long falhasDefinitivas,
        SituacaoNotificacao statusGeral,
        LocalDateTime ultimaNotificacaoEm,
        LocalDateTime proximaTentativaEm,
        int maiorTentativas,
        String ultimoErro,
        boolean podeReenviar) (PascalCase) em backend/src/main/java/sgc/alerta/dto/NotificacaoSubprocessoResumoDto.java
- NotificacaoSubprocessoResumoQuery(Long subprocessoCodigo,
        Long processoCodigo,
        String processoDescricao,
        String unidadeSigla,
        SituacaoSubprocesso situacaoSubprocesso,
        long totalNotificacoes,
        long pendentes,
        long enviando,
        long enviadas,
        long falhasTemporarias,
        long falhasDefinitivas,
        LocalDateTime ultimaNotificacaoEm,
        LocalDateTime proximaTentativaEm,
        int maiorTentativas,
        String ultimoErro) (PascalCase) em backend/src/main/java/sgc/alerta/dto/NotificacaoSubprocessoResumoQuery.java
- UrlLeitorEmailTestesDto(String url) (PascalCase) em backend/src/main/java/sgc/alerta/dto/UrlLeitorEmailTestesDto.java
- EmailAtribuicaoTemporariaCommand(String assunto,
            String nomeServidor,
            String siglaUnidade,
            LocalDateTime dataInicio,
            LocalDateTime dataTermino,
            String justificativa,
            String urlSistema) (PascalCase) em backend/src/main/java/sgc/alerta/EmailModelosService.java
- RuntimeException(e) (PascalCase) em backend/src/main/java/sgc/alerta/EmailService.java
- EnfileirarNotificacaoCommand(@Nullable Subprocesso subprocesso,
        @Nullable TipoNotificacao tipoNotificacao,
        @Nullable String usuarioDestinoTitulo,
        @Nullable String unidadeDestinoSigla,
        String destinatario,
        String assunto,
        String corpoHtml,
        String chaveIdempotencia) (PascalCase) em backend/src/main/java/sgc/alerta/EnfileirarNotificacaoCommand.java
- findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc(Long subprocessoCodigo, Pageable pageable) (outro) em backend/src/main/java/sgc/alerta/model/NotificacaoEmailRepo.java
- MarcarEnviadoCommand(Long codigo, LocalDateTime agora) (PascalCase) em backend/src/main/java/sgc/alerta/model/NotificacaoEmailRepo.java
- MarcarFalhaCommand(Long codigo,
            SituacaoNotificacao situacao,
            int tentativas,
            String ultimoErro,
            LocalDateTime proximaTentativaEm) (PascalCase) em backend/src/main/java/sgc/alerta/model/NotificacaoEmailRepo.java
- ComumDtos() (PascalCase) em backend/src/main/java/sgc/comum/ComumDtos.java
- ErroAcessoNegado(String message) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroAcessoNegado.java
- ErroApi(HttpStatusCode status, String message) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroApi.java
- ErroApi(HttpStatusCode status, String message, String code) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroApi.java
- ErroAutenticacao(String mensagem) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroAutenticacao.java
- ErroConfiguracao(String message) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroConfiguracao.java
- ErroEntidadeNaoEncontrada(String message) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroEntidadeNaoEncontrada.java
- ErroEntidadeNaoEncontrada(String entidade, Object codigo) (PascalCase) em backend/src/main/java/sgc/comum/erros/ErroEntidadeNaoEncontrada.java

### Parametros com `id`
- id em FeedbackRespostaDto(UUID id,
        OffsetDateTime enviadoEm) (backend/src/main/java/sgc/feedback/dto/FeedbackRespostaDto.java)
- id em exibirScreenshot(@PathVariable UUID id) (backend/src/main/java/sgc/feedback/FeedbackController.java)
- id em obterScreenshot(UUID id) (backend/src/main/java/sgc/feedback/FeedbackService.java)
- id em unidadeComId(Long id) (backend/src/test/java/sgc/fixture/UnidadeFixture.java)

### Pacotes Java fora do padrao
- Nenhum encontrado
