package com.pss.proxy.image;


import io.micronaut.context.ApplicationContext;
import io.micronaut.runtime.Micronaut;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ImageProxyApplication  {

    private static final Logger log = LogManager.getLogger(ImageProxyApplication.class);

    public static void main(String[] args) {
        log.info("Starting Application");
        ApplicationContext context = Micronaut.build(args)
                .banner(false)
                .environments("jwt")
                .classes(ImageProxy.class)
                .start();
    }

}
