package ec.edu.monster.modelos.rest;

import ec.edu.monster.config.RestConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestTransporte {

    private final int tiempoEsperaMs;

    public RestTransporte() {
        this(RestConfig.TIEMPO_ESPERA_MS);
    }

    public RestTransporte(int tiempoEsperaMs) {
        this.tiempoEsperaMs = tiempoEsperaMs;
    }

    /** Envía un POST con cuerpo JSON. Devuelve también las respuestas 4xx/5xx, que pueden traer estado y mensaje. */
    public RespuestaHttp enviar(String url, String json) throws IOException {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) new URL(url).openConnection();
            conexion.setRequestMethod("POST");
            conexion.setConnectTimeout(tiempoEsperaMs);
            conexion.setReadTimeout(tiempoEsperaMs);
            conexion.setUseCaches(false);
            conexion.setDoOutput(true);
            conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conexion.setRequestProperty("Accept", "application/json");

            try (OutputStream salida = conexion.getOutputStream()) {
                salida.write(json.getBytes(StandardCharsets.UTF_8));
            }

            int codigo = conexion.getResponseCode();
            InputStream flujo = codigo >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            return new RespuestaHttp(codigo, flujo != null ? leer(flujo) : "");
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static String leer(InputStream flujo) throws IOException {
        try (InputStream entrada = flujo; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] bloque = new byte[4096];
            int leidos;
            while ((leidos = entrada.read(bloque)) != -1) {
                buffer.write(bloque, 0, leidos);
            }
            return buffer.toString(StandardCharsets.UTF_8.name());
        }
    }
}
