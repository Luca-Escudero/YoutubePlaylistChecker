import { api } from "./apiConfig.js";

export async function verificarPlaylistUrl(url) {
  return api.post("/verificaciones", { url });
}

export async function verificarPlaylistPath(playlistIdOrUrl) {
  return api.post(`/verificaciones/${encodeURIComponent(playlistIdOrUrl)}`);
}
