package com.voltledger.web;

import com.voltledger.billing.BillRequest;
import com.voltledger.billing.BillingService;
import com.voltledger.billing.Consumer;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PageController {
    private final BillingService billingService;

    public PageController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/")
    public String home() { return "index"; }

    @GetMapping("/calculator")
    public String calculator(Model model) {
        model.addAttribute("billForm", new BillForm());
        return "calculator";
    }

    @PostMapping("/calculator")
    public String preview(@Valid @ModelAttribute("billForm") BillForm form,
                          BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return "calculator";

        Consumer consumer = new Consumer(
            form.getConsumerId(), form.getConsumerName(),
            form.getAddress(), form.getMeterNumber()
        );
        model.addAttribute("bill", billingService.calculate(
            new BillRequest(consumer, form.getTariffType(), form.getConsumedUnits())
        ));
        return "calculator";
    }

    @GetMapping("/project")
    public String project() { return "project"; }
}
