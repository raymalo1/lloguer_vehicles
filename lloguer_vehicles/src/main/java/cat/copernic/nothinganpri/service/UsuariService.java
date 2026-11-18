
package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.dto.DocumentacioForm;
import cat.copernic.nothinganpri.dto.RegistreForm;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.SolicitudValidacionJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.repository.ClientRepository;
import cat.copernic.nothinganpri.repository.UsuariJpaRepository;
import cat.copernic.nothinganpri.repository.SolicitudValidacionRepository;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de gestionar las operaciones relacionadas
 * con los usuarios de la aplicación.
 *
 * Incluye funcionalidades de inicio de sesión, comprobación de datos
 * únicos, registro de clientes y modificación de contraseñas.
 *
 * También gestiona la documentación inicial y la creación de solicitudes
 * de validación para los nuevos clientes.
 */
@Service
public class UsuariService {

    /**
     * Repositorio general de usuarios.
     */
    private final UsuariJpaRepository usuariRepository;

    /**
     * Repositorio específico de clientes.
     */
    private final ClientRepository clientRepository;

    /**
     * Repositorio de solicitudes de validación.
     */
    private final SolicitudValidacionRepository solicitudRepository;

    /**
     * Componente utilizado para cifrar y verificar contraseñas.
     */
    private final PasswordEncoder passwordEncoder;
    private final LogQueryService logQueryService;

    /**
     * Constructor del servicio.
     *
     * @param usuariRepository repositorio general de usuarios
     * @param clientRepository repositorio de clientes
     * @param solicitudRepository repositorio de solicitudes de validación
     * @param passwordEncoder codificador de contraseñas
     */
    public UsuariService(
            UsuariJpaRepository usuariRepository,
            ClientRepository clientRepository,
            SolicitudValidacionRepository solicitudRepository,
            PasswordEncoder passwordEncoder,
            LogQueryService logQueryService) {

        this.usuariRepository = usuariRepository;
        this.clientRepository = clientRepository;
        this.solicitudRepository = solicitudRepository;
        this.passwordEncoder = passwordEncoder;
        this.logQueryService = logQueryService;
    }

    // =====================================================
    // INICIAR SESIÓN
    // =====================================================

    /**
     * Comprueba las credenciales de un usuario para iniciar sesión.
     *
     * Busca al usuario mediante su correo electrónico y compara
     * la contraseña introducida con la contraseña cifrada almacenada.
     *
     * @param email correo electrónico del usuario
     * @param password contraseña introducida por el usuario
     * @return usuario autenticado
     * @throws IllegalArgumentException si el usuario no existe
     *         o la contraseña no es correcta
     */
    public UsuariJpaEntity iniciarSessio(String email, String password) {

        UsuariJpaEntity usuari = usuariRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    logQueryService.registrarWarning(
                            "USUARI",
                            "Intent d'inici de sessió amb un usuari no trobat."
                    );
                    return new IllegalArgumentException(
                            "Email o contrasenya incorrectes.");
                });

        if (password == null
                || !passwordEncoder.matches(password, usuari.getPassword())) {

            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent d'inici de sessió amb credencials incorrectes."
            );

            throw new IllegalArgumentException(
                    "Email o contrasenya incorrectes.");
        }

        logQueryService.registrarInfo(
                "USUARI",
                "Inici de sessió correcte."
        );

        return usuari;
    }

    // =====================================================
    // COMPROBAR EMAIL Y TELÉFONO
    // =====================================================

    /**
     * Comprueba si un correo electrónico ya está registrado.
     *
     * @param email correo electrónico que se desea comprobar
     * @return true si el correo ya existe, false en caso contrario
     */
    public boolean emailExisteix(String email) {
        return usuariRepository.findByEmail(email).isPresent();
    }

    /**
     * Comprueba si un número de teléfono ya está registrado.
     *
     * @param telefon número de teléfono que se desea comprobar
     * @return true si el teléfono ya existe, false en caso contrario
     */
    public boolean telefonExisteix(String telefon) {
        return usuariRepository.findByTelefon(telefon).isPresent();
    }

    /**
     * Comprueba si un correo electrónico pertenece a otro usuario
     * distinto del usuario indicado.
     *
     * @param email correo electrónico que se desea comprobar
     * @param id identificador del usuario actual
     * @return true si el correo pertenece a otro usuario
     */
    public boolean emailExisteixEnAltreUsuari(String email, Long id) {
        return usuariRepository.findByEmail(email)
                .map(u -> !Objects.equals(u.getId(), id))
                .orElse(false);
    }

    /**
     * Comprueba si un teléfono pertenece a otro usuario
     * distinto del usuario indicado.
     *
     * @param telefon teléfono que se desea comprobar
     * @param id identificador del usuario actual
     * @return true si el teléfono pertenece a otro usuario
     */
    public boolean telefonExisteixEnAltreUsuari(String telefon, Long id) {
        return usuariRepository.findByTelefon(telefon)
                .map(u -> !Objects.equals(u.getId(), id))
                .orElse(false);
    }

    // =====================================================
    // REGISTRAR CLIENTE
    // =====================================================

    /**
     * Registra un nuevo cliente en el sistema.
     *
     * Comprueba que los datos obligatorios sean correctos, verifica
     * que el correo y el teléfono no estén registrados y cifra
     * la contraseña antes de guardar el cliente.
     *
     * También guarda la documentación inicial proporcionada y crea
     * una solicitud de validación con estado pendiente.
     *
     * Toda la operación se ejecuta dentro de una transacción para
     * mantener la coherencia de los datos.
     *
     * @param registre formulario con los datos personales y de acceso
     * @param documentacio formulario con la documentación inicial
     * @return cliente registrado y guardado en la base de datos
     * @throws IllegalArgumentException si los datos son incorrectos
     *         o el correo o teléfono ya están registrados
     */
    @Transactional
    public ClientJpaEntity registrarClient(
            RegistreForm registre,
            DocumentacioForm documentacio) {

        if (registre == null) {
            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de registre sense dades."
            );

            throw new IllegalArgumentException(
                    "Les dades del registre són obligatòries.");
        }

        if (documentacio == null) {
            throw new IllegalArgumentException(
                    "La documentació del client és obligatòria.");
        }

        String telefonNormalitzat;

        try {
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();

            Phonenumber.PhoneNumber numero =
                    util.parse(registre.getTelefon(), null);

            if (!util.isValidNumber(numero)) {
                throw new IllegalArgumentException(
                        "El telèfon internacional no és vàlid.");
            }

            telefonNormalitzat = util.format(
                    numero,
                    PhoneNumberUtil.PhoneNumberFormat.E164
            );

        } catch (NumberParseException e) {
            throw new IllegalArgumentException(
                    "El telèfon internacional no és vàlid.");
        }

        if (emailExisteix(registre.getEmail())) {
            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de registre amb un email ja existent."
            );

            throw new IllegalArgumentException(
                    "Aquest email ja està registrat.");
        }

        if (telefonExisteix(telefonNormalitzat)) {
            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de registre amb un telèfon ja existent."
            );

            throw new IllegalArgumentException(
                    "Aquest telèfon ja està registrat.");
        }

        if (registre.getPassword() == null
                || registre.getPassword().length() < 8) {

            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de registre amb una contrasenya que no compleix els requisits."
            );

            throw new IllegalArgumentException(
                    "La contrasenya ha de tenir almenys 8 caràcters.");
        }

        ClientJpaEntity client = new ClientJpaEntity();

        client.setNom(registre.getNom());
        client.setCognoms(registre.getCognoms());
        client.setEmail(registre.getEmail());
        client.setTelefon(telefonNormalitzat);
        client.setNacionalitat(registre.getNacionalitat());
        client.setAdreca(registre.getAdreca());
        client.setProvincia(registre.getProvincia());
        client.setPoblacio(registre.getPoblacio());

        client.setPassword(
                passwordEncoder.encode(registre.getPassword()));

        client.setRol("CLIENT");
        client.setActivo(false);

        // Guardar la documentación inicial del cliente.
        if (documentacio.getNumeroDocument() != null) {
            client.setDNI(documentacio.getNumeroDocument());
        }

        if (documentacio.getTipusDocument() != null) {
            client.setTipusDocument(documentacio.getTipusDocument());
        }

        if (documentacio.getDataCaducitat() != null) {
            client.setCadDNI(
                    documentacio.getDataCaducitat().toString());
        }

        if (documentacio.getTipusLlicencia() != null) {
            client.setLliconduir(
                    documentacio.getTipusLlicencia());
        }

        if (documentacio.getDataCaducitatLlicencia() != null) {
            client.setCdLLiconduir(
                    documentacio.getDataCaducitatLlicencia().toString());
        }

        // Guardar la imagen del anverso del documento de identidad.
        if (documentacio.getDocumentIdentitat() != null) {
            client.setFotoDniAnverso(
                    documentacio.getDocumentIdentitat().getDades());

            client.setFotoDniAnversoTipus(
                    documentacio.getDocumentIdentitat().getContentType());
        }

        // Guardar la imagen del permiso de conducir.
        if (documentacio.getLlicenciaConduir() != null) {
            client.setFotoPermiso(
                    documentacio.getLlicenciaConduir().getDades());

            client.setFotoPermisoTipus(
                    documentacio.getLlicenciaConduir().getContentType());
        }

        // Guardar el cliente con su repositorio específico.
        ClientJpaEntity clientGuardat;

        try {
            clientGuardat = clientRepository.save(client);
        } catch (RuntimeException e) {
            logQueryService.registrarError(
                    "USUARI",
                    "Error en guardar el client durant el registre: " + e.getMessage()
            );
            throw e;
        }

        // Crear la solicitud inicial de validación del cliente.
        SolicitudValidacionJpaEntity solicitud =
                new SolicitudValidacionJpaEntity();

        solicitud.setCliente(clientGuardat);
        solicitud.setTipo("REGISTRO");
        solicitud.setEstado("PENDIENTE");
        solicitud.setDataSolicitud(LocalDateTime.now());

        try {
            solicitudRepository.save(solicitud);
        } catch (RuntimeException e) {
            logQueryService.registrarError(
                    "USUARI",
                    "Error en crear la sol·licitud de validació inicial: " + e.getMessage()
            );
            throw e;
        }

        logQueryService.registrarInfo(
                "USUARI",
                "Client registrat correctament i sol·licitud inicial de validació creada."
        );

        return clientGuardat;
    }

    // =====================================================
    // CAMBIAR CONTRASEÑA
    // =====================================================

    /**
     * Permite cambiar la contraseña de un usuario.
     *
     * Comprueba la contraseña actual, valida que la nueva contraseña
     * tenga al menos ocho caracteres y verifica que ambas confirmaciones
     * coincidan.
     *
     * La nueva contraseña se cifra antes de guardarse.
     *
     * @param email correo electrónico del usuario
     * @param contrasenyaActual contraseña actual
     * @param novaContrasenya nueva contraseña
     * @param confirmarContrasenya confirmación de la nueva contraseña
     * @throws IllegalArgumentException si el usuario no existe,
     *         la contraseña actual es incorrecta, la nueva contraseña
     *         no cumple los requisitos o las contraseñas no coinciden
     */
    @Transactional
    public void cambiarContrasenya(
            String email,
            String contrasenyaActual,
            String novaContrasenya,
            String confirmarContrasenya) {

        UsuariJpaEntity usuari = usuariRepository
                .findByEmail(email)
                .orElseThrow(() -> {
                    logQueryService.registrarWarning(
                            "USUARI",
                            "Intent de canvi de contrasenya d'un usuari no trobat."
                    );
                    return new IllegalArgumentException(
                            "Usuari no trobat.");
                });

        if (contrasenyaActual == null
                || !passwordEncoder.matches(
                        contrasenyaActual,
                        usuari.getPassword())) {

            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de canvi de contrasenya amb la contrasenya actual incorrecta."
            );

            throw new IllegalArgumentException(
                    "La contrasenya actual no és correcta.");
        }

        if (novaContrasenya == null
                || novaContrasenya.length() < 8) {

            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de canvi de contrasenya amb una contrasenya que no compleix els requisits."
            );

            throw new IllegalArgumentException(
                    "La contrasenya ha de tenir almenys 8 caràcters.");
        }

        if (!novaContrasenya.equals(confirmarContrasenya)) {
            logQueryService.registrarWarning(
                    "USUARI",
                    "Intent de canvi de contrasenya amb confirmació incorrecta."
            );

            throw new IllegalArgumentException(
                    "Les contrasenyes no coincideixen.");
        }

        usuari.setPassword(
                passwordEncoder.encode(novaContrasenya));

        try {
            usuariRepository.save(usuari);

            logQueryService.registrarInfo(
                    "USUARI",
                    "Contrasenya modificada correctament."
            );

        } catch (RuntimeException e) {
            logQueryService.registrarError(
                    "USUARI",
                    "Error en guardar la nova contrasenya: " + e.getMessage()
            );
            throw e;
        }
    }
}
