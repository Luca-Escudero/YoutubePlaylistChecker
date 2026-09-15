import { api } from "./apiConfig.js";

export async function getMisConsultas() {
  return api.get("/consultas/mis-consultas");
}

export async function getConsultaPorId(id) {
  return api.get(`/consultas/${id}`);
}
