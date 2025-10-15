package sgc.processo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroDominioNaoEncontrado;
import sgc.processo.modelo.ErroProcesso;
import sgc.mapa.CopiaMapaService;
import sgc.mapa.modelo.Mapa;
import sgc.mapa.modelo.MapaRepo;
import sgc.mapa.modelo.UnidadeMapa;
import sgc.mapa.modelo.UnidadeMapaRepo;
import sgc.processo.eventos.ProcessoIniciadoEvento;
import sgc.processo.modelo.*;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Movimentacao;
import sgc.subprocesso.modelo.MovimentacaoRepo;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.subprocesso.modelo.SubprocessoRepo;
import sgc.unidade.modelo.TipoUnidade;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessoIniciacaoService {

    private final ProcessoRepo processoRepo;
    private final UnidadeRepo unidadeRepo;
    private final UnidadeProcessoRepo unidadeProcessoRepo;
    private final SubprocessoRepo subprocessoRepo;
    private final MapaRepo mapaRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UnidadeMapaRepo unidadeMapaRepo;
    private final CopiaMapaService servicoDeCopiaDeMapa;
    private final ApplicationEventPublisher publicadorDeEventos;

    @Transactional
    public void iniciarProcessoMapeamento(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de mapeamento.");
        }

        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", codigoUnidade));

            criarSubprocessoParaMapeamento(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processo.getCodigo(),
                processo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de mapeamento {} iniciado para {} unidades.", id, codigosUnidades.size());
    }

    @Transactional
    public void iniciarProcessoRevisao(Long id, List<Long> codigosUnidades) {
        Processo processo = processoRepo.findById(id)
                .orElseThrow(() -> new ErroDominioNaoEncontrado("Processo", id));

        if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
            throw new IllegalStateException("Apenas processos na situação 'CRIADO' podem ser iniciados.");
        }

        if (codigosUnidades == null || codigosUnidades.isEmpty()) {
            throw new IllegalArgumentException("A lista de unidades é obrigatória para iniciar o processo de revisão.");
        }

        validarUnidadesComMapasVigentes(codigosUnidades);
        validarUnidadesNaoEmProcessosAtivos(codigosUnidades);

        for (Long codigoUnidade : codigosUnidades) {
            Unidade unidade = unidadeRepo.findById(codigoUnidade)
                    .orElseThrow(() -> new ErroDominioNaoEncontrado("Unidade", codigoUnidade));

            criarSubprocessoParaRevisao(processo, unidade);
        }

        processo.setSituacao(SituacaoProcesso.EM_ANDAMENTO);
        processoRepo.save(processo);

        publicadorDeEventos.publishEvent(new ProcessoIniciadoEvento(
                processo.getCodigo(),
                processo.getTipo().name(),
                LocalDateTime.now(),
                codigosUnidades
        ));

        log.info("Processo de revisão {} iniciado para {} unidades.", id, codigosUnidades.size());
    }

    private void validarUnidadesNaoEmProcessosAtivos(List<Long> codigosUnidades) {
        List<Long> unidadesEmProcesso = unidadeProcessoRepo.findUnidadesInProcessosAtivos(codigosUnidades);
        if (!unidadesEmProcesso.isEmpty()) {
            throw new ErroProcesso("As seguintes unidades já participam de outro processo ativo: " + unidadesEmProcesso);
        }
    }

    private void validarUnidadesComMapasVigentes(List<Long> codigosUnidades) {
        // Busca todos os mapas vigentes para as unidades em uma única consulta.
        List<Long> unidadesComMapaVigente = unidadeMapaRepo.findCodigosUnidadesComMapaVigente(codigosUnidades);

        if (unidadesComMapaVigente.size() < codigosUnidades.size()) {
            List<Long> unidadesSemMapa = codigosUnidades.stream()
                .filter(c -> !unidadesComMapaVigente.contains(c))
                .toList();

            // Busca as siglas das unidades sem mapa em uma única consulta.
            List<String> siglasUnidadesSemMapa = unidadeRepo.findSiglasByCodigos(unidadesSemMapa);

            throw new ErroProcesso(String.format(
                "As seguintes unidades não possuem mapa vigente e não podem participar de um processo de revisão: %s",
                String.join(", ", siglasUnidadesSemMapa)
            ));
        }
    }

    private UnidadeProcesso criarSnapshotUnidadeProcesso(Processo processo, Unidade unidade) {
        return new UnidadeProcesso(
            processo.getCodigo(),
            unidade.getCodigo(), // Adicionado o código da unidade
            unidade.getNome(),
            unidade.getSigla(),
            unidade.getTitular() != null ? String.valueOf(unidade.getTitular().getTituloEleitoral()) : null,
            unidade.getTipo(),
            "PENDENTE",
            unidade.getUnidadeSuperior() != null ? unidade.getUnidadeSuperior().getCodigo() : null
        );
    }

    private void criarSubprocessoParaMapeamento(Processo processo, Unidade unidade) {
        UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
        unidadeProcessoRepo.save(unidadeProcesso);

        if (TipoUnidade.OPERACIONAL.equals(unidade.getTipo()) || TipoUnidade.INTEROPERACIONAL.equals(unidade.getTipo())) {
            Mapa mapa = mapaRepo.save(new Mapa());

            Subprocesso subprocesso = new Subprocesso(processo, unidade, mapa, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
            Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

            movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo iniciado"));
        }
    }

    private void criarSubprocessoParaRevisao(Processo processo, Unidade unidade) {
        UnidadeMapa unidadeMapa = unidadeMapaRepo.findByUnidadeCodigo(unidade.getCodigo())
            .orElseThrow(() -> new IllegalStateException("Configuração de mapa vigente não encontrada para a unidade: " + unidade.getSigla()));

        Long idMapaVigente = unidadeMapa.getMapaVigenteCodigo();

        Mapa mapaCopiado = servicoDeCopiaDeMapa.copiarMapaParaUnidade(idMapaVigente, unidade.getCodigo());

        UnidadeProcesso unidadeProcesso = criarSnapshotUnidadeProcesso(processo, unidade);
        unidadeProcessoRepo.save(unidadeProcesso);

        Subprocesso subprocesso = new Subprocesso(processo, unidade, mapaCopiado, SituacaoSubprocesso.NAO_INICIADO, processo.getDataLimite());
        Subprocesso subprocessoSalvo = subprocessoRepo.save(subprocesso);

        movimentacaoRepo.save(new Movimentacao(subprocessoSalvo, null, unidade, "Processo de revisão iniciado"));
    }
}