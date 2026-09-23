import { api } from "./apiConfig.js";

export async function getMisConsultas() {
  return api.get("/consultas/mis-consultas");
}

export async function getConsultaPorId(id) {
  return api.get(`/consultas/${id}`);
}

export async function getConsultasPorPlaylist(playlistId) {
  return api.get(`/consultas/playlist/${playlistId}`);
}

export async function compararConsultas(idAnterior, idNueva) {
  return api.get(`/consultas/comparar?idAnterior=${idAnterior}&idNueva=${idNueva}`);
}
