<template>
  <div class="task-page">
    <!-- 左侧：版本列表 -->
    <aside class="version-panel">
      <div class="version-header">
        <span class="version-title">版本</span>
        <el-button
          v-if="isProjectOwner"
          type="success"
          size="small"
          :icon="Plus"
          @click="openVersionCreate"
        >
          新建
        </el-button>
      </div>
      <div class="version-list" v-loading="loadingVersions">
        <div
          v-for="v in versions"
          :key="v.id"
          :class="['version-item', { active: v.id === selectedVersionId }]"
          @click="selectVersion(v)"
        >
          <div class="version-item-top">
            <span class="version-name">{{ v.name }}</span>
            <el-tag :type="versionTagType(v.status)" size="small">
              {{ versionStatusText(v.status) }}
            </el-tag>
          </div>
          <div class="version-item-meta">
            {{ v.description || '暂无描述' }}
          </div>
        </div>
        <div v-if="!loadingVersions && versions.length === 0" class="version-empty">
          暂无版本，点击右上角新建
        </div>
      </div>
    </aside>

    <!-- 右侧：单据 -->
    <section class="ticket-panel">
      <template v-if="currentVersion">
        <div class="ticket-header">
          <div class="ticket-header-info">
            <h3>{{ currentVersion.name }}</h3>
            <el-tag :type="versionTagType(currentVersion.status)" size="small">
              {{ versionStatusText(currentVersion.status) }}
            </el-tag>
          </div>
          <div v-if="isProjectOwner && currentVersion.status === 0" class="ticket-header-actions">
            <el-button size="small" @click="setVersionStatus(1)">关闭</el-button>
            <el-button size="small" type="warning" plain @click="setVersionStatus(2)">取消</el-button>
            <el-button size="small" type="danger" plain @click="deleteVersion">删除</el-button>
          </div>
        </div>

        <el-tabs v-model="activeTicketTab" class="ticket-tabs">
          <el-tab-pane label="需求单" name="requirement">
            <div class="ticket-toolbar">
              <el-button
                v-if="canCreateRequirement"
                type="success"
                size="small"
                :icon="Plus"
                @click="openRequirementCreate"
              >
                新建需求
              </el-button>
            </div>
            <div class="ticket-list" v-loading="loadingTickets">
              <div v-for="r in requirements" :key="r.id" class="ticket-card">
                <div class="ticket-card-main">
                  <span class="ticket-title">{{ r.title }}</span>
                  <el-tag :type="ticketTagType(r.status)" size="small">
                    {{ ticketStatusText(r.status) }}
                  </el-tag>
                </div>
                <div class="ticket-content">{{ r.content }}</div>
                <div class="ticket-footer">
                  <span class="ticket-owner">负责人：{{ userName(r.ownerId) }}</span>
                  <div class="ticket-actions">
                    <el-button
                      v-if="r.ownerId === auth.userId && r.status === 0"
                      size="small" text type="primary" @click="openRequirementEdit(r)"
                    >编辑</el-button>
                    <el-button
                      v-if="r.ownerId === auth.userId && r.status === 0"
                      size="small" text @click="openTransfer(0, r)"
                    >转包</el-button>
                    <el-button
                      v-if="r.ownerId === auth.userId && r.status === 0"
                      size="small" text type="warning" @click="openStatus(0, r)"
                    >状态</el-button>
                    <el-button size="small" text @click="openFlow(0, r)">流转</el-button>
                  </div>
                </div>
              </div>
              <div v-if="!loadingTickets && requirements.length === 0" class="ticket-empty">
                暂无需求单
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="Bug 单" name="bug">
            <div class="ticket-toolbar">
              <el-button
                type="success"
                size="small"
                :icon="Plus"
                @click="openBugCreate"
              >
                新建 Bug
              </el-button>
            </div>
            <div class="ticket-list" v-loading="loadingTickets">
              <div v-for="b in bugs" :key="b.id" class="ticket-card">
                <div class="ticket-card-main">
                  <span class="ticket-title">{{ b.title }}</span>
                  <el-tag :type="ticketTagType(b.status)" size="small">
                    {{ ticketStatusText(b.status) }}
                  </el-tag>
                </div>
                <div class="ticket-content">{{ b.detail }}</div>
                <div v-if="b.reason" class="ticket-extra">原因：{{ b.reason }}</div>
                <div v-if="b.solution" class="ticket-extra">方案：{{ b.solution }}</div>
                <div class="ticket-footer">
                  <span class="ticket-owner">负责人：{{ userName(b.ownerId) }}</span>
                  <div class="ticket-actions">
                    <el-button
                      v-if="b.creatorId === auth.userId && b.status === 0"
                      size="small" text type="primary" @click="openBugEdit(b)"
                    >编辑</el-button>
                    <el-button
                      v-if="b.creatorId === auth.userId && b.status === 0"
                      size="small" text @click="openTransfer(1, b)"
                    >转包</el-button>
                    <el-button
                      v-if="b.ownerId === auth.userId && b.status === 0"
                      size="small" text type="warning" @click="openStatus(1, b)"
                    >状态</el-button>
                    <el-button size="small" text @click="openFlow(1, b)">流转</el-button>
                  </div>
                </div>
              </div>
              <div v-if="!loadingTickets && bugs.length === 0" class="ticket-empty">
                暂无 Bug 单
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </template>

      <div v-else class="ticket-placeholder">
        请在左侧选择一个版本
      </div>
    </section>

    <!-- 新建版本 -->
    <el-dialog v-model="versionCreateVisible" title="新建版本" width="440px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="版本名称" required>
          <el-input v-model="versionForm.name" placeholder="请输入版本名称" maxlength="30" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="versionForm.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="计划发布时间">
          <el-date-picker
            v-model="versionForm.planReleaseTime"
            type="datetime"
            placeholder="选择时间（可选）"
            value-format="x"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="versionCreateVisible = false">取消</el-button>
        <el-button type="success" :loading="submitting" @click="submitVersionCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑需求单 -->
    <el-dialog v-model="requirementCreateVisible" title="新建需求单" width="480px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="requirementForm.title" placeholder="请输入标题" maxlength="50" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="requirementForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="负责人" required>
          <el-select v-model="requirementForm.ownerId" placeholder="选择负责人" style="width: 100%" filterable>
            <el-option
              v-for="f in friendStore.friends"
              :key="f.id"
              :label="f.name"
              :value="f.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requirementCreateVisible = false">取消</el-button>
        <el-button type="success" :loading="submitting" @click="submitRequirementCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="requirementEditVisible" title="编辑需求单" width="480px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="requirementEditForm.title" maxlength="50" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="requirementEditForm.content" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requirementEditVisible = false">取消</el-button>
        <el-button type="success" :loading="submitting" @click="submitRequirementEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新建/编辑 bug 单 -->
    <el-dialog v-model="bugCreateVisible" title="新建 Bug 单" width="480px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="bugForm.title" placeholder="请输入标题" maxlength="50" />
        </el-form-item>
        <el-form-item label="明细" required>
          <el-input v-model="bugForm.detail" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="截图">
          <el-input v-model="bugForm.screenshot" placeholder="文件地址（可选）" />
        </el-form-item>
        <el-form-item label="负责人" required>
          <el-select v-model="bugForm.ownerId" placeholder="选择负责人" style="width: 100%" filterable>
            <el-option
              v-for="f in friendStore.friends"
              :key="f.id"
              :label="f.name"
              :value="f.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bugCreateVisible = false">取消</el-button>
        <el-button type="success" :loading="submitting" @click="submitBugCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bugEditVisible" title="编辑 Bug 单" width="480px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="bugEditForm.title" maxlength="50" />
        </el-form-item>
        <el-form-item label="明细" required>
          <el-input v-model="bugEditForm.detail" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="截图">
          <el-input v-model="bugEditForm.screenshot" placeholder="文件地址（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bugEditVisible = false">取消</el-button>
        <el-button type="success" :loading="submitting" @click="submitBugEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 转包 -->
    <el-dialog v-model="transferVisible" title="转包" width="440px" destroy-on-close>
      <p class="dialog-tip">单据：{{ transferForm.title }}</p>
      <el-form label-width="80px">
        <el-form-item label="转入人" required>
          <el-select v-model="transferForm.toUserId" placeholder="选择转入人" style="width: 100%" filterable>
            <el-option
              v-for="f in friendStore.friends"
              :key="f.id"
              :label="f.name"
              :value="f.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" required>
          <el-input v-model="transferForm.remark" type="textarea" :rows="2" placeholder="转包备注（必填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="transferVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTransfer">确认转包</el-button>
      </template>
    </el-dialog>

    <!-- 修改状态 -->
    <el-dialog v-model="statusVisible" title="修改状态" width="440px" destroy-on-close>
      <p class="dialog-tip">单据：{{ statusForm.title }}</p>
      <el-form label-width="80px">
        <el-form-item label="状态" required>
          <el-select v-model="statusForm.status" style="width: 100%">
            <el-option label="已完成" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <template v-if="statusForm.ticketType === 1">
          <el-form-item label="原因">
            <el-input v-model="statusForm.reason" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="方案">
            <el-input v-model="statusForm.solution" type="textarea" :rows="2" />
          </el-form-item>
          <el-form-item label="结果截图">
            <el-input v-model="statusForm.resultScreenshot" placeholder="文件地址（可选）" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStatus">确认</el-button>
      </template>
    </el-dialog>

    <!-- 流转记录 -->
    <el-dialog v-model="flowVisible" title="流转记录" width="520px" destroy-on-close>
      <el-timeline v-if="flowRecords.length > 0">
        <el-timeline-item
          v-for="(fr, i) in flowRecords"
          :key="fr.id || i"
          :timestamp="formatTime(fr.createTime)"
          :type="fr.action === 0 ? 'primary' : 'warning'"
        >
          <div class="flow-line">
            <b>{{ flowActionText(fr.action) }}</b>
            <span>{{ userName(fr.operatorId) }}</span>
            <span v-if="fr.action === 1 && fr.toUserId"> → {{ userName(fr.toUserId) }}</span>
          </div>
          <div v-if="fr.remark" class="flow-remark">备注：{{ fr.remark }}</div>
        </el-timeline-item>
      </el-timeline>
      <div v-else class="ticket-empty">暂无流转记录</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth.js'
import { useFriendStore } from '@/stores/friend.js'

const auth = useAuthStore()
const friendStore = useFriendStore()

const versions = ref([])
const selectedVersionId = ref(null)
const activeTicketTab = ref('requirement')
const requirements = ref([])
const bugs = ref([])
const loadingVersions = ref(false)
const loadingTickets = ref(false)
const submitting = ref(false)

const currentVersion = computed(() =>
  versions.value.find(v => v.id === selectedVersionId.value)
)

const isProjectOwner = computed(() => auth.user?.duty === 0)
const canCreateRequirement = computed(() =>
  auth.user?.duty === 0 || auth.user?.duty === 1
)

const userMap = computed(() => {
  const map = {}
  friendStore.friends.forEach(f => { map[f.id] = f.name })
  return map
})
const userName = (id) => userMap.value[id] || '未知'

// ---- 文案映射 ----
const versionStatusText = (v) => ({ 0: '进行中', 1: '已关闭', 2: '已取消' })[v] ?? '未知'
const versionTagType = (v) => ({ 0: 'success', 1: 'info', 2: 'danger' })[v] ?? 'info'
const ticketStatusText = (v) => ({ 0: '开发中', 1: '已完成', 2: '已取消' })[v] ?? '未知'
const ticketTagType = (v) => ({ 0: 'primary', 1: 'success', 2: 'info' })[v] ?? 'info'
const flowActionText = (v) => ({ 0: '创建', 1: '转包' })[v] ?? '未知'

// ---- 对话框表单 ----
const versionCreateVisible = ref(false)
const versionForm = reactive({ name: '', description: '', planReleaseTime: null })

const requirementCreateVisible = ref(false)
const requirementForm = reactive({ title: '', content: '', ownerId: '' })
const requirementEditVisible = ref(false)
const requirementEditForm = reactive({ requirementId: '', title: '', content: '' })

const bugCreateVisible = ref(false)
const bugForm = reactive({ title: '', detail: '', screenshot: '', ownerId: '' })
const bugEditVisible = ref(false)
const bugEditForm = reactive({ bugId: '', title: '', detail: '', screenshot: '' })

const transferVisible = ref(false)
const transferForm = reactive({ ticketType: 0, ticketId: '', title: '', toUserId: '', remark: '' })

const statusVisible = ref(false)
const statusForm = reactive({
  ticketType: 0, ticketId: '', title: '', status: 1,
  reason: '', solution: '', resultScreenshot: ''
})

const flowVisible = ref(false)
const flowRecords = ref([])

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(Number(ts))
  return d.toLocaleString('zh-CN', { hour12: false })
}

// ---- 版本 ----
async function loadVersions() {
  loadingVersions.value = true
  try {
    versions.value = await request.get('/task/version/list')
    if (versions.value.length > 0 && !selectedVersionId.value) {
      selectVersion(versions.value[0])
    }
  } catch (e) {
    versions.value = []
  } finally {
    loadingVersions.value = false
  }
}

function selectVersion(v) {
  selectedVersionId.value = v.id
  activeTicketTab.value = 'requirement'
  loadTickets()
}

function openVersionCreate() {
  versionForm.name = ''
  versionForm.description = ''
  versionForm.planReleaseTime = null
  versionCreateVisible.value = true
}

async function submitVersionCreate() {
  if (!versionForm.name.trim()) {
    ElMessage.warning('请输入版本名称')
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: versionForm.name.trim(),
      description: versionForm.description
    }
    if (versionForm.planReleaseTime) {
      payload.planReleaseTime = Number(versionForm.planReleaseTime)
    }
    await request.post('/task/version/create', payload)
    ElMessage.success('版本创建成功')
    versionCreateVisible.value = false
    loadVersions()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

async function setVersionStatus(status) {
  const label = status === 1 ? '关闭' : '取消'
  try {
    await ElMessageBox.confirm(`确定要${label}该版本吗？`, `${label}版本`, {
      confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning'
    })
    await request.post('/task/version/status', { versionId: currentVersion.value.id, status })
    ElMessage.success(`版本已${label}`)
    loadVersions()
    loadTickets()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error(`${label}失败`)
    }
  }
}

async function deleteVersion() {
  try {
    await ElMessageBox.confirm('确定要删除该版本吗？（版本下存在单据时无法删除）', '删除版本', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning'
    })
    await request.post('/task/version/delete', null, { params: { versionId: currentVersion.value.id } })
    ElMessage.success('版本已删除')
    selectedVersionId.value = null
    requirements.value = []
    bugs.value = []
    loadVersions()
  } catch (e) {
    if (e !== 'cancel' && e?.message !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ---- 单据 ----
async function loadTickets() {
  if (!selectedVersionId.value) return
  loadingTickets.value = true
  try {
    const [reqs, bgs] = await Promise.all([
      request.get('/task/requirement/list', { params: { versionId: selectedVersionId.value } }),
      request.get('/task/bug/list', { params: { versionId: selectedVersionId.value } })
    ])
    requirements.value = reqs || []
    bugs.value = bgs || []
  } catch (e) {
    requirements.value = []
    bugs.value = []
  } finally {
    loadingTickets.value = false
  }
}

function openRequirementCreate() {
  requirementForm.title = ''
  requirementForm.content = ''
  requirementForm.ownerId = ''
  requirementCreateVisible.value = true
}

async function submitRequirementCreate() {
  if (!requirementForm.title.trim() || !requirementForm.content.trim() || !requirementForm.ownerId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await request.post('/task/requirement/create', {
      versionId: currentVersion.value.id,
      title: requirementForm.title.trim(),
      content: requirementForm.content.trim(),
      ownerId: requirementForm.ownerId
    })
    ElMessage.success('需求单已创建')
    requirementCreateVisible.value = false
    loadTickets()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

function openRequirementEdit(r) {
  requirementEditForm.requirementId = r.id
  requirementEditForm.title = r.title
  requirementEditForm.content = r.content
  requirementEditVisible.value = true
}

async function submitRequirementEdit() {
  submitting.value = true
  try {
    await request.post('/task/requirement/update', {
      requirementId: requirementEditForm.requirementId,
      title: requirementEditForm.title,
      content: requirementEditForm.content
    })
    ElMessage.success('已保存')
    requirementEditVisible.value = false
    loadTickets()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

function openBugCreate() {
  bugForm.title = ''
  bugForm.detail = ''
  bugForm.screenshot = ''
  bugForm.ownerId = ''
  bugCreateVisible.value = true
}

async function submitBugCreate() {
  if (!bugForm.title.trim() || !bugForm.detail.trim() || !bugForm.ownerId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    await request.post('/task/bug/create', {
      versionId: currentVersion.value.id,
      title: bugForm.title.trim(),
      detail: bugForm.detail.trim(),
      screenshot: bugForm.screenshot || null,
      ownerId: bugForm.ownerId
    })
    ElMessage.success('Bug 单已创建')
    bugCreateVisible.value = false
    loadTickets()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

function openBugEdit(b) {
  bugEditForm.bugId = b.id
  bugEditForm.title = b.title
  bugEditForm.detail = b.detail
  bugEditForm.screenshot = b.screenshot || ''
  bugEditVisible.value = true
}

async function submitBugEdit() {
  submitting.value = true
  try {
    await request.post('/task/bug/update', {
      bugId: bugEditForm.bugId,
      title: bugEditForm.title,
      detail: bugEditForm.detail,
      screenshot: bugEditForm.screenshot || null
    })
    ElMessage.success('已保存')
    bugEditVisible.value = false
    loadTickets()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

// ---- 转包 / 状态 / 流转 ----
function openTransfer(ticketType, ticket) {
  transferForm.ticketType = ticketType
  transferForm.ticketId = ticket.id
  transferForm.title = ticket.title
  transferForm.toUserId = ''
  transferForm.remark = ''
  transferVisible.value = true
}

async function submitTransfer() {
  if (!transferForm.toUserId || !transferForm.remark.trim()) {
    ElMessage.warning('请选择转入人并填写备注')
    return
  }
  submitting.value = true
  try {
    await request.post('/task/ticket/transfer', {
      ticketType: transferForm.ticketType,
      ticketId: transferForm.ticketId,
      toUserId: transferForm.toUserId,
      remark: transferForm.remark.trim()
    })
    ElMessage.success('转包成功')
    transferVisible.value = false
    loadTickets()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

function openStatus(ticketType, ticket) {
  statusForm.ticketType = ticketType
  statusForm.ticketId = ticket.id
  statusForm.title = ticket.title
  statusForm.status = 1
  statusForm.reason = ''
  statusForm.solution = ''
  statusForm.resultScreenshot = ''
  statusVisible.value = true
}

async function submitStatus() {
  submitting.value = true
  try {
    await request.post('/task/ticket/status', {
      ticketType: statusForm.ticketType,
      ticketId: statusForm.ticketId,
      status: statusForm.status,
      reason: statusForm.reason || null,
      solution: statusForm.solution || null,
      resultScreenshot: statusForm.resultScreenshot || null
    })
    ElMessage.success('状态已更新')
    statusVisible.value = false
    loadTickets()
  } catch (e) {
  } finally {
    submitting.value = false
  }
}

async function openFlow(ticketType, ticket) {
  flowRecords.value = []
  flowVisible.value = true
  try {
    flowRecords.value = await request.get('/task/ticket/flow', {
      params: { ticketType, ticketId: ticket.id }
    })
  } catch (e) {
    flowRecords.value = []
  }
}

onMounted(() => {
  friendStore.loadFriends()
  loadVersions()
})
</script>

<style scoped>
.task-page {
  display: flex;
  flex: 1;
  min-height: 0;
}

/* ---- 版本面板 ---- */
.version-panel {
  width: 280px;
  min-width: 280px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
}

.version-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.version-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.version-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.version-list::-webkit-scrollbar { width: 4px; }
.version-list::-webkit-scrollbar-thumb { background: #ccc; border-radius: 2px; }

.version-item {
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 6px;
  transition: background 0.15s;
}

.version-item:hover { background: #f5f5f5; }
.version-item.active { background: #ecf5ff; }

.version-item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.version-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-item-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.version-empty {
  padding: 40px 0;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

/* ---- 单据面板 ---- */
.ticket-panel {
  flex: 1;
  min-width: 0;
  background: #f5f6f8;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ticket-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.ticket-header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ticket-header-info h3 {
  margin: 0;
  font-size: 17px;
  color: #303133;
}

.ticket-tabs {
  flex: 1;
  overflow: hidden;
  padding: 0 20px;
  background: #f5f6f8;
}

.ticket-tabs :deep(.el-tabs__content) {
  overflow-y: auto;
  padding-bottom: 24px;
}

.ticket-toolbar {
  padding: 12px 0;
}

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ticket-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.ticket-card-main {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.ticket-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.ticket-content {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  word-break: break-word;
}

.ticket-extra {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.ticket-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #f5f5f5;
  padding-top: 8px;
}

.ticket-owner {
  font-size: 12px;
  color: #909399;
}

.ticket-actions {
  display: flex;
  gap: 4px;
}

.ticket-empty {
  padding: 60px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.ticket-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 15px;
}

.dialog-tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
}

.flow-line {
  font-size: 13px;
  color: #303133;
}

.flow-remark {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
</style>
