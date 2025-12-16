package com.maze.internet_cafe.report;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
public class BillingReportController {

    private final BillingReportService billingReportService;

    public BillingReportController(BillingReportService billingReportService) {
        this.billingReportService = billingReportService;
    }

    @GetMapping("/reports/daily")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DailyReportDto> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return billingReportService.dailyReport(from, to);
    }
}

