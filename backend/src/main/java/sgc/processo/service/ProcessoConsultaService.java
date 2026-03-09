package sgc.processo.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.comum.erros.*;
import sgc.comum.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.dto.*;
import sgc.processo.model.*;
import sgc.subprocesso.model.*;
import sgc.subprocesso.service.*;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessoConsultaService {
    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final SubprocessoService subprocessoService;
    private final UsuarioFacade usuarioService;
    private final ProcessoValidacaoService processoValidacaoService;

    public Processo buscarProcessoCodigo(Long codigo) {
        return repo.buscar(Processo.class, codigo);
    }

    public Processo buscarProcessoComParticipantes(Long codigo) {
        return processoRepo.findByIdComParticipantes(codigo)
                .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));
    }

    public Optional<Processo> buscarProcessoCodigoOpt(Long id) {
        return processoRepo.findById(id);
    }

    public List<Processo> processosFinalizados() {
        Usuario usuario = usuarioService.usuarioAutenticado();
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
        }

        List<Long> unidadesAcesso;
        if (usuario.getPerfilAtivo() == Perfil.GESTOR) {
            unidadesAcesso = processoValidacaoService.buscarCodigosDescendentes(usuario.getUnidadeAtivaCodigo());
        } else {
            // CHEFE ou outros perfis: apenas sua unidade
            unidadesAcesso = List.of(usuario.getUnidadeAtivaCodigo());
        }

        return processoRepo.listarPorSituacaoEUnidadeCodigos(SituacaoProcesso.FINALIZADO, unidadesAcesso);
    }

    public List<Processo> processosParaImportacao() {
        return processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
    }

    public Page<Processo> processos(Pageable pageable) {
        return processoRepo.findAll(pageable);
    }

    public List<Processo> processosAndamento() {
        Usuario usuario = usuarioService.usuarioAutenticado();
        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
        }

        List<Long> unidadesAcesso;
        if (usuario.getPerfilAtivo() == Perfil.GESTOR) {
            unidadesAcesso = processoValidacaoService.buscarCodigosDescendentes(usuario.getUnidadeAtivaCodigo());
        } else {
            unidadesAcesso = List.of(usuario.getUnidadeAtivaCodigo());
        }

        return processoRepo.listarPorSituacaoEUnidadeCodigos(SituacaoProcesso.EM_ANDAMENTO, unidadesAcesso);
    }

    public Page<Processo> processosIniciadosPorParticipantes(List<Long> unidadeIds, Pageable pageable) {
        return processoRepo.findDistinctByParticipantes_IdUnidadeCodigoInAndSituacaoNot(
                unidadeIds, SituacaoProcesso.CRIADO, pageable);
    }

    public Set<Long> buscarIdsUnidadesComProcessosAtivos(Long codProcessoIgnorar) {
        return new HashSet<>(
                processoRepo.findUnidadeCodigosBySituacaoInAndProcessoCodigoNot(
                        Arrays.asList(SituacaoProcesso.EM_ANDAMENTO, SituacaoProcesso.CRIADO),
                        codProcessoIgnorar));
    }

    public List<Long> unidadesBloqueadasPorTipo(TipoProcesso tipoProcesso) {
        return processoRepo.findUnidadeCodigosBySituacaoAndTipo(SituacaoProcesso.EM_ANDAMENTO, tipoProcesso);
    }

    public List<SubprocessoElegivelDto> subprocessosElegiveis(Long codProcesso) {
        Usuario usuario = usuarioService.usuarioAutenticado();

        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return subprocessoService.listarPorProcessoESituacoes(codProcesso,
                            List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                                    SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                                    SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO,
                                    SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO,
                                    SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                                    SituacaoSubprocesso.REVISAO_MAPA_VALIDADO))
                    .stream()
                    .map(this::toElegivelDto)
                    .toList();
        }

        List<Long> unidadesAcesso;
        if (usuario.getPerfilAtivo() == Perfil.GESTOR) {
            unidadesAcesso = processoValidacaoService.buscarCodigosDescendentes(usuario.getUnidadeAtivaCodigo());
        } else {
            unidadesAcesso = List.of(usuario.getUnidadeAtivaCodigo());
        }

        return subprocessoService.listarPorProcessoEUnidadeCodigosESituacoes(codProcesso, unidadesAcesso,
                        List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                                SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA,
                                SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO,
                                SituacaoSubprocesso.REVISAO_MAPA_VALIDADO))
                .stream()
                .map(this::toElegivelDto)
                .toList();
    }

    private SubprocessoElegivelDto toElegivelDto(Subprocesso sp) {
        return SubprocessoElegivelDto.builder()
                .codigo(sp.getCodigo())
                .unidadeCodigo(sp.getUnidade().getCodigo())
                .unidadeNome(sp.getUnidade().getNome())
                .unidadeSigla(sp.getUnidade().getSigla())
                .situacao(sp.getSituacao())
                .build();
    }
}
