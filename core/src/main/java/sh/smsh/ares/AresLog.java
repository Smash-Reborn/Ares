package sh.smsh.ares;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AresLog {

    @NotNull
    public static final Logger LOGGER = LogManager.getLogger(AresConstants.MOD_NAME);
}