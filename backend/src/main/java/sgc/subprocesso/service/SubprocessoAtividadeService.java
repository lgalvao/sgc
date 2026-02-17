package sgc.subprocesso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.repo.ComumRepo;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.service.CopiaMapaService;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.crud.SubprocessoCrudService;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsável por operações relacionadas a atividades de subprocessos.
 * 
 * <p>Extrai lógica de manipulação de atividades que estava em métodos privados de {@link SubprocessoFacade}.
 * Responsabilidades:
 * <ul>
 *   <li>Importar atividades entre subprocessos (via eventos)</li>
 *   <li>Listar atividades de um subprocesso para visualização</li>
 *   <li>Transformar atividades em DTOs para visualização</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
class SubprocessoAtividadeService {

    private final SubprocessoRepo subprocessoRepo;
    private final ComumRepo repo;
    private final SubprocessoCrudService crudService;
    private final MapaManutencaoService mapaManutencaoService;
    private final CopiaMapaService copiaMapaService;
    private final MovimentacaoRepo movimentacaoRepo;
    private final UsuarioFacade usuarioService;

    /**
     * Importa atividades de um subprocesso de origem para um subprocesso de destino.
     * 
     * <p>Regras:
     * <ul>
     *   <li>Destino deve estar em NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO ou REVISAO_CADASTRO_EM_ANDAMENTO</li>
     *   <li>Se destino está em NAO_INICIADO, atualiza situação para cadastro em andamento</li>
     *   <li>Registra movimentação da importação</li>
     *   <li>Publica evento {@link EventoImportacaoAtividades} para desacoplar do módulo mapa</li>
     * </ul>
     * 
     * @param codSubprocessoDestino código do subprocesso de destino
     * @param codSubprocessoOrigem código do subprocesso de origem
     * @throws ErroAtividadesEmSituacaoInvalida se destino está em situação inválida
     */
    @Transactional
    public void importarAtividades(Long codSubprocessoDestino, Long codSubprocessoOrigem) {
        final Subprocesso spDestino = repo.buscar(Subprocesso.class, codSubprocessoDestino);
        Subprocesso spOrigem = repo.buscar(Subprocesso.class, codSubprocessoOrigem);

        // Importar atividades diretamente (sem evento assíncrono)
        copiaMapaService.importarAtividadesDeOutroMapa(
                spOrigem.getMapa().getCodigo(),
                spDestino.getMapa().getCodigo());

        if (spDestino.getSituacao() == SituacaoSubprocesso.NAO_INICIADO) {
            var tipoProcesso = spDestino.getProcesso().getTipo();

            switch (tipoProcesso) {
                case MAPEAMENTO -> spDestino.setSituacao(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO);
                case REVISAO -> spDestino.setSituacao(SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO);
                default ->
                    log.debug("Tipo de processo {} não requer atualização automática de situação no import.",
                            tipoProcesso);
            }
            subprocessoRepo.save(spDestino);
        }

        final Unidade unidadeOrigem = spOrigem.getUnidade();
        String descMovimentacao = String.format("Importação de atividades do subprocesso #%d (Unidade: %s)",
                spOrigem.getCodigo(),
                unidadeOrigem.getSigla());

        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        movimentacaoRepo.save(Movimentacao.builder()
                .subprocesso(spDestino)
                .unidadeOrigem(unidadeOrigem)
                .unidadeDestino(spDestino.getUnidade())
                .descricao(descMovimentacao)
                .usuario(usuario)
                .build());

        log.info("Evento de importação de atividades publicado: subprocesso {} -> {}", 
                codSubprocessoOrigem, codSubprocessoDestino);
    }

    /**
     * Lista todas as atividades de um subprocesso para visualização.
     * 
     * @param codSubprocesso código do subprocesso
     * @return lista de atividades com seus conhecimentos
     */
    @Transactional(readOnly = true)
    public List<AtividadeDto> listarAtividadesSubprocesso(Long codSubprocesso) {
        Subprocesso subprocesso = crudService.buscarSubprocessoComMapa(codSubprocesso);
        List<Atividade> todasAtividades = mapaManutencaoService
                .buscarAtividadesPorMapaCodigoComConhecimentos(subprocesso.getMapa().getCodigo());
        return todasAtividades.stream().map(this::mapAtividadeToDto).toList();
    }

    /**
     * Transforma uma atividade em DTO para visualização.
     * 
     * @param atividade atividade a transformar
     * @return DTO com dados da atividade e seus conhecimentos
     */
    private AtividadeDto mapAtividadeToDto(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(new ArrayList<>(atividade.getConhecimentos()))
                .build();
    }
}
