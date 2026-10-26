package org.sid.inventoryservice;

import org.sid.inventoryservice.entities.Product;
import org.sid.inventoryservice.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(ProductRepository productRepository, RepositoryRestConfiguration restConfiguration) {
        return args -> {
            restConfiguration.exposeIdsFor(Product.class);

            productRepository.saveAll(List.of(
                    Product.builder().name("Ordinateur Portable Dell").price(9800.0).quantity(12).build(),
                    Product.builder().name("Imprimante Laser HP").price(1250.0).quantity(6).build(),
                    Product.builder().name("Smartphone Samsung S24").price(6900.0).quantity(20).build(),
                    Product.builder().name("Ecran 27 pouces 4K").price(3200.0).quantity(15).build()
            ));

            productRepository.findAll().forEach(p ->
                    System.out.println("Produit initialisé: " + p.getName() + " - " + p.getPrice() + " DH (Qte: " + p.getQuantity() + ")")
            );
        };
    }
}
