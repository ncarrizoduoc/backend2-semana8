package com.minimarket.minimarket.security.monitor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SuspiciousActivityService {
    private static final Logger log = LoggerFactory.getLogger(SuspiciousActivityService.class);

    private final ConcurrentHashMap<String, List<Long>> failedLoginTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Long>> requestTimestampsByIp = new ConcurrentHashMap<>();
    private final int FAILED_LOGIN_THRESHOLD = 5;
    private final int REQUEST_THRESHOLD = 200; // requests
    private final long WINDOW_MS = 15 * 60 * 1000L; // 15 minutos

    // Obtiene la dirección IP real del cliente, considerando primero el encabezado X-Forwarded-For.
    // Si el encabezado no existe o viene vacío, toma la IP remota entregada por la petición HTTP.
    // Este método sirve como apoyo para identificar correctamente el origen de una solicitud.
    // Su responsabilidad es abstraer la lógica de extracción de IP para reutilizarla en varios registros.
    // También ayuda a que el monitoreo sea más preciso en entornos con proxies o balanceadores.
    private String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return xf != null && !xf.isBlank() ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }

    // Elimina de la lista los registros antiguos que ya están fuera de la ventana de tiempo definida.
    // Calcula un corte temporal para conservar sólo los eventos recientes dentro del período monitoreado.
    // Luego filtra los timestamps que quedaron desactualizados para mantener la colección acotada.
    // Su responsabilidad es evitar que el historial crezca sin control y que los conteos se vuelvan incorrectos.
    // Con esto, los análisis de actividad sospechosa se basan sólo en eventos válidos y actuales.
    private void pruneOld(List<Long> list) {
        long cutoff = Instant.now().toEpochMilli() - WINDOW_MS;
        list.removeIf(t -> t < cutoff);
    }

    // Registra intentos fallidos de inicio de sesión usando una clave que combina usuario e IP.
    // Guarda el momento del evento, limpia los registros antiguos y calcula cuántos fallos hay en la ventana activa.
    // Además, genera advertencias en el log cuando detecta un intento fallido para facilitar el seguimiento.
    // Su responsabilidad es apoyar la detección de comportamientos anómalos relacionados con autenticación.
    // Cuando el conteo supera el umbral, deja trazabilidad de un posible ataque de fuerza bruta o abuso de credenciales.
    public void recordFailedLogin(HttpServletRequest req, String username) {
        String key = "FAILED_LOGIN:" + (username == null ? clientIp(req) : username + "@" + clientIp(req));
        List<Long> list = failedLoginTimestamps.computeIfAbsent(key, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            log.warn("SuspiciousActivity: failed login (user={}, ip={}, count={})", username, clientIp(req), count);
            if (count >= FAILED_LOGIN_THRESHOLD) {
                log.warn("SuspiciousActivity: threshold reached for failed logins (user/ip={}): {}", key, count);
            }
        }
    }

    // Registra un JWT inválido o expirado junto con la IP, la ruta solicitada y la causa reportada.
    // No intenta validar el token, sino dejar evidencia del evento para análisis posterior y auditoría.
    // También centraliza el registro de fallos de seguridad vinculados a autenticación con tokens.
    // Su responsabilidad es facilitar el monitoreo de accesos defectuosos o intentos de uso de credenciales inválidas.
    // De esta forma, se puede identificar patrones de error o actividad sospechosa sobre recursos protegidos.
    public void recordInvalidJwt(HttpServletRequest req, String token, Exception ex) {
        String ip = clientIp(req);
        log.warn("SuspiciousActivity: invalid JWT from ip={} path={} reason={}", ip, req.getRequestURI(),
                ex == null ? "invalid/expired" : ex.getMessage());
    }

    // Registra la frecuencia de solicitudes realizadas por una misma IP dentro de la ventana configurada.
    // Mantiene un historial reciente de accesos para contar cuántas peticiones llegan desde el mismo origen.
    // También escribe logs periódicos para dar visibilidad del ritmo de solicitudes sin saturar el archivo de logs.
    // Su responsabilidad es ayudar a identificar patrones de exceso de tráfico o posible abuso del servicio.
    // Cuando el conteo supera el límite configurado, deja una advertencia sobre una tasa de solicitudes elevada.
    public void recordRequest(HttpServletRequest req) {
        String ip = clientIp(req);
        List<Long> list = requestTimestampsByIp.computeIfAbsent(ip, k -> new ArrayList<>());
        synchronized (list) {
            list.add(Instant.now().toEpochMilli());
            pruneOld(list);
            int count = list.size();
            if (count % 50 == 0) { // log periodicamente para no spamear
                log.info("SuspiciousActivity: request rate ip={} count_last_15m={}", ip, count);
            }
            if (count >= REQUEST_THRESHOLD) {
                log.warn("SuspiciousActivity: high request rate detected ip={} count_last_15m={}", ip, count);
            }
        }
    }

    // Registra un intento de acceso no autorizado a un recurso, dejando evidencia de la IP y de la ruta consultada.
    // Recibe además el nombre lógico del recurso para que el log tenga un contexto más claro y útil.
    // Esta información permite revisar intentos de ingreso fuera de los permisos asignados.
    // Su responsabilidad es apoyar el rastreo de accesos bloqueados o solicitudes que violan las reglas de seguridad.
    // Con esto se obtiene trazabilidad para auditoría y diagnóstico de incidentes.
    public void recordUnauthorizedAccess(HttpServletRequest req, String resource) {
        String ip = clientIp(req);
        log.warn("SuspiciousActivity: unauthorized access attempt ip={} path={} resource={}", ip, req.getRequestURI(),
                resource);
    }

    // Registra operaciones CRUD ejecutadas por una solicitud, incorporando operación, recurso, IP y ruta.
    // No bloquea ni modifica la ejecución, sino que deja un rastro informativo del uso normal del sistema.
    // Esto permite revisar qué acciones se realizaron y desde dónde se originaron.
    // Su responsabilidad es aportar trazabilidad sobre operaciones sobre datos o recursos del backend.
    // También resulta útil para auditoría funcional y seguimiento de cambios realizados por los usuarios.
    public void recordCrudOperation(HttpServletRequest req, String operation, String resource) {
        String ip = clientIp(req);
        log.info("SuspiciousActivity: CRUD op={} resource={} by ip={} path={}", operation, resource, ip,
                req.getRequestURI());
    }
}
