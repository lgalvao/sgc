package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.analise.AnaliseService;
import sgc.analise.dto.CriarAnaliseRequest;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.MapaCompletoDto;
import sgc.mapa.dto.SalvarMapaRequest;
import sgc.mapa.model.CompetenciaRepo;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.MapaService;
import sgc.processo.eventos.*;
import sgc.processo.model.TipoProcesso;
import sgc.sgrh.model.Usuario;
import sgc.subprocesso.dto.CompetenciaReq;
import sgc.subprocesso.dto.DisponibilizarMapaRequest;
import sgc.subprocesso.dto.SubmeterMapaAjustadoReq;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.stream.Collectors;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaWorkflowService {
    private final SubprocessoRepo subprocessoRepo;
    private final CompetenciaRepo competenciaRepo;
    private final AtividadeRepo atividadeRepo;
    private final MapaService mapaService;
    private final CompetenciaService competenciaService;
    private final ApplicationEventPublisher publicadorDeEventos;
    private final AnaliseService analiseService;
    private final UnidadeRepo unidadeRepo;
    private final SubprocessoService subprocessoService;

    public MapaCompletoDto salvarMapaSubprocesso(Long codSubprocesso, SalvarMapaRequest request, String tituloUsuario) {
        log.debug("Salvando mapa do subprocesso: codSubprocesso={}, usuario={}", codSubprocesso, tituloUsuario);

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaRepo.findByMapaCodigo(codMapa).isEmpty();
        boolean temNovasCompetencias = !request.getCompetencias().isEmpty();

        MapaCompletoDto mapaDto = mapaService.salvarMapaCompleto(codMapa, request, tituloUsuario);

        if (eraVazio
                && temNovasCompetencias
                && subprocesso.getSituacao()
                == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaDto;
    }

    public MapaCompletoDto adicionarCompetencia(Long codSubprocesso, CompetenciaReq request, String tituloUsuario) {
        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        boolean eraVazio = competenciaRepo.findByMapaCodigo(codMapa).isEmpty();

        competenciaService.adicionarCompetencia(
                subprocesso.getMapa(), request.getDescricao(), request.getAtividadesIds()
        );

        // Alterar situação para MAPA_CRIADO se era vazio e passou a ter competências
        if (eraVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO);
            subprocessoRepo.save(subprocesso);
        }

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto atualizarCompetencia(
            Long codSubprocesso,
            Long codCompetencia,
            CompetenciaReq request,
            String tituloUsuario) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);
        competenciaService.atualizarCompetencia(
                codCompetencia, request.getDescricao(), request.getAtividadesIds());

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    public MapaCompletoDto removerCompetencia(
            Long codSubprocesso, Long codCompetencia, String tituloUsuario) {

        Subprocesso subprocesso = getSubprocessoParaEdicao(codSubprocesso);

        Long codMapa = subprocesso.getMapa().getCodigo();
        competenciaService.removerCompetencia(codCompetencia);

        // Se o mapa ficou vazio e estava em MAPA_CRIADO, voltar para CADASTRO_HOMOLOGADO
        boolean ficouVazio = competenciaRepo.findByMapaCodigo(codMapa).isEmpty();
        if (ficouVazio && subprocesso.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO);
            subprocessoRepo.save(subprocesso);
            log.info("Situação do subprocesso {} alterada para CADASTRO_HOMOLOGADO (mapa ficou vazio)", codSubprocesso);
        }

        return mapaService.obterMapaCompleto(subprocesso.getMapa().getCodigo(), codSubprocesso);
    }

    private Subprocesso getSubprocessoParaEdicao(Long codSubprocesso) {
        Subprocesso subprocesso = buscarSubprocesso(codSubprocesso);

        SituacaoSubprocesso situacao = subprocesso.getSituacao();
        if (situacao != SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO
                && situacao != SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO
                && situacao != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && situacao != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Mapa só pode ser editado com cadastro homologado ou mapa criado. Situação atual: %s"
                            .formatted(situacao));
        }

        if (subprocesso.getMapa() == null) {
            throw new ErroEntidadeNaoEncontrada("Subprocesso não possui mapa associado");
        }
        return subprocesso;
    }

    @Transactional
    public void disponibilizarMapa(Long codSubprocesso, DisponibilizarMapaRequest request, Usuario usuario) {
        log.info("Disponibilizando mapa do subprocesso: codSubprocesso={}", codSubprocesso);

        Subprocesso sp = getSubprocessoParaEdicao(codSubprocesso);
        validarMapaParaDisponibilizacao(sp);
        
        // Validação adicional de associações (do service antigo)
        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        if (request.getDataLimite() == null) {
            throw new ErroValidacao("A data limite para validação é obrigatória.");
        }

        sp.getMapa().setSugestoes(null);
        analiseService.removerPorSubprocesso(codSubprocesso);
        
        if (request.getObservacoes() != null && !request.getObservacoes().isBlank()) {
            sp.getMapa().setSugestoes(request.getObservacoes());
        }

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
        }
        
        sp.setDataLimiteEtapa2(request.getDataLimite().atStartOfDay());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        Unidade sedoc = unidadeRepo.findBySigla("SEDOC")
                .orElseThrow(() -> new IllegalStateException("Unidade 'SEDOC' não encontrada."));

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaDisponibilizado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sedoc)
                        .unidadeDestino(sp.getUnidade())
                        .observacoes(request.getObservacoes())
                        .build());

        log.info("Subprocesso {} atualizado e mapa disponibilizado.", codSubprocesso);
    }

    private void validarMapaParaDisponibilizacao(Subprocesso subprocesso) {
        Long codMapa = subprocesso.getMapa().getCodigo();
        var competencias = competenciaRepo.findByMapaCodigo(codMapa);

        if (competencias.stream().anyMatch(c -> c.getAtividades().isEmpty())) {
            throw new ErroValidacao("Todas as competências devem estar associadas a pelo menos uma atividade.");
        }

        var atividadesDoSubprocesso = atividadeRepo.findBySubprocessoCodigo(subprocesso.getCodigo());

        var atividadesAssociadas = competencias.stream()
                .flatMap(c -> c.getAtividades().stream())
                .map(Atividade::getCodigo)
                .collect(Collectors.toSet());

        var atividadesNaoAssociadas = atividadesDoSubprocesso.stream()
                .filter(a -> !atividadesAssociadas.contains(a.getCodigo()))
                .toList();

        if (!atividadesNaoAssociadas.isEmpty()) {
            String nomesAtividades = atividadesNaoAssociadas.stream()
                    .map(Atividade::getDescricao)
                    .collect(Collectors.joining(", "));

            throw new ErroValidacao("""
                    Todas as atividades devem estar associadas a pelo menos uma competência.
                    Atividades pendentes: %s""".
                    formatted(nomesAtividades)
            );
        }
    }

    @Transactional
    public void apresentarSugestoes(Long codSubprocesso, String sugestoes, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getMapa() != null) {
            sp.getMapa().setSugestoes(sugestoes);
        }

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_COM_SUGESTOES);
        } else {
            sp.setSituacao(REVISAO_MAPA_COM_SUGESTOES);
        }

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        analiseService.removerPorSubprocesso(sp.getCodigo());

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaComSugestoes.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(sp.getUnidade().getUnidadeSuperior())
                        .observacoes(sugestoes)
                        .build());
    }

    @Transactional
    public void validarMapa(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_VALIDADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_VALIDADO);
        }

        sp.setDataFimEtapa2(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaValidado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(sp.getUnidade().getUnidadeSuperior())
                        .build());
    }

    @Transactional
    public void devolverValidacao(Long codSubprocesso, String justificativa, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes(justificativa)
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.DEVOLUCAO_MAPEAMENTO)
                        .siglaUnidade(sp.getUnidade().getUnidadeSuperior().getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(justificativa)
                        .build());

        Unidade unidadeDevolucao = sp.getUnidade();

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
        }

        sp.setDataFimEtapa2(null);
        subprocessoRepo.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaDevolvido.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade().getUnidadeSuperior())
                        .unidadeDestino(unidadeDevolucao)
                        .motivo(justificativa)
                        .build());
    }

    @Transactional
    public void aceitarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        analiseService.criarAnalise(
                CriarAnaliseRequest.builder()
                        .codSubprocesso(codSubprocesso)
                        .observacoes("Aceite da validação")
                        .tipo(TipoAnalise.VALIDACAO)
                        .acao(TipoAcaoAnalise.ACEITE_MAPEAMENTO)
                        .siglaUnidade(sp.getUnidade().getUnidadeSuperior().getSigla())
                        .tituloUsuario(String.valueOf(usuario.getTituloEleitoral()))
                        .motivo(null)
                        .build());

        Unidade unidadeSuperior = sp.getUnidade().getUnidadeSuperior();
        Unidade proximaUnidade =
                unidadeSuperior != null ? unidadeSuperior.getUnidadeSuperior() : null;

        if (proximaUnidade == null) {
            if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
                sp.setSituacao(MAPEAMENTO_MAPA_HOMOLOGADO);
            } else {
                sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
            }
            subprocessoRepo.save(sp);
        } else {
            if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
                sp.setSituacao(MAPEAMENTO_MAPA_VALIDADO);
            } else {
                sp.setSituacao(REVISAO_MAPA_VALIDADO);
            }
            subprocessoRepo.save(sp);

            publicadorDeEventos.publishEvent(
                    EventoSubprocessoMapaAceito.builder()
                            .codSubprocesso(codSubprocesso)
                            .usuario(usuario)
                            .unidadeOrigem(unidadeSuperior)
                            .unidadeDestino(proximaUnidade)
                            .build());
        }
    }

    @Transactional
    public void homologarValidacao(Long codSubprocesso, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_HOMOLOGADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_HOMOLOGADO);
        }
        subprocessoRepo.save(sp);

        Unidade sedoc =
                unidadeRepo
                        .findBySigla("SEDOC")
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Unidade 'SEDOC' não encontrada para registrar a"
                                                        + " homologação."));

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaHomologado.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sedoc)
                        .unidadeDestino(sedoc)
                        .build());
    }

    @Transactional
    public void submeterMapaAjustado(
            Long codSubprocesso, SubmeterMapaAjustadoReq request, Usuario usuario) {
        Subprocesso sp = buscarSubprocesso(codSubprocesso);

        subprocessoService.validarAssociacoesMapa(sp.getMapa().getCodigo());

        if (sp.getProcesso().getTipo() == TipoProcesso.MAPEAMENTO) {
            sp.setSituacao(MAPEAMENTO_MAPA_DISPONIBILIZADO);
        } else {
            sp.setSituacao(REVISAO_MAPA_DISPONIBILIZADO);
        }

        sp.setDataLimiteEtapa2(request.getDataLimiteEtapa2());
        sp.setDataFimEtapa1(java.time.LocalDateTime.now());
        subprocessoRepo.save(sp);

        publicadorDeEventos.publishEvent(
                EventoSubprocessoMapaAjustadoSubmetido.builder()
                        .codSubprocesso(codSubprocesso)
                        .usuario(usuario)
                        .unidadeOrigem(sp.getUnidade())
                        .unidadeDestino(sp.getUnidade())
                        .build());
    }

    private Subprocesso buscarSubprocesso(Long codSubprocesso) {
        return subprocessoRepo
                .findById(codSubprocesso)
                .orElseThrow(
                        () ->
                                new ErroEntidadeNaoEncontrada(
                                        "Subprocesso não encontrado: " + codSubprocesso));
    }
}