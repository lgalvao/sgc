# CDU-20 - Analisar validação de mapa de competências - Alignment

## Current Status
- The test covers a full map validation flow.
- It tests the CHEFE validating the map.
- It tests a GESTOR opening the devolution modal, checking the mandatory observation, and canceling it.
- It tests GESTORs accepting the map.
- It tests ADMIN homologating the map.

## Gaps & Missing Coverage
1. **Devolution Execution:** The test *cancels* the devolution. It never actually executes the devolution to check the resulting state, movement, alerts, and emails (Steps 8.5-8.11).
2. **Alerts & Notifications for Devolution:** Steps 8.9 and 8.10 dictate emails and alerts to the lower unit upon devolution. This is untested.
3. **Alerts & Notifications for Accept:** Steps 9.7 and 9.8 dictate emails and alerts to the superior unit upon acceptance. The test doesn't check these explicitly.
4. **History/Movements:** Steps 8.8 (Devolution) and 9.6 (Accept) specify specific movement descriptions. The test does not assert the internal logs.

## Recommended Changes
- Add a specific negative/devolution scenario where a GESTOR actually confirms the devolution. Verify the state changes to 'Mapa com sugestões' or similar, and check the movement, alert, and email.
- In the accept/homologation scenarios, add assertions for the internal alerts and email notifications.
- Verify the internal movement log texts match the requirements.