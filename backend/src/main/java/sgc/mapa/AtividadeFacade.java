package sgc.mapa;

import lombok.extern.slf4j.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.mapa.dto.*;
import sgc.mapa.model.*;
import sgc.mapa.service.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.seguranca.*;
import sgc.subprocesso.dto.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Transactional
@Slf4j
public class AtividadeFacade {
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoService subprocessoService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final UsuarioFacade usuarioService;
    public AtividadeFacade(
            MapaManutencaoService mapaManutencaoService,
            @Lazy SubprocessoService subprocessoService,
            SgcPermissionEvaluator permissionEvaluator,
            UsuarioFacade usuarioService) {

        this.mapaManutencaoService = mapaManutencaoService;
        this.subprocessoService = subprocessoService;
        this.permissionEvaluator = permissionEvaluator;
        this.usuarioService = usuarioService;
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
        Subprocesso sp = subprocessoService.obterEntidadePorCodigoMapa(mapaCodigo);

        if (!permissionEvaluator.checkPermission(usuario, sp, "EDITAR_CADASTRO")) {
             throw new ErroAcessoNegado("Usuário não tem permissão para editar atividades neste subprocesso.");
        }

        if (!Set.of(NAO_INICIADO, MAPEAMENTO_CADASTRO_EM_ANDAMENTO, REVISAO_CADASTRO_EM_ANDAMENTO,
                MAPEAMENTO_MAPA_CRIADO, MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_AJUSTADO, REVISAO_MAPA_COM_SUGESTOES).contains(sp.getSituacao())) {
            throw new ErroValidacao(
                "Situação do subprocesso não permite esta operação. Situação atual: %s"
                    .formatted(sp.getSituacao()));
        }
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
        Subprocesso subprocesso = subprocessoService.obterEntidadePorCodigoMapa(codMapa);
        return subprocesso.getCodigo();
    }

    private Long obterCodigoSubprocessoPorAtividade(Long codigoAtividade) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigoAtividade);
        Mapa mapa = atividade.getMapa();

        return obterCodigoSubprocessoPorMapa(mapa.getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto situacaoDto = subprocessoService.obterStatus(codSubprocesso);
        List<AtividadeDto> todasAtividades = subprocessoService.listarAtividadesSubprocesso(codSubprocesso);
        Usuario usuario = usuarioService.usuarioAutenticado();
        Subprocesso sp = subprocessoService.buscarSubprocesso(codSubprocesso);
        PermissoesSubprocessoDto permissoes = subprocessoService.obterPermissoesUI(sp, usuario);

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