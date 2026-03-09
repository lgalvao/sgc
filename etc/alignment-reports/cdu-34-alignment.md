# CDU-34 - Enviar lembrete de prazo - Alignment

## Current Status
- The test creates a process, initiates it, and navigates to the subprocess.
- It verifies the manual sending of a reminder by the ADMIN and checks if the history/alert is recorded.
- It verifies the reception of the alert on the destination unit's panel.

## Gaps & Missing Coverage
1. **Indicator UI:** Step 2 of the requirement says "O sistema exibe os processos e subprocessos, indicando com cores ou ícones aqueles que estão próximos do prazo ou atrasados." The test does not verify these visual indicators (colors/icons).
2. **Mass Selection:** Step 3 says "O usuário seleciona uma ou mais unidades/subprocessos que possuem pendências." The test currently opens a specific subprocess and clicks "Enviar lembrete" inside it, rather than selecting multiple from the tracking screen.
3. **Email Notification Check:** The test does not verify the actual dispatch/payload of the email sent to the unit's responsibles, only the internal history and alert.

## Recommended Changes
- Add a setup scenario where a process is genuinely close to its deadline or delayed, and assert the presence of the correct color/icon indicators in the UI.
- Verify the bulk selection mechanism from the tracking view, rather than only testing the individual reminder inside a subprocess.
- Add coverage/verification for the email dispatch mechanism.
