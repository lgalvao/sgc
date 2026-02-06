package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.dto.PerfilDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.query.ProcessoSubprocessoQueryService;

import java.util.*;

/**
 * Serviço responsável por consultas e queries relacionadas a Processos.
 *
 * <p>
 * Centraliza operações de leitura e consultas complexas, incluindo
 * listagens filtradas, verificações de elegibilidade e queries específicas.
 * </p>
 *
 * <p><b>Refatoração v3.0:</b> Removido uso de @Lazy e dependência circular.
 * Agora utiliza {@link ProcessoSubprocessoQueryService} para queries de leitura,
 * eliminando acoplamento bidirecional com SubprocessoFacade.</p>
 *
 * @since 3.0.0 - Removido @Lazy, introduzido Query Service Pattern
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessoConsultaService {
    private final ProcessoRepo processoRepo;
    private final ProcessoSubprocessoQueryService queryService;
    private final UsuarioFacade usuarioService;
    private static final String ENTIDADE_PROCESSO = "Processo";


    public Processo buscarPorId(Long id) {
        return processoRepo.findById(id)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada(ENTIDADE_PROCESSO, id));
    }

    public Optional<Processo> buscarPorIdOpcional(Long id) {
        return processoRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Processo> listarFinalizados() {
        return processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
    }

    @Transactional(readOnly = true)
    public List<Processo> listarAtivos() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
    }

    public Page<Processo> listarTodos(Pageable pageable) {
        return processoRepo.findAll(pageable);
    }

    public Page<Processo> listarPorParticipantesIgnorandoCriado(
            List<Long> unidadeIds, Pageable pageable) {
        return processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                unidadeIds, SituacaoProcesso.CRIADO, pageable);
    }

    @Transactional(readOnly = true)
    public Set<Long> buscarIdsUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        return new HashSet<>(
                processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                        Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                        codProcessoIgnorar));
    }

    /**
     * Lista unidades bloqueadas (participantes de processos ativos) por tipo de
     * processo.
     *
     * @param tipo tipo de processo
     * @return lista de códigos de unidades bloqueadas
     */
    @Transactional(readOnly = true)
    public List<Long> listarUnidadesBloqueadasPorTipo(String tipo) {
        TipoProcesso tipoProcesso = TipoProcesso.valueOf(tipo);
        return processoRepo.findUnidadeCodigosBySituacaoAndTipo(SituacaoProcesso.EM_ANDAMENTO, tipoProcesso);
    }

    /**
     * Lista subprocessos elegíveis para o usuário atual no contexto do processo.
     *
     * @param codProcesso código do processo
     * @return lista de subprocessos elegíveis baseado no perfil do usuário
     */
    @Transactional(readOnly = true)
    public List<SubprocessoElegivelDto> listarSubprocessosElegiveis(Long codProcesso) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return List.of();
        }
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (isAdmin) {
            return queryService.listarPorProcessoESituacoes(codProcesso,
                            List.of(
                                    SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                                    SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                                    SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                                    SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO,
                                    SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                                    SituacaoSubprocesso.REVISAO_MAPA_VALIDADO
                            ))
                    .stream()
                    .map(this::toSubprocessoElegivelDto)
                    .toList();
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream().findFirst().map(PerfilDto::unidadeCodigo).orElse(null);

        if (codUnidadeUsuario == null) {
            return List.of();
        }

        return queryService.listarPorProcessoUnidadeESituacoes(codProcesso, codUnidadeUsuario,
                List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                        SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA))
                .stream()
                .map(this::toSubprocessoElegivelDto)
                .toList();
    }

    /**
     * Converte Subprocesso para DTO de elegibilidade.
     */
    private SubprocessoElegivelDto toSubprocessoElegivelDto(Subprocesso sp) {
        return SubprocessoElegivelDto.builder()
                .codSubprocesso(sp.getCodigo())
                .unidadeNome(sp.getUnidade().getNome())
                .unidadeSigla(sp.getUnidade().getSigla())
                .situacao(sp.getSituacao())
                .build();
    }
}

