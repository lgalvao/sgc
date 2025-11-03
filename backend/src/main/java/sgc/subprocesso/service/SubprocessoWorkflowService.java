package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import sgc.mapa.service.ImpactoMapaService;
import sgc.processo.eventos.EventoRevisaoSubprocessoDisponibilizada;
import sgc.processo.eventos.EventoSubprocessoDisponibilizado;
import sgc.sgrh.modelo.Usuario;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.modelo.*;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoWorkflowService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final UnidadeRepo unidadeRepo;
    private final AnaliseService analiseService;
    private final SubprocessoService subprocessoService;
    private final SubprocessoNotificacaoService subprocessoNotificacaoService;
    private final ImpactoMapaService impactoMapaService;

    /**
     * Disponibiliza o cadastro de atividades de um subprocesso para análise.
     * <p>
     * Altera a situação do subprocesso para {@code CADASTRO_DISPONIBILIZADO},
     * registra a movimentação e dispara notificações.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param usuario       O usuário (chefe da unidade) que está realizando a ação.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws ErroDominioAccessoNegado se o usuário não for o chefe da unidade.
     * @throws ErroValidacao            se existirem atividades sem conhecimentos.
     * @throws IllegalStateException    se o subprocesso não tiver um mapa associado.
     */
    @Transactional
    public void disponibilizarCadastro(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        validarSubprocessoParaDisponibilizacao(sp, usuario, codSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior = sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        repositorioMovimentacao.save(new Movimentacao(
                sp,
                sp.getUnidade(),
                unidadeSuperior,
                "Disponibilização do cadastro de atividades")
        );

        subprocessoNotificacaoService.notificarAceiteCadastro(sp, unidadeSuperior);
        publicadorDeEventos.publishEvent(new EventoSubprocessoDisponibilizado(sp.getCodigo()));
    }

    /**
     * Disponibiliza a revisão do cadastro de um subprocesso para análise.
     * <p>
     * Altera a situação para {@code REVISAO_CADASTRO_DISPONIBILIZADA}, limpa as
     * análises anteriores e dispara as notificações correspondentes.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param usuario       O usuário (chefe da unidade) que está realizando a ação.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws ErroDominioAccessoNegado se o usuário não for o chefe da unidade.
     * @throws ErroValidacao            se existirem atividades sem conhecimentos.
     * @throws IllegalStateException    se o subprocesso não tiver um mapa associado.
     */
    @Transactional
    public void disponibilizarRevisao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        validarSubprocessoParaDisponibilizacao(sp, usuario, codSubprocesso);
        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        Unidade unidadeSuperior = sp.getUnidade() != null ? sp.getUnidade().getUnidadeSuperior() : null;

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), unidadeSuperior, "Disponibilização da revisão do cadastro de atividades"));
        analiseService.removerPorSubprocesso(sp.getCodigo());

        subprocessoNotificacaoService.notificarAceiteRevisaoCadastro(sp, unidadeSuperior);
        publicadorDeEventos.publishEvent(new EventoRevisaoSubprocessoDisponibilizada(sp.getCodigo()));
    }

    private void validarSubprocessoParaDisponibilizacao(Subprocesso sp, Usuario usuario, Long codSubprocesso) {
        if (!sp.getUnidade().getTitular().equals(usuario)) {
            throw new ErroDominioAccessoNegado("Usuário não é o chefe da unidade do subprocesso.");
        }
        if (!subprocessoService.obterAtividadesSemConhecimento(codSubprocesso).isEmpty()) {
            throw new ErroValidacao("Existem atividades sem conhecimentos associados.");
        }
        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            throw new IllegalStateException("Subprocesso sem mapa associado");
        }
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());
    }

    /**
     * Disponibiliza o mapa de competências para a etapa de validação.
     * <p>
     * Esta ação, restrita ao ADMIN, valida a integridade das associações do mapa,
     * limpa dados históricos (sugestões, análises), atualiza a situação do
     * subprocesso para {@code MAPA_DISPONIBILIZADO} e notifica os envolvidos.
     *
     * @param codSubprocesso    O código do subprocesso.
     * @param observacoes      Observações a serem registradas no mapa.
     * @param dataLimiteEtapa2 A nova data limite para a próxima etapa.
     * @param usuario          O usuário (administrador) que está realizando a ação.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException    se o subprocesso não estiver em um estado válido
     *                                  para esta ação, ou se não tiver um mapa associado.
     * @throws ErroValidacao            se o mapa apresentar inconsistências de associação.
     */
    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, String observacoes, LocalDateTime dataLimiteEtapa2, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

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
        analiseService.removerPorSubprocesso(codSubprocesso);

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

    /**
     * Registra as sugestões de melhoria para um mapa e avança o workflow.
     * <p>
     * Atualiza o mapa com as sugestões, altera a situação do subprocesso para
     * {@code MAPA_COM_SUGESTOES}, limpa análises anteriores e notifica a
     * unidade superior.
     *
     * @param codSubprocesso          O código do subprocesso.
     * @param sugestoes              O texto com as sugestões.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que apresenta as sugestões.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, String sugestoes, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }
        sp.setSituacao(SituacaoSubprocesso.MAPA_COM_SUGESTOES);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Sugestões apresentadas para o mapa de competências"));
        analiseService.removerPorSubprocesso(sp.getCodigo());
        subprocessoNotificacaoService.notificarSugestoes(sp);
    }

    /**
     * Valida um mapa de competências e avança o workflow.
     * <p>
     * Altera a situação do subprocesso para {@code MAPA_VALIDADO}, registra a
     * movimentação, limpa análises anteriores e notifica a unidade superior.
     *
     * @param codSubprocesso          O código do subprocesso.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que valida o mapa.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void validarMapa(Long codSubprocesso, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_VALIDADO);
        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade().getUnidadeSuperior(), "Validação do mapa de competências"));
        subprocessoNotificacaoService.notificarValidacao(sp);
    }

    /**
     * Devolve uma validação de mapa para a unidade de origem para ajustes.
     * <p>
     * Cria um registro de análise com a devolução, reverte a situação do
     * subprocesso para {@code MAPA_DISPONIBILIZADO}, registra a movimentação
     * e notifica a unidade correspondente.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param justificativa A justificativa para a devolução.
     * @param usuario       O usuário que está realizando a devolução.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void devolverValidacao(Long codSubprocesso, String justificativa, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codSubprocesso)
                .observacoes(justificativa)
                .tipo(TipoAnalise.VALIDACAO)
                .acao(TipoAcaoAnalise.DEVOLUCAO)
                .unidadeSigla(sp.getUnidade().getUnidadeSuperior().getSigla())
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

    /**
     * Aceita a validação de um mapa e o encaminha para a próxima etapa hierárquica,
     * ou finaliza a homologação se não houver mais níveis.
     * <p>
     * Cria um registro de análise de aceite. Se houver uma unidade superior,
     * move o processo para ela. Caso contrário, considera o mapa homologado.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param usuario       O usuário que está aceitando a validação.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codSubprocesso)
                .observacoes("Aceite da validação")
                .tipo(TipoAnalise.VALIDACAO)
                .acao(TipoAcaoAnalise.ACEITE)
                .unidadeSigla(sp.getUnidade().getUnidadeSuperior().getSigla())
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

    /**
     * Homologa a validação de um mapa.
     * <p>
     * Altera a situação do subprocesso diretamente para {@code MAPA_HOMOLOGADO}.
     *
     * @param codSubprocesso O código do subprocesso.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void homologarValidacao(Long codSubprocesso) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        sp.setSituacao(SituacaoSubprocesso.MAPA_HOMOLOGADO);
        repositorioSubprocesso.save(sp);

        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new IllegalStateException("Unidade 'SEDOC' não encontrada para registrar a homologação."));

        repositorioMovimentacao.save(new Movimentacao(sp, sedoc, sedoc, "Mapa de competências homologado"));
        subprocessoNotificacaoService.notificarHomologacaoMapa(sp);
    }

    /**
     * Submete um mapa ajustado para uma nova rodada de validação.
     * <p>
     * Valida as associações do mapa, redefine a situação do subprocesso para
     * {@code MAPA_DISPONIBILIZADO}, atualiza a data limite e dispara as notificações.
     *
     * @param codSubprocesso          O código do subprocesso.
     * @param request                O DTO com os dados da submissão.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que está submetendo.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws ErroValidacao            se o mapa apresentar inconsistências de associação.
     */
    @Transactional
    public void submeterMapaAjustado(Long codSubprocesso, SubmeterMapaAjustadoReq request, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        // Validação: Verificar se todas as atividades estão associadas
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        sp.setSituacao(SituacaoSubprocesso.MAPA_DISPONIBILIZADO);
        sp.setDataLimiteEtapa2(request.dataLimiteEtapa2());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        repositorioSubprocesso.save(sp);

        repositorioMovimentacao.save(new Movimentacao(sp, sp.getUnidade(), sp.getUnidade(), "Disponibilização do mapa de competências para validação"));
        subprocessoNotificacaoService.notificarDisponibilizacaoMapa(sp);
    }

    /**
     * Devolve o cadastro de um subprocesso para a unidade de origem para ajustes.
     * <p>
     * Cria um registro de análise com a devolução, reverte a situação do subprocesso
     * para {@code CADASTRO_EM_ANDAMENTO}, registra a movimentação e notifica
     * a unidade correspondente.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param motivo        O motivo da devolução.
     * @param observacoes   Observações detalhadas sobre a devolução.
     * @param usuario       O usuário que está realizando a devolução.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     */
    @Transactional
    public void devolverCadastro(Long codSubprocesso, String motivo, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: %d".formatted(codSubprocesso)));
        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codSubprocesso)
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

    /**
     * Aceita o cadastro de um subprocesso e o encaminha para a unidade superior.
     * <p>
     * Cria um registro de análise de aceite, registra a movimentação para a
     * unidade superior e atualiza a situação do subprocesso para
     * {@code CADASTRO_HOMOLOGADO}.
     *
     * @param codSubprocesso          O código do subprocesso.
     * @param observacoes            Observações sobre o aceite.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que está aceitando.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException    se não for possível identificar a unidade superior.
     */
    @Transactional
    public void aceitarCadastro(Long codSubprocesso, String observacoes, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + codSubprocesso));

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codSubprocesso)
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

    /**
     * Homologa o cadastro de um subprocesso.
     * <p>
     * Válido apenas para subprocessos na situação {@code CADASTRO_DISPONIBILIZADO}.
     * Altera a situação para {@code CADASTRO_HOMOLOGADO} e registra a movimentação.
     *
     * @param codSubprocesso          O código do subprocesso.
     * @param observacoes            Observações da homologação.
     * @param usuarioTituloEleitoral O título de eleitor do usuário (ADMIN) que homologa.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException    se o subprocesso não estiver na situação correta
     *                                  ou se a unidade 'SEDOC' não for encontrada.
     */
    @Transactional
    public void homologarCadastro(Long codSubprocesso, String observacoes, Long usuarioTituloEleitoral) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + codSubprocesso));

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

    /**
     * Devolve a revisão de um cadastro para a unidade de origem para ajustes.
     * <p>
     * Ação válida apenas se o subprocesso estiver em {@code REVISAO_CADASTRO_DISPONIBILIZADA}.
     * Reverte a situação para {@code REVISAO_CADASTRO_EM_ANDAMENTO} e notifica a unidade.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param motivo        O motivo da devolução.
     * @param observacoes   Observações detalhadas.
     * @param usuario       O usuário que realiza a devolução.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException    se o subprocesso não estiver na situação correta.
     */
    @Transactional
    public void devolverRevisaoCadastro(Long codSubprocesso, String motivo, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + codSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de devolução só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.DEVOLUCAO_REVISAO)
                .unidadeSigla(usuario.getUnidade().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(motivo)
                .build());

        Unidade unidadeAnalise = unidadeRepo.findById(usuario.getUnidade().getCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade de análise não encontrada."));
        Unidade unidadeDestino = unidadeRepo.findById(sp.getUnidade().getCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade de destino não encontrada."));

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeAnalise, unidadeDestino, "Devolução do cadastro de atividades e conhecimentos para ajustes"));

        if (unidadeDestino.equals(sp.getUnidade())) {
            sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            sp.setDataFimEtapa1(null);
        }
        repositorioSubprocesso.save(sp);

        subprocessoNotificacaoService.notificarDevolucaoRevisaoCadastro(sp, unidadeAnalise, unidadeDestino);
    }

    /**
     * Aceita a revisão de um cadastro e a encaminha para a etapa de homologação.
     * <p>
     * Válido apenas para subprocessos em {@code REVISAO_CADASTRO_DISPONIBILIZADA}.
     * Altera a situação para {@code AGUARDANDO_HOMOLOGACAO_CADASTRO} e notifica
     * a unidade superior.
     *
     * @param codSubprocesso O código do subprocesso.
     * @param observacoes   Observações sobre o aceite.
     * @param usuario       O usuário que está aceitando a revisão.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException    se o subprocesso não estiver na situação correta.
     */
    @Transactional
    public void aceitarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + codSubprocesso));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de aceite só pode ser executada em revisões de cadastro disponibilizadas.");
        }

        analiseService.criarAnalise(CriarAnaliseRequestDto.builder()
                .subprocessoCodigo(codSubprocesso)
                .observacoes(observacoes)
                .tipo(TipoAnalise.CADASTRO)
                .acao(TipoAcaoAnalise.ACEITE_REVISAO)
                .unidadeSigla(sp.getUnidade().getUnidadeSuperior().getSigla())
                .analistaUsuarioTitulo(String.valueOf(usuario.getTituloEleitoral()))
                .motivo(null)
                .build());

        Unidade unidadeAnalise = unidadeRepo.findById(usuario.getUnidade().getCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade de origem não encontrada."));
        Unidade unidadeDestino = unidadeRepo.findById(unidadeAnalise.getUnidadeSuperior().getCodigo())
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade de destino não encontrada."));

        repositorioMovimentacao.save(new Movimentacao(sp, unidadeAnalise, unidadeDestino, "Revisão do cadastro de atividades e conhecimentos aceita"));

        subprocessoNotificacaoService.notificarAceiteRevisaoCadastro(sp, unidadeDestino);

        sp.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);
        repositorioSubprocesso.save(sp);
    }

    /**
     * Homologa a revisão de um cadastro, concluindo a etapa de revisão.
     * <p>
     * Válido para subprocessos em {@code AGUARDANDO_HOMOLOGACAO_CADASTRO}.
     * O método verifica se a revisão resultou em impactos no mapa. Se não houver
     * impactos, o mapa é diretamente homologado ({@code MAPA_HOMOLOGADO}).
     * Se houver impactos, o processo avança para a etapa de ajuste
     * ({@code REVISAO_CADASTRO_HOMOLOGADA}).
     *
     * @param codSubprocesso O código do subprocesso.
     * @param observacoes   Observações da homologação.
     * @param usuario       O usuário (ADMIN) que realiza a homologação.
     * @throws ErroDominioNaoEncontrado se o subprocesso não for encontrado.
     * @throws IllegalStateException    se o subprocesso não estiver na situação correta
     *                                  ou se a unidade 'SEDOC' não for encontrada.
     */
    @Transactional
    public void homologarRevisaoCadastro(Long codSubprocesso, String observacoes, Usuario usuario) {
        Subprocesso sp = repositorioSubprocesso.findById(codSubprocesso)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Subprocesso não encontrado: " + codSubprocesso));

        // Adicionar log para depuração


        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA) {
            throw new IllegalStateException("Ação de homologar só pode ser executada em revisões de cadastro aguardando homologação.");
        }

        var impactos = impactoMapaService.verificarImpactos(codSubprocesso, usuario);

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