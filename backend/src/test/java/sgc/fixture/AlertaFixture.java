package sgc.fixture;

import sgc.alerta.internal.model.Alerta;
import sgc.processo.api.model.Processo;
import sgc.sgrh.internal.model.Usuario;
import sgc.unidade.internal.model.Unidade;

import java.time.LocalDateTime;

public class AlertaFixture {

    public static Alerta alertaPadrao(Processo processo) {
        Alerta alerta = new Alerta();
        alerta.setCodigo(1L);
        alerta.setProcessoCodigo(processo.getCodigo());
        alerta.setDescricao("Alerta de Teste");
        alerta.setDataHora(LocalDateTime.now());
        return alerta;
    }

    public static Alerta alertaParaUsuario(Processo processo, Usuario usuario) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUsuarioDestino(usuario);
        alerta.setDescricao("Alerta para Usuario " + usuario.getNome());
        return alerta;
    }

    public static Alerta alertaParaUnidade(Processo processo, Unidade unidade) {
        Alerta alerta = alertaPadrao(processo);
        alerta.setUnidadeDestino(unidade);
        alerta.setDescricao("Alerta para Unidade " + unidade.getNome());
        return alerta;
    }
}
