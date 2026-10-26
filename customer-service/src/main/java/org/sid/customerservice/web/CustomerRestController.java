package org.sid.customerservice.web;

import org.sid.customerservice.entities.Customer;
import org.sid.customerservice.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RefreshScope
public class CustomerRestController {

    private final CustomerRepository customerRepository;

    @Value("${customer.params.p1:valeur_par_defaut_p1}")
    private String p1;

    @Value("${customer.params.p2:valeur_par_defaut_p2}")
    private String p2;

    @Value("${customer.welcome.message:Message local par defaut}")
    private String welcomeMessage;

    public CustomerRestController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/customers")
    public ResponseEntity<Customer> saveCustomer(@RequestBody Customer customer) {
        Customer saved = customerRepository.save(customer);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/customers/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return customerRepository.findById(id)
                .map(existing -> {
                    existing.setName(customer.getName());
                    existing.setEmail(customer.getEmail());
                    return ResponseEntity.ok(customerRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        if (!customerRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        customerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/params")
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("p1", p1);
        params.put("p2", p2);
        params.put("welcomeMessage", welcomeMessage);
        return params;
    }
}
