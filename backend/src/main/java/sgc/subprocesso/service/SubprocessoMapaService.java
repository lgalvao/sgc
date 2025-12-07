package sgc.subprocesso.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.model.Atividade;
import sgc.atividade.model.AtividadeRepo;
import sgc.atividade.model.Conhecimento;
import sgc.atividade.model.ConhecimentoRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.model.CompetenciaRepo;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaService {
    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoMovimentacaoRepo movimentacaoRepo;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final CompetenciaRepo competenciaRepo;

    @Transactional
    public void salvarAjustesMapa(
            Long codSubprocesso,
            List<CompetenciaAjusteDto> competencias,
            String usuarioTituloEleitoral) {
        Subprocesso sp =
                subprocessoRepo
                        .findById(codSubprocesso)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Subprocesso não encontrado: %d"
                                                        .formatted(codSubprocesso)));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", codSubprocesso);

        for (CompetenciaAjusteDto compDto : competencias) {
            var competencia =
                    competenciaRepo
                            .findById(compDto.getCodCompetencia())
                            .orElseThrow(
                                    () ->
                                            new ErroEntidadeNaoEncontrada(
                                                    "Competência não encontrada: %d"
                                                            .formatted(
                                                                    compDto.getCodCompetencia())));

            competencia.setDescricao(compDto.getNome());

            Set<Atividade> atividades = new HashSet<>();
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                var atividade =
                        atividadeRepo
                                .findById(ativDto.getCodAtividade())
                                .orElseThrow(
                                        () ->
                                                new ErroEntidadeNaoEncontrada(
                                                        "Atividade não encontrada: "
                                                                + ativDto.getCodAtividade()));
                atividade.setDescricao(ativDto.getNome());
                atividades.add(atividade);
            }
            competencia.setAtividades(atividades);
            competenciaRepo.save(competencia);
        }

        sp.setSituacao(SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino =
                subprocessoRepo
                        .findById(codSubprocessoDestino)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Subprocesso de destino não encontrado: %d"
                                                        .formatted(codSubprocessoDestino)));

        if (spDestino.getSituacao() != SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != SituacaoSubprocesso.NAO_INICIADO) {
            throw new ErroAtividadesEmSituacaoInvalida(
                    "Atividades só podem ser importadas para um subprocesso "
                            + "com cadastro em elaboração ou não iniciado.");
        }

        Subprocesso spOrigem =
                subprocessoRepo
                        .findById(codSubprocessoOrigem)
                        .orElseThrow(
                                () ->
                                        new ErroEntidadeNaoEncontrada(
                                                "Subprocesso de origem não encontrado: %d"
                                                        .formatted(codSubprocessoOrigem)));

        if (spOrigem.getMapa() == null || spDestino.getMapa() == null) {
            throw new ErroMapaNaoAssociado(
                    "Subprocesso de origem ou destino não possui mapa associado.");
        }

        List<Atividade> atividadesOrigem =
                atividadeRepo.findByMapaCodigo(spOrigem.getMapa().getCodigo());
        if (atividadesOrigem == null || atividadesOrigem.isEmpty()) {
            return;
        }

        List<String> descricoesExistentes =
                atividadeRepo.findByMapaCodigo(spDestino.getMapa().getCodigo()).stream()
                        .map(Atividade::getDescricao)
                        .toList();

        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                continue;
            }

            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeOrigem.getDescricao());
            novaAtividade.setMapa(spDestino.getMapa());
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);

            List<Conhecimento> conhecimentosOrigem =
                    conhecimentoRepo.findByAtividadeCodigo(atividadeOrigem.getCodigo());
            if (conhecimentosOrigem != null) {
                for (Conhecimento conhecimentoOrigem : conhecimentosOrigem) {
                    Conhecimento novoConhecimento = new Conhecimento();
                    novoConhecimento.setDescricao(conhecimentoOrigem.getDescricao());
                    novoConhecimento.setAtividade(atividadeSalva);
                    conhecimentoRepo.save(novoConhecimento);
                }
            }
        }

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();
            if (tipoProcesso == TipoProcesso.MAPEAMENTO) {
                spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
            } else if (tipoProcesso == TipoProcesso.REVISAO) {
                spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
            }
            subprocessoRepo.save(spDestino);
        }

        // final variable for distance check
        final Unidade unidadeOrigem = spOrigem.getUnidade();

        String descMovimentacao =
                String.format(
                        "Importação de atividades do subprocesso #%d (Unidade: %s)",
                        spOrigem.getCodigo(),
                        unidadeOrigem != null ? unidadeOrigem.getSigla() : "N/A");

        movimentacaoRepo.save(
                new Movimentacao(
                        spDestino, unidadeOrigem, spDestino.getUnidade(), descMovimentacao, null));

        log.info(
                "Atividades importadas do subprocesso {} para {}",
                codSubprocessoOrigem,
                codSubprocessoDestino);
    }
}
