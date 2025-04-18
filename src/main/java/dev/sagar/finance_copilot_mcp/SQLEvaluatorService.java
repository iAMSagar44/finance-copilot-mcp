package dev.sagar.finance_copilot_mcp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
class SQLEvaluatorService {

	private static final org.slf4j.Logger logger =
			org.slf4j.LoggerFactory.getLogger(SQLEvaluatorService.class);

	@Value("classpath:/prompts/schema.st")
	private Resource ddlResource;

	@Value("classpath:/prompts/sql-evaluator-prompt.st")
	private Resource systemPromptTemplate;

	@Value("${app.database.type}")
	private String databaseType;

	private final DatabaseService databaseService;

	private static final String USER_MESSAGE_TEMPLATE =
			"""
					For the given question and database table schema, validate the SQL query and provide feedback.
					                The user's question is -
					                {question}

					                The Table Schema is -
					                {ddl}

					                The transaction_type column is restricted to only two possible values: 'DEBIT' and 'CREDIT'.

					                Possible list of Categories -
					                {categories}

					                The SQL query is -
					                {sql_query}

					                Ensure the transaction_detail and category columns are converted to lowercase for case-insensitive matching.

					                Today's date is {current_date}

					                CRITICAL - The SQL query should be compatible with the given schema and must be compatible with {database_type} and dialect.

					                        """;

	private final ChatClient chatClient;

	public SQLEvaluatorService(ChatClient.Builder builder, DatabaseService databaseService) {
		this.chatClient = builder
				.defaultOptions(
						OpenAiChatOptions.builder().model("o3-mini").temperature(1D).build())
				.build();
		this.databaseService = databaseService;
	}

	EvaluationResponse evaluateQuery(String userQuestion, String sqlQuery) throws IOException {

		String schema = ddlResource.getContentAsString(Charset.defaultCharset());

		logger.info("Retrieving categories from the database.");
		var categories = String.join(", ", databaseService.getCategories());

		var evaluationResponse = chatClient.prompt()
				.system(systemSpec -> systemSpec.text(systemPromptTemplate))
				.user(userSpec -> userSpec.text(USER_MESSAGE_TEMPLATE)
						.param("question", userQuestion).param("ddl", schema)
						.param("categories", categories).param("sql_query", sqlQuery)
						.param("current_date",
								LocalDate.now().format(
										DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)))
						.param("database_type", databaseType))
				.call().entity(EvaluationResponse.class);

		logger.info("Evaluation: {} \n Feedback: {}", evaluationResponse.evaluation(),
				evaluationResponse.feedback());

		return evaluationResponse;

	}

}
