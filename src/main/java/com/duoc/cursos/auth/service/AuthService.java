package com.duoc.cursos.auth.service;

import com.duoc.cursos.auth.dto.LoginResponse;
import com.duoc.cursos.auth.dto.RegistroRequest;
import com.duoc.cursos.auth.dto.UsuarioResponse;
import com.duoc.cursos.auth.model.Usuario;
import com.duoc.cursos.auth.repository.UsuarioRepository;
import com.duoc.cursos.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class AuthService {

    private final CognitoIdentityProviderClient cognitoClient;
    private final UsuarioRepository usuarioRepository;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-secret:}")
    private String clientSecret;

    public AuthService(CognitoIdentityProviderClient cognitoClient, UsuarioRepository usuarioRepository) {
        this.cognitoClient = cognitoClient;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * FLUJO: REGISTRO Y COPIA LOCAL CON MÚLTIPLES ROLES
     * 'roles' es opcional: si no viene, no se asignan grupos en Cognito y el usuario
     * queda guardado localmente como CLIENTE (así la tienda puede registrar clientes
     * aunque los grupos no existan en el User Pool).
     */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        String email = req.getEmail();
        String password = req.getPassword();
        String nombre = req.getNombre();
        Set<String> roles = req.getRoles();
        boolean tieneRoles = roles != null && !roles.isEmpty();

        // 1. Registro en Cognito
        SignUpRequest.Builder signUpBuilder = SignUpRequest.builder()
                .clientId(clientId)
                .username(email)
                .password(password)
                .userAttributes(
                        AttributeType.builder().name("email").value(email).build(),
                        AttributeType.builder().name("name").value(nombre).build()
                );

        // Agregar SECRET_HASH si hay client secret
        if (clientSecret != null && !clientSecret.isBlank()) {
            signUpBuilder.secretHash(generateSecretHash(email));
        }

        SignUpResponse signUpResponse = cognitoClient.signUp(signUpBuilder.build());
        String cognitoSub = signUpResponse.userSub();

        // 2. Asignación a Grupos en Cognito (Roles), solo si se pidieron roles.
        // OJO: las APIs "admin*" necesitan credenciales IAM y las de usuario no.
        // Si falla porque el grupo no existe O porque la sesión/máquina no tiene
        // credenciales IAM, la cuenta queda creada igual (rol asignado en la base local).
        if (tieneRoles) {
            for (String rol : roles) {
                try {
                    AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                            .userPoolId(userPoolId)
                            .username(email)
                            .groupName(rol)
                            .build();
                    cognitoClient.adminAddUserToGroup(groupRequest);
                } catch (software.amazon.awssdk.services.cognitoidentityprovider.model.ResourceNotFoundException e) {
                    log.warn("No se pudo asignar el grupo '{}' en Cognito: no existe. La cuenta queda sin grupo.", rol);
                } catch (software.amazon.awssdk.core.exception.SdkClientException e) {
                    // sin credenciales IAM (o sin red a IAM): no interrumpe el registro
                    log.warn("No se pudo asignar el grupo '{}' en Cognito por credenciales/permisos. La cuenta queda con el rol solo en la base local. Detalle: {}", rol, e.getMessage());
                }
            }
        }

        // 3. Persistencia en Base de Datos Local
        Usuario usuarioLocal = new Usuario();
        usuarioLocal.setId(cognitoSub);
        usuarioLocal.setEmail(email);
        usuarioLocal.setNombre(nombre);
        usuarioLocal.setRoles(tieneRoles ? roles : Set.of("CLIENTE"));

        // 4. Datos del perfil de la tienda (si vienen en la petición)
        usuarioLocal.setRun(trimOrNull(req.getRun()));
        usuarioLocal.setApellidos(trimOrNull(req.getApellidos()));
        usuarioLocal.setFechaNacimiento(trimOrNull(req.getFechaNacimiento()));
        usuarioLocal.setTipoUsuario(trimOrNull(req.getTipoUsuario()));
        usuarioLocal.setRegion(trimOrNull(req.getRegion()));
        usuarioLocal.setComuna(trimOrNull(req.getComuna()));
        usuarioLocal.setDireccion(trimOrNull(req.getDireccion()));

        Usuario saved = usuarioRepository.save(usuarioLocal);

        // si Cognito no confirmó al usuario automáticamente, hay que pedirle el
        // código de verificación que llegó a su correo (el frontend lo recolecta)
        return UsuarioResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .nombre(saved.getNombre())
                .roles(saved.getRoles())
                .requiereVerificacion(!Boolean.TRUE.equals(signUpResponse.userConfirmed()))
                .run(saved.getRun())
                .apellidos(saved.getApellidos())
                .fechaNacimiento(saved.getFechaNacimiento())
                .tipoUsuario(saved.getTipoUsuario())
                .region(saved.getRegion())
                .comuna(saved.getComuna())
                .direccion(saved.getDireccion())
                .build();
    }

    private static String trimOrNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    /**
     * FLUJO: CONFIRMACIÓN DE REGISTRO
     * Cognito envía un código de verificación al email cuando User Pool no auto-confirma.
     * Con este método el frontend puede "activar" la cuenta con ese código.
     */
    public void confirmarRegistro(String email, String codigo) {
        ConfirmSignUpRequest.Builder builder = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .username(email)
                .confirmationCode(codigo);

        if (clientSecret != null && !clientSecret.isBlank()) {
            builder.secretHash(generateSecretHash(email));
        }
        cognitoClient.confirmSignUp(builder.build());
    }

    /**
     * FLUJO: REENVÍO DEL CÓDIGO DE VERIFICACIÓN
     */
    public void reenviarCodigoConfirmacion(String email) {
        ResendConfirmationCodeRequest.Builder builder = ResendConfirmationCodeRequest.builder()
                .clientId(clientId)
                .username(email);

        if (clientSecret != null && !clientSecret.isBlank()) {
            builder.secretHash(generateSecretHash(email));
        }
        cognitoClient.resendConfirmationCode(builder.build());
    }

    /**
     * FLUJO: INICIO DE SESIÓN (LOGIN) PROXY
     * Si el usuario tiene contraseña temporal (primer login), Cognito responde con el
     * challenge NEW_PASSWORD_REQUIRED: se devuelve el nombre del challenge y la sesión
     * (sin tokens) para que el frontend pida la nueva contraseña.
     */
    public LoginResponse login(String email, String password) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", email);
        authParameters.put("PASSWORD", password);
        if (clientSecret != null && !clientSecret.isBlank()) {
            authParameters.put("SECRET_HASH", generateSecretHash(email));
        }

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(authParameters)
                .build();

        InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);

        // Usuario con contraseña temporal: no hay tokens, hay un challenge por resolver
        if (authResponse.challengeName() != null) {
            return buildChallengeResponse(authResponse.challengeName().toString(), authResponse.session());
        }
        return buildTokenResponse(authResponse.authenticationResult());
    }

    /**
     * FLUJO: CAMBIO DE CONTRASEÑA OBLIGATORIO (PRIMER LOGIN)
     * Responde el challenge NEW_PASSWORD_REQUIRED con la nueva contraseña.
     * Al completarlo, Cognito marca al usuario como CONFIRMADO/activo y entrega los tokens.
     */
    public LoginResponse confirmarNuevaContrasena(String email, String session, String nuevaContrasena, String nombre) {
        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("USERNAME", email);
        challengeResponses.put("NEW_PASSWORD", nuevaContrasena);
        if (clientSecret != null && !clientSecret.isBlank()) {
            challengeResponses.put("SECRET_HASH", generateSecretHash(email));
        }
        // Si el User Pool exige el atributo "name" y el usuario no lo tiene,
        // Cognito rechaza el challenge con "Invalid attributes given, name is missing".
        // El frontend lo recolecta en el formulario de activación y lo enviamos acá
        if (nombre != null && !nombre.isBlank()) {
            challengeResponses.put("userAttributes.name", nombre);
        }

        RespondToAuthChallengeRequest request = RespondToAuthChallengeRequest.builder()
                .clientId(clientId)
                .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .session(session)
                .challengeResponses(challengeResponses)
                .build();

        RespondToAuthChallengeResponse response = cognitoClient.respondToAuthChallenge(request);

        // Caso normal: el usuario queda activo y se entregan los tokens
        if (response.authenticationResult() != null) {
            return buildTokenResponse(response.authenticationResult());
        }
        // Caso raro: Cognito exige resolver otro challenge adicional (p. ej. MFA)
        if (response.challengeName() != null) {
            return buildChallengeResponse(response.challengeName().toString(), response.session());
        }
        throw new BusinessException("LOGIN_ERROR", "No fue posible activar el usuario. Intente iniciar sesión nuevamente");
    }

    private LoginResponse buildTokenResponse(AuthenticationResultType result) {
        LoginResponse response = new LoginResponse();
        response.setAccessToken(result.accessToken());
        response.setIdToken(result.idToken());
        response.setRefreshToken(result.refreshToken());
        response.setExpiresIn(result.expiresIn());
        return response;
    }

    private LoginResponse buildChallengeResponse(String challengeName, String session) {
        LoginResponse response = new LoginResponse();
        response.setChallengeName(challengeName);
        response.setSession(session);
        return response;
    }

    /**
     * FLUJO: CIERRE DE SESIÓN GLOBAL (LOGOUT)
     */
    public void logout(String accessToken) {
        GlobalSignOutRequest signOutRequest = GlobalSignOutRequest.builder()
                .accessToken(accessToken)
                .build();
        cognitoClient.globalSignOut(signOutRequest);
    }

    /**
     * FLUJO: OLVIDÉ MI CONTRASEÑA - PASO 1 (SOLICITUD)
     */
    public void solicitarRecuperacionContrasena(String email) {
        ForgotPasswordRequest.Builder requestBuilder = ForgotPasswordRequest.builder()
                .clientId(clientId)
                .username(email);

        if (clientSecret != null && !clientSecret.isBlank()) {
            requestBuilder.secretHash(generateSecretHash(email));
        }

        cognitoClient.forgotPassword(requestBuilder.build());
    }

    /**
     * FLUJO: OLVIDÉ MI CONTRASEÑA - PASO 2 (CONFIRMACIÓN)
     */
    public void confirmarRecuperacionContrasena(String email, String codigoOtp, String nuevaContrasena) {
        ConfirmForgotPasswordRequest.Builder requestBuilder = ConfirmForgotPasswordRequest.builder()
                .clientId(clientId)
                .username(email)
                .confirmationCode(codigoOtp)
                .password(nuevaContrasena);

        if (clientSecret != null && !clientSecret.isBlank()) {
            requestBuilder.secretHash(generateSecretHash(email));
        }

        cognitoClient.confirmForgotPassword(requestBuilder.build());
    }

    /**
     * FLUJO: CAMBIO DE CONTRASEÑA CON SESIÓN ACTIVA
     * Cognito ChangePassword: pide la contraseña ACTUAL y la nueva (misma política del registro).
     * El accessToken sale del header Authorization de la petición.
     */
    public void cambiarContrasena(String accessToken, String contrasenaActual, String nuevaContrasena) {
        try {
            cognitoClient.changePassword(ChangePasswordRequest.builder()
                    .accessToken(accessToken)
                    .previousPassword(contrasenaActual)
                    .proposedPassword(nuevaContrasena)
                    .build());
        } catch (NotAuthorizedException e) {
            throw new BusinessException("SESION_EXPIRADA", "Tu sesión expiró: vuelve a iniciar sesión");
        } catch (InvalidPasswordException | InvalidParameterException e) {
            throw new BusinessException("CLAVE_INVALIDA",
                    "La nueva contraseña no cumple la política: mínimo 8 caracteres con mayúscula, minúscula, número y símbolo");
        } catch (CognitoIdentityProviderException e) {
            throw new BusinessException("CAMBIO_CLAVE_ERROR", traducirErrorCognito(e, "No se pudo cambiar la contraseña"));
        }
    }

    /**
     * FLUJO: CAMBIO DE CORREO CON SESIÓN ACTIVA - PASO 1
     * Verificación DOBLE antes de tocar el correo:
     *  1) prueba de identidad: la CONTRASEÑA ACTUAL la valida Cognito vía InitiateAuth
     *     (así una sesión abierta robada no puede cambiar el correo sin la clave)
     *  2) prueba de existencia: Cognito UpdateUserAttributes con el atributo "email"
     *     envía un código de confirmación al correo NUEVO (el anterior sigue vigente
     *     hasta confirmar). "Siempre y cuando lo permita Cognito": si el User Pool
     *     no tiene la verificación de atributos activada, no hay código y avisamos.
     */
    public void solicitarCambioCorreo(String accessToken, String contrasenaActual, String nuevoCorreo) {
        // ----- paso 1A: ¿la persona que pide el cambio sabe la contraseña de la cuenta? -----
        String username = obtenerUsernameDesdeToken(accessToken);
        try {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", username);
            authParameters.put("PASSWORD", contrasenaActual);
            if (clientSecret != null && !clientSecret.isBlank()) {
                authParameters.put("SECRET_HASH", generateSecretHash(username));
            }
            cognitoClient.initiateAuth(InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .clientId(clientId)
                    .authParameters(authParameters)
                    .build());
            // el resultado son solo tokens de Cognito que NO usamos: la autenticación
            // de acá arriba ya fue la prueba de identidad que pedimos
        } catch (NotAuthorizedException e) {
            throw new BusinessException("CLAVE_INCORRECTA", "Tu contraseña actual no es correcta");
        } catch (PasswordResetRequiredException e) {
            throw new BusinessException("CLAVE_TEMPORAL", "Tu cuenta requiere restablecer la contraseña: usa 'Olvidé mi contraseña'");
        } catch (CognitoIdentityProviderException e) {
            throw new BusinessException("VERIFICACION_IDENTITY_ERROR", traducirErrorCognito(e, "No se pudo verificar tu identidad con la contraseña"));
        }

        // ----- paso 1B: pedir el código de confirmación al correo NUEVO -----
        try {
            UpdateUserAttributesResponse respuesta = cognitoClient.updateUserAttributes(UpdateUserAttributesRequest.builder()
                    .accessToken(accessToken)
                    .userAttributes(AttributeType.builder().name("email").value(nuevoCorreo).build())
                    .build());

            boolean codigoEnviado = !respuesta.codeDeliveryDetailsList().isEmpty();
            if (!codigoEnviado) {
                throw new BusinessException("VERIFICACION_NO_CONFIGURADA",
                        "El cambio de correo no está disponible ahora. Ya volvemos");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (AliasExistsException | UsernameExistsException e) {
            throw new BusinessException("CORREO_EN_USO", "Ese correo ya está asociado a otra cuenta");
        } catch (NotAuthorizedException e) {
            throw new BusinessException("SESION_EXPIRADA", "Tu sesión expiró: vuelve a iniciar sesión");
        } catch (CognitoIdentityProviderException e) {
            throw new BusinessException("CAMBIO_CORREO_ERROR", traducirErrorCognito(e, "No se pudo solicitar el cambio de correo"));
        }
    }

    /**
     * Devuelve el username de Cognito correspondiente al access token, para poder
     * re-autenticar a la persona en el paso 1A (InitiateAuth necesita USERNAME).
     */
    private String obtenerUsernameDesdeToken(String accessToken) {
        try {
            return cognitoClient.getUser(GetUserRequest.builder().accessToken(accessToken).build()).username();
        } catch (NotAuthorizedException e) {
            throw new BusinessException("SESION_EXPIRADA", "Tu sesión expiró: vuelve a iniciar sesión");
        }
    }

    /**
     * FLUJO: CAMBIO DE CORREO CON SESIÓN ACTIVA - PASO 2
     * Cognito VerifyUserAttribute: confirma el código que llegó al correo NUEVO.
     * Ya confirmado el cambio, actualiza la copia local en la base (los pedidos
     * y las notificaciones leen el correo desde acá).
     */
    public void confirmarCambioCorreo(String usuarioId, String accessToken, String nuevoCorreo, String codigo) {
        try {
            cognitoClient.verifyUserAttribute(VerifyUserAttributeRequest.builder()
                    .accessToken(accessToken)
                    .attributeName("email")
                    .code(codigo)
                    .build());
        } catch (CodeMismatchException | ExpiredCodeException e) {
            throw new BusinessException("CODIGO_INVALIDO", "El código es inválido o ya expiró");
        } catch (NotAuthorizedException e) {
            throw new BusinessException("SESION_EXPIRADA", "Tu sesión expiró: vuelve a iniciar sesión");
        } catch (CognitoIdentityProviderException e) {
            throw new BusinessException("VERIFICACION_CORREO_ERROR", traducirErrorCognito(e, "No se pudo confirmar el cambio de correo"));
        }

        // la copia local queda igual al correo oficial de Cognito
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.setEmail(nuevoCorreo);
            usuarioRepository.save(usuario);
        });
    }

    /** Convierte los errores de Cognito en un mensaje útil (y legible) para el frontend */
    private String traducirErrorCognito(CognitoIdentityProviderException e, String mensajePorDefecto) {
        String detalle = e.awsErrorDetails() != null && e.awsErrorDetails().errorMessage() != null
                ? e.awsErrorDetails().errorMessage()
                : e.getMessage();
        log.warn("Cognito rechazó la operación: {}", detalle);
        return mensajePorDefecto + ": " + detalle;
    }

    /**
     * Genera SECRET_HASH usando HMAC-SHA256 (username + clientId + clientSecret)
     * Requerido cuando el App Client tiene "Generate client secret" activado
     */
    private String generateSecretHash(String username) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec =
                new javax.crypto.spec.SecretKeySpec(clientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal((username + clientId).getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generando SECRET_HASH", e);
        }
    }
}