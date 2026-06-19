package sgc.alerta;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sgc.alerta.model.Alerta;
import sgc.alerta.model.AlertaRepo;
import sgc.alerta.model.AlertaUsuario;
import sgc.alerta.model.AlertaUsuarioRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlertaService {
    private final AlertaRepo alertaRepo;
    private final AlertaUsuarioRepo alertaUsuarioRepo;

    Alerta salvar(Alerta alerta) {
        return alertaRepo.save(alerta);
    }

    List<Alerta> salvarTodos(List<Alerta> alertas) {
        return alertaRepo.saveAll(alertas);
    }

    public Optional<Alerta> porCodigo(Long codigo) {
        return alertaRepo.findById(codigo);
    }

    /**
     * Regra CDU-02: Para o perfil SERVIDOR, lista apenas os alertas destinados
     * individualmente ao seu título de eleitor.
     */
    public List<Alerta> listarParaServidor(String usuarioTitulo) {
        return alertaRepo.buscarAlertasExclusivosDoUsuario(usuarioTitulo);
    }

    /**
     * Regra CDU-02: Para os perfis de gestão, lista alertas pessoais do
     * usuário logado e alertas coletivos da unidade ativa.
     */
    public List<Alerta> listarParaGestao(Long codigoUnidade, String usuarioTitulo) {
        return alertaRepo.buscarAlertasDaGestao(codigoUnidade, usuarioTitulo);
    }

    public Page<Alerta> listarParaServidorPaginado(String usuarioTitulo, Pageable pageable) {
        return alertaRepo.buscarAlertasExclusivosDoUsuario(usuarioTitulo, pageable);
    }

    public Page<Alerta> listarParaGestaoPaginado(Long codigoUnidade, String usuarioTitulo, Pageable pageable) {
        return alertaRepo.buscarAlertasDaGestao(codigoUnidade, usuarioTitulo, pageable);
    }

    public Optional<AlertaUsuario> alertaUsuario(AlertaUsuario.Chave chave) {
        return alertaUsuarioRepo.findById(chave);
    }

    public List<AlertaUsuario> alertasUsuarios(String usuarioTitulo, List<Long> alertaCodigos) {
        return alertaUsuarioRepo.listarPorUsuarioEAlertas(usuarioTitulo, alertaCodigos);
    }

    public List<Alerta> listarPorCodigos(List<Long> codigos) {
        return alertaRepo.findAllById(codigos);
    }

    void salvarAlertaUsuario(AlertaUsuario au) {
        alertaUsuarioRepo.save(au);
    }

    List<AlertaUsuario> salvarAlertasUsuarios(List<AlertaUsuario> alertasUsuarios) {
        return alertaUsuarioRepo.saveAll(alertasUsuarios);
    }

    public Optional<LocalDateTime> dataHoraLeituraAlertaUsuario(Long codigoAlerta, String usuarioTitulo) {
        AlertaUsuario.Chave chave = AlertaUsuario.Chave.builder()
                .alertaCodigo(codigoAlerta)
                .usuarioTitulo(usuarioTitulo)
                .build();
        return alertaUsuarioRepo.findById(chave).map(AlertaUsuario::getDataHoraLeitura);
    }
}
