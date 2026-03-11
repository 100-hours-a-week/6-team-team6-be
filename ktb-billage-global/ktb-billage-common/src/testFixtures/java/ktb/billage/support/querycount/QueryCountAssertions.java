package ktb.billage.support.querycount;

public final class QueryCountAssertions {

    private QueryCountAssertions() {
    }

    public static void assertQueryCountLessThanOrEqual(QueryCountResult<?> result, long maxQueryCount) {
        long actual = result.totalQueryCount();
        if (actual > maxQueryCount) {
            throw new AssertionError("Expected query count <= " + maxQueryCount + ", but was " + actual
                    + System.lineSeparator() + result.describe());
        }
    }

    public static void assertQueryCountLessThan(QueryCountResult<?> result, long maxExclusiveQueryCount) {
        long actual = result.totalQueryCount();
        if (actual >= maxExclusiveQueryCount) {
            throw new AssertionError("Expected query count < " + maxExclusiveQueryCount + ", but was " + actual
                    + System.lineSeparator() + result.describe());
        }
    }
}
