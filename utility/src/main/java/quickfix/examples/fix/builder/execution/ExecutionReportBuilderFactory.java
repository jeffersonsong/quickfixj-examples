package quickfix.examples.fix.builder.execution;

import java.util.HashMap;
import java.util.Map;

public class ExecutionReportBuilderFactory {
	private Map<String, ExecutionReportBuilder> builders = new HashMap<String, ExecutionReportBuilder>();

	public ExecutionReportBuilderFactory() {
		builders.put("FIX.4.0", new FIX40ExecutionReportBuilder());
		builders.put("FIX.4.1", new FIX41ExecutionReportBuilder());
		builders.put("FIX.4.2", new FIX42ExecutionReportBuilder());
		builders.put("FIX.4.3", new FIX43ExecutionReportBuilder());
		builders.put("FIX.4.4", new FIX44ExecutionReportBuilder());
		builders.put("FIXT.1.1", new FIX50ExecutionReportBuilder());
	}

	public ExecutionReportBuilder getExecutionReportBuilder(String beginString) {
		return builders.get(beginString);
	}
}
