<template>
  <div class="i18n-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Setting /></el-icon>
            国际化配置管理
          </span>
          <el-space>
            <el-tag type="info">
              基准语言: {{ baseLanguageName }}
            </el-tag>
            <el-button
              type="info"
              plain
              @click="showDocumentation"
            >
              <el-icon><QuestionFilled /></el-icon>
              使用文档
            </el-button>
          </el-space>
        </div>
      </template>

      <!-- Tab导航 -->
      <el-tabs v-model="activeTab" type="border-card" class="main-tabs">
        <el-tab-pane name="languages">
          <template #label>
            <span class="tab-label">
              <el-icon><Setting /></el-icon>
              语言管理
            </span>
          </template>
          
          <LanguageList @view-missing="handleViewMissing" />
        </el-tab-pane>

        <el-tab-pane name="bundles">
          <template #label>
            <span class="tab-label">
              <el-icon><Files /></el-icon>
              语言包管理
            </span>
          </template>
          
          <TranslationBundleManagement :languages="languages" />
        </el-tab-pane>

        <el-tab-pane name="statistics">
          <template #label>
            <span class="tab-label">
              <el-icon><DataAnalysis /></el-icon>
              翻译统计
              <el-badge
                v-if="totalMissingKeys > 0"
                :value="totalMissingKeys"
                :max="999"
                class="badge"
              />
            </span>
          </template>
          
          <TranslationStatistics :initial-locale="targetLocale" />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 使用文档对话框 -->
    <el-dialog
      v-model="docDialogVisible"
      title="国际化配置管理 - 使用文档"
      width="800px"
    >
      <div class="documentation">
        <el-collapse v-model="activeDocSections">
          <el-collapse-item title="📚 功能概述" name="overview">
            <p>国际化配置管理系统帮助您集中管理多语言配置，包括：</p>
            <ul>
              <li><strong>语言管理</strong>：新增、编辑、启用/停用语言，设置默认语言</li>
              <li><strong>语言包管理</strong>：上传/下载语言包，版本管理与回滚</li>
              <li><strong>翻译统计</strong>：查看翻译进度、缺失项、质量问题</li>
            </ul>
          </el-collapse-item>

          <el-collapse-item title="🚀 快速开始" name="quickstart">
            <h4>1. 新增语言</h4>
            <p>在「语言管理」Tab中点击「新增语言」，填写：</p>
            <ul>
              <li>语言名称：使用该语言的自称，如"中文"、"English"</li>
              <li>Locale代码：符合BCP 47标准，如 zh-CN, en-US</li>
              <li>国旗图标：可输入Emoji国旗 🇨🇳</li>
              <li>文本方向：LTR（左到右）或RTL（右到左）</li>
            </ul>

            <h4>2. 上传语言包</h4>
            <p>在「语言包管理」Tab中：</p>
            <ol>
              <li>选择目标语言，点击「上传」按钮</li>
              <li>选择JSON或Excel文件（最大5MB）</li>
              <li>选择导入策略（覆盖/仅新增/替换）</li>
              <li>系统自动校验文件格式和内容</li>
              <li>确认无误后点击「确认导入」</li>
            </ol>

            <h4>3. 下载缺失翻译模板</h4>
            <p>在「语言包管理」Tab中：</p>
            <ol>
              <li>找到目标语言，点击「缺失模板」按钮</li>
              <li>选择Excel格式（适合翻译人员）</li>
              <li>下载后填写翻译列</li>
              <li>重新上传即可</li>
            </ol>
          </el-collapse-item>

          <el-collapse-item title="⚙️ 最佳实践" name="best-practices">
            <h4>Locale命名规范</h4>
            <p>使用 BCP 47 标准：<code>语言代码-地区代码</code></p>
            <ul>
              <li>zh-CN：中文（简体）- 中国大陆</li>
              <li>zh-TW：中文（繁體）- 台湾</li>
              <li>en-US：English - 美国</li>
              <li>es-MX：Español - 墨西哥</li>
            </ul>

            <h4>翻译Key命名规范</h4>
            <p>使用小写字母和点号分隔：<code>模块.组件.元素</code></p>
            <ul>
              <li>✅ common.button.save</li>
              <li>✅ auth.login.username_placeholder</li>
              <li>❌ btn_save（避免缩写）</li>
              <li>❌ auth.login.page.form.input.username（过长）</li>
            </ul>

            <h4>导入策略选择</h4>
            <ul>
              <li><strong>覆盖模式</strong>（推荐）：同名key覆盖，新key添加，旧key保留</li>
              <li><strong>仅新增模式</strong>：只添加不存在的key，适合部分更新</li>
              <li><strong>替换模式</strong>（危险）：完全替换，旧数据清空，谨慎使用</li>
            </ul>

            <h4>版本管理建议</h4>
            <ul>
              <li>小更新使用自动递增版本（v1.0 → v1.1）</li>
              <li>大更新使用自定义版本（v2.0）</li>
              <li>每次上传填写变更说明，便于追溯</li>
              <li>重要版本可手动备份到本地</li>
            </ul>
          </el-collapse-item>

          <el-collapse-item title="⚠️ 注意事项" name="warnings">
            <el-alert
              type="warning"
              :closable="false"
              style="margin-bottom: 12px"
            >
              <template #title>
                <strong>以下操作不可撤销，请谨慎：</strong>
              </template>
              <ul>
                <li>删除语言：该语言的所有翻译数据将被删除</li>
                <li>替换模式导入：当前语言包数据会被完全清空</li>
                <li>修改Locale：可能导致所有翻译数据失效</li>
              </ul>
            </el-alert>

            <h4>停用默认语言</h4>
            <p>系统默认语言不能停用，必须先设置其他语言为默认。</p>

            <h4>RTL语言支持</h4>
            <p>选择RTL（右到左）文本方向时，请确保前端已支持RTL布局适配，否则可能导致界面错乱。</p>

            <h4>大文件上传</h4>
            <p>语言包文件最大5MB，建议按命名空间拆分以提升性能。</p>

            <h4>并发操作</h4>
            <p>多人同时上传同一语言包可能导致版本冲突，建议协调时间或使用版本控制。</p>
          </el-collapse-item>

          <el-collapse-item title="🔍 故障排查" name="troubleshooting">
            <h4>上传失败："JSON格式无效"</h4>
            <p>解决方案：</p>
            <ul>
              <li>使用在线工具验证JSON语法</li>
              <li>检查是否有多余的逗号或括号</li>
              <li>确保文件编码为UTF-8</li>
            </ul>

            <h4>翻译未生效</h4>
            <p>解决方案：</p>
            <ul>
              <li>检查语言是否已启用</li>
              <li>清除浏览器缓存并刷新页面</li>
              <li>确认翻译key拼写正确</li>
              <li>查看浏览器控制台是否有错误</li>
            </ul>

            <h4>翻译进度不准确</h4>
            <p>解决方案：</p>
            <ul>
              <li>在「翻译统计」Tab点击「刷新统计」</li>
              <li>系统每5分钟自动更新统计</li>
            </ul>
          </el-collapse-item>

          <el-collapse-item title="📞 获取帮助" name="support">
            <p>如遇到问题，请联系：</p>
            <ul>
              <li><strong>技术支持</strong>：tech-support@example.com</li>
              <li><strong>翻译协作</strong>：i18n-team@example.com</li>
              <li><strong>文档中心</strong>：https://docs.example.com/i18n</li>
            </ul>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  QuestionFilled,
  Setting,
  Files,
  DataAnalysis,
} from '@element-plus/icons-vue'
import LanguageList from './components/LanguageList.vue'
import TranslationBundleManagement from './components/TranslationBundleManagement.vue'
import TranslationStatistics from './components/TranslationStatistics.vue'
import { getLanguageList, type Language } from '@/api/i18n'

// ==================== 响应式数据 ====================

const activeTab = ref('languages')
const languages = ref<Language[]>([])
const docDialogVisible = ref(false)
const activeDocSections = ref(['overview'])
const targetLocale = ref<string>() // 用于跳转到翻译统计时传递locale

// 计算基准语言名称
const baseLanguageName = computed(() => {
  const baseLanguage = languages.value.find(lang => lang.isDefault)
  return baseLanguage ? `${baseLanguage.name} (${baseLanguage.locale})` : 'zh-CN'
})

// 计算总缺失key数
const totalMissingKeys = computed(() => {
  return languages.value.reduce((total, lang) => {
    return total + (lang.missingKeysCount || 0)
  }, 0)
})

// ==================== 生命周期 ====================

onMounted(() => {
  fetchLanguages()
})

// ==================== 方法 ====================

/**
 * 获取语言列表
 */
async function fetchLanguages() {
  try {
    const res = await getLanguageList()
    const data = Array.isArray(res) ? res : res.data || []
    languages.value = data

    // Mock数据（后端未实现时）
    if (languages.value.length === 0) {
      languages.value = getMockLanguages()
    }
  } catch (error) {
    console.error('获取语言列表失败:', error)
    languages.value = getMockLanguages()
  }
}

/**
 * 处理查看缺失翻译
 */
function handleViewMissing(locale: string) {
  targetLocale.value = locale
  activeTab.value = 'statistics'
  
  ElMessage.info(`切换到翻译统计，查看 ${locale} 的缺失翻译`)
}

/**
 * 显示使用文档
 */
function showDocumentation() {
  docDialogVisible.value = true
}

/**
 * 获取Mock语言数据
 */
function getMockLanguages(): Language[] {
  return [
    {
      id: 1,
      locale: 'zh-CN',
      name: '中文',
      flagIcon: '🇨🇳',
      textDirection: 'ltr',
      isEnabled: true,
      isDefault: true,
      sortOrder: 1,
      translationProgress: 100,
      missingKeysCount: 0,
      version: 'v2.3',
      updatedAt: '2024-12-03 14:30:00',
    },
    {
      id: 2,
      locale: 'en-US',
      name: 'English',
      flagIcon: '🇺🇸',
      textDirection: 'ltr',
      isEnabled: true,
      isDefault: false,
      sortOrder: 2,
      translationProgress: 85,
      missingKeysCount: 156,
      version: 'v2.1',
      updatedAt: '2024-12-01 10:15:00',
    },
    {
      id: 3,
      locale: 'es-MX',
      name: 'Español',
      flagIcon: '🇲🇽',
      textDirection: 'ltr',
      isEnabled: true,
      isDefault: false,
      sortOrder: 3,
      translationProgress: 62,
      missingKeysCount: 398,
      version: 'v1.8',
      updatedAt: '2024-11-28 16:45:00',
    },
    {
      id: 4,
      locale: 'id-ID',
      name: 'Indonesia',
      flagIcon: '🇮🇩',
      textDirection: 'ltr',
      isEnabled: false,
      isDefault: false,
      sortOrder: 4,
      translationProgress: 35,
      missingKeysCount: 679,
      version: 'v1.2',
      updatedAt: '2024-11-20 09:00:00',
    },
  ]
}
</script>

<style scoped lang="scss">
.i18n-management {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: bold;
    }
  }

  .main-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 16px;
    }

    .tab-label {
      display: flex;
      align-items: center;
      gap: 6px;
      position: relative;

      .badge {
        position: absolute;
        top: -8px;
        right: -16px;
      }
    }
  }

  .documentation {
    max-height: 600px;
    overflow-y: auto;

    h4 {
      margin: 16px 0 8px;
      color: #303133;
      font-size: 14px;
    }

    p {
      margin: 8px 0;
      line-height: 1.6;
      color: #606266;
    }

    ul, ol {
      margin: 8px 0;
      padding-left: 24px;

      li {
        margin: 4px 0;
        line-height: 1.6;
        color: #606266;
      }
    }

    code {
      padding: 2px 6px;
      background: #f5f7fa;
      border: 1px solid #dcdfe6;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
      color: #e6a23c;
    }

    strong {
      color: #303133;
    }

    :deep(.el-collapse-item__header) {
      font-size: 15px;
      font-weight: 500;
    }

    :deep(.el-alert) {
      ul {
        margin: 8px 0 0;
        padding-left: 20px;
      }
    }
  }
}
</style>

