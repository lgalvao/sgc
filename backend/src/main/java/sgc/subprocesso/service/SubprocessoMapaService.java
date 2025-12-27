package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.AtividadeService;
import sgc.mapa.model.Conhecimento;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.mapa.service.CompetenciaService;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.erros.ErroAtividadesEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaEmSituacaoInvalida;
import sgc.subprocesso.erros.ErroMapaNaoAssociado;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoMovimentacaoRepo;
import sgc.subprocesso.model.SubprocessoRepo;
import sgc.unidade.model.Unidade;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.mapper.AtividadeMapper;
import sgc.mapa.mapper.ConhecimentoMapper;

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
    private final AtividadeMapper atividadeMapper;
    private final ConhecimentoMapper conhecimentoMapper;

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
            var competencia = competenciaService.buscarPorId(compDto.getCodCompetencia());

            competencia.setDescricao(compDto.getNome());

            Set<Atividade> atividades = new HashSet<>();
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                // Usando AtividadeService.atualizar -> mas preciso da Entidade para associar à competência
                // Se AtividadeService não expõe entidade, tenho que mudar o service
                // Mas AtividadeService expõe `obterEntidadePorCodigo`.
                var atividade = atividadeService.obterEntidadePorCodigo(ativDto.getCodAtividade());

                // Atualizar descrição via service (para manter regras de negócio se houver)
                // Ou apenas setar aqui já que é um "ajuste"?
                // Melhor usar o service para atualizar.

                AtividadeDto dto = atividadeMapper.toDto(atividade);
                dto.setDescricao(ativDto.getNome());
                atividadeService.atualizar(atividade.getCodigo(), dto);

                // Recarrega entidade atualizada
                atividades.add(atividadeService.obterEntidadePorCodigo(atividade.getCodigo()));
            }
            competencia.setAtividades(atividades);
            competenciaService.salvar(competencia);
        }

        sp.setSituacao(REVISAO_MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino =
                subprocessoRepo
                        .findById(codSubprocessoDestino)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de destino não encontrado: %d"
                                .formatted(codSubprocessoDestino)));

        if (spDestino.getSituacao() != MAPEAMENTO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != REVISAO_CADASTRO_EM_ANDAMENTO
                && spDestino.getSituacao() != NAO_INICIADO) {

            throw new ErroAtividadesEmSituacaoInvalida("""
                    Atividades só podem ser importadas para um subprocesso
                    com cadastro em elaboração ou não iniciado.""");
        }

        Subprocesso spOrigem =
                subprocessoRepo
                        .findById(codSubprocessoOrigem)
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
                                "Subprocesso de origem não encontrado: %d".formatted(codSubprocessoOrigem)));

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

            // Usando AtividadeService.criar
            AtividadeDto novaDto = new AtividadeDto();
            novaDto.setDescricao(atividadeOrigem.getDescricao());
            novaDto.setMapaCodigo(spDestino.getMapa().getCodigo());

            // Precisamos de um usuário titular para criar atividade via service...
            // O importador é um processo de sistema/usuário logado?
            // `importarAtividades` não recebe usuarioTitulo.
            // Mas `criar` exige validação de titularidade.
            // Solução: Criar um método `duplicarAtividade(Atividade origem, Mapa destino)` no AtividadeService?
            // Ou manter a lógica aqui, mas usando os métodos expostos pelo service que não exigem validação (se houver)
            // Como AtividadeService.criar exige validação, e aqui é uma importação sistêmica,
            // talvez devêssemos expor um método `importar` no AtividadeService.

            // Por enquanto, vou usar o AtividadeRepo indiretamente via Service? Não, a ideia é não usar Repo de outro módulo.
            // Então `AtividadeService` deve ter um método `duplicarAtividade`.

            // Vou assumir que posso chamar um método novo no AtividadeService ou refatorar isso depois.
            // Para simplificar agora, e dado que `importarAtividades` é um caso de uso específico,
            // vou adicionar `importarAtividade` no `AtividadeService`.
        }

        // REFAZENDO LOOP PARA CHAMAR NOVO METODO NO SERVICE
        atividadeService.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo()
        );

        if (spDestino.getSituacao() == NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(REVISAO_CADASTRO_EM_ANDAMENTO);
                case null, default -> {
                }
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao =
                String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
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
