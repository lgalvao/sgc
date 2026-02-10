package sgc.mapa.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.*;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.List;

import static sgc.seguranca.acesso.Acao.*;

@Service
@Transactional
public class AtividadeFacade {
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoFacade subprocessoFacade;
    private final AccessControlService accessControlService;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;

    public AtividadeFacade(
            MapaManutencaoService mapaManutencaoService,
            @Lazy SubprocessoFacade subprocessoFacade,
            AccessControlService accessControlService,
            UsuarioFacade usuarioService,
            MapaFacade mapaFacade) {

        this.mapaManutencaoService = mapaManutencaoService;
        this.subprocessoFacade = subprocessoFacade;
        this.accessControlService = accessControlService;
        this.usuarioService = usuarioService;
        this.mapaFacade = mapaFacade;
    }

    @Transactional(readOnly = true)
    public AtividadeResponse obterAtividadePorId(Long codAtividade) {
        return mapaManutencaoService.obterAtividadeResponse(codAtividade);
    }

    @Transactional(readOnly = true)
    public List<ConhecimentoResponse> listarConhecimentosPorAtividade(Long codAtividade) {
        return mapaManutencaoService.listarConhecimentosPorAtividade(codAtividade);
    }

    public AtividadeOperacaoResponse criarAtividade(CriarAtividadeRequest request) {
        Long mapaCodigo = request.mapaCodigo();
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        Mapa mapa = mapaFacade.obterPorCodigo(mapaCodigo);


        // TODO essa criação temporaria me parece um hack
        // Cria atividade temporária para verificação de acesso
        Atividade atividadeTemp = Atividade.builder().mapa(mapa).build();
        accessControlService.verificarPermissao(usuario, CRIAR_ATIVIDADE, atividadeTemp);

        AtividadeResponse salvo = mapaManutencaoService.criarAtividade(request);
        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.codigo(), true);
    }

    public AtividadeOperacaoResponse atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigo);
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        accessControlService.verificarPermissao(usuario, EDITAR_ATIVIDADE, atividade);
        mapaManutencaoService.atualizarAtividade(codigo, request);

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    public AtividadeOperacaoResponse excluirAtividade(Long codigo) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigo);
        Mapa mapa = atividade.getMapa();
        Long codMapa = mapa.getCodigo();

        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        accessControlService.verificarPermissao(usuario, EXCLUIR_ATIVIDADE, atividade);
        mapaManutencaoService.excluirAtividade(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);

        var salvo = mapaManutencaoService.criarConhecimento(codAtividade, request);
        var response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.codigo(), response);
    }

    public AtividadeOperacaoResponse atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);

        mapaManutencaoService.atualizarConhecimento(codAtividade, codConhecimento, request);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    public AtividadeOperacaoResponse excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);

        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);

        mapaManutencaoService.excluirConhecimento(codAtividade, codConhecimento);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorAtividade(Long codigoAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorAtividade(codigoAtividade);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, true);
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorMapaCodigo(Long mapaCodigo, Long codigoAtividade, boolean incluirAtividade) {
        Long codSubprocesso = obterCodigoSubprocessoPorMapa(mapaCodigo);
        return criarRespostaOperacao(codSubprocesso, codigoAtividade, incluirAtividade);
    }

    private Long obterCodigoSubprocessoPorMapa(Long codMapa) {
        Subprocesso subprocesso = subprocessoFacade.obterEntidadePorCodigoMapa(codMapa);
        return subprocesso.getCodigo();
    }

    private Long obterCodigoSubprocessoPorAtividade(Long codigoAtividade) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigoAtividade);
        Mapa mapa = atividade.getMapa();

        return obterCodigoSubprocessoPorMapa(mapa.getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto situacaoDto = subprocessoFacade.obterSituacao(codSubprocesso);
        List<AtividadeDto> todasAtividades = subprocessoFacade.listarAtividadesSubprocesso(codSubprocesso);

        AtividadeDto atividadeVis = null;
        if (incluirAtividade) {
            atividadeVis = todasAtividades.stream()
                    .filter(a -> a.codigo().equals(codigoAtividade))
                    .findFirst()
                    .orElse(null);
        }

        return AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(situacaoDto)
                .atividadesAtualizadas(todasAtividades)
                .build();
    }
}