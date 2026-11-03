package com.duoc.cursos.auth.controller;

import com.duoc.cursos.auth.dto.*;
import com.duoc.cursos.auth.service.AuthService;
import com.duoc.cursos.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    public ResponseEntity<ApiResponse<UsuarioResponse>> registrar(@Valid @RequestBody RegistroRequest req) {
        UsuarioResponse usuario = authService.registrar(req);
        return ResponseEntity.ok(ApiResponse.ok(usuario, "Usuario registrado exitosamente"));
    }

    @PostMapping("/confirmar-registro")
    public ResponseEntity<ApiResponse<Void>> confirmarRegistro(@Valid @RequestBody ConfirmarRegistroRequest req) {
        authService.confirmarRegistro(req.getEmail().trim(), req.getCodigo().trim());
        return ResponseEntity.ok(ApiResponse.ok(null, "Cuenta verificada exitosamente. Ya puedes iniciar sesión"));
    }

    @PostMapping("/reenviar-codigo")
    public ResponseEntity<ApiResponse<Void>> reenviarCodigo(@Valid @RequestBody ReenviarCodigoRequest req) {
        authService.reenviarCodigoConfirmacion(req.getEmail().trim());
        return ResponseEntity.ok(ApiResponse.ok(null, "Código de verificación reenviado al email"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest req) {
        // Si Cognito exige cambio de contraseña (primer login con contraseña temporal),
        // la respuesta incluye challengeName="NEW_PASSWORD_REQUIRED" + session y sin tokens
        LoginResponse response = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(ApiResponse.ok(response, "Login exitoso"));
    }

    /**
     * Completa el cambio de contraseña obligatorio (challenge NEW_PASSWORD_REQUIRED).
     * Con esto el usuario queda CONFIRMADO/activo en Cognito y se entregan los tokens.
     */
    @PostMapping("/nueva-contrasena")
    public ResponseEntity<ApiResponse<LoginResponse>> confirmarNuevaContrasena(
            @Valid @RequestBody NuevaContrasenaRequest req) {
        LoginResponse response = authService.confirmarNuevaContrasena(
                req.getEmail(), req.getSession(), req.getNuevaContrasena(), req.getNombre());
        return ResponseEntity.ok(ApiResponse.ok(response, "Contraseña actualizada. Usuario activado exitosamente"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Token de autorización requerido"));
        }
        String accessToken = bearerToken.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok(ApiResponse.ok(null, "Logout exitoso"));
    }

    @PostMapping("/olvide-contrasena/solicitar")
    public ResponseEntity<ApiResponse<Void>> solicitarPassword(@Valid @RequestBody SolicitudPasswordRequest req) {
        authService.solicitarRecuperacionContrasena(req.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(null, "Código de recuperación enviado al email"));
    }

    @PostMapping("/olvide-contrasena/confirmar")
    public ResponseEntity<ApiResponse<Void>> confirmarPassword(@Valid @RequestBody ConfirmarPasswordRequest req) {
        authService.confirmarRecuperacionContrasena(req.getEmail(), req.getCodigo(), req.getNuevaContrasena());
        return ResponseEntity.ok(ApiResponse.ok(null, "Contraseña actualizada exitosamente"));
    }
}