package com.main.netbankingapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private Double totalBalance;
    private Integer accountCount;
    private Integer recentTransactions;
}
