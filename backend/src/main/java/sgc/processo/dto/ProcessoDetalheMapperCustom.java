package sgc.processo.dto;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sgc.processo.modelo.Processo;
import sgc.processo.modelo.SituacaoProcesso;
import sgc.processo.modelo.UnidadeProcesso;
import sgc.subprocesso.modelo.SituacaoSubprocesso;
import sgc.subprocesso.modelo.Subprocesso;

import java.util.*;

/**
 * Implementação customizada para mapeamento complexo de Processo para ProcessoDetalheDto.
 * Esta classe implementa a lógica de associação entre unidades de processo e subprocessos,
 * que não pode ser feita automaticamente pelo MapStruct.
 */
@Component
public class ProcessoDetalheMapperCustom {

    private final ProcessoDetalheMapper processoDetalheMapper;

    public ProcessoDetalheMapperCustom(ProcessoDetalheMapper processoDetalheMapper) {
        this.processoDetalheMapper = processoDetalheMapper;
    }

    /**
     * Converte um Processo com suas associações para ProcessoDetalheDto,
     * mapeando corretamente as unidades de processo e subprocessos.
     */
    public ProcessoDetalheDto toDetailDTO(Processo p,
                                         List<UnidadeProcesso> unidadesProcesso,
                                         List<Subprocesso> subprocessos) {
        if (p == null) return null;

        // Mapeia os dados básicos do processo usando MapStruct
        ProcessoDetalheDto dto = processoDetalheMapper.toDetailDTO(p);

        Map<String, ProcessoDetalheDto.UnidadeParticipanteDto> unidadesBySigla = new HashMap<>();

        // Mapeia as unidades de processo
        if (unidadesProcesso != null) {
            for (UnidadeProcesso up : unidadesProcesso) {
                ProcessoDetalheDto.UnidadeParticipanteDto unit = processoDetalheMapper.unidadeProcessoToUnidadeParticipanteDTO(up);
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
                    ProcessoDetalheDto.UnidadeParticipanteDto unit = processoDetalheMapper.subprocessoToUnidadeParticipanteDTO(sp);
                    if (unit.getSigla() != null) {
                        unidadesBySigla.put(unit.getSigla(), unit);
                    }
                }
            }
        }

        // Constroi a lista final de unidades a partir do mapa para garantir que as atualizações sejam refletidas
        List<ProcessoDetalheDto.UnidadeParticipanteDto> todasUnidades = new ArrayList<>(unidadesBySigla.values());

        // Construir hierarquia de unidades (raízes com filhos)
        List<ProcessoDetalheDto.UnidadeParticipanteDto> unidades = construirHierarquiaUnidades(todasUnidades);

        // Mapeia os subprocessos para resumo
        List<ProcessoResumoDto> resumoSubprocessos = new ArrayList<>();
        if (subprocessos != null) {
            for (Subprocesso sp : subprocessos) {
                ProcessoResumoDto resumoDto = processoDetalheMapper.subprocessoToProcessoResumoDto(sp);
                resumoSubprocessos.add(resumoDto);
            }
        }

        // Lógica para os botões condicionais
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean allSubprocessosHomologados = subprocessos != null && !subprocessos.isEmpty() && subprocessos.stream()
                .allMatch(sp -> sp.getSituacao() == SituacaoSubprocesso.MAPA_HOMOLOGADO);

        boolean podeFinalizar = isAdmin
                && p.getSituacao() == SituacaoProcesso.EM_ANDAMENTO
                && allSubprocessosHomologados;

        final Set<SituacaoSubprocesso> situacoesHomologacaoCadastro = Set.of(
            SituacaoSubprocesso.CADASTRO_DISPONIBILIZADO,
            SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA
        );
        boolean podeHomologarCadastro = subprocessos != null && subprocessos.stream()
                .anyMatch(sp -> situacoesHomologacaoCadastro.contains(sp.getSituacao()));

        final Set<SituacaoSubprocesso> situacoesHomologacaoMapa = Set.of(
            SituacaoSubprocesso.MAPA_VALIDADO,
            SituacaoSubprocesso.MAPA_COM_SUGESTOES
        );
        boolean podeHomologarMapa = subprocessos != null && subprocessos.stream()
                .anyMatch(sp -> situacoesHomologacaoMapa.contains(sp.getSituacao()));

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

    /**
     * Constrói hierarquia de unidades participantes organizando-as em estrutura de árvore.
     * Unidades raiz (sem superior) ficam no nível principal, e as demais são organizadas
     * recursivamente como filhas de suas superiores.
     */
    private List<ProcessoDetalheDto.UnidadeParticipanteDto> construirHierarquiaUnidades(
            List<ProcessoDetalheDto.UnidadeParticipanteDto> todasUnidades) {

        if (todasUnidades == null || todasUnidades.isEmpty()) {
            return Collections.emptyList();
        }

        // Criar mapa para acesso rápido por código
        Map<Long, ProcessoDetalheDto.UnidadeParticipanteDto> unidadesPorCodigo = new HashMap<>();
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidade : todasUnidades) {
            unidadesPorCodigo.put(unidade.getCodUnidade(), unidade);
        }

        List<ProcessoDetalheDto.UnidadeParticipanteDto> raizes = new ArrayList<>();

        // Percorrer todas as unidades e organizar em hierarquia
        for (ProcessoDetalheDto.UnidadeParticipanteDto unidade : todasUnidades) {
            if (unidade.getCodUnidadeSuperior() == null) {
                // É raiz
                raizes.add(unidade);
            } else {
                // Adicionar como filha da superior
                ProcessoDetalheDto.UnidadeParticipanteDto superior = unidadesPorCodigo.get(unidade.getCodUnidadeSuperior());
                if (superior != null) {
                    // Adicionar aos filhos da superior
                    superior.getFilhos().add(unidade);
                }
            }
        }

        return raizes;
    }
}