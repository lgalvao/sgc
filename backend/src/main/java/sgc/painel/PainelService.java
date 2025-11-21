package sgc.painel;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.dto.AlertaDto;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;
import sgc.painel.erros.ErroParametroPainelInvalido;
import sgc.processo.dto.ProcessoResumoDto;
import sgc.processo.model.Processo;
import sgc.processo.model.ProcessoRepo;
import sgc.processo.model.SituacaoProcesso;
import sgc.sgrh.model.Perfil;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PainelService {
    private final ProcessoRepo processoRepo;
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;
    private final UnidadeRepo unidadeRepo;

    /**
     * Lista processos com base no perfil e na unidade do usuário.
     * <p>
     * - Se o perfil for 'ADMIN', todos os processos são retornados.
     * - Para outros perfis, os processos são filtrados pela unidade do usuário e
     * suas subordinadas. Processos no estado 'CRIADO' são omitidos.
     *
     * @param perfil        O perfil do usuário (obrigatório).
     * @param codigoUnidade O código da unidade do usuário (necessário para perfis não-ADMIN).
     * @param pageable      As informações de paginação.
     * @return Uma página {@link Page} de {@link ProcessoResumoDto}.
     * @throws IllegalArgumentException se o perfil for nulo or em branco.
     */
    public Page<ProcessoResumoDto> listarProcessos(Perfil perfil, Long codigoUnidade, Pageable pageable) {
        if (perfil == null) {
            throw new ErroParametroPainelInvalido("O parâmetro 'perfil' é obrigatório");
        }

        Page<Processo> processos;
        if (perfil == Perfil.ADMIN) {
            processos = processoRepo.findAll(pageable);
        } else {
            if (codigoUnidade == null) return Page.empty(pageable);

            List<Long> unidadeIds = obterIdsUnidadesSubordinadas(codigoUnidade);
            unidadeIds.add(codigoUnidade);

            processos = processoRepo.findDistinctByParticipantes_CodigoInAndSituacaoNot(
                unidadeIds, 
                SituacaoProcesso.CRIADO, 
                pageable
            );
        }

        return processos.map(processo -> paraProcessoResumoDto(processo, perfil, codigoUnidade));
    }

    /**
     * Lista alertas com base no usuário ou na unidade.
     * <p>
     * A busca prioriza o título do usuário. Se não for fornecido, busca pela
     * unidade e suas subordinadas. Se nenhum dos dois for fornecido, retorna
     * todos os alertas.
     *
     * @param usuarioTitulo Título de eleitor do usuário.
     * @param codigoUnidade Código da unidade.
     * @param pageable      As informações de paginação.
     * @return Uma página {@link Page} de {@link AlertaDto}.
     */
    public Page<AlertaDto> listarAlertas(String usuarioTitulo, Long codigoUnidade, Pageable pageable) {
        Page<Alerta> alertasPage;
        if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
            alertasPage = alertaRepo.findByUsuarioDestino_TituloEleitoral(usuarioTitulo, pageable);
        } else if (codigoUnidade != null) {
            List<Long> unidadeIds = obterIdsUnidadesSubordinadas(codigoUnidade);
            unidadeIds.add(codigoUnidade);
            alertasPage = alertaRepo.findByUnidadeDestino_CodigoIn(unidadeIds, pageable);
        } else {
            alertasPage = alertaRepo.findAll(pageable);
        }

        return alertasPage.map(alerta -> {
            LocalDateTime dataHoraLeitura = null;
            if (usuarioTitulo != null && !usuarioTitulo.isBlank()) {
                dataHoraLeitura = alertaUsuarioRepo.findById(new AlertaUsuario.Chave(alerta.getCodigo(), usuarioTitulo))
                        .map(AlertaUsuario::getDataHoraLeitura)
                        .orElse(null);
            }
            return paraAlertaDto(alerta, dataHoraLeitura);
        });
    }

    private List<Long> obterIdsUnidadesSubordinadas(Long codUnidade) {
        List<Unidade> subordinadas = unidadeRepo.findByUnidadeSuperiorCodigo(codUnidade);
        List<Long> ids = new ArrayList<>();
        for (Unidade u : subordinadas) {
            ids.add(u.getCodigo());
            ids.addAll(obterIdsUnidadesSubordinadas(u.getCodigo()));
        }
        return ids;
    }

    private ProcessoResumoDto paraProcessoResumoDto(Processo processo, Perfil perfil, Long codigoUnidade) {
        Unidade participante = processo.getParticipantes() != null && !processo.getParticipantes().isEmpty()
                ? processo.getParticipantes().iterator().next()
                : null;
        String linkDestino = calcularLinkDestinoProcesso(processo, perfil, codigoUnidade);

        String unidadesParticipantes = processo.getParticipantes() != null
            ? processo.getParticipantes().stream()
                .map(Unidade::getSigla)
                .sorted()
                .collect(Collectors.joining(", "))
            : "";

        return ProcessoResumoDto.builder()
                .codigo(processo.getCodigo())
                .descricao(processo.getDescricao())
                .situacao(processo.getSituacao())
                .tipo(processo.getTipo().name())
                .dataLimite(processo.getDataLimite())
                .dataCriacao(processo.getDataCriacao())
                .unidadeCodigo(participante != null ? participante.getCodigo() : null)
                .unidadeNome(participante != null ? participante.getNome() : null)
                .unidadesParticipantes(unidadesParticipantes)
                .linkDestino(linkDestino)
                .build();
    }

    private String calcularLinkDestinoProcesso(Processo processo, Perfil perfil, Long codigoUnidade) {
        if (perfil == Perfil.ADMIN && processo.getSituacao() == SituacaoProcesso.CRIADO) {
            return "/processo/cadastro?codProcesso=" + processo.getCodigo();
        }
        if (perfil == Perfil.ADMIN || perfil == Perfil.GESTOR) {
            return "/processo/" + processo.getCodigo();
        }
        // Para CHEFE ou SERVIDOR, precisamos da sigla da unidade
        if (codigoUnidade != null) {
            return unidadeRepo.findById(codigoUnidade)
                .map(unidade -> "/processo/" + processo.getCodigo() + "/" + unidade.getSigla())
                .orElse(null);
        }
        return null;
    }

    private AlertaDto paraAlertaDto(Alerta alerta, LocalDateTime dataHoraLeitura) {
        String linkDestino = calcularLinkDestinoAlerta(alerta);
        return AlertaDto.builder()
            .codigo(alerta.getCodigo())
            .codProcesso(alerta.getProcesso() != null ? alerta.getProcesso().getCodigo() : null)
            .descricao(alerta.getDescricao())
            .dataHora(alerta.getDataHora())
            .unidadeOrigem(alerta.getUnidadeOrigem() != null ? alerta.getUnidadeOrigem().getSigla() : null)
            .unidadeDestino(alerta.getUnidadeDestino() != null ? alerta.getUnidadeDestino().getSigla() : null)
            .dataHoraLeitura(dataHoraLeitura)
            .linkDestino(linkDestino)
            .build();
    }

    private String calcularLinkDestinoAlerta(Alerta alerta) {
        if (alerta.getProcesso() != null && alerta.getUnidadeDestino() != null) {
            return "/processo/" + alerta.getProcesso().getCodigo() + "/" + alerta.getUnidadeDestino().getSigla();
        }
        return null;
    }
}
