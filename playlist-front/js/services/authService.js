import { api } from "./apiConfig.js";

export async function login(email, password) {
  return api.post("/auth/login", { email, password }, { auth: false });
}

export async function register(nombre, email, password) {
  return api.post("/auth/registro", { nombre, email, password }, { auth: false });
}
