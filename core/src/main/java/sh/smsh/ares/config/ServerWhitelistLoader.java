package sh.smsh.ares.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ServerWhitelistLoader {

    private final Path configPath;
    private final ServerWhitelistConfig defaultConfig;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ServerWhitelistLoader(@NotNull final Path configPath, @NotNull final ServerWhitelistConfig defaultConfig) {
        this.configPath = configPath;
        this.defaultConfig = defaultConfig;
    }

    public void ensureConfigExists() throws Exception {
        if (Files.notExists(configPath.getParent())) {
            Files.createDirectories(configPath.getParent());
        }

        if (Files.notExists(configPath)) {
            Files.write(configPath, gson.toJson(defaultConfig).getBytes(StandardCharsets.UTF_8));
        }
    }

    public ServerWhitelistConfig load() throws Exception {
        this.ensureConfigExists();

        final InputStream input = Files.newInputStream(configPath);

        try {

            final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

            try {
                final StringBuilder builder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    builder.append(line).append('\n');
                }

                final ServerWhitelistConfig config = gson.fromJson(builder.toString(), ServerWhitelistConfig.class);
                return config != null ? config : defaultConfig;
            } finally {
                reader.close();
            }
        } finally {
            input.close();
        }
    }

    public void save(@NotNull final ServerWhitelistConfig config) throws Exception {
        this.ensureConfigExists();
        Files.write(configPath, gson.toJson(config).getBytes(StandardCharsets.UTF_8));
    }
}