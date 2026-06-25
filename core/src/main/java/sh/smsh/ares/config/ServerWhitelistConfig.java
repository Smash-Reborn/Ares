package sh.smsh.ares.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ServerWhitelistConfig {

    private boolean enabled = true;
    private List<String> servers = new ArrayList<>();
    private boolean allowSubdomains = true;
}