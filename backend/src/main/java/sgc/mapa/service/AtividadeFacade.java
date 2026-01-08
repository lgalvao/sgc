package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.subprocesso.dto.AtividadeOperacaoResp;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

/**
 * Facade para orquestrar operações de atividades e conhecimentos,
 * lidando com a interação entre AtividadeService, ConhecimentoService e SubprocessoService.
 * Remove a lógica de negócio do AtividadeController.
 *
 * <p>Implementa o padrão Facade para simplificar a interface de uso e centralizar a coordenação de serviços.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AtividadeFacade {
    private final AtividadeService atividadeService;
    private final ConhecimentoService conhecimentoService;
    private final SubprocessoService subprocessoService;

    /**
     * Cria uma nova atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp criarAtividade(AtividadeDto atividadeDto, String tituloUsuario) {
        if (tituloUsuario.isBlank()) throw new ErroAccessoNegado("Usuário não autenticado.");

        Long mapaCodigo = atividadeDto.getMapaCodigo();

        subprocessoService.validarPermissaoEdicaoMapa(mapaCodigo, tituloUsuario);
        AtividadeDto salvo = atividadeService.criar(atividadeDto);

        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.getCodigo(), true);
    }

    /**
     * Atualiza uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp atualizarAtividade(Long codigo, AtividadeDto atividadeDto) {
        atividadeService.atualizar(codigo, atividadeDto);

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    /**
     * Exclui uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp excluirAtividade(Long codigo) {
        Atividade atividade = atividadeService.obterPorCodigo(codigo);
        Long codMapa = atividade.getMapa().getCodigo();
        atividadeService.excluir(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    /**
     * Cria um conhecimento e retorna a resposta formatada junto com o ID criado.
     */
    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, ConhecimentoDto conhecimentoDto) {
        var salvo = conhecimentoService.criar(codAtividade, conhecimentoDto);
        var response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.getCodigo(), response);
    }

    /**
     * Atualiza um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp atualizarConhecimento(Long codAtividade, Long codConhecimento, ConhecimentoDto conhecimentoDto) {
        conhecimentoService.atualizar(codAtividade, codConhecimento, conhecimentoDto);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    /**
     * Exclui um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp excluirConhecimento(Long codAtividade, Long codConhecimento) {
        conhecimentoService.excluir(codAtividade, codConhecimento);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    private AtividadeOperacaoResp criarRespostaOperacaoPorAtividade(Long codigoAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorAtividade(codigoAtividade);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, true);
    }

    private AtividadeOperacaoResp criarRespostaOperacaoPorMapaCodigo(Long mapaCodigo, Long codigoAtividade, boolean incluirAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(mapaCodigo);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, incluirAtividade);
    }

    private Long obterCodigoSubprocessoPorMapa(Long codMapa) {
        Subprocesso subprocesso = subprocessoService.obterEntidadePorCodigoMapa(codMapa);
        return subprocesso.getCodigo();
    }

    private Long obterCodigoSubprocessoPorAtividade(Long codigoAtividade) {
        Atividade atividade = atividadeService.obterPorCodigo(codigoAtividade);
        return obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
    }

    private AtividadeOperacaoResp criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto situacaoDto = subprocessoService.obterSituacao(codSubprocesso);
        AtividadeVisualizacaoDto atividadeVis = null;
        if (incluirAtividade) {
            atividadeVis = subprocessoService.listarAtividadesSubprocesso(codSubprocesso)
                    .stream()
                    .filter(a -> a.getCodigo().equals(codigoAtividade))
                    .findFirst()
                    .orElse(null);
        }

        return AtividadeOperacaoResp.builder()
                .atividade(atividadeVis)
                .subprocesso(situacaoDto)
                .build();
    }
}
