// PÁGINA: INICIAR SESIÓN (ruta "/login")  <- mi login.html + login.js de antes
// Qué hace:
//  1) Guarda lo que escribo en dos estados (correo y clave).
//  2) Al enviar, revisa las reglas (correo con dominio permitido, clave de 4 a 10 caracteres).
//  3) Si todo está bien, inicia la sesión, le avisa a App.tsx (onLogin) y me lleva al inicio.
import { useEffect, useRef, useState, type FormEvent } from "react";
import { Link, useLocation } from "wouter";
import Field from "../../components/field/Field";
import Mensaje, { type MensajeForm } from "../../components/mensaje/Mensaje";
import PageTitle from "../../components/page-title/PageTitle";
import { iniciarSesion } from "../../utils/auth";
import type { ModoAuth } from "../../utils/cognito";
import type { Sesion } from "../../utils/storage";
import { hayErrores, validarClave, validarCorreo } from "../../utils/validators";

interface LoginProps {
  onLogin: (sesion: Sesion) => void; // función que viene de App.tsx
  modo: ModoAuth; // "local" o "cognito" (viene de useSesion)
  onCognito: () => void; // abre el inicio de sesión de Amazon Cognito
}

const Login = ({ onLogin, modo, onCognito }: LoginProps) => {
  const [correo, setCorreo] = useState("");
  const [clave, setClave] = useState("");
  const [errores, setErrores] = useState({ correo: "", clave: "" });
  const [mensaje, setMensaje] = useState<MensajeForm | null>(null);
  const [, irA] = useLocation(); // irA("/") cambia de página desde el código
  // acá guardo el temporizador del redireccionamiento para poder cancelarlo si me voy de la página antes
  const temporizador = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => () => clearTimeout(temporizador.current), []);

  // FormEvent<HTMLFormElement> es el tipo correcto del evento (así no uso "any")
  const handleSubmit = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault(); // frena el envío normal del navegador, que recargaría la página
    const nuevos = { correo: validarCorreo(correo), clave: validarClave(clave) };
    setErrores(nuevos);

    if (hayErrores(nuevos)) {
      setMensaje({ tipo: "fallo", texto: "Revisa los campos marcados en rojo." });
      return;
    }

    const sesion = iniciarSesion(correo, clave);
    onLogin(sesion); // le aviso a App.tsx quién entró
    setMensaje({
      tipo: "exito",
      texto: sesion.rol === "Admin" ? "¡Bienvenida/o administrador/a!" : "¡Bienvenida/o a Secret Sweet Maid!",
    });
    temporizador.current = setTimeout(() => irA("/"), 900); // espero un poquito para que se alcance a leer el mensaje
  };

  return (
    <div className="mx-auto max-w-xl">
      <PageTitle>Iniciar sesión</PageTitle>
      <Mensaje mensaje={mensaje} />
      {/* noValidate apaga la validación del navegador: la hago yo con mis funciones para poner mis mensajes */}
      <form onSubmit={handleSubmit} noValidate className="panel p-6">
        <Field
          id="login-correo" label="Correo electrónico" type="email" value={correo} onChange={setCorreo}
          error={errores.correo} placeholder="nombre@duoc.cl" autoComplete="email" maxLength={100}
        />
        <Field
          id="login-clave" label="Contraseña" type="password" value={clave} onChange={setClave}
          error={errores.clave} autoComplete="current-password" maxLength={10}
        />
        <button type="submit" className="boton">
          Ingresar
        </button>
      </form>
      {/* este bloque solo aparece si la tienda está configurada con Amazon Cognito */}
      {modo === "cognito" && (
        <div className="panel mt-4 p-6 text-center">
          <p className="mb-3 font-semibold">También puedes entrar con tu cuenta de Cognito.</p>
          <button type="button" onClick={onCognito} className="boton">
            Ingresar o crear cuenta con Cognito
          </button>
        </div>
      )}
      <p className="mt-4 font-semibold">
        ¿No tienes cuenta?{" "}
        <Link href="/register" className="text-rosa-500 underline">
          Regístrate aquí
        </Link>
        .
      </p>
    </div>
  );
};

export default Login;
