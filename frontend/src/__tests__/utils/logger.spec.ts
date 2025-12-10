import { describe, it, expect } from "vitest";
import logger from "@/utils/logger";

describe("utils/logger.ts", () => {
  it("deve exportar uma instância do logger", () => {
    expect(logger).toBeDefined();
    expect(typeof logger.info).toBe("function");
    expect(typeof logger.error).toBe("function");
    expect(typeof logger.warn).toBe("function");
    expect(typeof logger.success).toBe("function");
  });
});
