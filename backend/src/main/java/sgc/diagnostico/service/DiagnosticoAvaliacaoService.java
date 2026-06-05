package sgc.diagnostico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroValidacao;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiagnosticoAvaliacaoService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final AvaliacaoServidorRepo avaliacaoRepo;
    private final OcupacaoCriticaRepo ocupacaoRepo;
    private final DiagnosticoGapService gapService;
    private final DiagnosticoValidacaoService validacaoService;
    private final DiagnosticoNotificacaoService notificacaoService;
    private final SubprocessoConsultaService subprocessoConsultaService;
    private final DiagnosticoUsuarioContextoService usuarioContextoService;

    public void salvarAutoavaliacao(Long codSubprocesso, AutoavaliacaoRequest request) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

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
        Subprocesso subprocesso = subprocessoConsultaService.buscarSubprocesso(codSubprocesso);

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), servidorTitulo);
        List<AvaliacaoCompetenciaDto> competenciasConsenso = extrairCompetenciasConsenso(request);

        Map<Long, AvaliacaoServidor> porCompetencia = avaliacoes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        a -> a.getCompetencia().getCodigo(), a -> a));

        for (AvaliacaoServidor avaliacao : avaliacoes) {
            avaliacao.setSituacaoServidor(SituacaoAvaliacaoServidor.CONSENSO_CRIADO);
        }

        if (request.competenciasDetalhadas() != null && !request.competenciasDetalhadas().isEmpty()) {
            for (ConsensoCompetenciaDto item : request.competenciasDetalhadas()) {
                AvaliacaoServidor avaliacao = porCompetencia.get(item.competenciaCodigo());
                if (avaliacao == null) continue;
                avaliacao.setChefiaImportancia(item.chefiaImportancia());
                avaliacao.setChefiaDominio(item.chefiaDominio());
                avaliacao.setImportancia(item.consensoImportancia());
                avaliacao.setDominio(item.consensoDominio());
                gapService.recalcularGap(avaliacao);
            }
        } else {
            for (AvaliacaoCompetenciaDto item : competenciasConsenso) {
                AvaliacaoServidor avaliacao = porCompetencia.get(item.competenciaCodigo());
                if (avaliacao == null) continue;
                avaliacao.setChefiaImportancia(item.importancia());
                avaliacao.setChefiaDominio(item.dominio());
                avaliacao.setImportancia(item.importancia());
                avaliacao.setDominio(item.dominio());
                gapService.recalcularGap(avaliacao);
            }
        }

        avaliacaoRepo.saveAll(avaliacoes);
        notificacaoService.notificarConsensoDisponivel(subprocesso, servidorTitulo);
    }

    private List<AvaliacaoCompetenciaDto> extrairCompetenciasConsenso(ConsensoRequest request) {
        if (request.competenciasDetalhadas() != null && !request.competenciasDetalhadas().isEmpty()) {
            return request.competenciasDetalhadas().stream()
                    .map(item -> AvaliacaoCompetenciaDto.builder()
                            .competenciaCodigo(item.competenciaCodigo())
                            .importancia(item.consensoImportancia())
                            .dominio(item.consensoDominio())
                            .build())
                    .toList();
        }
        return request.competencias();
    }

    public void aprovarConsenso(Long codSubprocesso) {
        Usuario usuario = usuarioContextoService.usuarioAutenticado();
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var avaliacoes = avaliacaoRepo.buscarAvaliacoesDoServidor(
                diagnostico.getCodigo(), usuario.getTituloEleitoral());
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
            a.setImportancia(null);
            a.setDominio(null);
            a.setGap(null);
            a.setObservacao(justificativa);
        });
        avaliacaoRepo.saveAll(avaliacoes);
    }

    public void salvarOcupacoesCriticas(Long codSubprocesso, OcupacoesCriticasRequest request) {
        Diagnostico diagnostico = diagnosticoRepo.findBySubprocessoCodigo(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Diagnostico", codSubprocesso));

        var existentes = ocupacaoRepo.listarPorDiagnostico(diagnostico.getCodigo());
        Map<String, OcupacaoCritica> porChave = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        o -> o.getServidor().getTituloEleitoral() + ":" + o.getCompetencia().getCodigo(), o -> o));

        for (OcupacaoCriticaDto item : request.ocupacoes()) {
            String chave = item.servidorTitulo() + ":" + item.competenciaCodigo();
            OcupacaoCritica registro = porChave.get(chave);
            if (registro == null) {
                continue;
            }
            registro.setSituacaoCapacitacao(
                    item.situacaoCapacitacao() == null ? null : SituacaoCapacitacao.valueOf(item.situacaoCapacitacao())
            );
        }
        ocupacaoRepo.saveAll(existentes);
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
