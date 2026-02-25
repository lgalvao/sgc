package sgc.processo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.erros.ErroEntidadeNaoEncontrada;
import sgc.comum.model.ComumRepo;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.processo.dto.SubprocessoElegivelDto;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.processo.model.TipoProcesso;
import sgc.subprocesso.model.SituacaoSubprocesso;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.service.ConsultasSubprocessoService;

import java.util.*;

/**
 * Centraliza consultas envolvendo processos e subprocessos, incluindo listagens filtradas e verificações de elegibilidade.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProcessoConsultaService {
    private final ProcessoRepo processoRepo;
    private final ComumRepo repo;
    private final ConsultasSubprocessoService servicoConsultas;
    private final UsuarioFacade usuarioService;
    private final ProcessoAcessoService processoAcessoService;

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
        Perfil perfil = usuario.getPerfilAtivo();
        Long unidadeCodigo = usuario.getUnidadeAtivaCodigo();

        if (perfil == Perfil.ADMIN) {
            return processoRepo.listarPorSituacaoComParticipantes(SituacaoProcesso.FINALIZADO);
        }
        else {
            List<Long> unidadesAcesso = processoAcessoService.buscarCodigosDescendentes(unidadeCodigo);
            return processoRepo.listarPorSituacaoEUnidadeCodigos(SituacaoProcesso.FINALIZADO, unidadesAcesso);
        }
    }

    public Page<Processo> processos(Pageable pageable) {
        return processoRepo.findAll(pageable);
    }

    public List<Processo> processosAndamento() {
        return processoRepo.findBySituacao(SituacaoProcesso.EM_ANDAMENTO);
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

    /**
     * Lista subprocessos elegíveis para o usuário atual no contexto do processo especificado
     */
    public List<SubprocessoElegivelDto> subprocessosElegiveis(Long codProcesso) {
        Usuario usuario = usuarioService.usuarioAutenticado();

        if (usuario.getPerfilAtivo() == Perfil.ADMIN) {
            return servicoConsultas.listarPorProcessoESituacoes(codProcesso,
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

        return servicoConsultas.listarPorProcessoUnidadeESituacoes(codProcesso, usuario.getUnidadeAtivaCodigo(),
                        List.of(SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO,
                                SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA))
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
