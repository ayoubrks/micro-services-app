package org.sid.billingservice;

import org.sid.billingservice.entities.Bill;
import org.sid.billingservice.entities.ProductItem;
import org.sid.billingservice.feign.CustomerRestClient;
import org.sid.billingservice.feign.ProductRestClient;
import org.sid.billingservice.model.Customer;
import org.sid.billingservice.model.Product;
import org.sid.billingservice.repositories.BillRepository;
import org.sid.billingservice.repositories.ProductItemRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import java.util.Date;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class BillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(BillRepository billRepository,
                            ProductItemRepository productItemRepository,
                            CustomerRestClient customerRestClient,
                            ProductRestClient productRestClient,
                            RepositoryRestConfiguration restConfiguration) {
        return args -> {
            restConfiguration.exposeIdsFor(Bill.class, ProductItem.class);

            try {
                // Tentative de récupération dynamique depuis customer-service
                Customer customer = customerRestClient.getCustomerById(1L);
                List<Product> products = productRestClient.allProducts();

                Bill bill = billRepository.save(Bill.builder()
                        .billingDate(new Date())
                        .customerId(customer.getId())
                        .build());

                Random random = new Random();
                products.forEach(product -> {
                    ProductItem productItem = ProductItem.builder()
                            .bill(bill)
                            .productId(product.getId())
                            .price(product.getPrice())
                            .quantity(1 + random.nextInt(5))
                            .build();
                    productItemRepository.save(productItem);
                });

                System.out.println("Facture initialisée avec succès pour le client: " + customer.getName());
            } catch (Exception e) {
                System.out.println("Services distants non encore disponibles lors du démarrage (initialisation avec données de test par défaut): " + e.getMessage());

                // Données de secours si les microservices distants ne sont pas encore lancés
                Bill fallbackBill1 = billRepository.save(Bill.builder()
                        .billingDate(new Date())
                        .customerId(1L)
                        .build());

                productItemRepository.save(ProductItem.builder()
                        .bill(fallbackBill1)
                        .productId(1L)
                        .price(9800.0)
                        .quantity(2)
                        .build());

                productItemRepository.save(ProductItem.builder()
                        .bill(fallbackBill1)
                        .productId(2L)
                        .price(1250.0)
                        .quantity(1)
                        .build());

                Bill fallbackBill2 = billRepository.save(Bill.builder()
                        .billingDate(new Date())
                        .customerId(2L)
                        .build());

                productItemRepository.save(ProductItem.builder()
                        .bill(fallbackBill2)
                        .productId(3L)
                        .price(6900.0)
                        .quantity(1)
                        .build());

                System.out.println("Factures de démonstration créées dans la base de données H2 de billing-service.");
            }
        };
    }
}
