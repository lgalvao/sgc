package sgc.mapa.service;

import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Mapa;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;
import static sgc.seguranca.acesso.Acao.ASSOCIAR_CONHECIMENTOS;
import static sgc.seguranca.acesso.Acao.CRIAR_ATIVIDADE;
import static sgc.seguranca.acesso.Acao.EDITAR_ATIVIDADE;
import static sgc.seguranca.acesso.Acao.EXCLUIR_ATIVIDADE;
import sgc.seguranca.acesso.AccessControlService;
import sgc.subprocesso.dto.AtividadeOperacaoResponse;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;
import sgc.mapa.dto.AtualizarAtividadeRequest;
import sgc.mapa.dto.AtualizarConhecimentoRequest;
import sgc.mapa.dto.ConhecimentoResponse;
import sgc.mapa.dto.CriarAtividadeRequest;
import sgc.mapa.dto.CriarConhecimentoRequest;

/**
 * Facade para orquestrar operações de atividades e conhecimentos,
 * lidando com a interação entre MapaManutencaoService e SubprocessoFacade.
 * Remove a lógica de negócio do AtividadeController.
 *
 * <p>Implementa o padrão Facade para simplificar a interface de uso e centralizar a coordenação de serviços.
 *
 * <p><b>IMPORTANTE:</b> Este Facade é o ponto de entrada único para operações de atividades.
 * Controllers devem usar APENAS este Facade, nunca acessar Services diretamente.
 *
 * <p><b>Nota sobre Injeção de Dependências:</b>
 * SubprocessoFacade é injetado com @Lazy para quebrar dependência circular:
 * AtividadeFacade → SubprocessoFacade → MapaFacade/MapaManutencaoService → AtividadeFacade
 */
@Service
@Transactional
public class AtividadeFacade {
    private static final String MAPA_NAO_PODE_SER_NULO = "Mapa não pode ser nulo";
    
    private final MapaManutencaoService mapaManutencaoService;
    private final SubprocessoFacade subprocessoFacade;
    private final AccessControlService accessControlService;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Constructor com @Lazy para quebrar dependência circular.
     */
    public AtividadeFacade(
            MapaManutencaoService mapaManutencaoService,
            @Lazy SubprocessoFacade subprocessoFacade,
            AccessControlService accessControlService,
            UsuarioFacade usuarioService,
            MapaFacade mapaFacade,
            ApplicationEventPublisher eventPublisher) {
        this.mapaManutencaoService = mapaManutencaoService;
        this.subprocessoFacade = subprocessoFacade;
        this.accessControlService = accessControlService;
        this.usuarioService = usuarioService;
        this.mapaFacade = mapaFacade;
        this.eventPublisher = eventPublisher;
    }

    // ===== Consultas =====

    /**
     * Obtém uma atividade por código.
     *
     * @param codAtividade O código da atividade
     * @return Response da atividade
     */
    @Transactional(readOnly = true)
    public AtividadeResponse obterAtividadePorId(Long codAtividade) {
        return mapaManutencaoService.obterAtividadeResponse(codAtividade);
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade.
     *
     * @param codAtividade O código da atividade
     * @return Lista de conhecimentos
     */
    @Transactional(readOnly = true)
    public List<ConhecimentoResponse> listarConhecimentosPorAtividade(Long codAtividade) {
        return mapaManutencaoService.listarConhecimentosPorAtividade(codAtividade);
    }

    // ===== Operações de Atividade =====

    /**
     * Cria uma nova atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse criarAtividade(CriarAtividadeRequest request) {
        Long mapaCodigo = request.mapaCodigo();

        // Busca usuário autenticado através do contexto Spring Security
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        Mapa mapa = mapaFacade.obterPorCodigo(mapaCodigo);

        // Cria atividade temporária para verificação de acesso
        Atividade atividadeTemp = Atividade.builder()
                .mapa(mapa)
                .build();

        // Verifica permissão usando AccessControlService
        accessControlService.verificarPermissao(usuario, CRIAR_ATIVIDADE, atividadeTemp);

        AtividadeResponse salvo = mapaManutencaoService.criarAtividade(request);

        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.codigo(), true);
    }

    /**
     * Atualiza uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse atualizarAtividade(Long codigo, AtualizarAtividadeRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigo);

        // Busca usuário autenticado através do contexto Spring Security
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        // Verifica permissão
        accessControlService.verificarPermissao(usuario, EDITAR_ATIVIDADE, atividade);

        mapaManutencaoService.atualizarAtividade(codigo, request);

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    /**
     * Exclui uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse excluirAtividade(Long codigo) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codigo);
        Mapa mapa = Objects.requireNonNull(atividade.getMapa(), MAPA_NAO_PODE_SER_NULO);
        Long codMapa = mapa.getCodigo();

        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        // Verifica permissão
        accessControlService.verificarPermissao(usuario, EXCLUIR_ATIVIDADE, atividade);

        mapaManutencaoService.excluirAtividade(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    /**
     * Cria um conhecimento e retorna a resposta formatada junto com o ID criado.
     */
    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, CriarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);

        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        // Verifica permissão usando a ação ASSOCIAR_CONHECIMENTOS
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);

        var salvo = mapaManutencaoService.criarConhecimento(codAtividade, request);
        var response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.codigo(), response);
    }

    /**
     * Atualiza um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse atualizarConhecimento(Long codAtividade, Long codConhecimento, AtualizarConhecimentoRequest request) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);

        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        // Verifica permissão
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);

        mapaManutencaoService.atualizarConhecimento(codAtividade, codConhecimento, request);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    /**
     * Exclui um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Atividade atividade = mapaManutencaoService.obterAtividadePorCodigo(codAtividade);

        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        // Verifica permissão
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
        Mapa mapa = Objects.requireNonNull(atividade.getMapa(), MAPA_NAO_PODE_SER_NULO);
        return obterCodigoSubprocessoPorMapa(mapa.getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto situacaoDto = subprocessoFacade.obterSituacao(codSubprocesso);

        // Buscar todas as atividades do subprocesso
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