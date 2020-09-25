package net.rhizomik.rhizomer.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@Service
public class HttpClient {
    public void loadUrl(URL url, OutputStream out) throws IOException {
        Map<String, Integer> visited;
        HttpURLConnection conn;
        int times;
        visited = new HashMap<>();
        while (true)
        {
            times = visited.compute(url.toExternalForm(), (key, count) -> count == null ? 1 : count + 1);
            if (times > 3)
                throw new IOException("Stuck in redirect loop");
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "Http Client");

            switch (conn.getResponseCode())
            {
                case HttpURLConnection.HTTP_MOVED_PERM:
                case HttpURLConnection.HTTP_MOVED_TEMP:
                    String location = conn.getHeaderField("Location");
                    location = URLDecoder.decode(location, "UTF-8");
                    url     = new URL(url, location);
                    continue;
            }
            break;
        }
        int n;
        byte[] buffer = new byte[1024];
        while ((n = conn.getInputStream().read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }
    }
}
