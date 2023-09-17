package org.luncert.tinystorage.storemodule.config;

import java.nio.charset.Charset;

public interface ConfigurationAccessor {

    void dataStorePath(String dataStorePath);

    void maxFileSize(String maxFileSize);

    void charset(Charset charset);
}
