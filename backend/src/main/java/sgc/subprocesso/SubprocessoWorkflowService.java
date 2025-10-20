package sgc.subprocesso;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseRequestDto;
import sgc.analise.modelo.TipoAcaoAnalise;
import sgc.analise.modelo.TipoAnalise;
import sgc.comum.erros.ErroDominioAccessoNegado;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.ImpactoMapaService;
import sgc.processo.eventos.SubprocessoDisponibilizadoEvento;
import sgc.processo.eventos.SubprocessoRevisaoDisponibilizadaEvento;
import sgc.sgrh.Usuario;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubprocessoWorkflowService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final UnidadeRepo unidadeRepo;
    private final AnaliseService analiseService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoNotificacaoService subprocessoNotificacaoService;
    private final ImpactoMapaService impactoMapaService;

    @Transactional
    public void disponibilizarCadastro(Long idSubprocesso, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        validarSubprocessoParaDisponibilizacao(sp, usuario, idSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior = sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), unidadeSuperior, "Disponibilização do cadastro de atividades"));

        // Notification
        subprocessoNotificacaoService.notificarAceiteCadastro(sp, unidadeSuperior);

        publicadorDeEventos.publishEvent(new SubprocessoDisponibilizadoEvento(sp.getCodigo()));
    }

    @Transactional
    public void disponibilizarRevisao(Long idSubprocesso, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        validarSubprocessoParaDisponibilizacao(sp, usuario, idSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior = sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), unidadeSuperior, "Disponibilização da revisão do cadastro de atividades"));
        analiseService.removerPorSubprocesso(sp.getCodigo());

        subprocessoNotificacaoService.notificarAceiteRevisaoCadastro(sp, unidadeSuperior);

        publicadorDeEventos.publishEvent(new SubprocessoRevisaoDisponibilizadaEvento(sp.getCodigo()));
    }

    private void validarSubprocessoParaDisponibilizacao(Subprocesso sp, Usuario usuario, Long idSubprocesso) {
        if (!sp.getUnidade().getTitular().equals(usuario)) {
            throw new ErroDominioAccessoNegado("Usuário não é o chefe da unidade do subprocesso.");
        }
        if (!subprocessoService.obterAtividadesSemConhecimento(idSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }
    }

    @Transactional
    public void disponibilizarMapa(Long idSubprocesso, String observacoes, LocalDateTime dataLimiteEtapa2, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        // Pré-condição: Ação só pode ser executada por ADMIN em subprocessos com situações específicas.
        final SituacaoSubprocesso situacaoAtual = sp.getSituacao();
        if (situacaoAtual != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA && situacaoAtual != SituacaoSubprocesso.MAPA_AJUSTADO) {
            throw new IllegalStateException("O mapa de competências só pode ser disponibilizado a partir dos estados 'Revisão de Cadastro Homologada' ou 'Mapa Ajustado'. Estado atual: " + situacaoAtual);
        }

        if (sp.getMapa() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }

        // Validações da Lógica de Negócio
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        // Limpeza de Dados Históricos
        sp.getMapa().setSugestoes(null); // Limpa sugestões anteriores
        analiseService.removerPorSubprocesso(idSubprocesso);

        // Persistência de Dados
        if (observacoes != null && !observacoes.isBlank()) {
            sp.getMapa().setSugestoes(observacoes);
        }

        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        sp.setDataLimiteEtapa2(dataLimiteEtapa2);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade sedoc = unidadeRepo.findBySigla("SEDOC").orElseThrow(() -> new IllegalStateException("Unidade 'SEDOC' não encontrada."));
        repositorioMovimentacao.save(new Movimentacao(sp, sedoc, sp.getUnidade(), "Disponibilização do mapa de competências para validação"));

        subprocessoNotificacaoService.notificarDisponibilizacaoMapa(sp);
    }

    @Transactional
    public void apresentarSugestoes(Long idSubprocesso, String sugestoes, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }
        sp.setSituacao(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());
        subprocessoNotificacaoService.notificarSugestoes(sp);
    }

    @Transactional
    public void validarMapa(Long idSubprocesso, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Validação do mapa de competências"));
        analiseService.removerPorSubprocesso(sp.getCodigo());
        subprocessoNotificacaoService.notificarValidacao(sp);
    }

    @Transactional
    public void devolverValidacao(Long idSubprocesso, String justificativa, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(idSubprocesso)
                .observacoes(justificativa)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(TipoAcaoAnalise.DEVOLUCAO)
                .unidadeSigla(usuario.getUnidade().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(justificativa)
                .build());

        Unidade unidadeDevolucao = sp.getUnidade();
        repositorioMovimentacao.save(new Movimentacao(
                sp,
                sp.getUnidade().getUnidadeSuperior(),
                unidadeDevolucao,
                "Devolução da validação do mapa de competências para ajustes")
        );

        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        sp.setDataFimEtapa2(null);
        repositorioSubprocesso.save(sp);

        subprocessoNotificacaoService.notificarDevolucao(sp, unidadeDevolucao);
    }

    @Transactional
    public void aceitarValidacao(Long idSubprocesso, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(idSubprocesso)
                .observacoes("Aceite da validação")
                .tipo(TipoAnalise.VALIDACAO)
                .acao(TipoAcaoAnalise.ACEITE)
                .unidadeSigla(usuario.getUnidade().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(null)
                .build());

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade = unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        if (proximaUnidade == null) {
            sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
            repositorioSubprocesso.save(sp);
        } else {
            repositorioMovimentacao.save(new Movimentacao(sp, unidadeSuperior, proximaUnidade, "Mapa de competências validado"));
            sp.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
            repositorioSubprocesso.save(sp);
            subprocessoNotificacaoService.notificarAceite(sp);
        }
    }

    @Transactional
    public void homologarValidacao(Long idSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        repositorioSubprocesso.save(sp);
    }

    @Transactional
    public void submeterMapaAjustado(Long idSubprocesso, SubmeterMapaAjustadoReq request, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));

        // Validação: Verificar se todas as atividades estão associadas
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade(), "Disponibilização do mapa de competências para validação"));
        subprocessoNotificacaoService.notificarDisponibilizacaoMapa(sp);
    }

    @Transactional
    public void devolverCadastro(Long idSubprocesso, String motivo, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(idSubprocesso)));
        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(idSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.DEVOLUCAO)
                .unidadeSigla(usuario.getUnidade().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(motivo)
                .build());

        Unidade unidadeDevolucao = sp.getUnidade();

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade().getUnidadeSuperior(), unidadeDevolucao, "Devolução do cadastro de atividades para ajustes: " + motivo));
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO);
        sp.setDataFimEtapa1(null);
        repositorioSubprocesso.save(sp);

        subprocessoNotificacaoService.notificarDevolucaoCadastro(sp, unidadeDevolucao, motivo);
    }

    @Transactional
    public void aceitarCadastro(Long idSubprocesso, String observacoes, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(idSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.ACEITE)
                .unidadeSigla(sp.getUnidade().getUnidadeSuperior().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuarioTituloEleitoral))
                .motivo(null)
                .build());

        Unidade unidadeOrigem = sp.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();
        if (unidadeDestino == null) {
            throw new IllegalStateException("Não foi possível identificar a unidade superior para enviar a análise.");
        }

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigem, unidadeDestino, "Cadastro de atividades e conhecimentos aceito"));

        // Notificar unidade superior
        subprocessoNotificacaoService.notificarAceiteCadastro(sp, unidadeDestino);

        sp.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);
    }

    @Transactional
    public void homologarCadastro(Long idSubprocesso, String observacoes, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em cadastros disponibilizados.");
        }

        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new IllegalStateException("Unidade 'SEDOC' não encontrada para registrar a homologação."));

        // A homologação é uma ação final do ADMIN (SEDOC), a movimentação é registrada na própria unidade.
        repositorioMovimentacao.save(new Movimentacao(sp, sedoc, sedoc, "Cadastro de atividades e conhecimentos homologado"));
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_HOMOLOGADO);
        repositorioSubprocesso.save(sp);
    }

    @Transactional
    public void devolverRevisaoCadastro(Long idSubprocesso, String motivo, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de devolução só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(idSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                .unidadeSigla(usuario.getUnidade().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(motivo)
                .build());

        Unidade unidadeAnalise = usuario.getUnidade();
        Unidade unidadeDestino = sp.getUnidade(); // A devolução é para a unidade do subprocesso

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeAnalise, unidadeDestino, "Devolução do cadastro de atividades e conhecimentos para ajustes"));

        if (unidadeDestino.equals(sp.getUnidade())) {
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            sp.setDataFimEtapa1(null);
        }
        repositorioSubprocesso.save(sp);

        subprocessoNotificacaoService.notificarDevolucaoRevisaoCadastro(sp, unidadeAnalise, unidadeDestino);
    }

    @Transactional
    public void aceitarRevisaoCadastro(Long idSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(idSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.ACEITE_REVISAO)
                .unidadeSigla(sp.getUnidade().getUnidadeSuperior().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(null)
                .build());

        Unidade unidadeOrigem = usuario.getUnidade();
        Unidade unidadeDestino = unidadeOrigem.getUnidadeSuperior();

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeOrigem, unidadeDestino, "Revisão do cadastro de atividades e conhecimentos aceita"));

        subprocessoNotificacaoService.notificarAceiteRevisaoCadastro(sp, unidadeDestino);

        sp.setSituacao(SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO);
        repositorioSubprocesso.save(sp);
    }

    @Transactional
    public void homologarRevisaoCadastro(Long idSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(idSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + idSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.AGUARDANDO_HOMOLOGACAO_CADASTRO) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em revisões de cadastro aguardando homologação.");
        }

        var impactos = impactoMapaService.verificarImpactos(idSubprocesso, usuario);

        if (!impactos.temImpactos()) {
            // CDU-14 Item 12.2: Sem impactos
            sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        } else {
            // CDU-14 Item 12.3: Com impactos
            Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                    .orElseThrow(() -> new IllegalStateException("Unidade 'SEDOC' não encontrada para registrar a homologação."));

            repositorioMovimentacao.save(new Movimentacao(sp, sedoc, sedoc, "Cadastro de atividades e conhecimentos homologado"));
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
        }

        repositorioSubprocesso.save(sp);
    }
}