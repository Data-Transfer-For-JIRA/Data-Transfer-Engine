package com.utils;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class ViewErrorConfig {

    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return new ErrorPageRegisterar();
    }

    private static class ErrorPageRegisterar implements ErrorPageRegistrar {

        @Override
        public void registerErrorPages(ErrorPageRegistry registry) {
            ErrorPage errorPage = new ErrorPage(HttpStatus.NOT_FOUND, "/");
            registry.addErrorPages(errorPage);
        }

    }
}