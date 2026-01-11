# Next Steps - Test Structural Improvements

**Status:** Analysis complete, ready for implementation  
**Date:** 2026-01-11  
**Priority:** Focus on structural changes only

---

## 🎯 What to Do Next

### Option 1: Start with Easy Win (RECOMMENDED)
**Task:** Consolidate PainelService unit tests  
**Files:** `backend/src/test/java/sgc/painel/PainelService*.java`  
**Effort:** 2-3 hours  
**Complexity:** LOW  
**Why start here:** Good learning, low risk, quick impact

**Steps:**
1. Read Task 1.3 in `test-improvement-tracking.md` (lines 123-212)
2. Review current PainelService test files
3. Create @Nested structure in PainelServiceTest.java
4. Copy tests from CoverageTest and UpdateTest
5. Test after each change
6. Delete old files after verification

### Option 2: Tackle the Big One
**Task:** Split ProcessoFacadeTest  
**Files:** `backend/src/test/java/sgc/processo/service/ProcessoFacade*.java`  
**Effort:** 6-8 hours  
**Complexity:** HIGH  
**Why:** Biggest impact, critical issue

**Steps:**
1. Read Task 1.2 in `test-improvement-tracking.md` (lines 79-122)
2. Study the 7 @Nested classes structure
3. Create 4 new test files one by one
4. Test after EACH new file
5. Delete old files only after all tests pass

---

## 📚 Key Documents

1. **test-structural-changes-analysis.md** - Read this FIRST
   - Complete analysis with findings
   - Anti-patterns identified
   - Implementation guidelines

2. **test-improvement-tracking.md** - Detailed task breakdown
   - Task 1.1: UsuarioService (SKIP - already correct)
   - Task 1.2: ProcessoFacadeTest (CRITICAL - split needed)
   - Task 1.3: PainelService (HIGH - consolidation needed)

3. **test-quality-report.md** - Original analysis
4. **regras/guia-testes-junit.md** - Testing standards

---

## ⚡ Quick Commands

```bash
# Count test files
find backend/src/test -name "*Test.java" | wc -l

# Run specific module tests
./gradlew :backend:test --tests "sgc.painel.*"
./gradlew :backend:test --tests "sgc.processo.*ProcessoFacade*"

# Run all backend tests (if Java 21 available)
./gradlew :backend:test

# Check file sizes
wc -l backend/src/test/java/sgc/processo/service/ProcessoFacadeTest.java
wc -l backend/src/test/java/sgc/painel/PainelService*.java
```

---

## ✅ Success Criteria

### For Each Task
- [ ] Test count unchanged (before == after)
- [ ] All tests pass
- [ ] No duplicate tests
- [ ] Clear @Nested organization
- [ ] File sizes reasonable (<450 lines)
- [ ] Old files deleted (after verification)

### Overall
- [ ] Reduce test files: 147 → ~140
- [ ] Eliminate large files (>800 lines)
- [ ] Better maintainability
- [ ] Update tracking document with progress

---

## 🚨 Important Notes

1. **ONE TASK AT A TIME** - Don't try to do everything at once
2. **TEST FREQUENTLY** - After each change, not just at the end
3. **COMMIT OFTEN** - Small commits, easy to rollback if needed
4. **COUNT TESTS** - Before and after must match
5. **READ FIRST** - Review all documentation before starting

---

## 💡 Tips

- Start with PainelService (easier, builds confidence)
- Keep original files until 100% verified
- Use @Nested classes for organization
- Extract shared mocks to @BeforeEach
- Test after EACH new file creation
- Update progress in tracking document

---

**Good luck! Start with the easy win (PainelService) to build momentum.**
