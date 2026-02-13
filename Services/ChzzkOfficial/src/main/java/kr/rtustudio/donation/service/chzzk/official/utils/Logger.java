package kr.rtustudio.donation.service.chzzk.official.utils;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class Logger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("chzzk-openapi-java");

    public static void info(@NotNull String message) {
        LOGGER.info(message);
    }

    public static void info(@NotNull String message, @NotNull Exception ex) {
        LOGGER.log(Level.INFO, message, ex);
    }

    public static void warn(@NotNull String message) {
        LOGGER.warning(message);
    }

    public static void warn(@NotNull String message, @NotNull Exception ex) {
        LOGGER.log(Level.WARNING, message, ex);
    }

    public static void error(@NotNull String message) {
        LOGGER.log(Level.SEVERE, message);
    }

    public static void error(@NotNull String message, @NotNull Exception ex) {
        LOGGER.log(Level.SEVERE, message, ex);
    }

}
