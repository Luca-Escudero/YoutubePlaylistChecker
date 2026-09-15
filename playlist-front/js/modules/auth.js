import { login as serviceLogin, register as serviceRegister } from "../services/authService.js";
import { setToken, clearToken, getToken } from "../services/apiConfig.js";

const SESSION_KEY = "userSession";

const PATHS = {
  USER: "/pages/user/home-user.html",
  LOGIN: "/pages/register-login/login.html",
};

function decodeJwt(token) {
  try {
    const payload = token.split(".")[1];
    const json = decodeURIComponent(
      atob(payload.replace(/-/g, "+").replace(/_/g, "/"))
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join("")
    );
    return JSON.parse(json);
  } catch (err) {
    console.error("No se pudo decodificar el token:", err);
    return null;
  }
}

function saveSession(token, responseData = {}) {
  const claims = decodeJwt(token);
  const session = {
    email: responseData.email || claims?.sub || null,
    nombre: responseData.nombre || claims?.nombre || "Usuario",
  };
  setToken(token);
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
  return session;
}

export function getSession() {
  const session = localStorage.getItem(SESSION_KEY);
  return session ? JSON.parse(session) : null;
}

export function isAuthenticated() {
  return !!getToken() && !!getSession();
}

export function logout() {
  clearToken();
  localStorage.removeItem(SESSION_KEY);
  window.location.href = PATHS.LOGIN;
}

export async function loginUser(email, password) {
  if (!email || !password) {
    throw new Error("Email y contraseña son requeridos.");
  }
  const response = await serviceLogin(email, password);
  if (!response?.token) {
    throw new Error("Respuesta inválida del servidor.");
  }
  return saveSession(response.token, response);
}

export async function registerUser(datos) {
  const { nombre, email, password } = datos;
  if (!nombre || !email || !password) {
    throw new Error("Todos los campos son requeridos.");
  }
  const response = await serviceRegister(nombre, email, password);
  if (response?.token) {
    return saveSession(response.token, response);
  }
  return response;
}

export function redirectToDashboard(session) {
  if (isAuthenticated()) {
    window.location.href = PATHS.USER;
  } else {
    window.location.href = PATHS.LOGIN;
  }
}

export function requireAuth() {
  if (!isAuthenticated()) {
    window.location.href = PATHS.LOGIN;
    return false;
  }
  return true;
}

export { PATHS };
