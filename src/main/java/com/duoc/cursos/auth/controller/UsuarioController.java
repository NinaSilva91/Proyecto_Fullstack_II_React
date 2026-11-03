package com.duoc.cursos.auth.controller;

import com.duoc.cursos.auth.dto.ActualizarDireccionRequest;
import com.duoc.cursos.auth.dto.CambiarContrasenaRequest;
import com.duoc.cursos.auth.dto.ConfirmarCambioCorreoRequest;
import com.duoc.cursos.auth.dto.SolicitarCambioCorreoRequest;
import com.duoc.cursos.auth.dto.UsuarioResponse;
import com.duoc.cursos.auth.model.Usuario;
import com.duoc.cursos.auth.repository.UsuarioRepository;
import com.duoc.cursos.auth.service.AuthService;
import com.duoc.cursos.common.dto.ApiResponse;
import com.duoc.cursos.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Apartado "Mi cuenta" del usuario con sesión iniciada. El id sale del token de
 * Cognito (jwt.sub), nunca del cuerpo de la petición, así nadie edita datos ajenos.
 * El accessToken para Cognito sale del header Authorization (igual que en logout).
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    @GetMapping("/mi")
    public ResponseEntity<ApiResponse<UsuarioResponse>> miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ApiResponse.ok(aResponse(obtener(jwt))));
    }

    /**
     * Cambio de ubicación (dirección + región + comuna): se guarda en la base del
     * backend (no es atributo de Cognito). El frontend hace la "confirmación previa"
     * (paso extra en pantalla) antes de llamar acá.
     */
    @PatchMapping("/direccion")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarDireccion(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ActualizarDireccionRequest req) {
        Usuario usuario = obtener(jwt);
        usuario.setDireccion(req.getDireccion().trim());
        if (req.getRegion() != null && !req.getRegion().isBlank()) {
            usuario.setRegion(req.getRegion().trim());
        }
        if (req.getComuna() != null && !req.getComuna().isBlank()) {
            usuario.setComuna(req.getComuna().trim());
        }
        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(ApiResponse.ok(aResponse(guardado), "Dirección actualizada exitosamente"));
    }

    /** Cambio de contraseña con las validaciones de Cognito (contraseña actual + política nueva) */
    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody CambiarContrasenaRequest req) {
        authService.cambiarContrasena(extraerAccessToken(bearerToken), req.getContrasenaActual(), req.getNuevaContrasena());
        return ResponseEntity.ok(ApiResponse.ok(null, "Contraseña actualizada exitosamente"));
    }

    /**
     * Cambio de correo - PASO 1: Cognito envía un código de confirmación al correo
     * nuevo (si el User Pool lo permite). El correo anterior sigue funcionando.
     */
    @PostMapping("/cambiar-correo/solicitar")
    public ResponseEntity<ApiResponse<Void>> solicitarCambioCorreo(
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody SolicitarCambioCorreoRequest req) {
        authService.solicitarCambioCorreo(
                extraerAccessToken(bearerToken), req.getContrasenaActual(), req.getNuevoCorreo().trim());
        return ResponseEntity.ok(ApiResponse.ok(null, "Código de confirmación enviado al nuevo correo"));
    }

    /**
     * Cambio de correo - PASO 2: se confirma el código; Cognito hace el cambio oficial
     * y el backend actualiza su copia local del email.
     */
    @PostMapping("/cambiar-correo/confirmar")
    public ResponseEntity<ApiResponse<Void>> confirmarCambioCorreo(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Authorization", required = false) String bearerToken,
            @Valid @RequestBody ConfirmarCambioCorreoRequest req) {
        authService.confirmarCambioCorreo(
                jwt.getSubject(), extraerAccessToken(bearerToken), req.getNuevoCorreo().trim(), req.getCodigo().trim());
        return ResponseEntity.ok(ApiResponse.ok(null, "Correo actualizado exitosamente"));
    }

    private Usuario obtener(Jwt jwt) {
        return usuarioRepository.findById(jwt.getSubject())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", jwt.getSubject()));
    }

    private static UsuarioResponse aResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .roles(usuario.getRoles())
                .run(usuario.getRun())
                .apellidos(usuario.getApellidos())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .tipoUsuario(usuario.getTipoUsuario())
                .region(usuario.getRegion())
                .comuna(usuario.getComuna())
                .direccion(usuario.getDireccion())
                .build();
    }

    private static String extraerAccessToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new com.duoc.cursos.common.exception.BusinessException("SESION_EXPIRADA", "Tu sesión expiró: vuelve a iniciar sesión");
        }
        return bearerToken.substring(7);
    }
}
