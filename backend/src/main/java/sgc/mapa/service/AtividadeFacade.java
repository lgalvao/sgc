package sgc.mapa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroAccessoNegado;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.dto.ConhecimentoDto;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtividadeOperacaoResp;
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoService;

import static sgc.seguranca.acesso.Acao.*;

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
    private final AccessControlService accessControlService;
    private final UsuarioService usuarioService;
    private final MapaService mapaService;

    /**
     * Cria uma nova atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp criarAtividade(AtividadeDto atividadeDto, String tituloUsuario) {
        if (tituloUsuario.isBlank()) throw new ErroAccessoNegado("Usuário não autenticado.");

        Long mapaCodigo = atividadeDto.getMapaCodigo();
        
        // Busca o usuário e o mapa para verificação de acesso
        Usuario usuario = usuarioService.buscarPorLogin(tituloUsuario);
        if (usuario == null) {
            throw new ErroAccessoNegado("Usuário não encontrado: " + tituloUsuario);
        }
        
        Mapa mapa = mapaService.obterPorCodigo(mapaCodigo);
        
        // Cria atividade temporária para verificação de acesso
        Atividade atividadeTemp = new Atividade();
        atividadeTemp.setMapa(mapa);
        
        // Verifica permissão usando AccessControlService
        accessControlService.verificarPermissao(usuario, CRIAR_ATIVIDADE, atividadeTemp);
        
        AtividadeDto salvo = atividadeService.criar(atividadeDto);

        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.getCodigo(), true);
    }

    /**
     * Atualiza uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp atualizarAtividade(Long codigo, AtividadeDto atividadeDto) {
        Atividade atividade = atividadeService.obterPorCodigo(codigo);
        
        // Busca usuário autenticado através do contexto Spring Security
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, EDITAR_ATIVIDADE, atividade);
        
        atividadeService.atualizar(codigo, atividadeDto);

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    /**
     * Exclui uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp excluirAtividade(Long codigo) {
        Atividade atividade = atividadeService.obterPorCodigo(codigo);
        Long codMapa = atividade.getMapa().getCodigo();
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, EXCLUIR_ATIVIDADE, atividade);
        
        atividadeService.excluir(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    /**
     * Cria um conhecimento e retorna a resposta formatada junto com o ID criado.
     */
    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, ConhecimentoDto conhecimentoDto) {
        Atividade atividade = atividadeService.obterPorCodigo(codAtividade);
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão usando a ação ASSOCIAR_CONHECIMENTOS
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);
        
        var salvo = conhecimentoService.criar(codAtividade, conhecimentoDto);
        var response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.getCodigo(), response);
    }

    /**
     * Atualiza um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp atualizarConhecimento(Long codAtividade, Long codConhecimento, ConhecimentoDto conhecimentoDto) {
        Atividade atividade = atividadeService.obterPorCodigo(codAtividade);
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);
        
        conhecimentoService.atualizar(codAtividade, codConhecimento, conhecimentoDto);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    /**
     * Exclui um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResp excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Atividade atividade = atividadeService.obterPorCodigo(codAtividade);
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);
        
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
