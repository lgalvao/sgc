package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.service.CompetenciaService;
import sgc.mapa.service.CopiaMapaService;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaService {
    private final SubprocessoRepo subprocessoRepo;
    private final SubprocessoMovimentacaoRepo movimentacaoRepo;
    private final AtividadeService atividadeService;
    private final CompetenciaService competenciaService;
    private final CopiaMapaService copiaMapaService;
    private final AtividadeMapper atividadeMapper;

    @Transactional
    public void salvarAjustesMapa(
            Long codSubprocesso,
            List<CompetenciaAjusteDto> competencias,
            String usuarioTituloEleitoral) {

        Subprocesso sp = subprocessoRepo
                .findById(codSubprocesso)
                .orElseThrow(() ->
                        new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        if (sp.getSituacao() != REVISAO_CADASTRO_HOMOLOGADA
                && sp.getSituacao() != REVISAO_MAPA_AJUSTADO) {
            throw new ErroMapaEmSituacaoInvalida(
                    "Ajustes no mapa só podem ser feitos em estados específicos. "
                            + "Situação atual: %s".formatted(sp.getSituacao()));
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", codSubprocesso);

        for (CompetenciaAjusteDto compDto : competencias) {
            var competencia = competenciaService.buscarPorCodigo(compDto.getCodCompetencia());

            competencia.setDescricao(compDto.getNome());

            Set<Atividade> atividades = new HashSet<>();
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                // Usando AtividadeService.atualizar -> mas preciso da Entidade para associar à competência
                // Se AtividadeService não expõe entidade, tenho que mudar o service
                // Mas AtividadeService expõe `obterEntidadePorCodigo`.
                var atividade = atividadeService.obterPorCodigo(ativDto.getCodAtividade());

                // Atualizar descrição via service (para manter regras de negócio se houver)
                // Ou apenas setar aqui já que é um "ajuste"?
                // Melhor usar o service para atualizar.

                AtividadeDto dto = atividadeMapper.toDto(atividade);
                dto.setDescricao(ativDto.getNome());
                atividadeService.atualizar(atividade.getCodigo(), dto);

                // Recarrega entidade atualizada
                atividades.add(atividadeService.obterPorCodigo(atividade.getCodigo()));
            }
            competencia.setAtividades(atividades);
            competenciaService.salvar(competencia);
        }

        sp.setSituacao(REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino = subprocessoRepo
                .findById(codSubprocessoDestino)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de destino não encontrado: %d".formatted(codSubprocessoDestino)));

        if (spDestino.getSituacao() != MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != REVISAO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != NAO_INICIADO) {

            throw new ErroAtividadesEmSituacaoInvalida("""
                    Atividades só podem ser importadas para um subprocesso
                    com cadastro em elaboração ou não iniciado.""");
        }

        Subprocesso spOrigem = subprocessoRepo.findById(codSubprocessoOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de origem não encontrado: %d".formatted(codSubprocessoOrigem)));

        if (spOrigem.getMapa() == null || spDestino.getMapa() == null) {
            throw new ErroMapaNaoAssociado("Subprocesso de origem ou destino não possui mapa associado.");
        }

        List<Atividade> atividadesOrigem = atividadeService.buscarPorMapaCodigo(spOrigem.getMapa().getCodigo());

        if (atividadesOrigem == null || atividadesOrigem.isEmpty()) {
            return;
        }

        List<String> descricoesExistentes =
                atividadeService.buscarPorMapaCodigo(spDestino.getMapa().getCodigo()).stream()
                        .map(Atividade::getDescricao)
                        .toList();

        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                continue;
            }

            AtividadeDto novaDto = new AtividadeDto();
            novaDto.setDescricao(atividadeOrigem.getDescricao());
            novaDto.setMapaCodigo(spDestino.getMapa().getCodigo());
        }

        copiaMapaService.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo()
        );

        if (spDestino.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                case null, default -> log.debug("Tipo de processo {} não requer atualização automática de situação no import.", tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem != null ? unidadeOrigem.getSigla() : "N/A");

        movimentacaoRepo.save(new Movimentacao(
                spDestino,
                unidadeOrigem,
                spDestino.getUnidade(),
                descMovimentacao,
                null)
        );

        log.info("Atividades importadas do subprocesso {} para {}", codSubprocessoOrigem, codSubprocessoDestino);
    }
}
