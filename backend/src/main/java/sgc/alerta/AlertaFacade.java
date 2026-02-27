package sgc.alerta;

import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import sgc.alerta.model.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;
import sgc.processo.model.*;

import java.time.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaFacade {
    private final AlertaService alertaService;
    private final UsuarioFacade usuarioService;
    private final OrganizacaoFacade organizacaoFacade;

    private Unidade unidadeRaiz() {
        return organizacaoFacade.unidadePorCodigo(1L);
    }

    public List<Alerta> alertasPorUsuario(String usuarioTitulo) {
        Usuario usuario = usuarioService.buscarPorTitulo(usuarioTitulo);
        Unidade lotacao = usuario.getUnidadeLotacao();

        List<Alerta> alertas = alertaService.porUnidadeDestino(lotacao.getCodigo());
        if (alertas.isEmpty()) return Collections.emptyList();

        List<Long> alertaCodigos = alertas.stream().map(Alerta::getCodigo).toList();
        List<AlertaUsuario> leituras = alertaService.alertasUsuarios(usuarioTitulo, alertaCodigos);

        Map<Long, LocalDateTime> mapaLeitura = new HashMap<>();
        for (AlertaUsuario alertaUsuario : leituras) {
            mapaLeitura.put(alertaUsuario.getId().getAlertaCodigo(), alertaUsuario.getDataHoraLeitura());
        }

        alertas.forEach(alerta -> alerta.setDataHoraLeitura(mapaLeitura.get(alerta.getCodigo())));

        return alertas;
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
    public Alerta criarAlertaAdmin(Processo processo, Unidade destino, String descricao) {
        return criarAlerta(processo, unidadeRaiz(), destino, descricao);
    }

    @Transactional
    public Alerta criarAlertaTransicao(Processo processo, String descricao, Unidade origem, Unidade destino) {
        return criarAlerta(processo, origem, destino, descricao);
    }

    @Transactional
    public List<Alerta> criarAlertasProcessoIniciado(Processo processo, List<Unidade> unidadesParticipantes) {
        Set<Long> codsOperacionais = new HashSet<>();
        Set<Long> codsIntermediarias = new HashSet<>();
        Map<Long, Unidade> todasUnidadesMap = new HashMap<>();

        for (Unidade unidade : unidadesParticipantes) {
            todasUnidadesMap.put(unidade.getCodigo(), unidade);
            TipoUnidade tipo = unidade.getTipo();

            if (tipo == TipoUnidade.OPERACIONAL || tipo == TipoUnidade.INTEROPERACIONAL || tipo == TipoUnidade.RAIZ) {
                codsOperacionais.add(unidade.getCodigo());
            }

            if (tipo == TipoUnidade.INTERMEDIARIA || tipo == TipoUnidade.INTEROPERACIONAL) {
                codsIntermediarias.add(unidade.getCodigo());
            }

            Unidade superior = unidade.getUnidadeSuperior();
            while (superior != null) {
                todasUnidadesMap.put(superior.getCodigo(), superior);
                codsIntermediarias.add(superior.getCodigo());
                superior = superior.getUnidadeSuperior();
            }
        }

        List<Alerta> alertasCriados = new ArrayList<>();
        for (Long cod : codsOperacionais) {
            alertasCriados.add(criarAlertaAdmin(processo, todasUnidadesMap.get(cod), "Início do processo"));
        }

        for (Long cod : codsIntermediarias) {
            alertasCriados.add(criarAlertaAdmin(processo, todasUnidadesMap.get(cod), "Início do processo em unidade(s) subordinada(s)"));
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

            Optional<AlertaUsuario> optAlertaUsuario = alertaService.alertaUsuario(chave);

            if (optAlertaUsuario.isPresent()) {
                AlertaUsuario au = optAlertaUsuario.get();
                if (au.getDataHoraLeitura() == null) {
                    au.setDataHoraLeitura(agora);
                    alertaService.salvarAlertaUsuario(au);
                }
                continue;
            }

            alertaService.porCodigo(codigo).ifPresent(alerta -> {
                AlertaUsuario novo = AlertaUsuario.builder()
                        .id(chave)
                        .alerta(alerta)
                        .usuario(usuario)
                        .dataHoraLeitura(agora)
                        .build();
                alertaService.salvarAlertaUsuario(novo);
            });
        }
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
