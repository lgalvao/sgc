package sgc.e2e;

public class E2eTestContext {
    private static final ThreadLocal<String> currentTestId = new ThreadLocal<>();

    public static void setCurrentTestId(String testId) {
        currentTestId.set(testId);
    }

    public static String getCurrentTestId() {
        return currentTestId.get();
    }

    public static void clear() {
        currentTestId.remove();
    }
}
