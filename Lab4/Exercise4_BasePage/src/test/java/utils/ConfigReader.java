
package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in =
                     ConfigReader.class.getResourceAsStream("/config.properties")) {

            if (in != null) {
                PROPS.load(in);
            } else {
                throw new RuntimeException(
                        "Khong tim thay src/test/resources/config.properties");
            }

        } catch (IOException e) {
            throw new RuntimeException("Khong the doc config.properties", e);
        }
    }

    private ConfigReader() {
        // Khong cho tao doi tuong ConfigReader
    }

    /**
     * Thu tu uu tien:
     * -Dkey=value > config.properties > gia tri mac dinh
     */
    public static String get(String key, String defaultValue) {
        return System.getProperty(
                key,
                PROPS.getProperty(key, defaultValue)
        );
    }

    public static boolean getBool(String key, boolean defaultValue) {
        return Boolean.parseBoolean(
                get(key, String.valueOf(defaultValue))
        );
    }

    public static int getInt(String key, int defaultValue) {
        return Integer.parseInt(
                get(key, String.valueOf(defaultValue))
        );
    }
}
