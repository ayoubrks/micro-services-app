package org.sid.customerservice;

import org.sid.customerservice.entities.Customer;
import org.sid.customerservice.repositories.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner start(CustomerRepository customerRepository, RepositoryRestConfiguration restConfiguration) {
        return args -> {
            restConfiguration.exposeIdsFor(Customer.class);

            customerRepository.saveAll(List.of(
                    Customer.builder().name("Mohamed Youssfi").email("med@gmail.com").build(),
                    Customer.builder().name("Hassan El Amrani").email("hassan@gmail.com").build(),
                    Customer.builder().name("Salma Bennani").email("salma@gmail.com").build()
            ));

            customerRepository.findAll().forEach(c ->
                    System.out.println("Customer initialisé: " + c.getName() + " - " + c.getEmail())
            );
        };
    }
}
