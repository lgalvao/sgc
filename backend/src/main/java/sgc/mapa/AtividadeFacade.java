package sgc.mapa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.mapa.dto.*;
import sgc.mapa.dto.AtividadeDto;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;
import sgc.subprocesso.service.crud.SubprocessoValidacaoService;
import sgc.comum.erros.ErroAcessoNegado;

import java.util.List;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Transactional
@Slf4j
public class AtividadeFacade {
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoFacade subprocessoFacade;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;
    private final SubprocessoValidacaoService validacaoService;

    public AtividadeFacade(
            MapaManutencaoService mapaManutencaoService,
            @Lazy SubprocessoFacade subprocessoFacade,
            SgcPermissionEvaluator permissionEvaluator,
            UsuarioFacade usuarioService,
            MapaFacade mapaFacade,
            SubprocessoValidacaoService validacaoService) {

        this.mapaManutencaoService = mapaManutencaoService;
        this.subprocessoFacade = subprocessoFacade;
        this.permissionEvaluator = permissionEvaluator;
        this.usuarioService = usuarioService;
        this.mapaFacade = mapaFacade;
        this.validacaoService = validacaoService;
    }

    @Transactional(readOnly = true)
    public Atividade obterAtividadePorId(Long codAtividade) {
        return mapaManutencaoService.obterAtividadePorCodigo(codAtividade);
    }

    @Transactional(readOnly = true)
    public List<Conhecimento> listarConhecimentosPorAtividade(Long codAtividade) {
        return mapaManutencaoService.listarConhecimentosPorAtividade(codAtividade);
    }

    public AtividadeOperacaoResponse criarAtividade(CriarAtividadeRequest request) {
        Long mapaCodigo = request.mapaCodigo();
        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(mapaCodigo, usuario);

        Atividade salvo = mapaManutencaoService.criarAtividade(request);
        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.getCodigo(), true);
    }

    public AtividadeOperacaoResponse atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigo);
        Usuario usuario = usuarioService.usuarioAutenticado();

        verificarPermissaoEdicao(atividade.getMapa().getCodigo(), usuario);
        mapaManutencaoService.atualizarAtividade(codigo, request);

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    public AtividadeOperacaoResponse excluirAtividade(Long codigo) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigo);
        Long codMapa = atividade.getMapa().getCodigo();

        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(codMapa, usuario);
        mapaManutencaoService.excluirAtividade(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);
        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(atividade.getMapa().getCodigo(), usuario);

        Conhecimento salvo = mapaManutencaoService.criarConhecimento(codAtividade, request);
        AtividadeOperacaoResponse response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.getCodigo(), response);
    }

    public AtividadeOperacaoResponse atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);
        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(atividade.getMapa().getCodigo(), usuario);

        mapaManutencaoService.atualizarConhecimento(codAtividade, codConhecimento, request);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    public AtividadeOperacaoResponse excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);

        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(atividade.getMapa().getCodigo(), usuario);

        mapaManutencaoService.excluirConhecimento(codAtividade, codConhecimento);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    private void verificarPermissaoEdicao(Long mapaCodigo, Usuario usuario) {
        Subprocesso sp = subprocessoFacade.obterEntidadePorCodigoMapa(mapaCodigo);

        if (!permissionEvaluator.checkPermission(usuario, sp, "EDITAR_CADASTRO")) {
             throw new ErroAcessoNegado("Usuário não tem permissão para editar atividades neste subprocesso.");
        }

        validacaoService.validarSituacaoPermitida(sp,
                NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO,
                MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES);
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
        Usuario usuario = usuarioService.usuarioAutenticado();
        PermissoesSubprocessoDto permissoes = subprocessoFacade.obterPermissoesUI(codSubprocesso, usuario);

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
                .permissoes(permissoes)
                .build();
    }
}