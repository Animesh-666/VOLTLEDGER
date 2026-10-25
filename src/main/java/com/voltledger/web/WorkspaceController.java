package com.voltledger.web;

import com.voltledger.analytics.BillingAnalyticsService;
import com.voltledger.data.BillArchiveRepository;
import com.voltledger.data.BillArchiveService;
import com.voltledger.data.UserAccountRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WorkspaceController {
    private final BillArchiveRepository bills;
    private final BillArchiveService archive;
    private final UserAccountRepository users;
    private final BillingAnalyticsService analytics;

    public WorkspaceController(BillArchiveRepository bills, BillArchiveService archive,
        UserAccountRepository users, BillingAnalyticsService analytics) {
        this.bills=bills; this.archive=archive; this.users=users; this.analytics=analytics;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ? "redirect:/admin" : "redirect:/account";
    }

    @GetMapping("/admin")
    public String admin(Model model,
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "") String tariff,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var all = bills.allInvoices();
        var selected = (from != null && to != null && from.isAfter(to)) ? java.util.List.<com.voltledger.data.InvoiceSummary>of() :
            bills.searchInvoices(null, q, tariff, from, to);
        model.addAttribute("invoices", selected);
        model.addAttribute("billCount", all.size());
        model.addAttribute("consumerCount", bills.countConsumers());
        model.addAttribute("totalBilled", all.stream().map(i -> i.total()).reduce(BigDecimal.ZERO, BigDecimal::add));
        addReports(model, all, selected.size(), q, tariff, from, to);
        return "admin";
    }

    @GetMapping("/admin/bills/new")
    public String newBill(Model model) {
        if (!model.containsAttribute("billForm")) model.addAttribute("billForm", new BillForm());
        model.addAttribute("consumerAccounts", users.consumers());
        return "new-bill";
    }

    @PostMapping("/admin/bills")
    public String createBill(@Valid @ModelAttribute("billForm") BillForm form, BindingResult errors,
        RedirectAttributes redirect, Model model) {
        if (form.getOwnerUsername() == null || form.getOwnerUsername().isBlank())
            errors.rejectValue("ownerUsername", "required", "Choose a registered consumer account");
        if (errors.hasErrors()) {
            model.addAttribute("consumerAccounts", users.consumers());
            return "new-bill";
        }
        try {
            var bill = archive.create(form, form.getOwnerUsername());
            redirect.addFlashAttribute("success", "Saved invoice " + bill.invoiceNumber() + " successfully.");
            return "redirect:/admin";
        } catch (IllegalArgumentException | DataIntegrityViolationException ex) {
            errors.reject("invalid.bill", "Check account ownership, consumer ID and meter number. " +
                "For existing consumers, use Manage consumers to update account assignment first.");
            model.addAttribute("consumerAccounts", users.consumers());
            return "new-bill";
        }
    }

    @GetMapping("/admin/consumers")
    public String consumers(Model model) {
        model.addAttribute("consumerRecords", bills.allConsumers());
        model.addAttribute("consumerAccounts", users.consumers());
        return "consumers";
    }

    @PostMapping("/admin/consumers/{id}/assign")
    public String assignConsumer(@PathVariable("id") String id,
        @RequestParam("ownerUsername") String username, RedirectAttributes redirect) {
        var user = users.findByUsername(username)
            .filter(u -> u.enabled() && "CONSUMER".equals(u.role()))
            .orElseThrow(() -> new IllegalArgumentException("Account is not a registered consumer"));
        if (bills.assignOwner(id, user.username()) == 0) throw new InvoiceNotFound();
        redirect.addFlashAttribute("success", "Consumer " + id + " linked to @" + user.username());
        return "redirect:/admin/consumers";
    }

    @GetMapping("/account")
    public String account(Model model, Authentication auth,
        @RequestParam(defaultValue = "") String q,
        @RequestParam(defaultValue = "") String tariff,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var all = bills.invoicesFor(auth.getName());
        var selected = (from != null && to != null && from.isAfter(to)) ? java.util.List.<com.voltledger.data.InvoiceSummary>of() :
            bills.searchInvoices(auth.getName(), q, tariff, from, to);
        model.addAttribute("invoices", selected);
        model.addAttribute("username", auth.getName());
        model.addAttribute("billCount", all.size());
        model.addAttribute("totalBilled", all.stream().map(i -> i.total()).reduce(BigDecimal.ZERO, BigDecimal::add));
        addReports(model, all, selected.size(), q, tariff, from, to);
        return "account";
    }

    private void addReports(Model model, java.util.List<com.voltledger.data.InvoiceSummary> all,
        int filteredCount, String q, String tariff, LocalDate from, LocalDate to) {
        model.addAttribute("overview", analytics.summarize(all, YearMonth.now()));
        model.addAttribute("filteredCount", filteredCount);
        model.addAttribute("searchQuery", q.length() > 80 ? q.substring(0,80) : q);
        model.addAttribute("selectedTariff", tariff);
        model.addAttribute("startDate", from);
        model.addAttribute("endDate", to);
        model.addAttribute("invalidDates", from != null && to != null && from.isAfter(to));
    }

    @GetMapping("/admin/bills/{number}")
    public String adminInvoice(@PathVariable String number, Model model) {
        model.addAttribute("bill", bills.invoice(number, null).orElseThrow(InvoiceNotFound::new));
        model.addAttribute("backPath", "/admin");
        model.addAttribute("downloadPath", "/admin/bills/" + number + "/pdf");
        return "saved-invoice";
    }

    @GetMapping("/account/bills/{number}")
    public String accountInvoice(@PathVariable String number, Model model, Authentication auth) {
        model.addAttribute("bill", bills.invoice(number, auth.getName()).orElseThrow(InvoiceNotFound::new));
        model.addAttribute("backPath", "/account");
        model.addAttribute("downloadPath", "/account/bills/" + number + "/pdf");
        return "saved-invoice";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static final class InvoiceNotFound extends RuntimeException { }
}
