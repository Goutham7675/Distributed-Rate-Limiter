# Manual Test Files

These scripts default to sending requests to `http://localhost:7675` and assume that the currently running application uses Redis **DB 15**.

It is recommended to execute the scripts in the following order:

1. `./manual-tests/00-reset-db15.sh`
2. `./manual-tests/01-health.sh`
3. `./manual-tests/02-create-user-token-bucket-rule.sh`
4. `./manual-tests/03-list-rules.sh`
5. `./manual-tests/04-check-user-token-bucket.sh`
6. `./manual-tests/05-consume-user-token-bucket-1.sh`
7. `./manual-tests/06-consume-user-token-bucket-2.sh`
8. `./manual-tests/07-consume-user-token-bucket-3-blocked.sh`
9. `./manual-tests/08-create-ip-fixed-window-rule.sh`
10. `./manual-tests/09-consume-ip-fixed-window-1.sh`
11. `./manual-tests/10-consume-ip-fixed-window-2-blocked.sh`
12. `./manual-tests/11-create-endpoint-sliding-window-rule.sh`
13. `./manual-tests/12-consume-endpoint-sliding-window-1.sh`
14. `./manual-tests/13-consume-endpoint-sliding-window-2.sh`
15. `./manual-tests/14-consume-endpoint-sliding-window-3-blocked.sh`

## Expected Results

- **Health Check:** Returns `status=UP`.
- **Token Bucket:** The first two consumption requests succeed, while the third is rate-limited.
- **Fixed Window:** The first request succeeds, while the second is rate-limited.
- **Sliding Window:** The first two requests succeed, while the third is rate-limited.

## Changing the Base URL

To use a different application URL, set the `BASE_URL` environment variable when executing a script:

```bash
BASE_URL=http://localhost:7675 ./manual-tests/01-health.sh
```
