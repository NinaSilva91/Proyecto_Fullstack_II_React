// PÁGINA: REGISTRO (ruta "/register")  <- mi registro.html de antes
// Qué hace: pide mis datos, los valida y guarda la cuenta en el navegador (LocalStorage).
// Uso un solo estado "datos" con todos los campos juntos (más ordenado que 9 useState sueltos).
import { useState, type FormEvent } from "react";
import { Link } from "wouter";
import Field from "../../components/field/Field";
import Mensaje, { type MensajeForm } from "../../components/mensaje/Mensaje";
import PageTitle from "../../components/page-title/PageTitle";
import { comunasPorRegion, regiones } from "../../data/regiones";
import { formatearRun, validarRun } from "../../utils/run";
import { registrarUsuario } from "../../utils/storage";
import {
  hayErrores, validarApellidos, validarCorreo, validarDireccion, validarNacimiento, validarNombre,
  validarSeleccion,
} from "../../utils/validators";

const vacio = {
  run: "", nombre: "", apellidos: "", correo: "", fechaNacimiento: "",
  tipoUsuario: "", region: "", comuna: "", direccion: "",
};

const tiposUsuario = [
  { valor: "cliente", etiqueta: "Cliente" },
  { valor: "vendedor", etiqueta: "Vendedor / miembro de la comunidad" },
];

const Register = () => {
  const [datos, setDatos] = useState(vacio);
  const [errores, setErrores] = useState(vacio); // mismos campos, pero con los textos de error
  const [mensaje, setMensaje] = useState<MensajeForm | null>(null);
  const [creada, setCreada] = useState(false); // para mostrar el link "Ir a iniciar sesión"

  // cambia solo UN campo y deja los demás igual. Se usa así: cambiar("correo")  ->  función que recibe el texto
  const cambiar = (campo: keyof typeof vacio) => (valor: string) =>
    setDatos((anterior) => ({ ...anterior, [campo]: valor }));

  // el RUN lo formateo mientras escribo (pone los puntos y el guion solo)
  const cambiarRun = (valor: string) => cambiar("run")(formatearRun(valor));

  // cuando cambio de región, borro la comuna elegida porque ya no corresponde
  const cambiarRegion = (valor: string) => setDatos((anterior) => ({ ...anterior, region: valor, comuna: "" }));

  const handleSubmit = (evento: FormEvent<HTMLFormElement>) => {
    evento.preventDefault();
    const nuevos = {
      run: validarRun(datos.run) ? "" : "Ingresa un RUN válido (ej: 12.345.678-5).",
      nombre: validarNombre(datos.nombre),
      apellidos: validarApellidos(datos.apellidos),
      correo: validarCorreo(datos.correo),
      fechaNacimiento: validarNacimiento(datos.fechaNacimiento),
      tipoUsuario: validarSeleccion(datos.tipoUsuario, "Selecciona un tipo de usuario."),
      region: validarSeleccion(datos.region, "Selecciona una región."),
      comuna: validarSeleccion(datos.comuna, "Selecciona una comuna."),
      direccion: validarDireccion(datos.direccion),
    };
    setErrores(nuevos);
    setCreada(false);

    if (hayErrores(nuevos)) {
      setMensaje({ tipo: "fallo", texto: "Revisa los campos marcados en rojo." });
      return;
    }

    const problema = registrarUsuario({
      ...datos,
      nombre: datos.nombre.trim(),
      apellidos: datos.apellidos.trim(),
      correo: datos.correo.trim(),
      direccion: datos.direccion.trim(),
    });
    if (problema) {
      setMensaje({ tipo: "fallo", texto: problema });
      return;
    }
    setMensaje({ tipo: "exito", texto: "¡Cuenta creada con éxito! Ya puedes iniciar sesión." });
    setCreada(true);
    setDatos(vacio); // limpio el formulario
  };

  return (
    <div className="mx-auto max-w-2xl">
      <PageTitle>Crear cuenta nueva</PageTitle>
      <Mensaje mensaje={mensaje} />
      <form onSubmit={handleSubmit} noValidate className="panel p-6">
        <Field
          id="registro-run" label="RUN" value={datos.run} onChange={cambiarRun} error={errores.run}
          placeholder="Ej: 19.011.022-K" maxLength={12}
          ayuda="Escribe solo números y la K: yo pongo los puntos y el guion."
        />
        {/* en pantallas medianas pongo nombre y apellidos lado a lado */}
        <div className="grid gap-x-4 sm:grid-cols-2">
          <Field id="registro-nombre" label="Nombre" value={datos.nombre} onChange={cambiar("nombre")} error={errores.nombre} maxLength={100} autoComplete="given-name" />
          <Field id="registro-apellidos" label="Apellidos" value={datos.apellidos} onChange={cambiar("apellidos")} error={errores.apellidos} maxLength={100} autoComplete="family-name" />
        </div>
        <Field id="registro-correo" label="Correo electrónico" type="email" value={datos.correo} onChange={cambiar("correo")} error={errores.correo} placeholder="nombre@duoc.cl" maxLength={100} autoComplete="email" />
        <div className="grid gap-x-4 sm:grid-cols-2">
          <Field id="registro-fecha" label="Fecha de nacimiento" type="date" value={datos.fechaNacimiento} onChange={cambiar("fechaNacimiento")} error={errores.fechaNacimiento} autoComplete="bday" />
          <Field id="registro-tipo" label="Tipo de usuario" value={datos.tipoUsuario} onChange={cambiar("tipoUsuario")} error={errores.tipoUsuario} opciones={tiposUsuario} />
        </div>
        <div className="grid gap-x-4 sm:grid-cols-2">
          <Field id="registro-region" label="Región" value={datos.region} onChange={cambiarRegion} error={errores.region} opciones={regiones} textoOpcionVacia="Selecciona una región" />
          {/* las comunas dependen de la región: si no elegí región, la lista queda vacía y apagada */}
          <Field
            id="registro-comuna" label="Comuna" value={datos.comuna} onChange={cambiar("comuna")} error={errores.comuna}
            opciones={(comunasPorRegion[datos.region] ?? []).map((c) => ({ valor: c, etiqueta: c }))}
            textoOpcionVacia={datos.region ? "Selecciona una comuna" : "Primero elige una región"}
            deshabilitado={!datos.region}
          />
        </div>
        <Field id="registro-direccion" label="Dirección" value={datos.direccion} onChange={cambiar("direccion")} error={errores.direccion} maxLength={150} autoComplete="street-address" />

        <div className="flex flex-wrap items-center gap-4">
          <button type="submit" className="boton">
            Crear cuenta
          </button>
          <p className="m-0 text-sm font-semibold">
            ¿Ya tienes cuenta?{" "}
            <Link href="/login" className="text-rosa-500 underline">
              {creada ? "Ir a iniciar sesión" : "Inicia sesión"}
            </Link>
          </p>
        </div>
      </form>
    </div>
  );
};

export default Register;
