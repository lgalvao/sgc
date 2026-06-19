package sgc.mapa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.Mensagens;
import sgc.comum.erros.ErroAcessoNegado;
import sgc.comum.erros.ErroValidacao;
import sgc.mapa.dto.*;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.Mapa;
import sgc.mapa.service.MapaManutencaoService;
import sgc.organizacao.UsuarioAplicacaoService;
import sgc.organizacao.model.Usuario;
import sgc.seguranca.SgcPermissionEvaluator;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.subprocesso.dto.PermissoesSubprocessoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoConsultaService;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static sgc.seguranca.AcaoPermissao.EDITAR_CADASTRO;
import static sgc.subprocesso.model.SituacaoSubprocesso.*;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AtividadeService {
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoConsultaService consultaService;
    private final SgcPermissionEvaluator permissionEvaluator;
    private final UsuarioAplicacaoService usuarioService;
    private final MapaDtoMapper mapaDtoMapper;

    @Transactional(readOnly = true)
    public AtividadeDto obterAtividadePorCodigo(Long codAtividade) {
        return mapaDtoMapper.paraAtividadeDto(mapaManutencaoService.atividadeCodigo(codAtividade));
    }

    @Transactional(readOnly = true)
    public List<ConhecimentoResumoDto> listarConhecimentosPorAtividade(Long codAtividade) {
        return mapaManutencaoService.conhecimentosCodigoAtividade(codAtividade).stream()
                .map(mapaDtoMapper::paraConhecimentoResumoDto)
                .toList();
    }

    public AtividadeOperacaoResponse criarAtividade(CriarAtividadeRequest request) {
        Long mapaCodigo = request.mapaCodigo();
        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(mapaCodigo, usuario);

        Atividade salvo = mapaManutencaoService.criarAtividade(request);
        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.getCodigo(), true);
    }

    public AtividadeOperacaoResponse atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade atividade = mapaManutencaoService.atividadeCodigo(codigo);
        Usuario usuario = usuarioService.usuarioAutenticado();

        Mapa mapa = atividade.getMapa();
        Long mapaCodigo = mapa.getCodigo();
        verificarPermissaoEdicao(mapaCodigo, usuario);
        mapaManutencaoService.atualizarAtividade(codigo, request);

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    public AtividadeOperacaoResponse excluirAtividade(Long codigo) {
        Atividade atividade = mapaManutencaoService.atividadeCodigo(codigo);
        Mapa mapa = atividade.getMapa();
        Long codMapa = mapa.getCodigo();

        Usuario usuario = usuarioService.usuarioAutenticado();
        verificarPermissaoEdicao(codMapa, usuario);
        mapaManutencaoService.excluirAtividade(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.atividadeCodigo(codAtividade);
        Usuario usuario = usuarioService.usuarioAutenticado();

        Mapa mapa = atividade.getMapa();
        Long mapaCodigo = mapa.getCodigo();
        verificarPermissaoEdicao(mapaCodigo, usuario);

        Conhecimento salvo = mapaManutencaoService.criarConhecimento(codAtividade, request);
        AtividadeOperacaoResponse response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.getCodigo(), response);
    }

    public AtividadeOperacaoResponse atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.atividadeCodigo(codAtividade);
        Usuario usuario = usuarioService.usuarioAutenticado();

        Mapa mapa = atividade.getMapa();
        verificarPermissaoEdicao(mapa.getCodigo(), usuario);

        mapaManutencaoService.atualizarConhecimento(codAtividade, codConhecimento, request);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    public AtividadeOperacaoResponse excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Atividade atividade = mapaManutencaoService.atividadeCodigo(codAtividade);

        Usuario usuario = usuarioService.usuarioAutenticado();
        Mapa mapa = atividade.getMapa();
        verificarPermissaoEdicao(mapa.getCodigo(), usuario);

        mapaManutencaoService.excluirConhecimento(codAtividade, codConhecimento);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    private void verificarPermissaoEdicao(Long mapaCodigo, Usuario usuario) {
        Subprocesso sp = consultaService.obterEntidadePorCodigoMapa(mapaCodigo);

        if (!permissionEvaluator.verificarPermissao(usuario, sp, EDITAR_CADASTRO)) {
            throw new ErroAcessoNegado(Mensagens.SEM_PERMISSAO_EDITAR_ATIVIDADES);
        }

        SituacaoSubprocesso situacao = sp.getSituacao();
        if (!Set.of(NAO_INICIADO,
                MAPEAMENTO_CADASTRO_EM_ANDAMENTO,
                REVISAO_CADASTRO_EM_ANDAMENTO,
                MAPEAMENTO_MAPA_CRIADO,
                MAPEAMENTO_MAPA_COM_SUGESTOES,
                REVISAO_MAPA_AJUSTADO,
                REVISAO_MAPA_COM_SUGESTOES).contains(situacao)) {

            throw new ErroValidacao(Mensagens.SITUACAO_ATUAL.formatted(situacao));
        }
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorAtividade(Long codigoAtividade) {
        Subprocesso subprocesso = obterSubprocessoPorAtividade(codigoAtividade);
        return criarRespostaOperacao(subprocesso, codigoAtividade, true);
    }

    private AtividadeOperacaoResponse criarRespostaOperacaoPorMapaCodigo(Long mapaCodigo, Long codigoAtividade, boolean incluirAtividade) {
        Subprocesso subprocesso = obterSubprocessoPorMapa(mapaCodigo);
        return criarRespostaOperacao(subprocesso, codigoAtividade, incluirAtividade);
    }

    private Subprocesso obterSubprocessoPorMapa(Long codMapa) {
        return consultaService.obterEntidadePorCodigoMapa(codMapa);
    }

    private Subprocesso obterSubprocessoPorAtividade(Long codigoAtividade) {
        Atividade atividade = mapaManutencaoService.atividadeCodigo(codigoAtividade);
        Mapa mapa = atividade.getMapa();

        return obterSubprocessoPorMapa(mapa.getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Subprocesso subprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto situacaoDto = consultaService.obterStatus(subprocesso);
        List<AtividadeDto> todasAtividades = consultaService.listarAtividadesSubprocesso(subprocesso);
        PermissoesSubprocessoDto permissoes = consultaService.obterPermissoesUI(subprocesso);

        AtividadeDto atividadeVis = null;
        if (incluirAtividade) {
            atividadeVis = todasAtividades.stream()
                    .filter(a -> Objects.equals(a.codigo(), codigoAtividade))
                    .findFirst()
                    .orElse(null);
        }



        return AtividadeOperacaoResponse.builder()
                .atividade(atividadeVis)
                .subprocesso(situacaoDto)
                .atividadesAtualizadas(todasAtividades)
                .permissoes(permissoes)
                .message(null)
                .aviso(null)
                .build();
    }
}
