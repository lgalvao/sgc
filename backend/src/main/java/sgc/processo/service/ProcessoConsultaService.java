package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.organizacao.UsuarioService;
import sgc.organizacao.dto.PerfilDto;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.SubprocessoFacade;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Serviço responsável por consultas e queries relacionadas a Processos.
 * 
 * <p>Centraliza operações de leitura e consultas complexas, incluindo
 * listagens filtradas, verificações de elegibilidade e queries específicas.</p>
 */
@Service
@RequiredArgsConstructor
public class ProcessoConsultaService {

    private final ProcessoRepo processoRepo;
    private final SubprocessoFacade subprocessoFacade;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public Set<Long> buscarIdsUnidadesEmProcessosAtivos(Long codProcessoIgnorar) {
        return new HashSet<>(
                processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                        Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                        codProcessoIgnorar));
    }

    /**
     * Lista unidades bloqueadas (participantes de processos ativos) por tipo de processo.
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

        List<Subprocesso> subprocessos = subprocessoFacade.listarEntidadesPorProcesso(codProcesso);
        if (isAdmin) {
            return subprocessos.stream()
                    .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO)
                    .map(this::toSubprocessoElegivelDto)
                    .toList();
        }

        List<PerfilDto> perfis = usuarioService.buscarPerfisUsuario(username);
        Long codUnidadeUsuario = perfis.stream().findFirst().map(PerfilDto::getUnidadeCodigo).orElse(null);

        if (codUnidadeUsuario == null) {
            return List.of();
        }

        return subprocessos.stream()
                .filter(sp -> sp.getUnidade() != null
                        && sp.getUnidade().getCodigo().equals(codUnidadeUsuario))
                .filter(sp -> sp.getSituacao() == SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO
                        || sp.getSituacao() == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA)
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
