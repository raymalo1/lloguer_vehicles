
package cat.copernic.nothinganpri.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.dto.DocumentacioForm;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.repository.ClientRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import cat.copernic.nothinganpri.repository.SolicitudValidacionRepository;
import jakarta.transaction.Transactional;

/**
 * Servicio que gestiona la lógica de negocio de los clientes.
 *
 * Se encarga de consultar, actualizar y eliminar clientes, gestionar
 * su documentación personal y sus tarjetas bancarias, y modificar
 * sus contraseñas.
 *
 * Las contraseñas se protegen mediante BCrypt y los números de las
 * tarjetas bancarias mediante hashes HMAC-SHA-256.
 *
 * @author Nothinganpri Development Team
 * @since 1.0
 */
@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ReservaJpaRepository reservaRepository;

    @Autowired
    private LogQueryService logQueryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SolicitudValidacionRepository solicitudValidacionRepository;

    /**
     * Servicio encargado de generar hashes HMAC-SHA-256
     * de los números de tarjeta.
     */
    @Autowired
    private TargetaEncryptionService targetaEncryptionService;

    /**
     * Obtiene todos los clientes activos con rol CLIENT.
     *
     * @return lista de clientes activos
     */
    public List<ClientJpaEntity> llistarTots() {
        try {
            return clientRepository.findByRolAndActivoTrue("CLIENT");
        } catch (RuntimeException e) {
            registrarError("llistarTots", e);
            throw e;
        }
    }

    /**
     * Convierte los valores nulos o vacíos en null.
     *
     * @param valor cadena que se desea limpiar
     * @return cadena limpia o null si está vacía
     */
    private String buitANull(String valor) {
        return valor == null || valor.trim().isEmpty()
                ? null
                : valor.trim();
    }

    /**
     * Busca clientes aplicando diferentes filtros combinables.
     *
     * @param nacionalitat nacionalidad del cliente
     * @param provincia    provincia del cliente
     * @param dni          documento nacional de identidad
     * @param cognoms      apellidos del cliente
     * @param telefon      teléfono del cliente
     * @param email        correo electrónico del cliente
     * @param poblacio    población del cliente
     * @return lista de clientes que cumplen los filtros
     */
    public List<ClientJpaEntity> cercarFiltres(
            String nacionalitat,
            String provincia,
            String dni,
            String cognoms,
            String telefon,
            String email,
            String poblacio) {

        try {
            return clientRepository.cercarClients(
                    buitANull(nacionalitat),
                    buitANull(provincia),
                    buitANull(dni),
                    buitANull(cognoms),
                    buitANull(telefon),
                    buitANull(email),
                    buitANull(poblacio),
                    "CLIENT");

        } catch (RuntimeException e) {
            registrarError("cercarFiltres", e);
            throw e;
        }
    }

    /**
     * Busca un cliente mediante su DNI.
     *
     * @param DNI documento nacional de identidad
     * @return Optional con el cliente encontrado, si existe
     */
    public Optional<ClientJpaEntity> findByDNI(String DNI) {
        try {
            return clientRepository.findByDNIAndRol(DNI, "CLIENT");
        } catch (RuntimeException e) {
            registrarError("findByDNI", e);
            throw e;
        }
    }

    /**
     * Busca un cliente mediante su correo electrónico.
     *
     * @param email correo electrónico del cliente
     * @return Optional con el cliente encontrado, si existe
     */
    public Optional<ClientJpaEntity> findByEmail(String email) {
        try {
            return clientRepository.findByEmail(buitANull(email));
        } catch (RuntimeException e) {
            registrarError("findByEmail", e);
            throw e;
        }
    }

    /**
     * Busca un cliente mediante su identificador.
     *
     * @param id identificador del cliente
     * @return Optional con el cliente encontrado, si existe
     */
    public Optional<ClientJpaEntity> findById(Long id) {
        try {
            return clientRepository.findById(id);
        } catch (RuntimeException e) {
            registrarError("findById", e);
            throw e;
        }
    }

    /**
     * Elimina definitivamente un cliente si no tiene reservas asociadas.
     *
     * @param id identificador del cliente
     * @return true si se elimina correctamente; false en caso contrario
     */
    @Transactional
    public boolean eliminarClient(Long id) {

        Optional<ClientJpaEntity> resultado = clientRepository.findById(id);

        if (resultado.isEmpty()) {
            return false;
        }

        // Comprobar si tiene alguna reserva, independientemente del estado
        if (reservaRepository.existsByClientId(id)) {
            return false;
        }

        try {
            // Borrar las solicitudes de validación
            solicitudValidacionRepository.deleteByClienteId(id);

            // Borrar el cliente
            clientRepository.delete(resultado.get());

            // Forzar la ejecución del DELETE dentro de este bloque
            clientRepository.flush();

            return true;

        } catch (RuntimeException e) {
            registrarError("eliminarClient", e);
            throw e;
        }
    }

    /**
     * Obtiene la imagen del DNI del cliente.
     *
     * @param dni documento nacional de identidad
     * @return Optional con el documento si existe
     */
    public Optional<DocumentClient> obtenirDni(String dni) {

        try {
            Optional<ClientJpaEntity> resultat = findByDNI(dni);

            if (resultat.isEmpty()) {
                return Optional.empty();
            }

            ClientJpaEntity client = resultat.get();

            if (client.getFotoDniAnverso() == null) {
                return Optional.empty();
            }

            return Optional.of(
                    new DocumentClient(
                            client.getFotoDniAnverso(),
                            client.getFotoDniAnversoTipus()));

        } catch (RuntimeException e) {
            registrarError("obtenirDni", e);
            throw e;
        }
    }

    /**
     * Obtiene la imagen del permiso de conducir del cliente.
     *
     * @param dni documento nacional de identidad
     * @return Optional con el documento si existe
     */
    public Optional<DocumentClient> obtenirLlicencia(String dni) {

        try {
            Optional<ClientJpaEntity> resultat = findByDNI(dni);

            if (resultat.isEmpty()) {
                return Optional.empty();
            }

            ClientJpaEntity client = resultat.get();

            if (client.getFotoPermiso() == null) {
                return Optional.empty();
            }

            return Optional.of(
                    new DocumentClient(
                            client.getFotoPermiso(),
                            client.getFotoPermisoTipus()));

        } catch (RuntimeException e) {
            registrarError("obtenirLlicencia", e);
            throw e;
        }
    }

    /**
     * Guarda los cambios de un cliente en la base de datos.
     *
     * @param client entidad del cliente que se desea guardar
     * @return entidad del cliente guardada
     */
    @Transactional
    public ClientJpaEntity guardarClient(ClientJpaEntity client) {

        try {
            ClientJpaEntity clientGuardat = clientRepository.save(client);

            logQueryService.registrarInfo(
                    "CLIENTS",
                    "Cliente actualizado correctamente. ID: "
                            + clientGuardat.getId());

            return clientGuardat;

        } catch (RuntimeException e) {
            registrarError("guardarClient", e);
            throw e;
        }
    }

    /**
     * Actualiza los datos personales de un cliente.
     *
     * @param dni         DNI del cliente
     * @param datos       nuevos datos personales
     * @param fotoDni     fotografía del DNI
     * @param fotoPermiso fotografía del permiso de conducir
     * @return cliente actualizado
     * @throws IOException si ocurre un error al leer las fotografías
     */
    @Transactional
    public ClientJpaEntity actualizarClientAdmin(
            String dni,
            ClientJpaEntity datos,
            MultipartFile fotoDni,
            MultipartFile fotoPermiso) throws IOException {

        ClientJpaEntity client = findByDNI(dni)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se ha encontrado el cliente."));

        try {
            client.setNom(datos.getNom());
            client.setCognoms(datos.getCognoms());
            client.setTelefon(datos.getTelefon());
            client.setEmail(datos.getEmail());
            client.setAdreca(datos.getAdreca());
            client.setNacionalitat(datos.getNacionalitat());
            client.setProvincia(datos.getProvincia());
            client.setPoblacio(datos.getPoblacio());
            client.setPremium(datos.isPremium());
            
            if (fotoDni != null && !fotoDni.isEmpty()) {
                validarImatge(fotoDni);
                client.setFotoDniAnverso(fotoDni.getBytes());
                client.setFotoDniAnversoTipus(fotoDni.getContentType());
            }

            if (fotoPermiso != null && !fotoPermiso.isEmpty()) {
                validarImatge(fotoPermiso);
                client.setFotoPermiso(fotoPermiso.getBytes());
                client.setFotoPermisoTipus(fotoPermiso.getContentType());
            }

            return guardarClient(client);

        } catch (IOException | RuntimeException e) {
            registrarError("actualizarClientAdmin", e);
            throw e;
        }
    }

    /**
     * Actualiza la fecha de caducidad y la fotografía del DNI.
     *
     * @param dni              DNI del cliente
     * @param documentacioForm formulario de documentación
     * @param fotoDni          nueva fotografía del DNI
     * @return cliente actualizado
     * @throws IOException si ocurre un error al leer la fotografía
     */
    @Transactional
    public ClientJpaEntity actualizarDocumentacio(
            String dni,
            DocumentacioForm documentacioForm,
            MultipartFile fotoDni) throws IOException {

        try {
            ClientJpaEntity client = findByDNI(dni)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se ha encontrado el cliente."));

            if (documentacioForm.getNumeroDocument() != null
                    && !dni.equalsIgnoreCase(
                            documentacioForm.getNumeroDocument().trim())) {

                throw new IllegalArgumentException(
                        "No está permitido modificar el DNI.");
            }

            LocalDate caducitatDni = documentacioForm.getDataCaducitat();

            if (caducitatDni == null) {
                throw new IllegalArgumentException(
                        "La fecha de caducidad del DNI es obligatoria.");
            }

            if (!caducitatDni.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "La fecha de caducidad del DNI debe ser futura.");
            }

            client.setCadDNI(caducitatDni.toString());

            if (fotoDni != null && !fotoDni.isEmpty()) {
                validarImatge(fotoDni);
                client.setFotoDniAnverso(fotoDni.getBytes());
                client.setFotoDniAnversoTipus(fotoDni.getContentType());
            }

            return guardarClient(client);

        } catch (IOException | RuntimeException e) {
            registrarError("actualizarDocumentacio", e);
            throw e;
        }
    }

    /**
     * Actualiza el tipo, la caducidad y la fotografía
     * del permiso de conducir.
     *
     * @param dni              DNI del cliente
     * @param documentacioForm formulario de licencia
     * @param fotoPermiso      nueva fotografía del permiso
     * @return cliente actualizado
     * @throws IOException si ocurre un error al leer la fotografía
     */
    @Transactional
    public ClientJpaEntity actualizarLlicencia(
            String dni,
            DocumentacioForm documentacioForm,
            MultipartFile fotoPermiso) throws IOException {

        try {
            ClientJpaEntity client = findByDNI(dni)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se ha encontrado el cliente."));

            String tipoLicencia = documentacioForm.getTipusLlicencia();

            if (tipoLicencia == null || tipoLicencia.isBlank()) {
                throw new IllegalArgumentException(
                        "El tipo de licencia es obligatorio.");
            }

            tipoLicencia = tipoLicencia.trim().toUpperCase();

            if (!tipoLicencia.matches(
                    "^(AM|A1|A2|A|B|BE|C1|C1E|C|CE|D1|D1E|D|DE)$")) {

                throw new IllegalArgumentException(
                        "El tipo de licencia seleccionado no es válido.");
            }

            LocalDate caducitatLlicencia = documentacioForm.getDataCaducitatLlicencia();

            if (caducitatLlicencia == null) {
                throw new IllegalArgumentException(
                        "La fecha de caducidad de la licencia es obligatoria.");
            }

            if (!caducitatLlicencia.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "La fecha de caducidad de la licencia debe ser futura.");
            }

            client.setLliconduir(tipoLicencia);
            client.setCdLLiconduir(caducitatLlicencia.toString());

            if (fotoPermiso != null && !fotoPermiso.isEmpty()) {
                validarImatge(fotoPermiso);
                client.setFotoPermiso(fotoPermiso.getBytes());
                client.setFotoPermisoTipus(fotoPermiso.getContentType());
            }

            return guardarClient(client);

        } catch (IOException | RuntimeException e) {
            registrarError("actualizarLlicencia", e);
            throw e;
        }
    }

    /**
     * Actualiza los datos de la tarjeta bancaria de un cliente.
     *
     * El número se valida mediante Luhn y se almacena como
     * hash HMAC-SHA-256, que no puede recuperarse al valor original.
     *
     * Si no se introduce un número nuevo, se conserva el hash anterior.
     *
     * @param dni       DNI del cliente
     * @param titular   nombre del titular
     * @param numero    número de tarjeta
     * @param caducitat fecha de caducidad MM/yy
     * @return cliente actualizado
     */
    @Transactional
    public ClientJpaEntity actualizarTargeta(
            String dni,
            String titular,
            String numero,
            String caducitat) {

        try {
            ClientJpaEntity client = findByDNI(dni)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Client no trobat."));

            String numeroLimpio = numero == null
                    ? ""
                    : numero.replaceAll("\\s+", "")
                            .replaceAll("[^0-9]", "");

            // Generar el hash solamente si se introduce una tarjeta nueva.
            if (!numeroLimpio.isBlank()) {

                if (!luhn(numeroLimpio)) {
                    throw new IllegalArgumentException(
                            "El número de targeta no és vàlid.");
                }

                String numeroHash = targetaEncryptionService.encriptar(numeroLimpio);

                client.setNumtarjetacredit(numeroHash);
            }

            if (caducitat == null
                    || !caducitat.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {

                throw new IllegalArgumentException(
                        "La caducitat ha de tenir el format MM/yy.");
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yy");

            YearMonth dataCaducitat;

            try {
                dataCaducitat = YearMonth.parse(caducitat, formatter);

            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "La data de caducitat no és vàlida.");
            }

            if (dataCaducitat.isBefore(YearMonth.now())) {
                throw new IllegalArgumentException(
                        "La targeta està caducada.");
            }

            client.setTitularTargeta(
                    titular == null ? null : titular.trim());

            client.setCaducitatTargeta(caducitat.trim());

            return guardarClient(client);

        } catch (RuntimeException e) {
            registrarError(
                    "Error actualitzant targeta del client " + dni, e);
            throw e;
        }
    }

    /**
     * Comprueba si un número de tarjeta coincide con el hash almacenado.
     *
     * @param dni    DNI del cliente
     * @param numero número de tarjeta introducido
     * @return true si el número coincide con el hash almacenado
     */
    public boolean verificarTargeta(String dni, String numero) {

        try {
            ClientJpaEntity client = findByDNI(dni)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se ha encontrado el cliente."));

            String numeroLimpio = numero == null
                    ? ""
                    : numero.replaceAll("\\s+", "")
                            .replaceAll("[^0-9]", "");

            if (numeroLimpio.isBlank()
                    || !luhn(numeroLimpio)) {
                return false;
            }

            return targetaEncryptionService.verificar(
                    numeroLimpio,
                    client.getNumtarjetacredit());

        } catch (RuntimeException e) {
            registrarError("verificarTargeta", e);
            throw e;
        }
    }

    /**
     * Cambia la contraseña de un cliente mediante BCrypt.
     *
     * @param dni         DNI del cliente
     * @param nova        nueva contraseña
     * @param confirmacio confirmación de la contraseña
     */
    @Transactional
    public void canviarContrasenya(
            String dni,
            String nova,
            String confirmacio) {

        try {
            if (nova == null
                    || !nova.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {

                throw new IllegalArgumentException(
                        "La contrasenya ha de tenir entre 8 y 72 caràcters, amb lletres i números.");
            }

            if (!nova.equals(confirmacio)) {
                throw new IllegalArgumentException(
                        "Las contraseñas no coinciden.");
            }

            ClientJpaEntity client = findByDNI(dni)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se ha encontrado el cliente."));

            client.setPassword(passwordEncoder.encode(nova));
            guardarClient(client);

        } catch (RuntimeException e) {
            registrarError("canviarContrasenya", e);
            throw e;
        }
    }

    /**
     * Valida un número de tarjeta mediante el algoritmo de Luhn.
     *
     * @param n número de tarjeta
     * @return true si es válido
     */
    private static boolean luhn(String n) {

        if (n == null || !n.matches("\\d{13,19}")) {
            return false;
        }

        int sum = 0;
        boolean alt = false;

        for (int i = n.length() - 1; i >= 0; i--) {
            int d = n.charAt(i) - '0';

            if (alt) {
                d *= 2;

                if (d > 9) {
                    d -= 9;
                }
            }

            sum += d;
            alt = !alt;
        }

        return sum % 10 == 0;
    }

    /**
     * Comprueba que el archivo recibido sea una imagen
     * y que su tamaño no supere los 5 MB.
     *
     * @param fitxer archivo recibido
     */
    private void validarImatge(MultipartFile fitxer) {

        if (fitxer.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "La imagen no puede superar los 5 MB.");
        }

        String tipus = fitxer.getContentType();

        if (tipus == null || !tipus.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "El archivo seleccionado debe ser una imagen.");
        }
    }

    /**
     * Registra un error relacionado con la gestión de clientes.
     *
     * @param operacio nombre de la operación
     * @param e        excepción producida
     */
    private void registrarError(String operacio, Exception e) {

        logQueryService.registrarError(
                "CLIENTS",
                "Error en " + operacio + ": " + e.getMessage());
    }
}
