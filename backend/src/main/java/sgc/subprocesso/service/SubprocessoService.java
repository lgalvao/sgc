package sgc.subprocesso.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.organizacao.service.*;
import sgc.processo.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import static sgc.organizacao.model.TipoUnidade.*;
import static sgc.seguranca.AcaoPermissao.*;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SubprocessoService {
    private final ComumRepo repo;
    private final UnidadeService unidadeService;
    private final UsuarioFacade usuarioFacade;
    private final MapaSalvamentoService mapaSalvamentoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final SubprocessoValidacaoService validacaoService;
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final CopiaMapaService copiaMapaService;
    private final MapaManutencaoService mapaManutencaoService;
        private final SubprocessoConsultaService consultaService;

    private static final Set<SituacaoSubprocesso> SITUACOES_PERMITIDAS_IMPORTACAO = Set.of(
            NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO);

    @Transactional
    public Mapa salvarMapa(Long codSubprocesso, SalvarMapaRequest request) {
        return salvarMapaSubprocesso(codSubprocesso, request);
    }

    @Transactional
    public Subprocesso criarEntidade(CriarSubprocessoRequest request) {
        Processo processo = repo.buscar(Processo.class, request.codProcesso());
        Unidade unidade = unidadeService.buscarPorCodigo(request.codUnidade());

        Subprocesso entity = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .dataLimiteEtapa1(request.dataLimiteEtapa1())
                .dataLimiteEtapa2(request.dataLimiteEtapa2())
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(entity);
        Mapa mapa = Mapa.builder().subprocesso(subprocessoSalvo).build();
        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapa);
        subprocessoSalvo.setMapa(mapaSalvo);
        return subprocessoRepo.save(subprocessoSalvo);
    }

    @Transactional
    public Subprocesso atualizarEntidade(Long codigo, AtualizarSubprocessoRequest request) {
        Subprocesso subprocesso = consultaService.buscarSubprocesso(codigo);
        processarAlteracoes(subprocesso, request);
        return subprocessoRepo.save(subprocesso);
    }

    @Transactional
    public void excluir(Long codigo) {
        consultaService.buscarSubprocesso(codigo);
        subprocessoRepo.deleteById(codigo);
    }

    private void processarAlteracoes(Subprocesso sp, AtualizarSubprocessoRequest request) {
        Optional.ofNullable(request.codUnidade())
                .map(unidadeService::buscarPorCodigo)
                .ifPresent(sp::setUnidade);

        Optional.ofNullable(request.codMapa()).ifPresent(cod -> {
            Mapa m = Mapa.builder().codigo(cod).build();
            Mapa mapa = sp.getMapa();
            Long codAtual = mapa != null ? mapa.getCodigo() : null;
            if (!Objects.equals(codAtual, cod)) {
                sp.setMapa(m);
            }
        });

        LocalDateTime dataLimiteEtapa1 = request.dataLimiteEtapa1();
        if (dataLimiteEtapa1 != null) sp.setDataLimiteEtapa1(dataLimiteEtapa1);

        LocalDateTime dataFimEtapa1 = request.dataFimEtapa1();
        if (dataFimEtapa1 != null) sp.setDataFimEtapa1(dataFimEtapa1);

        LocalDateTime dataLimiteEtapa2 = request.dataLimiteEtapa2();
        if (dataLimiteEtapa2 != null) sp.setDataLimiteEtapa2(dataLimiteEtapa2);

        LocalDateTime dataFimEtapa2 = request.dataFimEtapa2();
        if (dataFimEtapa2 != null) sp.setDataFimEtapa2(dataFimEtapa2);
    }

    @Transactional
    public void criarParaMapeamento(Processo processo, Collection<Unidade> unidades, Unidade unidadeOrigem, Usuario usuario) {
        List<Unidade> unidadesElegiveis = unidades.stream().filter(u -> {
            TipoUnidade tipo = u.getTipo();
            return tipo == OPERACIONAL || tipo == INTEROPERACIONAL || tipo == RAIZ;
        }).toList();

        if (unidadesElegiveis.isEmpty()) {
            log.warn("Nenhuma unidade elegível encontrada para o processo {}", processo.getCodigo());
            return;
        }

        log.info("Criando {} subprocessos para o processo {}", unidadesElegiveis.size(), processo.getCodigo());
        List<Subprocesso> subprocessos = unidadesElegiveis.stream()
                .map(unidade -> Subprocesso.builder()
                        .processo(processo)
                        .unidade(unidade)
                        .situacao(NAO_INICIADO)
                        .dataLimiteEtapa1(processo.getDataLimite())
                        .build())
                .map(Subprocesso.class::cast)
                .toList();

        List<Subprocesso> subprocessosSalvos = subprocessoRepo.saveAll(subprocessos);
        List<Mapa> mapas = subprocessosSalvos.stream()
                .<Mapa>map(sp -> Mapa.builder()
                        .subprocesso(sp)
                        .build())
                .toList();

        List<Mapa> mapasSalvos = mapaManutencaoService.salvarMapas(mapas);
        for (int i = 0; i < subprocessosSalvos.size(); i++) {
            subprocessosSalvos.get(i).setMapa(mapasSalvos.get(i));
        }

        List<Movimentacao> movimentacoes = new ArrayList<>();
        for (Subprocesso sp : subprocessosSalvos) {
            movimentacoes.add(Movimentacao.builder()
                    .subprocesso(sp)
                    .unidadeOrigem(unidadeOrigem)
                    .unidadeDestino(sp.getUnidade())
                    .usuario(usuario)
                    .descricao("Processo iniciado")
                    .build());
            sp.setLocalizacaoAtual(sp.getUnidade());
        }
        movimentacaoRepo.saveAll(movimentacoes);
    }

    public void criarParaRevisao(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario, NAO_INICIADO, "Processo iniciado");
        log.info("Criado subprocesso para unidade {}", unidade.getSigla());
    }

    public void criarParaDiagnostico(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa, Unidade unidadeOrigem, Usuario usuario) {
        Subprocesso subprocessoSalvo = criarSubprocessoComMapa(processo, unidade, unidadeMapa, unidadeOrigem, usuario,
                DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO, "Processo de diagnóstico iniciado");
        log.info("Criado subprocesso {} para unidade {}", subprocessoSalvo.getCodigo(), unidade.getSigla());
    }

    private Subprocesso criarSubprocessoComMapa(Processo processo, Unidade unidade, UnidadeMapa unidadeMapa,
                                                Unidade unidadeOrigem, Usuario usuario,
                                                SituacaoSubprocesso situacaoInicial, String descMovimentacao) {

        Mapa mapaVigente = unidadeMapa.getMapaVigente();
        if (mapaVigente == null) {
            throw new IllegalStateException("Unidade %s sem mapa vigente para revisão/diagnóstico".formatted(unidade.getSigla()));
        }
        Long codMapaVigente = mapaVigente.getCodigo();
        Subprocesso subprocesso = Subprocesso.builder()
                .processo(processo)
                .unidade(unidade)
                .situacao(situacaoInicial)
                .dataLimiteEtapa1(processo.getDataLimite())
                .build();

        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        Mapa mapaCopiado = copiaMapaService.copiarMapaParaUnidade(codMapaVigente);
        mapaCopiado.setSubprocesso(subprocessoSalvo);

        Mapa mapaSalvo = mapaManutencaoService.salvarMapa(mapaCopiado);
        subprocessoSalvo.setMapa(mapaSalvo);

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(subprocessoSalvo)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(unidade)
                .usuario(usuario)
                .descricao(descMovimentacao)
                .build());

        subprocessoSalvo.setLocalizacaoAtual(unidade);
        return subprocessoSalvo;
    }

    @Transactional
    public Mapa salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = consultaService.obterCodigoMapaObrigatorio(subprocesso);

        boolean eraVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.competencias().isEmpty();

        Mapa mapa = mapaSalvamentoService.salvarMapaCompleto(codMapa, request);
        mapaManutencaoService.reconciliarSituacaoSubprocesso(subprocesso);

        if (eraVazio && temNovasCompetencias) {
            atualizarSituacaoMapaVazio(subprocesso, false);
        }

        return mapa;
    }

    @Transactional
    public Mapa adicionarCompetencia(Long codSubprocesso, CompetenciaRequest request) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Mapa mapa = consultaService.obterMapaObrigatorio(subprocesso);
        Long codMapa = mapa.getCodigo();

        validarCompetenciaParaCriacao(request);

        boolean eraVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();

        mapaManutencaoService.criarCompetenciaComAtividades(mapa, request.descricao(), request.atividadesIds());

        if (eraVazio) {
            atualizarSituacaoMapaVazio(subprocesso, false);
        }

        return mapaManutencaoService.mapaCodigo(mapa.getCodigo());
    }

    public Mapa atualizarCompetencia(Long codSubprocesso, Long codCompetencia, CompetenciaRequest request) {
        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        mapaManutencaoService.atualizarCompetencia(codCompetencia, request.descricao(), request.atividadesIds());

        Mapa mapa = consultaService.obterMapaObrigatorio(sp);
        return mapaManutencaoService.mapaCodigo(mapa.getCodigo());
    }

    private void validarCompetenciaParaCriacao(CompetenciaRequest request) {
        if (request.atividadesIds().isEmpty()) {
            throw new ErroValidacao(Mensagens.COMPETENCIA_DEVE_TER_ATIVIDADE);
        }
    }

    public Mapa removerCompetencia(Long codSubprocesso, Long codCompetencia) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = consultaService.obterCodigoMapaObrigatorio(subprocesso);
        mapaManutencaoService.removerCompetencia(codCompetencia);

        boolean ficouVazio = mapaManutencaoService.competenciasCodMapa(codMapa).isEmpty();
        mapaManutencaoService.reconciliarSituacaoSubprocesso(subprocesso);
        if (ficouVazio) {
            atualizarSituacaoMapaVazio(subprocesso, true);
        }

        return mapaManutencaoService.mapaCodigo(consultaService.obterCodigoMapaObrigatorio(subprocesso));
    }

    private void atualizarSituacaoMapaVazio(Subprocesso subprocesso, boolean ficouVazio) {
        SituacaoSubprocesso situacaoAtual = subprocesso.getSituacao();

        if (ficouVazio) {
            if (situacaoAtual == MAPEAMENTO_MAPA_CRIADO) {
                subprocesso.setSituacao(MAPEAMENTO_CADASTRO_HOMOLOGADO);
                subprocessoRepo.save(subprocesso);
                log.info("Sit. subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", subprocesso.getCodigo());
            } else if (situacaoAtual == REVISAO_MAPA_AJUSTADO) {
                subprocesso.setSituacao(REVISAO_CADASTRO_HOMOLOGADA);
                subprocessoRepo.save(subprocesso);
                log.info("Sit. subprocesso {} alterada para REVISAO_CADASTRO_HOMOLOGADA (mapa ficou vazio)", subprocesso.getCodigo());
            }
        } else {
            if (situacaoAtual == MAPEAMENTO_CADASTRO_HOMOLOGADO) {
                subprocesso.setSituacao(MAPEAMENTO_MAPA_CRIADO);
                subprocessoRepo.save(subprocesso);
            } else if (situacaoAtual == REVISAO_CADASTRO_HOMOLOGADA) {
                subprocesso.setSituacao(REVISAO_MAPA_AJUSTADO);
                subprocessoRepo.save(subprocesso);
            }
        }
    }

    @Transactional(readOnly = true)
    public Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso sp = consultaService.buscarSubprocesso(codSubprocesso);
        validacaoService.validarSituacaoPermitida(sp,
                "Mapa só pode ser editado nas situações 'Mapa criado', 'Cadastro homologado', 'Mapa ajustado' ou 'Revisão do cadastro homologada'. Se o mapa estiver com sugestões, valide as sugestões primeiro para retornar à situação editável. Situação atual: %s".formatted(sp.getSituacao()),
                MAPEAMENTO_CADASTRO_HOMOLOGADO,
                MAPEAMENTO_MAPA_CRIADO,
                REVISAO_CADASTRO_HOMOLOGADA,
                REVISAO_MAPA_AJUSTADO);
        return sp;
    }

    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias) {
        log.info("Salvando ajustes no mapa do subprocesso {} ({} competências)", codSubprocesso, competencias.size());
        Subprocesso sp = repo.buscar(Subprocesso.class, codSubprocesso);

        validarSituacaoParaAjuste(sp);
        atualizarDescricoesAtividades(competencias);
        atualizarCompetenciasEAssociacoes(competencias);

        sp.setSituacao(REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    private void validarSituacaoParaAjuste(Subprocesso sp) {
        if (sp.getSituacao() != REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != REVISAO_MAPA_AJUSTADO) {
            throw new ErroValidacao(
                    Mensagens.AJUSTES_ESTADOS_ESPECIFICOS
                            .formatted(sp.getSituacao()));
        }
    }

    private void atualizarDescricoesAtividades(List<CompetenciaAjusteDto> competencias) {
        Map<Long, String> atividadeDescricoes = new HashMap<>();
        competencias.forEach(compDto -> compDto.getAtividades().forEach(
                ativDto -> atividadeDescricoes.put(ativDto.codAtividade(), ativDto.nome()))
        );

        if (!atividadeDescricoes.isEmpty()) {
            mapaManutencaoService.atualizarDescricoesAtividadeEmBloco(atividadeDescricoes);
        }
    }

    private void atualizarCompetenciasEAssociacoes(List<CompetenciaAjusteDto> competencias) {
        List<Long> competenciaIds = competencias.stream()
                .map(CompetenciaAjusteDto::getCodCompetencia)
                .toList();

        Map<Long, Competencia> mapaCompetencias = mapaManutencaoService.competenciasCodigos(competenciaIds)
                .stream()
                .collect(Collectors.toMap(Competencia::getCodigo, Function.identity()));

        List<Long> todasAtividadesIds = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(AtividadeAjusteDto::codAtividade)
                .distinct()
                .toList();

        Map<Long, Atividade> mapaAtividades = mapaManutencaoService.atividadesCodigos(todasAtividadesIds)
                .stream()
                .collect(Collectors.toMap(Atividade::getCodigo, Function.identity()));

        List<Competencia> competenciasParaSalvar = new ArrayList<>();
        for (CompetenciaAjusteDto compDto : competencias) {
            Competencia competencia = mapaCompetencias.get(compDto.getCodCompetencia());
            if (competencia != null) {
                competencia.setDescricao(compDto.getNome());

                Set<Atividade> atividadesSet = new HashSet<>();
                for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                    Atividade ativ = mapaAtividades.get(ativDto.codAtividade());
                    atividadesSet.add(ativ);
                }
                competencia.setAtividades(atividadesSet);
                competenciasParaSalvar.add(competencia);
            }
        }
        mapaManutencaoService.salvarCompetencias(competenciasParaSalvar);
    }

    @Transactional
    public boolean importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem, List<Long> codigosAtividades) {
        final Subprocesso spDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        Usuario usuario = usuarioFacade.usuarioAutenticado();

        if (!permissionEvaluator.verificarPermissao(usuario, spDestino, EDITAR_CADASTRO)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_IMPORTAR);
        }
        validacaoService.validarSituacaoPermitida(spDestino,
                Mensagens.SITUACAO_IMPEDE_IMPORTACAO.formatted(spDestino.getSituacao()),
                SITUACOES_PERMITIDAS_IMPORTACAO.toArray(new SituacaoSubprocesso[0]));

        Subprocesso spOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);
        if (!permissionEvaluator.verificarPermissao(usuario, spOrigem, CONSULTAR_PARA_IMPORTACAO)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_CONSULTAR_ORIGEM);
        }

        Long codMapaOrigem = consultaService.obterCodigoMapaObrigatorio(spOrigem);
        Long codMapaDestino = consultaService.obterCodigoMapaObrigatorio(spDestino);

        int totalParaImportar = codigosAtividades.size();
        log.info("Importando {} atividades do mapa #{} para o mapa #{}", totalParaImportar, codMapaOrigem, codMapaDestino);
        int importadas = copiaMapaService.importarAtividadesDeOutroMapa(codMapaOrigem, codMapaDestino, codigosAtividades);

        boolean temDuplicatas = !codigosAtividades.isEmpty() && importadas < totalParaImportar;

        if (spDestino.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();
            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                default -> log.debug("Tipo de processo {} não requer atualização de situação.", tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = Mensagens.HIST_IMPORTACAO_ATIVIDADES.formatted(
                spOrigem.getCodigo(),
                unidadeOrigem.getSigla());

        Movimentacao movimentacao = Movimentacao.builder()
                .subprocesso(spDestino)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(spDestino.getUnidade())
                .descricao(descMovimentacao)
                .usuario(usuario)
                .build();

        movimentacaoRepo.save(movimentacao);
        spDestino.setLocalizacaoAtual(spDestino.getUnidade());
        return temDuplicatas;
    }
}


