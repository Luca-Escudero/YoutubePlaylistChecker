import { api } from "./apiConfig.js";

export async function getTodasPlaylists() {
  return api.get("/playlists");
}

export async function getPlaylistPorId(id) {
  return api.get(`/playlists/${id}`);
}

export async function getPlaylistPorYoutubeId(youtubeId) {
  return api.get(`/playlists/youtube/${youtubeId}`);
}

export async function testYoutubePlaylist(youtubeId) {
  return api.get(`/playlists/youtube/test/${youtubeId}`);
}

export async function guardarPlaylist(playlist) {
  return api.post("/playlists", playlist);
}

export async function eliminarPlaylist(id) {
  return api.delete(`/playlists/${id}`);
}
