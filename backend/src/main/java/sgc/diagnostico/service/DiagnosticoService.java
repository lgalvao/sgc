package sgc.diagnostico.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sgc.diagnostico.dto.*;
import sgc.diagnostico.model.*;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.Mapa;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static sgc.diagnostico.model.SituacaoServidorDiagnostico.*;

/**
 * Serviço com a lógica de negócio do módulo de diagnóstico.
 */
@Service
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class DiagnosticoService {
    private final DiagnosticoRepo diagnosticoRepo;
    private final AvaliacaoServidorRepo avaliacaoServidorRepo;
    private final OcupacaoCriticaRepo ocupacaoCriticaRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final UsuarioRepo usuarioRepo;
    private final DiagnosticoDtoService dtoService;
    private final sgc.mapa.model.CompetenciaRepo competenciaRepo;

    /**
     * Busca ou cria um diagnóstico para um subprocesso.
     * Retorna o DTO completo com a visão geral.
     */
    @Transactional
    public DiagnosticoDto buscarDiagnosticoCompleto(Long subprocessoCodigo) {
        Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);
        Subprocesso subprocesso = diagnostico.getSubprocesso();

        // Busca todos os servidores da unidade
        // Assumindo que participam todos os servidores lotados na unidade do subprocesso
        List<Usuario> servidores = usuarioRepo.findByUnidadeLotacaoCodigo(subprocesso.getUnidade().getCodigo());

        // Busca todas as avaliações já realizadas
        List<AvaliacaoServidor> todasAvaliacoes = avaliacaoServidorRepo.findByDiagnosticoCodigo(diagnostico.getCodigo());

        // Agrupa avaliações por servidor
        Map<String, List<AvaliacaoServidor>> avaliacoesPorServidor = todasAvaliacoes.stream()
                .collect(Collectors.groupingBy(a -> a.getServidor().getTituloEleitoral()));

        // Busca ocupações críticas
        List<OcupacaoCritica> todasOcupacoes = ocupacaoCriticaRepo.findByDiagnosticoCodigo(diagnostico.getCodigo());
        Map<String, List<OcupacaoCritica>> ocupacoesPorServidor = todasOcupacoes.stream()
                .collect(Collectors.groupingBy(o -> o.getServidor().getTituloEleitoral()));

        // Total de competências do mapa
        int totalCompetenciasMap = 0;
        if (subprocesso.getMapa() != null) {
            totalCompetenciasMap = competenciaRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo()).size();
        }

        final int totalCompetencias = totalCompetenciasMap; // final para uso no lambda

        // Monta DTOs dos servidores
        List<ServidorDiagnosticoDto> servidoresDto = servidores.stream()
                .map(servidor -> {
                    List<AvaliacaoServidor> avaliacoes = avaliacoesPorServidor.getOrDefault(
                            servidor.getTituloEleitoral(), List.of()
                    );
                    List<OcupacaoCritica> ocupacoes = ocupacoesPorServidor.getOrDefault(
                            servidor.getTituloEleitoral(), List.of()
                    );
                    return dtoService.toDto(servidor, avaliacoes, ocupacoes, totalCompetencias);
                })
                .toList();

        // Verifica se pode ser concluído
        boolean podeSerConcluido = validarSePodeConcluirDiagnostico(servidoresDto);
        String motivo = podeSerConcluido ? null : "Existem pendências de avaliações ou ocupações críticas.";

        return dtoService.toDto(diagnostico, servidoresDto, podeSerConcluido, motivo);
    }

    @Transactional
    public Diagnostico buscarOuCriarDiagnosticoEntidade(Long subprocessoCodigo) {
        return diagnosticoRepo.findBySubprocessoCodigo(subprocessoCodigo).orElseGet(() -> {
            Subprocesso subprocesso = subprocessoRepo.findById(subprocessoCodigo)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Subprocesso não encontrado: %d".formatted(subprocessoCodigo))
                    );
            Diagnostico novo = new Diagnostico(subprocesso);
            return diagnosticoRepo.save(novo);
        });
    }

    @Transactional
    public AvaliacaoServidorDto salvarAvaliacao(
            Long subprocessoCodigo,
            String servidorTitulo,
            SalvarAvaliacaoRequest request) {
        try {
            Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);

            Usuario usuario = usuarioRepo.findById(servidorTitulo)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Servidor não encontrado: %s".formatted(servidorTitulo))
                    );

            Subprocesso subprocesso = diagnostico.getSubprocesso();
            Mapa mapa = subprocesso.getMapa();
            if (mapa == null) {
                throw new IllegalStateException("Subprocesso não possui mapa associado.");
            }

            List<Competencia> competenciasDoMapa = competenciaRepo.findByMapaCodigo(mapa.getCodigo());
            Competencia competencia = competenciasDoMapa.stream()
                    .filter(c -> c.getCodigo().equals(request.competenciaCodigo()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Competência não pertence ao mapa da unidade."));

            // Busca avaliação existente ou cria nova
            AvaliacaoServidor avaliacao = avaliacaoServidorRepo
                    .findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
                            diagnostico.getCodigo(), servidorTitulo, request.competenciaCodigo())
                    .orElse(new AvaliacaoServidor(diagnostico, usuario, competencia));

            avaliacao.setImportancia(request.importancia())
                    .setDominio(request.dominio())
                    .setObservacoes(request.observacoes())
                    .calcularGap();

            AvaliacaoServidor salvo = avaliacaoServidorRepo.save(avaliacao);

            log.debug("Avaliação salva com sucesso: diagnóstico={}, usuario={}, competencia={}",
                    diagnostico.getCodigo(), usuario.getTituloEleitoral(), competencia.getCodigo());

            return dtoService.toDto(salvo);
        } catch (Exception e) {
            log.error("Erro ao salvar avaliação: subprocesso={}, servidor={}, msg={}",
                    subprocessoCodigo, servidorTitulo, e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public List<AvaliacaoServidorDto> buscarMinhasAvaliacoes(Long subprocessoCodigo, String titulo) {
        Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);

        return avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoral(diagnostico.getCodigo(), titulo)
                .stream()
                .map(dtoService::toDto)
                .toList();
    }

    @Transactional
    public void concluirAutoavaliacao(Long subprocessoCodigo,
                                      String servidorTitulo,
                                      ConcluirAutoavaliacaoRequest request) {

        Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);

        // Valida se avaliou todas as competências
        Subprocesso subprocesso = diagnostico.getSubprocesso();
        if (subprocesso.getMapa() == null) throw new IllegalStateException("Mapa não encontrado");

        int totalCompetencias = competenciaRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo()).size();

        List<AvaliacaoServidor> avaliacoes = avaliacaoServidorRepo.findByDiagnosticoCodigoAndServidorTituloEleitoral(
                diagnostico.getCodigo(), servidorTitulo);

        if (avaliacoes.size() < totalCompetencias) {
            throw new IllegalStateException("Não é possível concluir autoavaliação: " +
                    "existem competências não avaliadas.");
        }

        // Verifica se todas têm importância e domínio
        boolean temIncompleta = avaliacoes.stream()
                .anyMatch(a -> a.getImportancia() == null || a.getDominio() == null);
        if (temIncompleta) {
            throw new IllegalStateException("Existem avaliações incompletas.");
        }

        // Atualiza situação de todas as avaliações deste servidor
        avaliacoes.forEach(a -> a.setSituacao(AUTOAVALIACAO_CONCLUIDA));

        avaliacaoServidorRepo.saveAll(avaliacoes);
    }

    @Transactional
    public OcupacaoCriticaDto salvarOcupacao(Long subprocessoCodigo, SalvarOcupacaoRequest request) {
        Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);

        Usuario servidor = usuarioRepo.findById(request.servidorTitulo())
                .orElseThrow(() -> new EntityNotFoundException("Servidor não encontrado"));

        Subprocesso subprocesso = diagnostico.getSubprocesso();
        if (subprocesso.getMapa() == null) throw new IllegalStateException("Mapa não encontrado");

        Competencia competencia = competenciaRepo.findByMapaCodigo(subprocesso.getMapa().getCodigo()).stream()
                .filter(c -> c.getCodigo().equals(request.competenciaCodigo()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Competência não encontrada no mapa"));

        OcupacaoCritica ocupacao = ocupacaoCriticaRepo
                .findByDiagnosticoCodigoAndServidorTituloEleitoralAndCompetenciaCodigo(
                        diagnostico.getCodigo(), request.servidorTitulo(), request.competenciaCodigo())
                .orElse(new OcupacaoCritica(diagnostico, servidor, competencia, request.situacao()));

        ocupacao.setSituacao(request.situacao());

        return dtoService.toDto(ocupacaoCriticaRepo.save(ocupacao));
    }

    @Transactional
    public List<OcupacaoCriticaDto> buscarOcupacoes(Long subprocessoCodigo) {
        Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);
        return ocupacaoCriticaRepo.findByDiagnosticoCodigo(diagnostico.getCodigo())
                .stream()
                .map(dtoService::toDto)
                .toList();
    }

    @Transactional
    public DiagnosticoDto concluirDiagnostico(Long subprocessoCodigo, ConcluirDiagnosticoRequest request) {
        Diagnostico diagnostico = buscarOuCriarDiagnosticoEntidade(subprocessoCodigo);

        DiagnosticoDto overview = buscarDiagnosticoCompleto(subprocessoCodigo);

        boolean temPendencias = false;
        long servidoresPendentes = overview.servidores().stream()
                .filter(s -> !s.situacao().equals(AUTOAVALIACAO_CONCLUIDA.name())
                        && !s.situacao().equals(CONSENSO_APROVADO.name())
                        && !s.situacao().equals(AVALIACAO_IMPOSSIBILITADA.name()))
                .count();

        if (servidoresPendentes > 0) {
            temPendencias = true;
        }

        if (temPendencias && (request.justificativa() == null || request.justificativa().isBlank())) {
            throw new IllegalStateException("Existem pendências e justificativa não foi fornecida.");
        }

        diagnostico.setSituacao(SituacaoDiagnostico.CONCLUIDO);
        diagnostico.setDataConclusao(LocalDateTime.now());
        diagnostico.setJustificativaConclusao(request.justificativa());

        diagnosticoRepo.save(diagnostico);

        // Atualiza situação do subprocesso
        Subprocesso sp = diagnostico.getSubprocesso();
        sp.setSituacao(SituacaoSubprocesso.DIAGNOSTICO_CONCLUIDO);
        subprocessoRepo.save(sp);

        return overview;
    }

    private boolean validarSePodeConcluirDiagnostico(List<ServidorDiagnosticoDto> servidores) {
        return servidores.stream()
                .allMatch(s -> AUTOAVALIACAO_CONCLUIDA.name().equals(s.situacao()) ||
                        CONSENSO_APROVADO.name().equals(s.situacao()) ||
                        AVALIACAO_IMPOSSIBILITADA.name().equals(s.situacao())
                );
    }
}
