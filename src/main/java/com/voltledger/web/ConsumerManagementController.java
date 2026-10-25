package com.voltledger.web;

import com.voltledger.data.BillArchiveRepository;
import com.voltledger.data.UserAccountRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConsumerManagementController {
    private final BillArchiveRepository consumers;
    private final UserAccountRepository users;
    public ConsumerManagementController(BillArchiveRepository consumers, UserAccountRepository users) {
        this.consumers=consumers; this.users=users;
    }

    @GetMapping("/admin/consumers/new")
    public String newConsumer(Model model) {
        model.addAttribute("consumerRecordForm", new ConsumerRecordForm());
        model.addAttribute("consumerAccounts", users.consumers());
        return "consumer-new";
    }

    @PostMapping("/admin/consumers/new")
    public String create(@Valid @ModelAttribute("consumerRecordForm") ConsumerRecordForm form,
        BindingResult errors, Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) return showForm(model);
        boolean validOwner = users.findByUsername(form.getOwnerUsername())
            .filter(u -> u.enabled() && "CONSUMER".equals(u.role())).isPresent();
        if (!validOwner) {
            errors.rejectValue("ownerUsername", "invalid", "Choose an active consumer account");
            return showForm(model);
        }
        try {
            consumers.createConsumer(form.getConsumerId().trim(), form.getFullName().trim(),
                form.getAddress().trim(), form.getMeterNumber().trim(), form.getOwnerUsername());
        } catch (DataIntegrityViolationException exception) {
            errors.reject("duplicate", "This consumer ID or meter is already registered");
            return showForm(model);
        }
        redirect.addFlashAttribute("success", "Created consumer " + form.getConsumerId());
        return "redirect:/admin/consumers";
    }

    private String showForm(Model model) {
        model.addAttribute("consumerAccounts", users.consumers());
        return "consumer-new";
    }
}
