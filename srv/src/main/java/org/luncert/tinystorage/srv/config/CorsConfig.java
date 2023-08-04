package org.luncert.tinystorage.srv.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for local development.
 */
//@Profile("local")
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter(@Value("${cors.origin.urls}") String[] allowedOrigins) {
    CorsConfiguration config = new CorsConfiguration();
    for (String allowedOrigin : allowedOrigins) {
      config.addAllowedOrigin(allowedOrigin);
    }
    config.setAllowCredentials(true);
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);

    FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
    bean.setOrder(Integer.MIN_VALUE); // max priority
    return bean;
  }
}
