# Manual Test Files

这套脚本默认请求 `http://localhost:8080`，并假设当前运行中的应用使用的是 Redis `DB 15`。

建议按下面顺序执行：

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

预期结果：

- 健康检查返回 `status=UP`
- Token Bucket: 前两次消费通过，第三次被限流
- Fixed Window: 第一次通过，第二次被限流
- Sliding Window: 前两次通过，第三次被限流

如果你想换地址，可以这样执行：

```bash
BASE_URL=http://localhost:8080 ./manual-tests/01-health.sh
```
