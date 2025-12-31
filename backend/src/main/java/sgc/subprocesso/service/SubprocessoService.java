package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.model.Conhecimento;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.comum.erros.ErroNegocio;
import sgc.mapa.model.Competencia;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.model.Mapa;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.mapper.SubprocessoMapper;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.analise.AnaliseService;
import sgc.analise.model.Analise;
import sgc.analise.model.TipoAnalise;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.ConhecimentoMapper;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.usuario.UsuarioService;
import sgc.usuario.model.Perfil;
import sgc.usuario.model.Usuario;
import sgc.subprocesso.mapper.MapaAjusteMapper;
import sgc.subprocesso.mapper.SubprocessoDetalheMapper;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.MovimentacaoRepo;
import sgc.unidade.model.Unidade;
import org.springframework.security.core.context.SecurityContextHolder;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.alerta.AlertaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;


@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoService {
    private final SubprocessoRepo repositorioSubprocesso;
    private final AtividadeService atividadeService;
    private final CompetenciaService competenciaService;
    private final SubprocessoMapper subprocessoMapper;
    private final sgc.mapa.service.MapaService mapaService;
    private final MovimentacaoRepo repositorioMovimentacao;
    private final AnaliseService analiseService;
    private final ConhecimentoMapper conhecimentoMapper;
    private final UsuarioService usuarioService;
    private final SubprocessoPermissoesService subprocessoPermissoesService;
    private final SubprocessoDetalheMapper subprocessoDetalheMapper;
    private final MapaAjusteMapper mapaAjusteMapper;
    private final AlertaService alertaService;
    // SubprocessoEmailService is not injected to keep scope minimal and avoid potential circular dependencies if any,
    // assuming alerts are the primary system notification for now.

    /**
     * Busca e retorna um subprocesso pelo seu código.
     *
     * @param codigo O código do subprocesso.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    public Subprocesso buscarSubprocesso(Long codigo) {
        return repositorioSubprocesso
                .findById(codigo)
                .orElseThrow(
                        () -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    /**
     * Busca e retorna um subprocesso pelo seu código, garantindo que ele possua um mapa de
     * competências associado.
     *
     * @param codigo O código do subprocesso.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado ou se não possuir um
     *                                   mapa associado.
     */
    public Subprocesso buscarSubprocessoComMapa(Long codigo) {
        Subprocesso subprocesso = buscarSubprocesso(codigo);
        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }

    @Transactional(readOnly = true)
    public boolean verificarAcessoUnidadeAoProcesso(Long codProcesso, List<Long> codigosUnidadesHierarquia) {
        return repositorioSubprocesso.existsByProcessoCodigoAndUnidadeCodigoIn(codProcesso, codigosUnidadesHierarquia);
    }

    @Transactional(readOnly = true)
    public List<Subprocesso> listarEntidadesPorProcesso(Long codProcesso) {
        return repositorioSubprocesso.findByProcessoCodigoWithUnidade(codProcesso);
    }

    @Transactional(readOnly = true)
    public List<AtividadeVisualizacaoDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        if (subprocesso.getMapa() == null) {
            return List.of();
        }

        List<Atividade> todasAtividades =
                atividadeService.buscarPorMapaCodigo(subprocesso.getMapa().getCodigo());

        return todasAtividades.stream()
                .map(this::mapAtividadeToDto)
                .toList();
    }

    private AtividadeVisualizacaoDto mapAtividadeToDto(Atividade atividade) {
        List<Conhecimento> conhecimentos =
                atividadeService.listarConhecimentosPorAtividade(atividade.getCodigo());

        List<ConhecimentoVisualizacaoDto> conhecimentosDto = conhecimentos.stream()
                .map(c -> ConhecimentoVisualizacaoDto.builder()
                        .codigo(c.getCodigo())
                        .descricao(c.getDescricao())
                        .build())
                .toList();

        return AtividadeVisualizacaoDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(conhecimentosDto)
                .build();
    }

    /**
     * Obtém o status atual de um subprocesso de forma leve.
     *
     * @param codSubprocesso O código do subprocesso.
     * @return DTO com informações básicas de status.
     */
    @Transactional(readOnly = true)
    public SubprocessoSituacaoDto obterStatus(Long codSubprocesso) {
        Subprocesso subprocesso = repositorioSubprocesso
                .findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        return SubprocessoSituacaoDto.builder()
                .codigo(subprocesso.getCodigo())
                .situacao(subprocesso.getSituacao())
                .situacaoLabel(subprocesso.getSituacao() != null ? subprocesso.getSituacao().name() : null)
                .build();
    }

    /**
     * Busca um subprocesso pelo código do mapa e retorna a entidade.
     *
     * @param codMapa O código do mapa.
     * @return A entidade {@link Subprocesso} correspondente.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso não for encontrado.
     */
    @Transactional(readOnly = true)
    public Subprocesso obterEntidadePorCodigoMapa(Long codMapa) {
        return repositorioSubprocesso
                .findByMapaCodigo(codMapa)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado para o mapa com código %d".formatted(codMapa))
                );
    }

    @Transactional
    public void alterarDataLimite(Long codSubprocesso, java.time.LocalDate novaDataLimite) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        SituacaoSubprocesso s = sp.getSituacao();
        int etapa = 1;

        if (s.name().contains("CADASTRO")) {
             sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
             etapa = 1;
        } else if (s.name().contains("MAPA")) {
             sp.setDataLimiteEtapa2(novaDataLimite.atStartOfDay());
             etapa = 2;
        } else {
             sp.setDataLimiteEtapa1(novaDataLimite.atStartOfDay());
        }

        repositorioSubprocesso.save(sp);

        // Notificação e Alerta (CDU-27)
        try {
            String novaDataStr = novaDataLimite.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (sp.getUnidade() != null) {
                alertaService.criarAlertaAlteracaoDataLimite(sp.getProcesso(), sp.getUnidade(), novaDataStr, etapa);
            }
        } catch (Exception e) {
            log.error("Erro ao enviar notificações de alteração de prazo: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public void validarPermissaoEdicaoMapa(Long mapaCodigo, String tituloUsuario) {
        Subprocesso subprocesso = obterEntidadePorCodigoMapa(mapaCodigo);

        if (subprocesso.getUnidade() == null) {
            throw new ErroEntidadeNaoEncontrada("Unidade não associada ao Subprocesso %d".formatted(subprocesso.getCodigo()));
        }

        Usuario usuario = usuarioService.buscarUsuarioPorLogin(tituloUsuario);

        // Validação: Garante que apenas o titular da unidade pode editar o mapa.
        String titularTitulo = subprocesso.getUnidade().getTituloTitular();

        // Verifica se o título eleitoral do usuário corresponde ao titular da unidade.
        // Assume-se que getTituloEleitoral() retorna um Long, então convertemos para String para comparar com titularTitulo (String).
        if (titularTitulo == null || !titularTitulo.equals(String.valueOf(usuario.getTituloEleitoral()))) {
             throw new ErroAccessoNegado("Usuário não autorizado a editar este mapa.");
        }
    }

    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Long codSubprocesso) {
        Subprocesso sp =
                repositorioSubprocesso
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: %d"
                                                        .formatted(codSubprocesso)));

        if (sp.getMapa() == null || sp.getMapa().getCodigo() == null) {
            return emptyList();
        }

        // --- OTIMIZAÇÃO APLICADA AQUI ---
        // Utiliza o método que faz JOIN FETCH de conhecimentos para evitar N+1 queries.
        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());

        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }

        return atividades.stream()
                .filter(a -> a.getConhecimentos() == null || a.getConhecimentos().isEmpty())
                .collect(Collectors.toList());
    }

    // Método auxiliar para uso interno quando já temos o Mapa
    @Transactional(readOnly = true)
    public List<Atividade> obterAtividadesSemConhecimento(Mapa mapa) {
        if (mapa == null || mapa.getCodigo() == null) {
            return emptyList();
        }

        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(mapa.getCodigo());

        if (atividades == null || atividades.isEmpty()) {
            return emptyList();
        }

        return atividades.stream()
                .filter(a -> a.getConhecimentos() == null || a.getConhecimentos().isEmpty())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void validarExistenciaAtividades(Long codSubprocesso) {
        log.debug("Validando existência de atividades para o subprocesso: {}", codSubprocesso);
        Subprocesso subprocesso =
                repositorioSubprocesso
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () -> new ErroEntidadeNaoEncontrada("Subprocesso", codSubprocesso));

        Mapa mapa = subprocesso.getMapa();
        if (mapa == null) {
            throw new ErroValidacao("Mapa não encontrado para o subprocesso.");
        }

        // --- OTIMIZAÇÃO APLICADA AQUI ---
        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(mapa.getCodigo());

        if (atividades == null || atividades.isEmpty()) {
            throw new ErroValidacao(
                    "O mapa de competências deve ter ao menos uma atividade cadastrada.");
        }

        List<Atividade> atividadesSemConhecimento = new ArrayList<>();
        for (Atividade atividade : atividades) {
            if (atividade.getConhecimentos() == null || atividade.getConhecimentos().isEmpty()) {
                atividadesSemConhecimento.add(atividade);
            }
        }

        if (!atividadesSemConhecimento.isEmpty()) {
            throw new ErroValidacao(
                    "Todas as atividades devem possuir conhecimentos vinculados. Verifique as"
                            + " atividades pendentes.");
        }
    }


    public void validarAssociacoesMapa(Long mapaId) {
        List<Competencia> competencias = competenciaService.buscarPorMapa(mapaId);
        List<String> competenciasSemAssociacao = new ArrayList<>();
        for (Competencia competencia : competencias) {
            if (competencia.getAtividades().isEmpty()) {
                competenciasSemAssociacao.add(competencia.getDescricao());
            }
        }
        if (!competenciasSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem competências que não foram associadas a nenhuma atividade.",
                    Map.of("competenciasNaoAssociadas", competenciasSemAssociacao));
        }

        List<Atividade> atividades = atividadeService.buscarPorMapaCodigo(mapaId);
        List<String> atividadesSemAssociacao = new ArrayList<>();
        for (Atividade atividade : atividades) {
            if (atividade.getCompetencias().isEmpty()) {
                atividadesSemAssociacao.add(atividade.getDescricao());
            }
        }
        if (!atividadesSemAssociacao.isEmpty()) {
            throw new ErroValidacao(
                    "Existem atividades que não foram associadas a nenhuma competência.",
                    Map.of("atividadesNaoAssociadas", atividadesSemAssociacao));
        }
    }

    @Transactional
    public SubprocessoDto criar(SubprocessoDto subprocessoDto) {
        var entity = subprocessoMapper.toEntity(subprocessoDto);
        
        // 1. Criar subprocesso SEM mapa primeiro
        entity.setMapa(null);
        var subprocessoSalvo = repositorioSubprocesso.save(entity);
        
        // 2. Criar mapa COM referência ao subprocesso
        Mapa mapa = new Mapa();
        mapa.setSubprocesso(subprocessoSalvo);
        Mapa mapaSalvo = mapaService.salvar(mapa);
        
        // 3. Atualizar subprocesso com o mapa
        subprocessoSalvo.setMapa(mapaSalvo);
        var salvo = repositorioSubprocesso.save(subprocessoSalvo);
        
        return subprocessoMapper.toDTO(salvo);
    }

    @Transactional
    public SubprocessoDto atualizar(Long codigo, SubprocessoDto subprocessoDto) {
        return repositorioSubprocesso
                .findById(codigo)
                .map(
                        subprocesso -> {
                            // NOTA: Processo e Unidade não podem ser alterados após criação
                            // pois fazem parte da identidade do subprocesso em UNIDADE_PROCESSO
                            
                            if (subprocessoDto.getCodMapa() != null) {
                                Mapa mapa = new Mapa();
                                mapa.setCodigo(subprocessoDto.getCodMapa());
                                subprocesso.setMapa(mapa);
                            } else {
                                subprocesso.setMapa(null);
                            }

                            subprocesso.setDataLimiteEtapa1(subprocessoDto.getDataLimiteEtapa1());
                            subprocesso.setDataFimEtapa1(subprocessoDto.getDataFimEtapa1());
                            var dataFimEtapa2 = subprocessoDto.getDataFimEtapa2();
                            subprocesso.setDataFimEtapa2(dataFimEtapa2);
                            subprocesso.setSituacao(subprocessoDto.getSituacao());
                            var atualizado = repositorioSubprocesso.save(subprocesso);
                            return subprocessoMapper.toDTO(atualizado);
                        })
                .orElseThrow(
                        () -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo));
    }

    @Transactional
    public void excluir(Long codigo) {
        if (!repositorioSubprocesso.existsById(codigo)) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não encontrado", codigo);
        }
        repositorioSubprocesso.deleteById(codigo);
    }

    // --- Methods migrated from SubprocessoDtoService ---

    @Transactional(readOnly = true)
    public SubprocessoDetalheDto obterDetalhes(Long codigo, Perfil perfil, Long codUnidadeUsuario) {
        log.debug("Obtendo detalhes para o subprocesso {}", codigo);
        if (perfil == null) {
            log.warn("Perfil inválido para acesso aos detalhes do subprocesso.");
            throw new ErroAccessoNegado("Perfil inválido para acesso aos detalhes do subprocesso.");
        }

        Subprocesso sp = buscarSubprocesso(codigo);
        log.debug("Subprocesso encontrado: {}", sp.getCodigo());

        Usuario usuario = obterUsuarioAutenticado();

        verificarPermissaoVisualizacao(sp, perfil, usuario);
        Usuario responsavel = usuarioService.buscarResponsavelVigente(sp.getUnidade().getSigla());

        Usuario titular = null;
        if (sp.getUnidade() != null && sp.getUnidade().getTituloTitular() != null) {
            try {
                titular = usuarioService.buscarUsuarioPorLogin(sp.getUnidade().getTituloTitular());
            } catch (Exception e) {
                log.warn("Erro ao buscar titular da unidade: {}", e.getMessage());
            }
        }

        List<Movimentacao> movimentacoes = repositorioMovimentacao
                .findBySubprocessoCodigoOrderByDataHoraDesc(sp.getCodigo());

        SubprocessoPermissoesDto permissoes = subprocessoPermissoesService.calcularPermissoes(sp, usuario);
        log.debug("Permissões calculadas: {}", permissoes);

        return subprocessoDetalheMapper.toDto(sp, responsavel, titular, movimentacoes, permissoes);
    }

    private void verificarPermissaoVisualizacao(Subprocesso sp, Perfil perfil, Usuario usuario) {
        boolean hasPerfil = usuario.getTodasAtribuicoes().stream().anyMatch(a -> a.getPerfil() == perfil);

        if (!hasPerfil) {
            log.warn("Usuário não possui o perfil sSolicitado.");
            throw new ErroAccessoNegado("Perfil inválido para o usuário.");
        }
        if (perfil == Perfil.ADMIN) return;

        Unidade unidadeAlvo = sp.getUnidade();
        if (unidadeAlvo == null) throw new ErroAccessoNegado("Unidade não identificada.");

        boolean hasPermission = usuario.getTodasAtribuicoes().stream()
                .filter(a -> a.getPerfil() == perfil)
                .anyMatch(a -> {
                    Unidade unidadeUsuario = a.getUnidade();
                    if (perfil == Perfil.GESTOR) {
                        return isMesmaUnidadeOuSubordinada(unidadeAlvo, unidadeUsuario);
                    } else if (perfil == Perfil.CHEFE || perfil == Perfil.SERVIDOR) {
                        return unidadeAlvo.getCodigo().equals(unidadeUsuario.getCodigo());
                    }
                    return false;
                });

        if (!hasPermission) throw new ErroAccessoNegado("Usuário sem permissão para visualizar este subprocesso.");
    }

    private boolean isMesmaUnidadeOuSubordinada(Unidade alvo, Unidade superior) {
        sgc.unidade.model.Unidade atual = alvo;
        while (atual != null) {
            if (atual.getCodigo().equals(superior.getCodigo())) {
                return true;
            }
            atual = atual.getUnidadeSuperior();
        }
        return false;
    }

    @Transactional(readOnly = true)
    public SubprocessoCadastroDto obterCadastro(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        List<SubprocessoCadastroDto.AtividadeCadastroDto> atividadesComConhecimentos = new ArrayList<>();
        if (sp.getMapa() != null && sp.getMapa().getCodigo() != null) {
            List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());
            if (atividades == null) atividades = emptyList();

            for (Atividade a : atividades) {
                List<Conhecimento> ks = a.getConhecimentos();
                List<ConhecimentoDto> ksDto = ks == null ? emptyList() : ks.stream().map(conhecimentoMapper::toDto).toList();

                atividadesComConhecimentos.add(SubprocessoCadastroDto.AtividadeCadastroDto.builder()
                        .codigo(a.getCodigo())
                        .descricao(a.getDescricao())
                        .conhecimentos(ksDto)
                        .build());
            }
        }

        return SubprocessoCadastroDto.builder()
                .subprocessoCodigo(sp.getCodigo())
                .unidadeSigla(sp.getUnidade() != null ? sp.getUnidade().getSigla() : null)
                .atividades(atividadesComConhecimentos)
                .build();
    }

    @Transactional(readOnly = true)
    public SugestoesDto obterSugestoes(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        return SugestoesDto.of(sp);
    }

    @Transactional(readOnly = true)
    public MapaAjusteDto obterMapaParaAjuste(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocessoComMapa(codSubprocesso);

        Long codMapa = sp.getMapa().getCodigo();

        Analise analise = analiseService.listarPorSubprocesso(codSubprocesso, TipoAnalise.VALIDACAO).stream()
                .findFirst()
                .orElse(null);

        List<Competencia> competencias = competenciaService.buscarPorMapa(codMapa);
        List<Atividade> atividades = atividadeService.buscarPorMapaCodigo(codMapa);
        List<Conhecimento> conhecimentos = atividadeService.listarConhecimentosPorMapa(codMapa);

        return mapaAjusteMapper.toDto(sp, analise, competencias, atividades, conhecimentos);
    }

    @Transactional(readOnly = true)
    public List<SubprocessoDto> listar() {
        return repositorioSubprocesso.findAll().stream().map(subprocessoMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public SubprocessoDto obterPorProcessoEUnidade(Long codProcesso, Long codUnidade) {
        Subprocesso sp = repositorioSubprocesso
                .findByProcessoCodigoAndUnidadeCodigo(codProcesso, codUnidade)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado para o processo %d e unidade %d".formatted(codProcesso, codUnidade)));
        return subprocessoMapper.toDTO(sp);
    }

    @Transactional(readOnly = true)
    public SubprocessoPermissoesDto obterPermissoes(Long codSubprocesso) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);
        Usuario usuario = obterUsuarioAutenticado();
        return subprocessoPermissoesService.calcularPermissoes(sp, usuario);
    }

    @Transactional(readOnly = true)
    public ValidacaoCadastroDto validarCadastro(Long codSubprocesso) {
        log.debug("Validando cadastro para disponibilização. Subprocesso: {}", codSubprocesso);
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        List<ErroValidacaoDto> erros = new ArrayList<>();

        if (sp.getMapa() == null) {
            return ValidacaoCadastroDto.builder()
                    .valido(false)
                    .erros(List.of(ErroValidacaoDto.builder()
                            .tipo("MAPA_INEXISTENTE")
                            .mensagem("O subprocesso não possui um mapa associado.")
                            .build()))
                    .build();
        }

        List<Atividade> atividades = atividadeService.buscarPorMapaCodigoComConhecimentos(sp.getMapa().getCodigo());

        if (atividades == null || atividades.isEmpty()) {
            erros.add(ErroValidacaoDto.builder()
                    .tipo("SEM_ATIVIDADES")
                    .mensagem("O mapa não possui atividades cadastradas.")
                    .build());
        } else {
            for (Atividade atividade : atividades) {
                // Valida se tem conhecimentos
                long qtdConhecimentos = atividade.getConhecimentos() == null ? 0 : atividade.getConhecimentos().size();
                if (qtdConhecimentos == 0) {
                    erros.add(ErroValidacaoDto.builder()
                            .tipo("ATIVIDADE_SEM_CONHECIMENTO")
                            .atividadeCodigo(atividade.getCodigo())
                            .descricaoAtividade(atividade.getDescricao())
                            .mensagem("Esta atividade não possui conhecimentos associados.")
                            .build());
                }
            }
        }

        return ValidacaoCadastroDto.builder()
                .valido(erros.isEmpty())
                .erros(erros)
                .build();
    }

    private Usuario obterUsuarioAutenticado() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ErroAccessoNegado("Usuário não autenticado.");
        }
        String username = authentication.getName();
        return usuarioService.buscarUsuarioPorLogin(username);
    }

    /**
     * Atualiza a situação do subprocesso para EM_ANDAMENTO (cadastro ou revisão)
     * quando há movimentação em atividades.
     */
    @Transactional
    public void atualizarSituacaoParaEmAndamento(Long mapaCodigo) {
        var subprocesso = repositorioSubprocesso
                .findByMapaCodigo(mapaCodigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                        "Subprocesso não encontrado para o mapa com código %d".formatted(mapaCodigo)));

        if (subprocesso.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            if (subprocesso.getProcesso() == null) {
                throw new ErroEntidadeNaoEncontrada("Processo não associado ao Subprocesso %d"
                        .formatted(subprocesso.getCodigo()));
            }
            var tipoProcesso = subprocesso.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                repositorioSubprocesso.save(subprocesso);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                subprocesso.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                repositorioSubprocesso.save(subprocesso);
            }
        }
    }

    /**
     * Recupera a lista de subprocessos em revisão de cadastro que já foram homologados.
     *
     * @return Lista de subprocessos na situação REVISAO_CADASTRO_HOMOLOGADA.
     */
    public List<Subprocesso> listarSubprocessosHomologados() {
        return repositorioSubprocesso.findBySituacao(SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA);
    }
}
