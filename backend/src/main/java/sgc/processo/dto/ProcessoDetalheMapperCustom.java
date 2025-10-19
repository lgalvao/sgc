package sgc.processo.dto;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.sgrh.Usuario;
import sgc.subprocesso.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementação customizada para mapeamento complexo de Processo para ProcessoDetalheDto.
 * Esta classe implementa a lógica de associação entre unidades de processo e subprocessos,
 * que não pode ser feita automaticamente pelo MapStruct.
 */
@Component
public class ProcessoDetalheMapperCustom {

    private final ProcessoDetalheMapperInterface processoDetalheMapperInterface;
    private final UnidadeRepo unidadeRepo;

    public ProcessoDetalheMapperCustom(ProcessoDetalheMapperInterface processoDetalheMapperInterface, UnidadeRepo unidadeRepo) {
        this.processoDetalheMapperInterface = processoDetalheMapperInterface;
        this.unidadeRepo = unidadeRepo;
    }

    /**
     * Converte um Processo com suas associações para ProcessoDetalheDto,
     * mapeando corretamente as unidades de processo e subprocessos.
     */
    public ProcessoDetalheDto toDetailDTO(Processo p,
                                         List<UnidadeProcesso> unidadesProcesso,
                                         List<Subprocesso> subprocessos,
                                         Authentication authentication) {
        if (p == null) return null;

        // Mapeia os dados básicos do processo usando MapStruct
        ProcessoDetalheDto dto = processoDetalheMapperInterface.toDetailDTO(p);

        // Lógica de controle de tela
        boolean isGestor = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR"));

        boolean podeFinalizar = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean podeHomologarCadastro = false;
        boolean podeHomologarMapa = false;

        if (isGestor) {
            Usuario usuario = (Usuario) authentication.getPrincipal();
            Long unidadeGestorId = usuario.getUnidade().getCodigo();

            List<Long> unidadesSubordinadasIds = unidadeRepo.findByUnidadeSuperiorCodigo(unidadeGestorId)
                .stream()
                .map(Unidade::getCodigo)
                .toList();

            if (!unidadesSubordinadasIds.isEmpty()) {
                podeHomologarCadastro = subprocessos.stream()
                    .filter(sp -> unidadesSubordinadasIds.contains(sp.getUnidade().getCodigo()))
                    .anyMatch(sp -> sp.getSituacao() == SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO ||
                                   sp.getSituacao() == SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA);

                podeHomologarMapa = subprocessos.stream()
                    .filter(sp -> unidadesSubordinadasIds.contains(sp.getUnidade().getCodigo()))
                    .anyMatch(sp -> sp.getSituacao() == SituacaoSubprocesso.MAPA_VALIDADO ||
                                   sp.getSituacao() == SituacaoSubprocesso.MAPA_COM_SUGESTOES);
            }
        }

        Map<String, ProcessoDetalheDto.UnidadeParticipanteDto> unidadesBySigla = new HashMap<>();

        // Mapeia as unidades de processo
        if (unidadesProcesso != null) {
            for (UnidadeProcesso up : unidadesProcesso) {
                ProcessoDetalheDto.UnidadeParticipanteDto unit = processoDetalheMapperInterface.unidadeProcessoToUnidadeParticipanteDTO(up);
                if (unit.getSigla() != null) {
                    unidadesBySigla.put(unit.getSigla(), unit);
                }
            }
        }

        // Associa as informações dos subprocessos às unidades
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                String sigla = (sp.getUnidade() != null) ? sp.getUnidade().getSigla() : null;

                if (sigla != null && unidadesBySigla.containsKey(sigla)) {
                    // Atualiza a unidade existente com informações do subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDto existingUnit = unidadesBySigla.get(sigla);
                    ProcessoDetalheDto.UnidadeParticipanteDto updatedUnit = ProcessoDetalheDto.UnidadeParticipanteDto.builder()
                        .codUnidade(existingUnit.getCodUnidade())
                        .nome(existingUnit.getNome())
                        .sigla(existingUnit.getSigla())
                        .codUnidadeSuperior(existingUnit.getCodUnidadeSuperior())
                        .situacaoSubprocesso(sp.getSituacao()) // Novo valor
                        .dataLimite(sp.getDataLimiteEtapa1())   // Novo valor
                        .filhos(existingUnit.getFilhos())
                        .build();
                    unidadesBySigla.put(sigla, updatedUnit); // Atualizar no mapa
                } else {
                    // Cria uma nova unidade participante baseada no subprocesso
                    ProcessoDetalheDto.UnidadeParticipanteDto unit = processoDetalheMapperInterface.subprocessoToUnidadeParticipanteDTO(sp);
                    if (unit.getSigla() != null) {
                        unidadesBySigla.put(unit.getSigla(), unit);
                    }
                }
            }
        }

        // Constroi a lista final de unidades a partir do mapa para garantir que as atualizações sejam refletidas
        List<ProcessoDetalheDto.UnidadeParticipanteDto> unidades = new ArrayList<>(unidadesBySigla.values());

        // Mapeia os subprocessos para resumo
        List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                ProcessoResumoDto resumoDto = processoDetalheMapperInterface.subprocessoToProcessoResumoDto(sp);
                resumoSubprocessos.add(resumoDto);
            }
        }

        return ProcessoDetalheDto.builder()
            .codigo(dto.getCodigo())
            .descricao(dto.getDescricao())
            .tipo(dto.getTipo())
            .situacao(dto.getSituacao())
            .dataLimite(dto.getDataLimite())
            .dataCriacao(dto.getDataCriacao())
            .dataFinalizacao(dto.getDataFinalizacao())
            .unidades(unidades)
            .resumoSubprocessos(resumoSubprocessos)
            .podeFinalizar(podeFinalizar)
            .podeHomologarCadastro(podeHomologarCadastro)
            .podeHomologarMapa(podeHomologarMapa)
            .build();
    }
}