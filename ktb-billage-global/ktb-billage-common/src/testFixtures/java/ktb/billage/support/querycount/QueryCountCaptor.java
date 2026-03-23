package ktb.billage.support.querycount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import org.hibernate.resource.jdbc.spi.StatementInspector;

public final class QueryCountCaptor {

    private static final List<String> CAPTURED_SQL = new CopyOnWriteArrayList<>();

    private QueryCountCaptor() {
    }

    public static void clear() {
        CAPTURED_SQL.clear();
    }

    public static List<String> snapshot() {
        return List.copyOf(CAPTURED_SQL);
    }

    public static <T> QueryCountResult<T> measure(Supplier<T> action) {
        clear();
        T result = action.get();
        return QueryCountResult.of(result, snapshot());
    }

    public static Map<String, Long> aggregate(List<String> sqls) {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String sql : sqls) {
            counts.merge(normalizeWhitespace(sql), 1L, Long::sum);
        }
        return counts;
    }

    private static String normalizeWhitespace(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    public static final class SqlCaptureStatementInspector implements StatementInspector {

        @Override
        public String inspect(String sql) {
            if (sql != null && !sql.isBlank()) {
                CAPTURED_SQL.add(sql);
            }
            return sql;
        }
    }
}
