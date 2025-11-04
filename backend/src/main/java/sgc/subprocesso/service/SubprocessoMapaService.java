package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.atividade.modelo.Atividade;
import sgc.atividade.modelo.AtividadeRepo;
import sgc.atividade.modelo.Conhecimento;
import sgc.atividade.modelo.ConhecimentoRepo;
import sgc.competencia.modelo.CompetenciaAtividade;
import sgc.competencia.modelo.CompetenciaAtividadeRepo;
import sgc.competencia.modelo.CompetenciaRepo;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.erros.ErroNegocio;
import sgc.subprocesso.dto.AtividadeAjusteDto;
import sgc.subprocesso.dto.CompetenciaAjusteDto;
import sgc.subprocesso.modelo.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubprocessoMapaService {
    private final SubprocessoRepo subprocessoRepo;
    private final MovimentacaoRepo movimentacaoRepo;
    private final AtividadeRepo atividadeRepo;
    private final ConhecimentoRepo conhecimentoRepo;
    private final CompetenciaAtividadeRepo competenciaAtividadeRepo;
    private final CompetenciaRepo competenciaRepo;

    /**
     * Salva os ajustes realizados em um mapa de competências após a fase de validação.
     * <p>
     * Este método executa as seguintes ações:
     * <ul>
     *     <li>Valida se o subprocesso está em uma situação que permite ajustes.</li>
     *     <li>Atualiza as descrições das competências e atividades conforme os dados recebidos.</li>
     *     <li>Remove todos os vínculos existentes entre competências e atividades do mapa.</li>
     *     <li>Recria os vínculos com base na nova estrutura fornecida.</li>
     *     <li>Altera a situação do subprocesso para 'MAPA_AJUSTADO'.</li>
     * </ul>
     *
     * @param codSubprocesso         O código do subprocesso cujo mapa está sendo ajustado.
     * @param competencias           A lista de competências com suas atividades aninhadas,
     *                               representando o estado final do mapa.
     * @param usuarioTituloEleitoral O título de eleitor do usuário que realiza a operação.
     * @throws ErroEntidadeNaoEncontrada se o subprocesso ou alguma das entidades
     *                                   (competência, atividade) não forem encontradas.
     * @throws IllegalStateException     se o subprocesso não estiver na situação correta
     *                                   para permitir o ajuste.
     */
    @Transactional
    public void salvarAjustesMapa(Long codSubprocesso, List<CompetenciaAjusteDto> competencias, Long usuarioTituloEleitoral) {
        Subprocesso sp = subprocessoRepo.findById(codSubprocesso)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso não encontrado: %d".formatted(codSubprocesso)));

        if (sp.getSituacao() != SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA &&
                sp.getSituacao() != SituacaoSubprocesso.MAPA_AJUSTADO) {
            // TODO usar exceções mais específicas nessa classe toda
            throw new ErroNegocio("Ajustes no mapa só podem ser feitos em estados específicos. Situação atual: %s".formatted(sp.getSituacao()));
        }

        log.info("Salvando ajustes para o mapa do subprocesso {}...", codSubprocesso);

        // Atualiza as descrições
        for (CompetenciaAjusteDto compDto : competencias) {
            competenciaRepo.findById(compDto.getCodCompetencia()).ifPresent(c -> c.setDescricao(compDto.getNome()));
            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                atividadeRepo.findById(ativDto.getCodAtividade()).ifPresent(a -> a.setDescricao(ativDto.getNome()));
            }
        }

        // Recria os Vínculos
        competenciaAtividadeRepo.deleteByCompetenciaMapaCodigo(sp.getMapa().getCodigo());

        for (CompetenciaAjusteDto compDto : competencias) {
            var competencia = competenciaRepo.findById(compDto.getCodCompetencia())
                    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Competência não encontrada: %d".formatted(compDto.getCodCompetencia())));

            for (AtividadeAjusteDto ativDto : compDto.getAtividades()) {
                var atividade = atividadeRepo.findById(ativDto.getCodAtividade())
                        .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Atividade não encontrada: " + ativDto.getCodAtividade()));

                var id = new CompetenciaAtividade.Id(ativDto.getCodAtividade(), compDto.getCodCompetencia());
                CompetenciaAtividade novoLink = new CompetenciaAtividade(id, competencia, atividade);
                competenciaAtividadeRepo.save(novoLink);
            }
        }

        sp.setSituacao(SituacaoSubprocesso.MAPA_AJUSTADO);
        subprocessoRepo.save(sp);
    }

    /**
     * Importa atividades de um subprocesso de origem para um subprocesso de destino.
     * <p>
     * A importação clona as atividades e seus respectivos conhecimentos. Uma
     * atividade só é importada se uma outra com a mesma descrição ainda não
     * existir no mapa de destino.
     *
     * @param codSubprocessoDestino O código do subprocesso para o qual as atividades serão importadas.
     * @param codSubprocessoOrigem  O código do subprocesso do qual as atividades serão copiadas.
     * @throws ErroEntidadeNaoEncontrada se um dos subprocessos não for encontrado.
     * @throws IllegalStateException     se o subprocesso de destino não estiver na
     *                                   situação 'CADASTRO_EM_ANDAMENTO', ou se um
     *                                   dos subprocessos não tiver um mapa associado.
     */
    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        Subprocesso spDestino = subprocessoRepo.findById(codSubprocessoDestino)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de destino não encontrado: %d".formatted(codSubprocessoDestino)));

        if (spDestino.getSituacao() != SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO) {
            throw new ErroNegocio("Atividades só podem ser importadas para um subprocesso com cadastro em elaboração.");
        }

        Subprocesso spOrigem = subprocessoRepo.findById(codSubprocessoOrigem)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Subprocesso de origem não encontrado: %d".formatted(codSubprocessoOrigem)));

        if (spOrigem.getMapa() == null || spDestino.getMapa() == null) {
            throw new ErroNegocio("Subprocesso de origem ou destino não possui mapa associado.");
        }

        List<Atividade> atividadesOrigem = atividadeRepo.findByMapaCodigo(spOrigem.getMapa().getCodigo());
        if (atividadesOrigem == null || atividadesOrigem.isEmpty()) {
            return; // Nada a importar
        }

        List<String> descricoesExistentes = atividadeRepo.findByMapaCodigo(spDestino.getMapa().getCodigo())
                .stream()
                .map(Atividade::getDescricao)
                .toList();

        for (Atividade atividadeOrigem : atividadesOrigem) {
            if (descricoesExistentes.contains(atividadeOrigem.getDescricao())) {
                continue; // Pula a importação se a atividade já existe
            }

            Atividade novaAtividade = new Atividade();
            novaAtividade.setDescricao(atividadeOrigem.getDescricao());
            novaAtividade.setMapa(spDestino.getMapa());
            Atividade atividadeSalva = atividadeRepo.save(novaAtividade);

            List<Conhecimento> conhecimentosOrigem = conhecimentoRepo.findByAtividadeCodigo(atividadeOrigem.getCodigo());
            if (conhecimentosOrigem != null) {
                for (Conhecimento conhecimentoOrigem : conhecimentosOrigem) {
                    Conhecimento novoConhecimento = new Conhecimento();
                    novoConhecimento.setDescricao(conhecimentoOrigem.getDescricao());
                    novoConhecimento.setAtividade(atividadeSalva);
                    conhecimentoRepo.save(novoConhecimento);
                }
            }
        }

        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                spOrigem.getUnidade() != null ? spOrigem.getUnidade().getSigla() : "N/A");

        // TODO Estranho passar o destino duas vezes nesse construtor. Bug?
        movimentacaoRepo.save(new Movimentacao(spDestino, spDestino.getUnidade(), spDestino.getUnidade(), descMovimentacao));

        log.info("Atividades importadas com sucesso do subprocesso {} para {}", codSubprocessoOrigem, codSubprocessoDestino);
    }
}