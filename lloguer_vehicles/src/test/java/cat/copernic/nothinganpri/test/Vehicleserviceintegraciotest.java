package cat.copernic.nothinganpri.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible; 
import cat.copernic.nothinganpri.repository.VehicleJpaRepository;
import cat.copernic.nothinganpri.service.VehicleService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class VehicleServiceIntegracioTest {

    @Autowired
    private VehicleService service;

    @Autowired
    private VehicleJpaRepository repo;

    private VehicleJpaEntity vehicle(String matricula) {
        VehicleJpaEntity v = new VehicleJpaEntity();
        v.setMatricula(matricula);
        v.setMarca("Peugeot");
        v.setModel("3008");
        v.setAnyMatriculacio(2021);
        v.setCategoria(CategoriaVehicle.SUV);
        v.setCombustible(TipusCombustible.GASOLINA); // AJUSTA al valor real del enum
        v.setPlaces(5);
        v.setPreuHora(new BigDecimal("2.60"));
        v.setFiancaEstandard(new BigDecimal("300"));
        return v;
    }

    @Test
    void desaUnVehicleRealmentALaBaseDeDades() {
        VehicleJpaEntity desat = service.guardar(vehicle("9999 ZZZ"));

        assertNotNull(desat.getId(), "MySQL ha d'haver assignat un id");
        assertTrue(repo.findByMatricula(desat.getMatricula()).isPresent(),
                "S'ha de poder tornar a trobar per matrícula");
    }

    @Test
    void rebutjaDuesMatriculesIgualsAlaBaseDeDades() {
        service.guardar(vehicle("8888 YYY"));

        assertThrows(IllegalArgumentException.class, () -> service.guardar(vehicle("8888 YYY")));
    }
}
