package dev.sagar.finance_copilot_mcp;

record EvaluationResponse(Evaluation evaluation, String feedback) {
	public enum Evaluation {

		PASS, NEEDS_IMPROVEMENT, FAIL, UNKNOWN

	}

}
