package com.sheath.hammermining.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("hammermining");
    private static final String PREFIX = "[Hammer Mining] ";

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";

    public static void info(String format, Object... args) {
        LOGGER.info(GREEN + PREFIX + format + RESET, args);
    }

    public static void warn(String format, Object... args) {
        LOGGER.warn(YELLOW + PREFIX + format + RESET, args);
    }

    public static void error(String format, Object... args) {
        LOGGER.error(RED + PREFIX + format + RESET, args);
    }

    public static void debug(String format, Object... args) {
        LOGGER.debug(CYAN + PREFIX + format + RESET, args);
    }
}
