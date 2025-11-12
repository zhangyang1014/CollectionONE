# SuperAdmin 账号设置说明

## 方法一：浏览器控制台设置（推荐，快速测试）

1. 打开浏览器开发者工具（F12 或 Cmd+Option+I）
2. 切换到 Console（控制台）标签
3. 执行以下代码：

```javascript
// 设置 SuperAdmin 用户信息
const userStore = window.__VUE_DEVTOOLS_GLOBAL_HOOK__?.stores?.get('user') || 
                  (() => {
                    // 如果无法直接访问store，使用localStorage
                    const userInfo = {
                      id: 1,
                      username: 'superadmin',
                      role: 'SuperAdmin',
                      email: 'admin@cco.com',
                      name: '超级管理员'
                    }
                    localStorage.setItem('userInfo', JSON.stringify(userInfo))
                    localStorage.setItem('token', 'superadmin-token-' + Date.now())
                    console.log('SuperAdmin 用户信息已设置')
                    console.log('用户信息:', userInfo)
                    return { userInfo }
                  })()
```

或者更简单的方法，直接在控制台执行：

```javascript
localStorage.setItem('userInfo', JSON.stringify({
  id: 1,
  username: 'superadmin',
  role: 'SuperAdmin',
  email: 'admin@cco.com',
  name: '超级管理员'
}))
localStorage.setItem('token', 'superadmin-token-' + Date.now())
location.reload()
```

## 方法二：通过 Vue DevTools（如果已安装）

1. 安装 Vue DevTools 浏览器扩展
2. 打开 Vue DevTools
3. 找到 Pinia store，选择 `user` store
4. 调用 `setUserInfo` 方法，传入：
```javascript
{
  id: 1,
  username: 'superadmin',
  role: 'SuperAdmin',
  email: 'admin@cco.com',
  name: '超级管理员'
}
```

## SuperAdmin 账号信息

- **用户名**: superadmin
- **角色**: SuperAdmin
- **邮箱**: admin@cco.com
- **密码**: 无需密码（当前系统未实现登录功能）

## 注意事项

1. 设置后刷新页面即可生效
2. 用户信息会保存在 localStorage 中
3. 如果需要清除，执行：`localStorage.removeItem('userInfo')` 和 `localStorage.removeItem('token')`

