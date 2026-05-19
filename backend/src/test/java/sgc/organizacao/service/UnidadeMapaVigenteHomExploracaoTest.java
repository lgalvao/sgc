package sgc.organizacao.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sgc.organizacao.dto.MapaVigenteReferenciaDto;
import sgc.organizacao.model.UnidadeMapa;
import sgc.organizacao.model.UnidadeMapaRepo;
import sgc.processo.model.Processo;
import sgc.subprocesso.model.Subprocesso;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("hom")
@Disabled("Teste diagnóstico manual para inspecionar dados reais do perfil hom.")
class UnidadeMapaVigenteHomExploracaoTest {
    private static final Long CODIGO_UNIDADE = 732L;

    @Autowired
    private UnidadeService unidadeService;

    @Autowired
    private UnidadeMapaRepo unidadeMapaRepo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void inspecionarMapaVigenteDaUnidade732() {
        System.out.println("==== Diagnóstico unidade " + CODIGO_UNIDADE + " ====");

        boolean temMapaVigente = unidadeService.temMapaVigente(CODIGO_UNIDADE);
        Optional<MapaVigenteReferenciaDto> referencia = unidadeService.buscarReferenciaMapaVigente(CODIGO_UNIDADE);
        Optional<UnidadeMapa> unidadeMapa = unidadeMapaRepo.findById(CODIGO_UNIDADE);

        System.out.println("temMapaVigente(): " + temMapaVigente);
        System.out.println("buscarReferenciaMapaVigente(): " + referencia.orElse(null));
        System.out.println("registro UNIDADE_MAPA existe: " + unidadeMapa.isPresent());

        unidadeMapa.ifPresent(registro -> {
            System.out.println("mapaVigente.codigo: " + (registro.getMapaVigente() == null ? null : registro.getMapaVigente().getCodigo()));
            if (registro.getMapaVigente() != null && registro.getMapaVigente().getSubprocesso() != null) {
                Subprocesso sp = registro.getMapaVigente().getSubprocesso();
                Processo processo = sp.getProcesso();
                System.out.println("subprocesso.codigo: " + sp.getCodigo());
                System.out.println("subprocesso.situacao: " + sp.getSituacao());
                System.out.println("processo.codigo: " + (processo == null ? null : processo.getCodigo()));
                System.out.println("processo.tipo: " + (processo == null ? null : processo.getTipo()));
                System.out.println("processo.situacao: " + (processo == null ? null : processo.getSituacao()));
                System.out.println("processo.dataFinalizacao: " + (processo == null ? null : processo.getDataFinalizacao()));
            }
        });

        List<Object[]> subprocessosDaUnidade = entityManager.createQuery("""
                select sp.codigo, sp.situacao, p.codigo, p.tipo, p.situacao, p.dataFinalizacao
                from Subprocesso sp
                join sp.processo p
                where sp.unidade.codigo = :codigoUnidade
                order by p.codigo desc
                """, Object[].class)
                .setParameter("codigoUnidade", CODIGO_UNIDADE)
                .getResultList();

        System.out.println("subprocessos da unidade:");
        for (Object[] linha : subprocessosDaUnidade) {
            System.out.println("  subprocesso=" + linha[0]
                    + ", situacaoSp=" + linha[1]
                    + ", processo=" + linha[2]
                    + ", tipo=" + linha[3]
                    + ", situacaoProcesso=" + linha[4]
                    + ", dataFinalizacao=" + linha[5]);
        }
    }
}
