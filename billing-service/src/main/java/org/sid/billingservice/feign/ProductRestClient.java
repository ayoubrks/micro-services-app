package org.sid.billingservice.feign;

import org.sid.billingservice.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "inventory-service")
public interface ProductRestClient {

    @GetMapping(path = "/products/{id}")
    Product getProductById(@PathVariable(name = "id") Long id);

    @GetMapping(path = "/products")
    List<Product> allProducts();
}
