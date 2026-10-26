package org.sid.billingservice.entities;

import org.springframework.data.rest.core.config.Projection;

import java.util.Date;
import java.util.List;

@Projection(name = "fullBill", types = Bill.class)
public interface BillProjection {
    Long getId();
    Date getBillingDate();
    Long getCustomerId();
    List<ProductItem> getProductItems();
}
