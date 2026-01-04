package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

// Refactored in Phase 2

/**
 * Facade para orquestrar operações de atividades e conhecimentos,
 * lidando com a interação entre AtividadeService e SubprocessoService.
 * Remove a lógica de negócio do AtividadeController.
 *
 * <p>Implementa o padrão Facade para simplificar a interface de uso e centralizar a coordenação de serviços.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AtividadeFacade {

    private final AtividadeService atividadeService;
    private final SubprocessoService subprocessoService;

    /**
     * Cria uma nova atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse criarAtividade(AtividadeDto atividadeDto, String tituloUsuario) {
        if (tituloUsuario == null || tituloUsuario.isBlank()) {
            throw new ErroAccessoNegado("Usuário não autenticado.");
        }

        // 1. Validar permissão
        subprocessoService.validarPermissaoEdicaoMapa(atividadeDto.getMapaCodigo(), tituloUsuario);

        // 2. Criar a atividade
        var salvo = atividadeService.criar(atividadeDto, tituloUsuario);

        // 3. Montar resposta
        return criarRespostaOperacaoPorMapaCodigo(atividadeDto.getMapaCodigo(), salvo.getCodigo(), true);
    }

    /**
     * Atualiza uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse atualizarAtividade(Long codigo, AtividadeDto atividadeDto) {
        atividadeService.atualizar(codigo, atividadeDto);
        return criarRespostaOperacaoPorAtividade(codigo);
    }

    /**
     * Exclui uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse excluirAtividade(Long codigo) {
        Atividade atividade = atividadeService.obterEntidadePorCodigo(codigo);
        Long codMapa = atividade.getMapa().getCodigo();

        atividadeService.excluir(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    /**
     * Cria um conhecimento e retorna a resposta formatada junto com o ID criado.
     */
    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, ConhecimentoDto conhecimentoDto) {
        var salvo = atividadeService.criarConhecimento(codAtividade, conhecimentoDto);
        var response = criarRespostaOperacaoPorAtividade(codAtividade);
        return new ResultadoOperacaoConhecimento(salvo.getCodigo(), response);
    }

    /**
     * Atualiza um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse atualizarConhecimento(Long codAtividade, Long codConhecimento, ConhecimentoDto conhecimentoDto) {
        atividadeService.atualizarConhecimento(codAtividade, codConhecimento, conhecimentoDto);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    /**
     * Exclui um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse excluirConhecimento(Long codAtividade, Long codConhecimento) {
        atividadeService.excluirConhecimento(codAtividade, codConhecimento);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    // ========================================================================================
    // Métodos Helper (Movidos do Controller)
    // ========================================================================================

    private AtividadeOperacaoResponse criarRespostaOperacaoPorAtividade(Long codigoAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorAtividade(codigoAtividade);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, true);
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorMapaCodigo(Long mapaCodigo, Long codigoAtividade, boolean incluirAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(mapaCodigo);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, incluirAtividade);
    }

    private Long obterCodigoSubprocessoPorMapa(Long codMapa) {
        Subprocesso subprocesso = subprocessoService.obterEntidadePorCodigoMapa(codMapa);
        return subprocesso.getCodigo();
    }

    private Long obterCodigoSubprocessoPorAtividade(Long codigoAtividade) {
        Atividade atividade = atividadeService.obterEntidadePorCodigo(codigoAtividade);
        return obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto status = subprocessoService.obterStatus(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = null;
        if (incluirAtividade) {
            atividadeVis = subprocessoService.listarAtividadesSubprocesso(codSubprocesso)
                    .stream()
                    .filter(a -> a.getCodigo().equals(codigoAtividade))
                    .findFirst()
                    .orElse(null);
        }
        return AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(status)
                .build();
    }
}
