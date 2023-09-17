package org.luncert.tinystorage.storemodule.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.luncert.tinystorage.storemodule.util.CommonUtils;

import java.nio.charset.Charset;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    private static volatile Configuration INSTANCE;

    public static Configuration get() {
        if (INSTANCE == null) {
            synchronized (Configuration.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Configuration();
                }
            }
        }

        return INSTANCE;
    }

    private String dataStorePath;
    private long maxFileSize;
    private Charset charset = Charset.defaultCharset();

    public static ConfigurationAccessor config() {
        return get().createConfigurationAccessor();
    }

    private ConfigurationAccessor createConfigurationAccessor() {
        return new ConfigurationAccessorImpl();
    }

    private class ConfigurationAccessorImpl implements ConfigurationAccessor {

        @Override
        public void dataStorePath(String dataStorePath) {
            Configuration.this.dataStorePath = dataStorePath;
        }

        @Override
        public void maxFileSize(String maxFileSize) {
            Configuration.this.maxFileSize = CommonUtils.toByteCount(maxFileSize);
        }

        @Override
        public void charset(Charset charset) {
            Configuration.this.charset = charset;
        }
    }
}
