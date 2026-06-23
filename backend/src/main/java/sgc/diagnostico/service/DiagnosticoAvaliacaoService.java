package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoAvaliacaoService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final SituacaoCapacitacaoRepo situacaoCapacitacaoRepo;
    private final DiagnosticoGapService gapService;
    private final DiagnosticoValidacaoService validacaoService;
    private final DiagnosticoNotificacaoService notificacaoService;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final DiagnosticoUsuarioContextoService usuarioContextoService;

    public void salvarAutoavaliacao(Long codSubprocesso, AutoavaliacaoRequest request) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), usuario.getTituloEleitoral());

        validarCompetenciasEsperadas(avaliacoes, request.competencias());

        Map<Long, AvaliacaoServidor> porCompetencia = avaliacoes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getCompetencia().getCodigo(), a -> a));

        for (AvaliacaoCompetenciaDto item : request.competencias()) {
            AvaliacaoServidor avaliacao = porCompetencia.get(item.competenciaCodigo());
            if (avaliacao == null) {
                continue;
            }
            avaliacao.setAutoimportancia(item.importancia());
            avaliacao.setAutodominio(item.dominio());
            avaliacao.setImportancia(item.importancia());
            avaliacao.setDominio(item.dominio());
            gapService.recalcularGap(avaliacao);
        }

        avaliacaoRepo.saveAll(avaliacoes);
        if (subprocesso.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            subprocesso.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_EM_ANDAMENTO);
        }
    }

    public void concluirAutoavaliacao(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        validacaoService.validarAutoavaliacaoCompleta(diagnostico.getCodigo(), usuario.getTituloEleitoral());

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), usuario.getTituloEleitoral());

        avaliacoes.forEach(a -> a.setSituacaoServidor(SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA));
        avaliacaoRepo.saveAll(avaliacoes);

        notificacaoService.notificarAutoavaliacaoConcluida(
                subprocessoConsultaService.buscarSubprocesso(codSubprocesso),
                usuario.getTituloEleitoral());
    }

    public void salvarConsenso(Long codSubprocesso, ConsensoRequest request, String servidorTitulo) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);

        Map<Long, AvaliacaoServidor> porCompetencia = avaliacoes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getCompetencia().getCodigo(), a -> a));

        for (ConsensoCompetenciaDto item : request.competencias()) {
            AvaliacaoServidor avaliacao = porCompetencia.get(item.competenciaCodigo());
            if (avaliacao == null) continue;
            avaliacao.setChefiaImportancia(item.chefiaImportancia());
            avaliacao.setChefiaDominio(item.chefiaDominio());
            avaliacao.setConsensoImportancia(item.consensoImportancia());
            avaliacao.setConsensoDominio(item.consensoDominio());
            avaliacao.setImportancia(item.consensoImportancia());
            avaliacao.setDominio(item.consensoDominio());
            gapService.recalcularGap(avaliacao);
        }

        avaliacaoRepo.saveAll(avaliacoes);
    }

    public void concluirConsenso(Long codSubprocesso, String servidorTitulo) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);

        validacaoService.validarConsensoCompleto(diagnostico.getCodigo(), servidorTitulo);

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);
        avaliacoes.forEach(avaliacao -> avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO));
        avaliacaoRepo.saveAll(avaliacoes);

        notificacaoService.notificarConsensoDisponivel(subprocesso, servidorTitulo);
    }

    public void aprovarConsenso(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), usuario.getTituloEleitoral());
        if (avaliacoes.stream().allMatch(a -> a.getSituacaoServidor() == SituacaoAvaliacaoServidor.CONSENSO_APROVADO)) {
            throw new ErroValidacao(Mensagens.CONSENSO_JA_APROVADO);
        }
        avaliacoes.forEach(a -> a.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_APROVADO));
        avaliacaoRepo.saveAll(avaliacoes);

        notificacaoService.notificarConsensoAprovado(
                subprocessoConsultaService.buscarSubprocesso(codSubprocesso),
                usuario.getTituloEleitoral());
    }

    public void impossibilitarAvaliacao(Long codSubprocesso, String servidorTitulo, String justificativa) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);
        avaliacoes.forEach(a -> {
            a.setSituacaoServidor(SituacaoAvaliacaoServidor.AVALIACAO_IMPOSSIBILITADA);
            a.setObservacao(justificativa);
        });
        avaliacaoRepo.saveAll(avaliacoes);
    }

    public void reverterImpossibilidade(Long codSubprocesso, String servidorTitulo) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);

        if (avaliacoes.isEmpty()) {
            throw new ErroEntidadeNaoEncontrada("AvaliacaoServidor", servidorTitulo);
        }

        boolean temAutoavaliacaoNula = avaliacoes.stream()
                .anyMatch(a -> a.getAutoimportancia() == null || a.getAutodominio() == null);

        SituacaoAvaliacaoServidor situacaoRetorno;
        if (temAutoavaliacaoNula) {
            situacaoRetorno = SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA;
        } else {
            boolean temChefiaPreenchida = avaliacoes.stream()
                    .anyMatch(a -> a.getChefiaImportancia() != null || a.getChefiaDominio() != null);
            if (temChefiaPreenchida) {
                situacaoRetorno = SituacaoAvaliacaoServidor.CONSENSO_CRIADO;
            } else {
                situacaoRetorno = SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA;
            }
        }

        avaliacoes.forEach(a -> {
            a.setSituacaoServidor(situacaoRetorno);
            a.setObservacao(null);

            if (situacaoRetorno == SituacaoAvaliacaoServidor.AUTOAVALIACAO_NAO_INICIADA) {
                a.setImportancia(null);
                a.setDominio(null);
                a.setConsensoImportancia(null);
                a.setConsensoDominio(null);
                a.setGap(null);
            } else if (situacaoRetorno == SituacaoAvaliacaoServidor.AUTOAVALIACAO_CONCLUIDA) {
                a.setImportancia(a.getAutoimportancia());
                a.setDominio(a.getAutodominio());
                a.calculaGap();
            } else {
                a.setImportancia(a.getConsensoImportancia() != null ? a.getConsensoImportancia()
                        : a.getChefiaImportancia() != null ? a.getChefiaImportancia() : a.getAutoimportancia());
                a.setDominio(a.getConsensoDominio() != null ? a.getConsensoDominio()
                        : a.getChefiaDominio() != null ? a.getChefiaDominio() : a.getAutodominio());
                a.calculaGap();
            }
        });

        avaliacaoRepo.saveAll(avaliacoes);
    }

    public void salvarSituacoesCapacitacao(Long codSubprocesso, SituacoesCapacitacaoRequest request) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var existentes = situacaoCapacitacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        Map<String, SituacaoCapacitacao> porChave = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        o -> o.getServidor().getTituloEleitoral() + ":" + o.getCompetencia().getCodigo(), o -> o));
        Map<String, AvaliacaoServidor> avaliacoesAprovadasPorChave = avaliacaoRepo.listarPorDiagnostico(diagnostico.getCodigo()).stream()
                .filter(avaliacao -> avaliacao.getSituacaoServidor() == SituacaoAvaliacaoServidor.CONSENSO_APROVADO)
                .collect(java.util.stream.Collectors.toMap(
                        avaliacao -> avaliacao.getServidor().getTituloEleitoral() + ":" + avaliacao.getCompetencia().getCodigo(),
                        avaliacao -> avaliacao
                ));
        var registrosParaSalvar = new ArrayList<>(existentes);

        for (SituacaoCapacitacaoDto item : request.situacoes()) {
            String chave = item.servidorTitulo() + ":" + item.competenciaCodigo();
            SituacaoCapacitacao registro = porChave.get(chave);
            if (registro == null) {
                AvaliacaoServidor avaliacao = avaliacoesAprovadasPorChave.get(chave);
                if (avaliacao == null) {
                    continue;
                }
                registro = criarSituacaoCapacitacao(diagnostico, avaliacao);
                porChave.put(chave, registro);
                registrosParaSalvar.add(registro);
            }
            registro.setSituacaoCapacitacao(
                    item.situacaoCapacitacao() == null ? null : ValorSituacaoCapacitacao.valueOf(item.situacaoCapacitacao())
            );
        }
        situacaoCapacitacaoRepo.saveAll(registrosParaSalvar);
    }

    private SituacaoCapacitacao criarSituacaoCapacitacao(Diagnostico diagnostico, AvaliacaoServidor avaliacao) {
        Subprocesso subprocesso = diagnostico.getSubprocesso();
        return SituacaoCapacitacao.builder()
                .diagnostico(diagnostico)
                .servidor(avaliacao.getServidor())
                .servidorNomeSnapshot(avaliacao.getServidorNomeDiagnostico())
                .unidadeCodigoSnapshot(subprocesso.getUnidade().getCodigo())
                .unidadeSiglaSnapshot(subprocesso.getUnidade().getSigla())
                .unidadeNomeSnapshot(subprocesso.getUnidade().getNome())
                .competencia(avaliacao.getCompetencia())
                .build();
    }

    private void validarCompetenciasEsperadas(
            java.util.List<AvaliacaoServidor> avaliacoes,
            java.util.List<AvaliacaoCompetenciaDto> competenciasInformadas
    ) {
        Set<Long> competenciasEsperadas = avaliacoes.stream()
                .map(a -> a.getCompetencia().getCodigo())
                .collect(Collectors.toSet());
        Set<Long> competenciasRecebidas = competenciasInformadas.stream()
                .map(AvaliacaoCompetenciaDto::competenciaCodigo)
                .collect(Collectors.toSet());
        if (competenciasEsperadas.size() != competenciasInformadas.size()
                || !competenciasEsperadas.equals(competenciasRecebidas)) {
            throw new ErroValidacao("A requisição deve informar exatamente as competências esperadas para a avaliação.");
        }
    }
}
