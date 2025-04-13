package dev.sagar.finance_copilot_mcp;

import java.util.List;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FinanceCopilotMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceCopilotMcpApplication.class, args);
	}

	@Bean
	public List<ToolCallback> financeAnalysisTools(PersonalFinanceService personalFinanceService) {
		return List.of(ToolCallbacks.from(personalFinanceService));
	}

}
