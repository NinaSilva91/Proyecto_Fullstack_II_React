// api.ts: este archivo lo rescaté de la rama feature-conectar-api y lo ordené.
// Es el "mensajero" entre mi tienda y un backend (servidor).
//
// ESTADO HONESTO (para no confundirme en la presentación):
//  - guardarToken / borrarToken: SÍ se usan (cuando inicio sesión con Cognito guardo el token acá).
//  - estaDisponible: SÍ se usa (el pie de página muestra si el servidor responde, solo si hay VITE_API_URL).
//  - login / register / logout: están PREPARADOS, pero ninguna página los llama todavía, y los endpoints
//    (/auth/login, /auth/registro, /auth/logout) NO están comprobados porque no tengo el backend.
import { entorno } from "../config/entorno";

// si no hay VITE_API_URL uso la misma dirección que traía la rama (localhost:8080)
const BASE = entorno.apiUrl || "http://localhost:8080/api";
const CLAVE_TOKEN = "auth_token"; // misma clave que usaba la rama

export interface User {
  id: string;
  email: string;
  nombre: string;
  roles: string[];
}

export interface AuthResponse {
  accessToken: string;
  idToken: string;
  refreshToken: string;
  expiresIn: number;
}

// forma de respuesta que esperaba la rama: { success, data, message }
interface RespuestaApi<T> {
  success: boolean;
  data: T;
  message?: string;
  mensaje?: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterCredentials extends LoginCredentials {
  nombre: string;
  roles: string[];
}

// ---------- token (guardado en LocalStorage, con try/catch por si el navegador lo bloquea) ----------
export const guardarToken = (token: string): void => {
  try {
    localStorage.setItem(CLAVE_TOKEN, token);
  } catch {
    /* si no se puede guardar, no pasa nada grave */
  }
};

export const borrarToken = (): void => {
  try {
    localStorage.removeItem(CLAVE_TOKEN);
  } catch {
    /* igual que arriba */
  }
};

// un JWT siempre tiene 3 partes separadas por puntos; si no, lo descarto
const leerToken = (): string | null => {
  try {
    const token = localStorage.getItem(CLAVE_TOKEN);
    if (!token) return null;
    if (token.split(".").length !== 3) {
      borrarToken();
      return null;
    }
    return token;
  } catch {
    return null;
  }
};

const encabezados = (): HeadersInit => {
  const token = leerToken();
  return token
    ? { "Content-Type": "application/json", Authorization: `Bearer ${token}` }
    : { "Content-Type": "application/json" };
};

// función central: todas las llamadas pasan por acá, así reviso los errores una sola vez
// (en la rama original, register() no revisaba si la respuesta venía con error)
const pedir = async <T,>(ruta: string, init: RequestInit = {}): Promise<T> => {
  const respuesta = await fetch(`${BASE}${ruta}`, { ...init, headers: encabezados() });
  let cuerpo: RespuestaApi<T> | null = null;
  try {
    cuerpo = (await respuesta.json()) as RespuestaApi<T>;
  } catch {
    /* si no vino JSON, sigo con cuerpo = null */
  }
  if (!respuesta.ok || cuerpo?.success === false) {
    throw new Error(cuerpo?.message || cuerpo?.mensaje || `El servidor respondió con error ${respuesta.status}`);
  }
  return cuerpo?.data as T;
};

// el payload del JWT viene en base64url (con - y _), por eso lo convierto antes de usar atob
const decodificarPayload = (token: string): Record<string, unknown> | null => {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(base64)) as Record<string, unknown>;
  } catch {
    return null;
  }
};

const api = {
  async login(credenciales: LoginCredentials): Promise<AuthResponse> {
    const datos = await pedir<AuthResponse>("/auth/login", { method: "POST", body: JSON.stringify(credenciales) });
    if (!datos?.accessToken) throw new Error("No se pudo iniciar sesión");
    guardarToken(datos.accessToken);
    return datos;
  },

  register: (credenciales: RegisterCredentials) =>
    pedir<User>("/auth/registro", { method: "POST", body: JSON.stringify(credenciales) }),

  async logout(): Promise<void> {
    try {
      await pedir<unknown>("/auth/logout", { method: "POST" });
    } finally {
      borrarToken(); // aunque el servidor falle, igual borro el token de este navegador
    }
  },

  // me dice si el servidor responde (/health). OJO: solo prueba que el servidor está vivo, no que la sesión sea válida
  async estaDisponible(): Promise<boolean> {
    try {
      const respuesta = await fetch(`${BASE}/health`, { headers: encabezados() });
      return respuesta.ok;
    } catch {
      return false;
    }
  },

  // saco los datos de la persona desde el token guardado (sin preguntarle al servidor)
  getCurrentUser(): User | null {
    const token = leerToken();
    const payload = token ? decodificarPayload(token) : null;
    if (!payload) return null;
    return {
      id: String(payload.sub ?? ""),
      email: String(payload.email ?? ""),
      nombre: String(payload.name ?? ""),
      roles: Array.isArray(payload.roles) ? (payload.roles as string[]) : [],
    };
  },
};

export default api;
