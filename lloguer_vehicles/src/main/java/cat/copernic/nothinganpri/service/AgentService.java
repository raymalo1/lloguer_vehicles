
package cat.copernic.nothinganpri.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;

import cat.copernic.nothinganpri.dto.AgentForm;
import cat.copernic.nothinganpri.entity.AgentJpaEntity;
import cat.copernic.nothinganpri.repository.AgentRepository;
import cat.copernic.nothinganpri.validacio.ProvinciaValidator;

@Service
public class AgentService {

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UsuariService usuariService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LogQueryService logQueryService;

    private void registrarError(String operacio, Exception e) {
        logQueryService.registrarError(
                "AGENTS",
                "Error en " + operacio + ": " + e.getMessage());
    }

    // LISTAR TODOS LOS AGENTES

    public List<AgentJpaEntity> llistarTots() {
        try {
            return agentRepository.findByRolIn(
                    List.of("ADMINISTRADOR", "AGENT", "ADMIN"));

        } catch (Exception e) {
            registrarError("llistar tots els agents", e);
            throw e;
        }
    }

    // BUSCAR AGENTES CON FILTROS

    public List<AgentJpaEntity> cercarFiltres(
            String dni,
            Boolean activo) {

        try {
            return agentRepository.cercarAgents(
                    dni == null || dni.trim().isEmpty()
                            ? null
                            : dni.trim(),
                    activo,
                    List.of("ADMINISTRADOR", "AGENT", "ADMIN"));

        } catch (Exception e) {
            registrarError("cercar filtres d'agents", e);
            throw e;
        }
    }

    // FILTRAR POR DNI

    public List<AgentJpaEntity> filtrarPerDNI(String dni) {
        try {
            return agentRepository.findByActivoTrueAndDNIContainingIgnoreCaseAndRolIn(
                    dni,
                    List.of("ADMINISTRADOR", "AGENT", "ADMIN"));

        } catch (Exception e) {
            registrarError("filtrar agents per DNI", e);
            throw e;
        }
    }

    // BUSCAR AGENTE POR DNI

    public AgentJpaEntity buscarAgentPerDNI(String dni) {
        try {
            return agentRepository.findByDNIAndRolIn(
                    dni,
                    List.of("ADMINISTRADOR", "AGENT", "ADMIN"));

        } catch (Exception e) {
            registrarError("buscar agent per DNI", e);
            throw e;
        }
    }

    // REGISTRAR AGENTE

    @Transactional
    public void guardar(AgentJpaEntity agent) {
        try {
            if (agentRepository.existsByDNI(agent.getDNI())) {
                throw new IllegalArgumentException(
                        "Aquest DNI ja està registrat.");
            }

            if (usuariService.emailExisteix(agent.getEmail())) {
                throw new IllegalArgumentException(
                        "Aquest email ja està registrat.");
            }

            if (usuariService.telefonExisteix(agent.getTelefon())) {
                throw new IllegalArgumentException(
                        "Aquest telèfon ja està registrat.");
            }

            if (!ProvinciaValidator.esProvinciaValida(
                    agent.getProvincia())) {
                throw new IllegalArgumentException(
                        "La província no és vàlida.");
            }

            if (agent.getRol() == null
                    || (!agent.getRol().equals("AGENT")
                    && !agent.getRol().equals("ADMINISTRADOR"))) {
                throw new IllegalArgumentException(
                        "El rol seleccionat no és vàlid.");
            }

            if (agent.getPassword() == null
                    || agent.getPassword().isBlank()) {
                throw new IllegalArgumentException(
                        "La contrasenya és obligatòria.");
            }

            agent.setProvincia(agent.getProvincia().trim());

            // Encriptar la contraseña antes de guardarla.
            String passwordEncriptada =
                    passwordEncoder.encode(agent.getPassword());

            agent.setPassword(passwordEncriptada);

            // Asignar fecha actual si no se ha indicado ninguna.
            if (agent.getDataContracte() == null) {
                agent.setDataContracte(LocalDate.now());
            }

            agentRepository.save(agent);

        } catch (Exception e) {
            registrarError("guardar un agent", e);
            throw e;
        }
    }

    // BUSCAR AGENTE POR ID

    public AgentJpaEntity buscarPerId(Long id) {
        try {
            return agentRepository.findById(id).orElse(null);

        } catch (Exception e) {
            registrarError("buscar agent per ID", e);
            throw e;
        }
    }

    // ACTUALIZAR AGENTE

    @Transactional
    public AgentJpaEntity actualitzar(Long id, AgentForm form) {
        try {
            AgentJpaEntity agent = agentRepository.findById(id)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "L'agent no existeix."));

            // Comprobar email duplicado.
            if (usuariService.emailExisteixEnAltreUsuari(
                    form.getEmail(), id)) {

                throw new IllegalArgumentException(
                        "Aquest email ja està registrat.");
            }

            // Comprobar teléfono duplicado.
            if (usuariService.telefonExisteixEnAltreUsuari(
                    form.getTelefon(), id)) {

                throw new IllegalArgumentException(
                        "Aquest telèfon ja està registrat.");
            }

            // Validar provincia.
            if (!ProvinciaValidator.esProvinciaValida(
                    form.getProvincia())) {

                throw new IllegalArgumentException(
                        "La província no és vàlida.");
            }

            // Validar rol.
            if (form.getRol() == null
                    || (!form.getRol().equals("AGENT")
                    && !form.getRol().equals("ADMINISTRADOR"))) {

                throw new IllegalArgumentException(
                        "El rol seleccionat no és vàlid.");
            }

            // Actualizar datos personales.
            // El DNI se conserva de la entidad original.
            agent.setNom(form.getNom());
            agent.setCognoms(form.getCognoms());
            agent.setProvincia(form.getProvincia().trim());
            agent.setTelefon(form.getTelefon());
            agent.setEmail(form.getEmail());

            // Conservar la fecha original si el formulario
            // no ha enviado una fecha nueva.
            if (form.getDataContracte() != null) {
                agent.setDataContracte(form.getDataContracte());
            }

            // Actualizar el rol.
            agent.setRol(form.getRol());

            // No modificar la contraseña:
            // el formulario de edición no la envía.

            return agentRepository.save(agent);

        } catch (Exception e) {
            registrarError("actualitzar un agent", e);
            throw e;
        }
    }

    // ELIMINAR AGENTE

    @Transactional
    public void eliminar(Long id) {
        try {
            AgentJpaEntity agent = agentRepository.findById(id)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "L'agent no existeix."));

            agentRepository.delete(agent);

            logQueryService.registrarInfo(
                    "AGENTS",
                    "Agent eliminat definitivament. ID: " + id);

        } catch (Exception e) {
            registrarError("eliminar un agent", e);
            throw e;
        }
    }
}
