package net.rhizomik.rhizomer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class RhizomerAPIApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(RhizomerAPIApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(RhizomerAPIApplication.class, args);
    }
}
