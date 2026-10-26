package org.sid.billingservice.web;

import org.sid.billingservice.entities.Bill;
import org.sid.billingservice.entities.ProductItem;
import org.sid.billingservice.feign.CustomerRestClient;
import org.sid.billingservice.feign.ProductRestClient;
import org.sid.billingservice.model.Customer;
import org.sid.billingservice.model.Product;
import org.sid.billingservice.repositories.BillRepository;
import org.sid.billingservice.repositories.ProductItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RefreshScope
public class BillRestController {

    private final BillRepository billRepository;
    private final ProductItemRepository productItemRepository;
    private final CustomerRestClient customerRestClient;
    private final ProductRestClient productRestClient;

    @Value("${billing.params.taxRate:0.20}")
    private String taxRate;

    @Value("${billing.params.currency:MAD}")
    private String currency;

    @Value("${billing.welcome.message:Message local Billing}")
    private String welcomeMessage;

    public BillRestController(BillRepository billRepository,
                              ProductItemRepository productItemRepository,
                              CustomerRestClient customerRestClient,
                              ProductRestClient productRestClient) {
        this.billRepository = billRepository;
        this.productItemRepository = productItemRepository;
        this.customerRestClient = customerRestClient;
        this.productRestClient = productRestClient;
    }

    /**
     * Endpoint permettant de récupérer une facture complète avec les détails du Client
     * et les détails de chaque Produit consommés via OpenFeign.
     */
    @GetMapping(path = {"/fullBill/{id}", "/bills/full/{id}"})
    public ResponseEntity<Bill> getFullBill(@PathVariable(name = "id") Long id) {
        return billRepository.findById(id)
                .map(bill -> {
                    // Récupération du Client via OpenFeign
                    try {
                        Customer customer = customerRestClient.getCustomerById(bill.getCustomerId());
                        bill.setCustomer(customer);
                    } catch (Exception e) {
                        System.err.println("Impossible de charger le client id=" + bill.getCustomerId() + " : " + e.getMessage());
                    }

                    // Récupération de chaque Produit de la facture via OpenFeign
                    if (bill.getProductItems() != null) {
                        bill.getProductItems().forEach(pi -> {
                            try {
                                Product product = productRestClient.getProductById(pi.getProductId());
                                pi.setProduct(product);
                            } catch (Exception e) {
                                System.err.println("Impossible de charger le produit id=" + pi.getProductId() + " : " + e.getMessage());
                            }
                        });
                    }
                    return ResponseEntity.ok(bill);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/bills")
    public List<Bill> getAllBills() {
        List<Bill> bills = billRepository.findAll();
        bills.forEach(bill -> {
            try {
                bill.setCustomer(customerRestClient.getCustomerById(bill.getCustomerId()));
            } catch (Exception ignored) {}

            if (bill.getProductItems() != null) {
                bill.getProductItems().forEach(pi -> {
                    try {
                        pi.setProduct(productRestClient.getProductById(pi.getProductId()));
                    } catch (Exception ignored) {}
                });
            }
        });
        return bills;
    }

    @GetMapping("/bills/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return billRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/bills")
    public ResponseEntity<Bill> createBill(@RequestBody Bill bill) {
        if (bill.getBillingDate() == null) {
            bill.setBillingDate(new Date());
        }
        Bill savedBill = billRepository.save(bill);
        if (bill.getProductItems() != null) {
            bill.getProductItems().forEach(pi -> {
                pi.setBill(savedBill);
                productItemRepository.save(pi);
            });
        }
        return new ResponseEntity<>(savedBill, HttpStatus.CREATED);
    }

    /**
     * Endpoint pour tester le rechargement dynamique des paramètres centralisés depuis Config-Service
     */
    @GetMapping("/params")
    public Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("taxRate", taxRate);
        params.put("currency", currency);
        params.put("welcomeMessage", welcomeMessage);
        return params;
    }
}
