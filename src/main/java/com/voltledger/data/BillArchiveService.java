package com.voltledger.data;

import com.voltledger.billing.BillRequest;
import com.voltledger.billing.BillingService;
import com.voltledger.billing.Consumer;
import com.voltledger.billing.ElectricityBill;
import com.voltledger.web.BillForm;
import org.springframework.stereotype.Service;

/** Orchestrates storage; all prices are calculated in BillingService, never taken from the browser. */
@Service
public class BillArchiveService {
    private final BillingService calculator;
    private final BillArchiveRepository repository;
    private final UserAccountRepository users;

    public BillArchiveService(BillingService calculator, BillArchiveRepository repository, UserAccountRepository users) {
        this.calculator = calculator;
        this.repository = repository;
        this.users = users;
    }

    public ElectricityBill create(BillForm form, String ownerUsername) {
        var account = users.findByUsername(ownerUsername)
            .filter(u -> u.enabled() && "CONSUMER".equals(u.role()))
            .orElseThrow(() -> new IllegalArgumentException("Select a valid active consumer account"));
        Consumer consumer = new Consumer(form.getConsumerId(), form.getConsumerName(),
            form.getAddress(), form.getMeterNumber());
        ElectricityBill result = calculator.calculate(new BillRequest(consumer, form.getTariffType(), form.getConsumedUnits()));
        repository.save(result, ownerUsername);
        return result;
    }
}
