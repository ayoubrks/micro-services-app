package org.sid.gatewayservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    /**
     * Étape 4 : Configuration statique du système de routage
     * Définition explicite des routes statiques vers les microservices via RouteLocatorBuilder.
     */
    @Bean
    public RouteLocator staticRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("r-customers", r -> r.path("/customers/**")
                        .uri("lb://CUSTOMER-SERVICE"))
                .route("r-products", r -> r.path("/products/**")
                        .uri("lb://INVENTORY-SERVICE"))
                .route("r-bills", r -> r.path("/bills/**")
                        .uri("lb://BILLING-SERVICE"))
                .route("r-fullbill", r -> r.path("/fullBill/**")
                        .uri("lb://BILLING-SERVICE"))
                .build();
    }

    /**
     * Étape 6 : Configuration dynamique des routes de la Gateway
     * Découverte automatique des routes basées sur l'annuaire Eureka.
     * Permet d'accéder aux microservices via: http://localhost:8888/{service-id}/**
     */
    @Bean
    public DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp) {
        dlp.setLowerCaseServiceId(true);
        return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
    }
}

@RestController
@RefreshScope
class GatewayRestController {

    @Value("${gateway.welcome.message:Bienvenue sur Spring Cloud Gateway (defaut)}")
    private String welcomeMessage;

    @GetMapping("/gateway/info")
    public Map<String, Object> getGatewayInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "gateway-service");
        info.put("port", 8888);
        info.put("welcomeMessage", welcomeMessage);
        info.put("status", "UP");
        return info;
    }
}
