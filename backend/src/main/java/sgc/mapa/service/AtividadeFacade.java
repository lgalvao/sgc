package sgc.mapa.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import sgc.mapa.dto.AtividadeResponse;
import sgc.mapa.dto.ResultadoOperacaoConhecimento;
import sgc.mapa.evento.EventoAtividadeAtualizada;
import sgc.mapa.evento.EventoAtividadeCriada;
import sgc.mapa.evento.EventoAtividadeExcluida;
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
import sgc.subprocesso.dto.AtividadeVisualizacaoDto;
import sgc.subprocesso.dto.SubprocessoSituacaoDto;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

/**
 * Facade para orquestrar operações de atividades e conhecimentos,
 * lidando com a interação entre AtividadeService, ConhecimentoService e SubprocessoFacade.
 * Remove a lógica de negócio do AtividadeController.
 *
 * <p>Implementa o padrão Facade para simplificar a interface de uso e centralizar a coordenação de serviços.
 * 
 * <p><b>IMPORTANTE:</b> Este Facade é o ponto de entrada único para operações de atividades.
 * Controllers devem usar APENAS este Facade, nunca acessar AtividadeService ou ConhecimentoService diretamente.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AtividadeFacade {
    private final AtividadeService atividadeService;
    private final ConhecimentoService conhecimentoService;
    private final SubprocessoFacade subprocessoFacade;
    private final AccessControlService accessControlService;
    private final UsuarioFacade usuarioService;
    private final MapaFacade mapaFacade;
    private final ApplicationEventPublisher eventPublisher;

    // ===== Consultas =====

    /**
     * Obtém uma atividade por código.
     *
     * @param codAtividade O código da atividade
     * @return Response da atividade
     */
    @Transactional(readOnly = true)
    public sgc.mapa.dto.AtividadeResponse obterAtividadePorId(Long codAtividade) {
        return atividadeService.obterResponse(codAtividade);
    }

    /**
     * Lista todos os conhecimentos associados a uma atividade.
     *
     * @param codAtividade O código da atividade
     * @return Lista de conhecimentos
     */
    @Transactional(readOnly = true)
    public List<sgc.mapa.dto.ConhecimentoResponse> listarConhecimentosPorAtividade(Long codAtividade) {
        return conhecimentoService.listarPorAtividade(codAtividade);
    }

    // ===== Operações de Atividade =====

    /**
     * Cria uma nova atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse criarAtividade(sgc.mapa.dto.CriarAtividadeRequest request) {
        Long mapaCodigo = request.mapaCodigo();
        
        // Busca usuário autenticado através do contexto Spring Security
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        Mapa mapa = mapaFacade.obterPorCodigo(mapaCodigo);
        
        // Cria atividade temporária para verificação de acesso
        Atividade atividadeTemp = new Atividade();
        atividadeTemp.setMapa(mapa);
        
        // Verifica permissão usando AccessControlService
        accessControlService.verificarPermissao(usuario, CRIAR_ATIVIDADE, atividadeTemp);
        
        AtividadeResponse salvo = atividadeService.criar(request);

        // Publica evento de criação
        Atividade atividadeCriada = atividadeService.obterPorCodigo(salvo.codigo());
        Subprocesso subprocesso = mapa.getSubprocesso();
        
        EventoAtividadeCriada evento = EventoAtividadeCriada.builder()
                .atividade(atividadeCriada)
                .codMapa(mapaCodigo)
                .codSubprocesso(subprocesso.getCodigo())
                .usuario(usuario)
                .dataHoraCriacao(LocalDateTime.now())
                .totalAtividadesNoMapa(atividadeService.contarPorMapa(mapaCodigo))
                .build();
        eventPublisher.publishEvent(evento);

        return criarRespostaOperacaoPorMapaCodigo(mapaCodigo, salvo.codigo(), true);
    }

    /**
     * Atualiza uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse atualizarAtividade(Long codigo, sgc.mapa.dto.AtualizarAtividadeRequest request) {
        Atividade atividade = atividadeService.obterPorCodigo(codigo);
        
        // Busca usuário autenticado através do contexto Spring Security
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, EDITAR_ATIVIDADE, atividade);

        // Captura estado anterior para detectar mudanças
        
        Set<String> camposAlterados = new HashSet<>();

        if (!Objects.equals(atividade.getDescricao(), request.descricao())) {
            camposAlterados.add("descricao");
        }
        // Nota: Competências são afetadas apenas via conhecimentos (endpoints separados)
        // Este método atualiza somente a descrição da atividade
        
        atividadeService.atualizar(codigo, request);

        // Busca estado atualizado
        Atividade atividadeAtualizada = atividadeService.obterPorCodigo(codigo);
        Long codMapa = atividadeAtualizada.getMapa().getCodigo();
        Subprocesso subprocesso = atividadeAtualizada.getMapa().getSubprocesso();

        // Publica evento de atualização se houve mudanças
        if (!camposAlterados.isEmpty()) {
            EventoAtividadeAtualizada evento = EventoAtividadeAtualizada.builder()
                    .atividade(atividadeAtualizada)
                    .codMapa(codMapa)
                    .codSubprocesso(subprocesso.getCodigo())
                    .usuario(usuario)
                    .camposAlterados(camposAlterados)
                    .dataHoraAtualizacao(LocalDateTime.now())
                    // Nota: false pois este método atualiza apenas descrição, não competências
                    .afetouCompetencias(false)
                    .build();
            eventPublisher.publishEvent(evento);
        }

        return criarRespostaOperacaoPorAtividade(codigo);
    }

    /**
     * Exclui uma atividade e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse excluirAtividade(Long codigo) {
        Atividade atividade = atividadeService.obterPorCodigo(codigo);
        Long codMapa = atividade.getMapa().getCodigo();
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, EXCLUIR_ATIVIDADE, atividade);

        // Captura dados para o evento ANTES da exclusão
        String descricao = atividade.getDescricao();
        Subprocesso subprocesso = atividade.getMapa().getSubprocesso();
        int quantidadeConhecimentos = atividade.getConhecimentos().size();
        int totalAntes = atividadeService.contarPorMapa(codMapa);

        // Publica evento ANTES da exclusão
        EventoAtividadeExcluida evento = EventoAtividadeExcluida.builder()
                .codAtividade(codigo)
                .descricao(descricao)
                .codMapa(codMapa)
                .codSubprocesso(subprocesso.getCodigo())
                .usuario(usuario)
                .quantidadeConhecimentos(quantidadeConhecimentos)
                .dataHoraExclusao(LocalDateTime.now())
                .totalAtividadesRestantes(totalAntes - 1)
                .build();
        eventPublisher.publishEvent(evento);
        
        atividadeService.excluir(codigo);

        return criarRespostaOperacaoPorMapaCodigo(codMapa, codigo, false);
    }

    /**
     * Cria um conhecimento e retorna a resposta formatada junto com o ID criado.
     */
    public ResultadoOperacaoConhecimento criarConhecimento(Long codAtividade, sgc.mapa.dto.CriarConhecimentoRequest request) {
        Atividade atividade = atividadeService.obterPorCodigo(codAtividade);
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão usando a ação ASSOCIAR_CONHECIMENTOS
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);
        
        var salvo = conhecimentoService.criar(codAtividade, request);
        var response = criarRespostaOperacaoPorAtividade(codAtividade);

        return new ResultadoOperacaoConhecimento(salvo.codigo(), response);
    }

    /**
     * Atualiza um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse atualizarConhecimento(Long codAtividade, Long codConhecimento, sgc.mapa.dto.AtualizarConhecimentoRequest request) {
        Atividade atividade = atividadeService.obterPorCodigo(codAtividade);
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);
        
        conhecimentoService.atualizar(codAtividade, codConhecimento, request);
        return criarRespostaOperacaoPorAtividade(codAtividade);
    }

    /**
     * Exclui um conhecimento e retorna a resposta formatada.
     */
    public AtividadeOperacaoResponse excluirConhecimento(Long codAtividade, Long codConhecimento) {
        Atividade atividade = atividadeService.obterPorCodigo(codAtividade);
        
        // Busca usuário autenticado
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        
        // Verifica permissão
        accessControlService.verificarPermissao(usuario, ASSOCIAR_CONHECIMENTOS, atividade);
        
        conhecimentoService.excluir(codAtividade, codConhecimento);
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
        Atividade atividade = atividadeService.obterPorCodigo(codigoAtividade);
        return obterCodigoSubprocessoPorMapa(atividade.getMapa().getCodigo());
    }

    private AtividadeOperacaoResponse criarRespostaOperacao(Long codSubprocesso, Long codigoAtividade, boolean incluirAtividade) {
        SubprocessoSituacaoDto situacaoDto = subprocessoFacade.obterSituacao(codSubprocesso);
        
        // Buscar todas as atividades do subprocesso
        List<AtividadeVisualizacaoDto> todasAtividades = subprocessoFacade.listarAtividadesSubprocesso(codSubprocesso);
        
        AtividadeVisualizacaoDto atividadeVis = null;
        if (incluirAtividade) {
            atividadeVis = todasAtividades.stream()
                    .filter(a -> a.getCodigo().equals(codigoAtividade))
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
