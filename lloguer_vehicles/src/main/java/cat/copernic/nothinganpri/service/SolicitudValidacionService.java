package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.SolicitudValidacionJpaEntity;
import cat.copernic.nothinganpri.repository.ClientRepository;
import cat.copernic.nothinganpri.repository.SolicitudValidacionRepository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import cat.copernic.nothinganpri.dto.CanviSolicitud;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SolicitudValidacionService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String APROBADA = "APROBADA";
    private static final String RECHAZADA = "RECHAZADA";

    private static final String DATOS_PERSONALES = "DATOS_PERSONALES";
    private static final String DOCUMENTACION = "DOCUMENTACION";
    private static final String TARJETA = "TARJETA";
    private static final String REGISTRO = "REGISTRO";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final SolicitudValidacionRepository solicitudRepository;
    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    public SolicitudValidacionService(
            SolicitudValidacionRepository solicitudRepository,
            ClientRepository clientRepository,
            ObjectMapper objectMapper) {

        this.solicitudRepository = solicitudRepository;
        this.clientRepository = clientRepository;
        this.objectMapper = objectMapper;
    }

    // =====================================================
    // CONSULTAS
    // =====================================================

    public List<SolicitudValidacionJpaEntity> obtenerPendientes() {
        return solicitudRepository.findByEstadoOrderByDataSolicitudAsc(
                PENDIENTE);
    }

    public List<SolicitudValidacionJpaEntity> obtenerSolicitudesCliente(
            Long clienteId) {

        return clientRepository.findById(clienteId)
                .map(solicitudRepository::findByClienteOrderByDataSolicitudDesc)
                .orElseThrow(() ->
                        new IllegalArgumentException("Cliente no encontrado."));
    }

    public SolicitudValidacionJpaEntity obtenerRegistroPendiente(
            Long clienteId) {

        return obtenerSolicitudesCliente(clienteId)
                .stream()
                .filter(s -> REGISTRO.equals(s.getTipo()))
                .filter(s -> PENDIENTE.equals(s.getEstado()))
                .findFirst()
                .orElse(null);
    }

    public List<SolicitudValidacionJpaEntity> obtenerRegistrosRechazados() {
        return solicitudRepository.findByTipoAndEstado(
                REGISTRO, RECHAZADA);
    }

    public List<SolicitudValidacionJpaEntity> obtenerPorTipoYEstado(
            String tipo, String estado) {

        return solicitudRepository.findByTipoAndEstado(tipo, estado);
    }

    // =====================================================
    // DATOS PERSONALES
    // =====================================================

    @Transactional
    public void solicitarDatosPersonales(
            ClientJpaEntity cliente,
            String nom,
            String cognoms,
            String telefon,
            String nacionalitat,
            String adreca,
            String provincia) {

        Map<String, Object> datos = new LinkedHashMap<>();

        // Només enviem a l'agent els camps que realment canvien.
        agregarSiCambia(datos, "nom", nom, cliente.getNom());
        agregarSiCambia(datos, "cognoms", cognoms, cliente.getCognoms());
        agregarSiCambia(datos, "telefon", telefon, cliente.getTelefon());
        agregarSiCambia(datos, "nacionalitat", nacionalitat,
                cliente.getNacionalitat());
        agregarSiCambia(datos, "adreca", adreca, cliente.getAdreca());
        agregarSiCambia(datos, "provincia", provincia, cliente.getProvincia());

        if (datos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No has modificat cap dada.");
        }

        crearSolicitudDesdeMapa(cliente, DATOS_PERSONALES, datos);
    }

    private void agregarSiCambia(
            Map<String, Object> datos,
            String clave,
            String valor,
            String actual) {

        if (valor == null || valor.trim().isEmpty()) {
            return;
        }

        if (actual != null && actual.trim().equals(valor.trim())) {
            return;
        }

        datos.put(clave, valor.trim());
    }

    // =====================================================
    // DOCUMENTACIÓN
    // =====================================================

    @Transactional
    public boolean procesarDocumentacion(
            ClientJpaEntity cliente,
            String numeroDocument,
            String dataCaducitat,
            String tipusLlicencia,
            String dataCaducitatLlicencia,
            MultipartFile documentIdentitat,
            MultipartFile llicenciaConduir) throws IOException {

        if (cliente == null) {
            throw new IllegalArgumentException("Client no vàlid.");
        }

        validarTexto(numeroDocument,
                "El número del document és obligatori.");
        validarTexto(dataCaducitat,
                "La caducitat del DNI és obligatòria.");
        validarTexto(tipusLlicencia,
                "El permís de conduir és obligatori.");
        validarTexto(dataCaducitatLlicencia,
                "La caducitat del permís és obligatòria.");

        validarFitxer(documentIdentitat);
        validarFitxer(llicenciaConduir);

        boolean primeraCarga = esPrimeraCargaDocumentacion(cliente);

        /*
         * Si ja hi ha una sol·licitud pendent, no permetem
         * guardar directament encara que sembli una primera càrrega.
         */
        comprobarSolicitudPendiente(cliente, DOCUMENTACION);

        // PRIMERA CÀRREGA: guardar directament.
        if (primeraCarga) {

            if (documentIdentitat == null || documentIdentitat.isEmpty()
                    || llicenciaConduir == null || llicenciaConduir.isEmpty()) {

                throw new IllegalArgumentException(
                        "La primera vegada has d'adjuntar els dos documents.");
            }

            cliente.setDNI(numeroDocument.trim());
            cliente.setCadDNI(dataCaducitat.trim());
            cliente.setLliconduir(tipusLlicencia.trim());
            cliente.setCdLLiconduir(dataCaducitatLlicencia.trim());

            cliente.setFotoDniAnverso(documentIdentitat.getBytes());
            cliente.setFotoDniAnversoTipus(
                    documentIdentitat.getContentType());

            cliente.setFotoPermiso(llicenciaConduir.getBytes());
            cliente.setFotoPermisoTipus(
                    llicenciaConduir.getContentType());

            clientRepository.save(cliente);

            return true;
        }

        // MODIFICACIONS POSTERIORS: crear sol·licitud.
        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("numeroDocument", numeroDocument.trim());
        datos.put("dataCaducitat", dataCaducitat.trim());
        datos.put("tipusLlicencia", tipusLlicencia.trim());
        datos.put("dataCaducitatLlicencia",
                dataCaducitatLlicencia.trim());

        if (documentIdentitat != null && !documentIdentitat.isEmpty()) {

            datos.put("documentIdentitat",
                    Base64.getEncoder().encodeToString(
                            documentIdentitat.getBytes()));

            datos.put("documentIdentitatTipus",
                    documentIdentitat.getContentType());
        }

        if (llicenciaConduir != null && !llicenciaConduir.isEmpty()) {

            datos.put("llicenciaConduir",
                    Base64.getEncoder().encodeToString(
                            llicenciaConduir.getBytes()));

            datos.put("llicenciaConduirTipus",
                    llicenciaConduir.getContentType());
        }

        crearSolicitudDesdeMapa(cliente, DOCUMENTACION, datos);

        return false;
    }

    /**
     * Primera càrrega = encara no hi ha fotos.
     * El DNI/text pot venir del registre; el que importa són les imatges.
     */
    private boolean esPrimeraCargaDocumentacion(ClientJpaEntity cliente) {
        return cliente.getFotoDniAnverso() == null
                && cliente.getFotoPermiso() == null;
    }

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void validarFitxer(MultipartFile fitxer) {

        if (fitxer == null || fitxer.isEmpty()) {
            return;
        }

        if (fitxer.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Cada fitxer pot tenir com a màxim 5 MB.");
        }

        String tipus = fitxer.getContentType();

        if (!"application/pdf".equals(tipus)
                && !"image/jpeg".equals(tipus)
                && !"image/png".equals(tipus)) {

            throw new IllegalArgumentException(
                    "Només s'accepten fitxers PDF, JPG o PNG.");
        }
    }

    private void validarTexto(String valor, String mensaje) {

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    // =====================================================
    // TARJETA SIMULADA
    // =====================================================

    @Transactional
    public boolean procesarTarjeta(
            ClientJpaEntity cliente,
            String titular,
            String numeroTarjeta,
            String caducidad) {

        if (cliente == null) {
            throw new IllegalArgumentException("Client no vàlid.");
        }

        validarTexto(titular, "El titular és obligatori.");
        validarTexto(numeroTarjeta, "El número de targeta és obligatori.");
        validarTexto(caducidad, "La caducitat és obligatòria.");

        String numeroNet = numeroTarjeta.replaceAll("\\s+", "");

        if (!numeroNet.matches("\\d{12,19}")) {
            throw new IllegalArgumentException(
                    "El número de targeta no és vàlid.");
        }

        String ultimsDigits =
                numeroNet.substring(numeroNet.length() - 4);

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("titular", titular.trim());
        datos.put("ultimsDigits", ultimsDigits);
        datos.put("caducitat", caducidad.trim());

        boolean primeraTarjeta =
                cliente.getNumtarjetacredit() == null
                || cliente.getNumtarjetacredit().trim().isEmpty();

        /*
         * Evitem que una primera configuració directa
         * salti una sol·licitud de targeta que ja estigui pendent.
         */
        comprobarSolicitudPendiente(cliente, TARJETA);

        if (primeraTarjeta) {

            cliente.setTitularTargeta(titular.trim());
            cliente.setNumtarjetacredit("****" + ultimsDigits);
            cliente.setCaducitatTargeta(caducidad.trim());

            clientRepository.save(cliente);

            return true;
        }

        crearSolicitudDesdeMapa(cliente, TARJETA, datos);

        return false;
    }

    // =====================================================
    // CREAR SOLICITUD DESDE JSON
    // =====================================================

    @Transactional
    public void crearSolicitud(
            ClientJpaEntity cliente,
            String tipo,
            String datosPropuestos) {

        try {

            Map<String, Object> datos = objectMapper.readValue(
                    datosPropuestos,
                    new TypeReference<Map<String, Object>>() {});

            crearSolicitudDesdeMapa(cliente, tipo, datos);

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "No s'han pogut llegir les dades de la sol·licitud.", e);
        }
    }

    // =====================================================
    // COMPROBAR SOLICITUD PENDIENTE
    // =====================================================

    private void comprobarSolicitudPendiente(
            ClientJpaEntity cliente,
            String tipo) {

        boolean existe = solicitudRepository
                .existsByClienteAndTipoAndEstado(
                        cliente, tipo, PENDIENTE);

        if (existe) {
            throw new IllegalStateException(
                    "Ja tens una sol·licitud pendent d'aquest tipus.");
        }
    }

    // =====================================================
    // CREAR SOLICITUD DESDE MAPA
    // =====================================================

    @Transactional
    public void crearSolicitudDesdeMapa(
            ClientJpaEntity cliente,
            String tipo,
            Map<String, Object> datos) {

        if (cliente == null || tipo == null || datos == null) {
            throw new IllegalArgumentException(
                    "Les dades de la sol·licitud no són vàlides.");
        }

        if (datos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No has modificat cap dada.");
        }

        if (TARJETA.equals(tipo)) {
            validarDatosTarjeta(datos);
        }

        try {

            // Si ja hi ha una sol·licitud pendent del mateix tipus,
            // actualitzem les dades proposades en lloc de bloquejar.
            SolicitudValidacionJpaEntity existente =
                    solicitudRepository
                            .findByClienteAndTipoAndEstado(
                                    cliente, tipo, PENDIENTE)
                            .stream()
                            .findFirst()
                            .orElse(null);

            if (existente != null) {
                // Fusionar dades: les noves sobreescriuen les anteriors
                Map<String, Object> anteriores = objectMapper.readValue(
                        existente.getDatosPropuestos() != null
                                ? existente.getDatosPropuestos()
                                : "{}",
                        new TypeReference<Map<String, Object>>() {});
                anteriores.putAll(datos);
                existente.setDatosPropuestos(
                        objectMapper.writeValueAsString(anteriores));
                existente.setDataSolicitud(LocalDateTime.now());
                solicitudRepository.save(existente);
                return;
            }

            SolicitudValidacionJpaEntity solicitud =
                    new SolicitudValidacionJpaEntity();

            solicitud.setCliente(cliente);
            solicitud.setTipo(tipo);
            solicitud.setEstado(PENDIENTE);
            solicitud.setDatosPropuestos(
                    objectMapper.writeValueAsString(datos));
            solicitud.setDataSolicitud(LocalDateTime.now());

            solicitudRepository.save(solicitud);

        } catch (IOException e) {

            throw new IllegalArgumentException(
                    "No s'ha pogut preparar la sol·licitud.", e);
        }
    }

    private void validarDatosTarjeta(Map<String, Object> datos) {

        for (String clave : datos.keySet()) {

            String claveNormalizada = clave.toLowerCase();

            if (claveNormalizada.contains("cvv")
                    || claveNormalizada.contains("cvc")
                    || claveNormalizada.contains("pan")
                    || claveNormalizada.contains("numerotarjeta")
                    || claveNormalizada.contains("numero_tarjeta")
                    || claveNormalizada.contains("codigoseguridad")) {

                throw new IllegalArgumentException(
                        "No es permet guardar el número complet ni el CVV.");
            }
        }

        Object ultimos = datos.get("ultimsDigits");

        if (!(ultimos instanceof String)
                || !((String) ultimos).matches("\\d{4}")) {

            throw new IllegalArgumentException(
                    "Només es poden guardar els quatre últims dígits.");
        }
    }

    // =====================================================
    // APROBAR REGISTRO
    // =====================================================

    @Transactional
    public void aprobarRegistro(Long solicitudId, String revisorEmail) {

        SolicitudValidacionJpaEntity solicitud =
                obtenerSolicitudPendiente(solicitudId);

        if (!REGISTRO.equals(solicitud.getTipo())) {
            throw new IllegalArgumentException(
                    "Aquesta sol·licitud no és un registre inicial.");
        }

        solicitud.getCliente().setActivo(true);

        clientRepository.save(solicitud.getCliente());

        marcarRevisada(solicitud, APROBADA, revisorEmail);
    }

    // =====================================================
    // APROBAR CAMBIOS
    // =====================================================

    @Transactional
    public void aprobarSolicitud(
            Long solicitudId,
            String revisorEmail) throws IOException {

        SolicitudValidacionJpaEntity solicitud =
                obtenerSolicitudPendiente(solicitudId);

        ClientJpaEntity cliente = solicitud.getCliente();

        Map<String, Object> datos = objectMapper.readValue(
                solicitud.getDatosPropuestos(),
                new TypeReference<Map<String, Object>>() {});

        switch (solicitud.getTipo()) {

            case DATOS_PERSONALES:

                aplicarTexto(datos, "nom", cliente::setNom);
                aplicarTexto(datos, "cognoms", cliente::setCognoms);
                aplicarTexto(datos, "telefon", cliente::setTelefon);
                aplicarTexto(datos, "nacionalitat",
                        cliente::setNacionalitat);
                aplicarTexto(datos, "adreca", cliente::setAdreca);
                aplicarTexto(datos, "provincia", cliente::setProvincia);

                break;

            case DOCUMENTACION:

                aplicarTexto(datos, "numeroDocument", cliente::setDNI);
                aplicarTexto(datos, "dataCaducitat", cliente::setCadDNI);
                aplicarTexto(datos, "tipusLlicencia",
                        cliente::setLliconduir);
                aplicarTexto(datos, "dataCaducitatLlicencia",
                        cliente::setCdLLiconduir);

                if (datos.containsKey("documentIdentitat")) {

                    cliente.setFotoDniAnverso(
                            Base64.getDecoder().decode(
                                    (String) datos.get("documentIdentitat")));

                    cliente.setFotoDniAnversoTipus(
                            (String) datos.get("documentIdentitatTipus"));
                }

                if (datos.containsKey("llicenciaConduir")) {

                    cliente.setFotoPermiso(
                            Base64.getDecoder().decode(
                                    (String) datos.get("llicenciaConduir")));

                    cliente.setFotoPermisoTipus(
                            (String) datos.get("llicenciaConduirTipus"));
                }

                break;

            case TARJETA:

                validarDatosTarjeta(datos);

                aplicarTexto(datos, "titular",
                        cliente::setTitularTargeta);

                String ultims = (String) datos.get("ultimsDigits");

                cliente.setNumtarjetacredit("****" + ultims);

                aplicarTexto(datos, "caducitat",
                        cliente::setCaducitatTargeta);

                break;

            default:

                throw new IllegalArgumentException(
                        "Tipus de sol·licitud no compatible.");
        }

        clientRepository.save(cliente);

        marcarRevisada(solicitud, APROBADA, revisorEmail);
    }

    private void aplicarTexto(
            Map<String, Object> datos,
            String clave,
            Consumer<String> setter) {

        Object valor = datos.get(clave);

        if (valor instanceof String && !((String) valor).isBlank()) {
            setter.accept((String) valor);
        }
    }

    // =====================================================
    // RECHAZAR SOLICITUD
    // =====================================================

    @Transactional
    public void rechazarSolicitud(
            Long solicitudId,
            String revisorEmail,
            String motivo) {

        if (motivo == null || motivo.trim().length() < 3
                || motivo.trim().length() > 1000) {

            throw new IllegalArgumentException(
                    "El motiu del rebuig ha de tenir entre 3 i 1000 caràcters.");
        }

        SolicitudValidacionJpaEntity solicitud =
                obtenerSolicitudPendiente(solicitudId);

        solicitud.setMotiuRebuig(motivo.trim());

        marcarRevisada(solicitud, RECHAZADA, revisorEmail);
    }

    private void marcarRevisada(
            SolicitudValidacionJpaEntity solicitud,
            String estado,
            String revisorEmail) {

        solicitud.setEstado(estado);
        solicitud.setDataRevisio(LocalDateTime.now());
        solicitud.setRevisorEmail(revisorEmail);

        solicitudRepository.save(solicitud);
    }

    private SolicitudValidacionJpaEntity obtenerSolicitudPendiente(
            Long solicitudId) {

        SolicitudValidacionJpaEntity solicitud =
                solicitudRepository.findById(solicitudId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Sol·licitud no trobada."));

        if (!PENDIENTE.equals(solicitud.getEstado())) {

            throw new IllegalStateException(
                    "Aquesta sol·licitud ja ha estat revisada.");
        }

        return solicitud;
    }

    // =====================================================
    // INFORMACIÓN LEGIBLE PARA EL AGENTE
    // =====================================================

    public Map<String, String> obtenerDatosLegibles(
            String datosPropuestos) {

        Map<String, String> resultado = new LinkedHashMap<>();

        if (datosPropuestos == null || datosPropuestos.isBlank()) {
            return resultado;
        }

        try {

            Map<String, Object> datos = objectMapper.readValue(
                    datosPropuestos,
                    new TypeReference<Map<String, Object>>() {});

            if (datos == null || datos.isEmpty()) {
                return resultado;
            }

            for (Map.Entry<String, Object> entrada : datos.entrySet()) {

                String clave = entrada.getKey();
                Object valor = entrada.getValue();

                if (valor == null) {
                    continue;
                }

                String etiqueta;

                switch (clave) {

                    case "nom":
                        etiqueta = "Nombre";
                        break;

                    case "cognoms":
                        etiqueta = "Apellidos";
                        break;

                    case "telefon":
                        etiqueta = "Teléfono";
                        break;

                    case "nacionalitat":
                        etiqueta = "Nacionalidad";
                        break;

                    case "adreca":
                        etiqueta = "Dirección";
                        break;

                    case "provincia":
                        etiqueta = "Província";
                        break;

                    case "numeroDocument":
                    case "DNI":
                        etiqueta = "DNI";
                        break;

                    case "dataCaducitat":
                    case "cadDNI":
                        etiqueta = "Caducidad DNI";
                        break;

                    case "tipusLlicencia":
                    case "Lliconduir":
                        etiqueta = "Licencia de conducir";
                        break;

                    case "dataCaducitatLlicencia":
                    case "cdLLiconduir":
                        etiqueta = "Caducidad de licencia";
                        break;

                    case "titular":
                        etiqueta = "Titular de tarjeta";
                        break;

                    case "ultimsDigits":
                        etiqueta = "Últimos cuatro dígitos";
                        valor = "****" + valor;
                        break;

                    case "caducitat":
                        etiqueta = "Caducidad de tarjeta";
                        break;

                    case "documentIdentitat":
                    case "llicenciaConduir":
                        etiqueta = "Documento adjunto";
                        valor = "Archivo adjuntado";
                        break;

                    case "documentIdentitatTipus":
                    case "llicenciaConduirTipus":
                        continue;

                    default:
                        etiqueta = clave;
                }

                resultado.put(etiqueta, String.valueOf(valor));
            }

        } catch (IOException e) {

            System.err.println(
                    "Error leyendo JSON de solicitud: " + e.getMessage());
        }

        return resultado;
    }

    // =====================================================
    // COMPARATIVA ACTUAL -> PROPUESTO PARA EL AGENTE
    // =====================================================

    /**
     * Devuelve, para cada dato propuesto, el valor actual del cliente
     * y el valor nuevo. Solo incluye los campos que realmente cambian.
     */
    public List<CanviSolicitud> obtenerComparativa(
            SolicitudValidacionJpaEntity solicitud) {

        List<CanviSolicitud> canvis = new ArrayList<>();

        if (solicitud == null
                || solicitud.getDatosPropuestos() == null
                || solicitud.getDatosPropuestos().isBlank()) {
            return canvis;
        }

        ClientJpaEntity c = solicitud.getCliente();

        Map<String, Object> datos;

        try {
            datos = objectMapper.readValue(
                    solicitud.getDatosPropuestos(),
                    new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            return canvis;
        }

        if (c == null || datos == null) {
            return canvis;
        }

        String tipo = solicitud.getTipo();

        if (DATOS_PERSONALES.equals(tipo)) {

            afegirText(canvis, datos, "nom", "Nom", c.getNom());
            afegirText(canvis, datos, "cognoms", "Cognoms", c.getCognoms());
            afegirText(canvis, datos, "telefon", "Telèfon", c.getTelefon());
            afegirText(canvis, datos, "nacionalitat", "Nacionalitat",
                    c.getNacionalitat());
            afegirText(canvis, datos, "adreca", "Adreça", c.getAdreca());
            afegirText(canvis, datos, "provincia", "Província", c.getProvincia());

        } else if (DOCUMENTACION.equals(tipo)) {

            afegirText(canvis, datos, "numeroDocument", "DNI", c.getDNI());
            afegirText(canvis, datos, "dataCaducitat", "Caducitat DNI",
                    c.getCadDNI());
            afegirText(canvis, datos, "tipusLlicencia", "Llicència de conduir",
                    c.getLliconduir());
            afegirText(canvis, datos, "dataCaducitatLlicencia",
                    "Caducitat llicència", c.getCdLLiconduir());

            if (datos.get("documentIdentitat") != null) {
                canvis.add(new CanviSolicitud("Imatge del DNI",
                        c.getFotoDniAnverso() != null
                                ? "Document actual" : "—",
                        "Nou fitxer adjunt"));
            }

            if (datos.get("llicenciaConduir") != null) {
                canvis.add(new CanviSolicitud("Imatge de la llicència",
                        c.getFotoPermiso() != null
                                ? "Document actual" : "—",
                        "Nou fitxer adjunt"));
            }

        } else if (TARJETA.equals(tipo)) {

            afegirText(canvis, datos, "titular", "Titular de la targeta",
                    c.getTitularTargeta());

            Object ultims = datos.get("ultimsDigits");

            if (ultims != null) {
                String nou = "****" + ultims;

                if (!nou.equals(c.getNumtarjetacredit())) {
                    canvis.add(new CanviSolicitud("Targeta",
                            valorOBuit(c.getNumtarjetacredit()), nou));
                }
            }

            afegirText(canvis, datos, "caducitat", "Caducitat targeta",
                    c.getCaducitatTargeta());
        }

        return canvis;
    }

    private void afegirText(
            List<CanviSolicitud> canvis,
            Map<String, Object> datos,
            String clave,
            String etiqueta,
            String actual) {

        Object valor = datos.get(clave);

        if (!(valor instanceof String) || ((String) valor).isBlank()) {
            return;
        }

        String proposat = ((String) valor).trim();

        if (actual != null && actual.trim().equals(proposat)) {
            return; // sense canvi real
        }

        canvis.add(new CanviSolicitud(
                etiqueta, valorOBuit(actual), proposat));
    }

    private String valorOBuit(String valor) {
        return valor == null || valor.isBlank() ? "—" : valor;
    }

    // =====================================================
    // OBTENER DOCUMENTOS PROPUESTOS
    // =====================================================

    public String obtenerFotoDni(String datosPropuestos) {
        return obtenerFoto(datosPropuestos, "documentIdentitat");
    }

    public String obtenerFotoPermiso(String datosPropuestos) {
        return obtenerFoto(datosPropuestos, "llicenciaConduir");
    }

    private String obtenerFoto(
            String datosPropuestos,
            String campo) {

        if (datosPropuestos == null || datosPropuestos.isBlank()) {
            return null;
        }

        try {

            Map<String, Object> datos = objectMapper.readValue(
                    datosPropuestos,
                    new TypeReference<Map<String, Object>>() {});

            String imagen = (String) datos.get(campo);
            String tipo = (String) datos.get(campo + "Tipus");

            if (imagen == null || imagen.isBlank()) {
                return null;
            }

            if (tipo == null || tipo.isBlank()) {
                tipo = "image/jpeg";
            }

            if (!"image/jpeg".equals(tipo)
                    && !"image/png".equals(tipo)
                    && !"application/pdf".equals(tipo)) {
                return null;
            }

            return "data:" + tipo + ";base64," + imagen;

        } catch (IOException | ClassCastException e) {

            return null;
        }
    }
}
