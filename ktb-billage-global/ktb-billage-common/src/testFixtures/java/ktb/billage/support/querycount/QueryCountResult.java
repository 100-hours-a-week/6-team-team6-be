package ktb.billage.support.querycount;

import java.util.List;
import java.util.Map;

public record QueryCountResult<T>(
        T result,
        List<String> executedSql,
        Map<String, Long> aggregatedQueryCounts
) {

    public static <T> QueryCountResult<T> of(T result, List<String> executedSql) {
        return new QueryCountResult<>(result, executedSql, QueryCountCaptor.aggregate(executedSql));
    }

    public long totalQueryCount() {
        return executedSql.size();
    }

    public String describe() {
        StringBuilder builder = new StringBuilder();
        builder.append("total: ").append(totalQueryCount());
        aggregatedQueryCounts.forEach((sql, count) ->
                builder.append(System.lineSeparator()).append(count).append("x | ").append(sql)
        );
        return builder.toString();
    }
}
