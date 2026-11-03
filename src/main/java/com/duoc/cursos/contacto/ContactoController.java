package com.duoc.cursos.contacto;

import com.duoc.cursos.common.dto.ApiResponse;
import com.duoc.cursos.contacto.dto.ContactoRequest;
import com.duoc.cursos.contacto.model.MensajeContacto;
import com.duoc.cursos.contacto.repository.MensajeContactoRepository;
import com.duoc.cursos.notification.dto.NotificationRequest;
import com.duoc.cursos.notification.dto.NotificationResponse;
import com.duoc.cursos.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recibe los mensajes del formulario "Contáctanos".
 * Es público para que cualquiera pueda escribir sin estar registrado.
 * El mensaje se guarda en la base y además se intenta enviar por ntfy (si falla el
 * envío, el mensaje igual queda guardado).
 */
@RestController
@RequestMapping("/api/contacto")
@RequiredArgsConstructor
@Slf4j
public class ContactoController {

    private final MensajeContactoRepository mensajeContactoRepository;
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> enviarMensaje(@Valid @RequestBody ContactoRequest request) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(request.getNombre().trim());
        mensaje.setEmail(request.getEmail().trim());
        mensaje.setComentario(request.getComentario().trim());
        mensajeContactoRepository.save(mensaje);

        try {
            NotificationRequest notificacion = new NotificationRequest();
            notificacion.setEmail(mensaje.getEmail());
            notificacion.setTitle("Nuevo mensaje de contacto: " + mensaje.getNombre());
            notificacion.setMessage(mensaje.getComentario());
            notificacion.setTags("store");
            NotificationResponse response = notificationService.sendNotification(notificacion);
            if (!response.isSuccess()) {
                log.warn("Mensaje de contacto guardado, pero no se pudo notificar por ntfy: {}", response.getMessage());
            }
        } catch (Exception e) {
            log.warn("No se pudo enviar la notificación de contacto", e);
        }

        return ResponseEntity.ok(ApiResponse.ok(null, "¡Gracias! Tu mensaje fue enviado."));
    }
}
