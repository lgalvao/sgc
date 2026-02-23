package sgc.alerta;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaUsuario;
import sgc.comum.ComumRepo;
import sgc.organizacao.OrganizacaoFacade;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.TipoUnidade;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.processo.model.Processo;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Facade para gerenciamento de alertas do sistema.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaFacade {
    private final AlertaService alertaService;
    private final UsuarioFacade usuarioService;
    private final OrganizacaoFacade organizacaoFacade;
    private final ComumRepo comumRepo;

    private Unidade unidadeRaiz() {
        return organizacaoFacade.unidadePorCodigo(1L);
    }

    @Transactional
    public Alerta criarAlertaAdmin(Processo processo, Unidade destino, String descricao) {
        return criarAlerta(processo, unidadeRaiz(), destino, descricao);
    }

    @Transactional
    public Alerta criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        return criarAlerta(processo, origem, destino, descricao);
    }

    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Unidade> unidadesParticipantes) {
        Map<Long, Unidade> operacionais = new HashMap<>();
        Map<Long, Unidade> intermediarias = new HashMap<>();

        for (Unidade unidade : unidadesParticipantes) {
            operacionais.put(unidade.getCodigo(), unidade);
            if (unidade.getTipo() == TipoUnidade.INTEROPERACIONAL) {
                intermediarias.put(unidade.getCodigo(), unidade);
            }

            Unidade unidadeSuperior = unidade.getUnidadeSuperior();
            while (unidadeSuperior != null) {
                intermediarias.put(unidadeSuperior.getCodigo(), unidadeSuperior);
                unidadeSuperior = unidadeSuperior.getUnidadeSuperior();
            }
        }

        List<Alerta> alertasCriados = new ArrayList<>();
        for (Unidade unidade : operacionais.values()) {
            alertasCriados.add(criarAlertaAdmin(processo, unidade, "Início do processo"));
        }

        for (Unidade unidade : intermediarias.values()) {
            Alerta alerta = criarAlertaAdmin(processo, unidade, "Início do processo em unidades subordinadas");
            alertasCriados.add(alerta);
        }

        return alertasCriados;
    }

    @Transactional
    public void criarAlertaCadastroDisponibilizado(Processo processo, Unidade unidadeOrigem, Unidade unidadeDestino) {
        String desc = "Cadastro disponibilizado pela unidade %s no processo '%s'."
                .formatted(unidadeOrigem.getSigla(), processo.getDescricao());

        criarAlerta(processo, unidadeOrigem, unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaCadastroDevolvido(Processo processo, Unidade unidadeDestino, String motivo) {
        String desc = "Cadastro devolvido no processo '%s'. Motivo: %s."
                .formatted(processo.getDescricao(), motivo);

        criarAlerta(processo, unidadeRaiz(), unidadeDestino, desc);
    }

    @Transactional
    public void criarAlertaAlteracaoDataLimite(Processo processo, Unidade unidadeDestino, String novaData, int etapa) {
        String desc = "Data limite da etapa %d alterada para %s".formatted(etapa, novaData);
        criarAlerta(processo, unidadeRaiz(), unidadeDestino, desc);
    }

    private Alerta criarAlerta(Processo processo, Unidade origem, Unidade destino, String descricao) {
        Alerta alerta = Alerta.builder()
                .processo(processo)
                .dataHora(LocalDateTime.now())
                .unidadeOrigem(origem)
                .unidadeDestino(destino)
                .descricao(descricao)
                .build();

        return alertaService.salvar(alerta);
    }

    @Transactional
    public void marcarComoLidos(String usuarioTitulo, List<Long> alertaCodigos) {
        Usuario usuario = usuarioService.buscarPorTitulo(usuarioTitulo);
        LocalDateTime agora = LocalDateTime.now();

        for (Long codigo : alertaCodigos) {
            AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                    .alertaCodigo(codigo)
                    .usuarioTitulo(usuarioTitulo)
                    .build();

            AlertaUsuario alertaUsuario = alertaService.alertaUsuario(chave).orElseGet(() -> {
                Alerta alerta = comumRepo.buscar(Alerta.class, codigo);
                return AlertaUsuario.builder()
                        .id(chave)
                        .alerta(alerta)
                        .usuario(usuario)
                        .dataHoraLeitura(agora)
                        .build();
            });

            alertaService.salvarAlertaUsuario(alertaUsuario);
        }
    }

    public List<Alerta> alertasPorUsuario(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscarPorTitulo(usuarioTitulo);
        Unidade lotacao = usuario.getUnidadeLotacao();

        List<Alerta> alertasUnidade = alertaService.porUnidadeDestino(lotacao.getCodigo());
        if (alertasUnidade.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> alertaCodigos = alertasUnidade.stream().map(Alerta::getCodigo).toList();
        List<AlertaUsuario> leituras = alertaService.alertasUsuarios(usuarioTitulo, alertaCodigos);

        Map<Long, LocalDateTime> mapaLeitura = new HashMap<>();
        for (AlertaUsuario au : leituras) {
            mapaLeitura.put(au.getId().getAlertaCodigo(), au.getDataHoraLeitura());
        }

        for (Alerta alerta : alertasUnidade) {
            alerta.setDataHoraLeitura(mapaLeitura.get(alerta.getCodigo()));
        }

        return alertasUnidade;
    }

    public List<Alerta> listarNaoLidos(String usuarioTitulo) {
        return alertasPorUsuario(usuarioTitulo).stream()
                .filter(alerta -> alerta.getDataHoraLeitura() == null)
                .toList();
    }

    public Page<Alerta> listarPorUnidade(Long codigoUnidade, Pageable pageable) {
        return alertaService.porUnidadeDestinoPaginado(codigoUnidade, pageable);
    }

    public Optional<LocalDateTime> obterDataHoraLeitura(Long codigoAlerta, String usuarioTitulo) {
        return alertaService.dataHoraLeituraAlertaUsuario(codigoAlerta, usuarioTitulo);
    }

    @Transactional
    public void criarAlertaReaberturaCadastro(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Cadastro de atividades reaberto pela ADMIN. Justificativa: %s".formatted(justificativa);
        criarAlertaAdmin(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaCadastroSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Cadastro da unidade %s reaberto pela ADMIN".formatted(subordinada.getSigla());
        criarAlertaAdmin(processo, superior, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisao(Processo processo, Unidade unidade, String justificativa) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela ADMIN. Justificativa: %s".formatted(unidade.getSigla(), justificativa);
        criarAlertaAdmin(processo, unidade, descricao);
    }

    @Transactional
    public void criarAlertaReaberturaRevisaoSuperior(Processo processo, Unidade superior, Unidade subordinada) {
        String descricao = "Revisão de cadastro da unidade %s reaberta pela ADMIN".formatted(subordinada.getSigla());
        criarAlertaAdmin(processo, superior, descricao);
    }
}
