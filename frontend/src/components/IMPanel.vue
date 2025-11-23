<template>
  <div class="im-panel">
    <!-- 左侧：联系人列表 -->
    <div class="contacts-sidebar">
      <!-- 筛选器和搜索框 -->
      <div class="contacts-filters">
        <!-- 搜索框 -->
        <div class="contact-search">
          <el-input
            v-model="contactSearchKeyword"
            placeholder="可模糊搜索姓名"
            size="small"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <!-- 筛选器下拉框（一行显示） -->
        <div class="filter-row">
          <el-select 
            v-model="contactTypeFilter" 
            size="small" 
            placeholder="联系人"
            class="filter-select"
          >
            <el-option label="全部" value="all" />
            <el-option label="本人" value="self" />
            <el-option label="紧急" value="emergency" />
          </el-select>
          
          <el-select 
            v-model="communicationFilter" 
            size="small" 
            placeholder="沟通筛选"
            class="filter-select"
          >
            <el-option label="全部" value="all" />
            <el-option label="未发送" value="no_sent" />
            <el-option label="有发送无答复" value="sent_no_reply" />
            <el-option label="有答复" value="has_reply" />
          </el-select>
        </div>
      </div>
      
      <div class="contacts-list">
        <div 
          v-for="contact in contactsWithUnread" 
          :key="contact.id"
          class="contact-item"
          :class="{ active: selectedContactId === contact.id }"
          @click="selectContact(contact)"
        >
          <div class="contact-avatar">
            <el-icon :size="24"><User /></el-icon>
            <span v-if="contact.hasUnread" class="unread-dot"></span>
          </div>
          <div class="contact-info">
            <div class="contact-relation-row">
              <span class="contact-relation">{{ contact.relation }}</span>
              <span v-if="contact.relation !== '本人' && contact.relation_level > 0" class="contact-stars">
                <el-icon 
                  v-for="star in contact.relation_level" 
                  :key="star" 
                  class="star-icon"
                  :size="12"
                >
                  <StarFilled />
                </el-icon>
              </span>
            </div>
            <div class="contact-details">
              <span class="contact-name">{{ contact.name }}</span>
              <span class="contact-phone">{{ contact.phone_last4 }}</span>
            </div>
          </div>
          <!-- 电话状态图标（只显示有电话渠道的联系人） -->
          <div class="contact-phone-status" v-if="contact.channels && contact.channels.includes('call') && contact.phoneStatus">
            <!-- 未拨打：绿色电话 -->
            <el-icon 
              v-if="contact.phoneStatus === 'never_called'" 
              class="phone-status-icon phone-status-never-called"
              :size="18"
              :title="'未拨打'"
            >
              <Phone />
            </el-icon>
            <!-- 从未接通：空心电话 -->
            <el-icon 
              v-else-if="contact.phoneStatus === 'never_connected'" 
              class="phone-status-icon phone-status-never-connected"
              :size="18"
              :title="'从未接通'"
            >
              <Phone />
            </el-icon>
            <!-- 播过且接通：对号 -->
            <el-icon 
              v-else-if="contact.phoneStatus === 'connected'" 
              class="phone-status-icon phone-status-connected"
              :size="18"
              :title="'已接通'"
            >
              <CircleCheck />
            </el-icon>
            <!-- 号码不存在：电话上打x -->
            <el-icon 
              v-else-if="contact.phoneStatus === 'invalid_number'" 
              class="phone-status-icon phone-status-invalid"
              :size="18"
              :title="'号码不存在'"
            >
              <CircleCloseFilled />
            </el-icon>
          </div>
        </div>
      </div>
      
      <div class="add-contact-btn">
        <el-button class="add-contact-button" @click="showAddContactDialog" :icon="Plus" style="width: 100%;">
          新增联系人
        </el-button>
      </div>
    </div>

    <!-- 中间：聊天窗口 -->
    <div class="chat-area">
      <div v-if="selectedContact" class="chat-container">
        <!-- 渠道Tab -->
        <el-tabs v-model="activeChannel" class="channel-tabs">
          <!-- 会话聚合 -->
          <el-tab-pane name="aggregated">
            <template #label>
              <span class="tab-label">
                <el-icon><ChatLineRound /></el-icon>
                <span>会话聚合</span>
                <span v-if="selectedContact && hasUnreadMessagesForContact(selectedContact.id)" class="channel-unread-dot"></span>
              </span>
            </template>
            <div class="messages-container" ref="messagesContainer">
              <div v-for="message in aggregatedMessages" :key="message.id" class="message-wrapper">
                <div :class="['message-item', message.sender_type === 'collector' ? 'message-sent' : 'message-received']">
                  
                  <div class="message-bubble">
                    <!-- 消息内容 -->
                    <div v-if="message.type === 'text'" class="message-content">
                      {{ message.content }}
                    </div>
                    <div v-else-if="message.type === 'image'" class="message-image">
                      <el-image :src="message.content" fit="cover" style="max-width: 200px; max-height: 200px;" />
                    </div>
                    <div v-else-if="message.type === 'voice'" class="message-voice">
                      <el-icon><Microphone /></el-icon>
                      <span>语音消息</span>
                    </div>
                    
                    <!-- 消息元数据 -->
                    <div class="message-meta">
                      <span class="message-channel">{{ getChannelLabel(message.channel) }}</span>
                      <span class="message-time">{{ formatMessageTime(message.sent_at) }}</span>
                      <el-icon v-if="message.sender_type === 'collector'" class="message-status">
                        <Select v-if="message.status === 'read'" style="color: #25D366;" />
                        <CircleCheck v-else-if="message.status === 'delivered'" style="color: #8696a0;" />
                        <Clock v-else style="color: #8696a0;" />
                      </el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- WhatsApp渠道 -->
          <el-tab-pane name="whatsapp">
            <template #label>
              <span class="tab-label">
                <el-icon><ChatDotRound /></el-icon>
                <span>WhatsApp</span>
                <span v-if="channelHasUnread('whatsapp')" class="channel-unread-dot"></span>
              </span>
            </template>
            <div class="messages-container" ref="whatsappContainer">
              <div v-for="message in whatsappMessages" :key="message.id" class="message-wrapper">
                <div :class="['message-item', message.sender_type === 'collector' ? 'message-sent' : 'message-received']">
                  
                  <div class="message-bubble">
                    <div v-if="message.type === 'text'" class="message-content">
                      {{ message.content }}
                    </div>
                    <div v-else-if="message.type === 'image'" class="message-image">
                      <el-image :src="message.content" fit="cover" style="max-width: 200px; max-height: 200px;" />
                    </div>
                    <div v-else-if="message.type === 'voice'" class="message-voice">
                      <el-icon><Microphone /></el-icon>
                      <span>语音消息</span>
                    </div>
                    
                    <div class="message-meta">
                      <span class="message-tool">{{ message.tool }}</span>
                      <span class="message-time">{{ formatMessageTime(message.sent_at) }}</span>
                      <el-icon v-if="message.sender_type === 'collector'" class="message-status">
                        <Select v-if="message.status === 'read'" style="color: #25D366;" />
                        <CircleCheck v-else-if="message.status === 'delivered'" style="color: #8696a0;" />
                        <Clock v-else style="color: #8696a0;" />
                      </el-icon>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- 电话外呼 -->
          <el-tab-pane name="call">
            <template #label>
              <span class="tab-label">
                <el-icon><Phone /></el-icon>
                <span>电话外呼</span>
              </span>
            </template>
            <div class="call-panel">
              <!-- 操作按钮区域 -->
              <div class="call-actions">
                <el-button type="primary" @click="handleCallOnce" :icon="Phone">
                  立即呼叫1次
                </el-button>
                <el-button type="success" @click="handleCallUntilAnswer" :icon="Phone">
                  立即呼叫5次直到接通
                </el-button>
                <el-button type="warning" @click="showAICallDialog" :icon="Connection">
                  请求AI机器人外呼协助
                </el-button>
              </div>

              <!-- 历史外呼记录列表 -->
              <div class="call-records-container">
                <el-scrollbar class="call-records-scrollbar">
                  <div v-if="callRecords.length === 0" class="empty-records">
                    <el-empty description="暂无外呼记录" />
                  </div>
                  <div v-else class="call-records-list">
                    <div 
                      v-for="record in callRecords" 
                      :key="record.id"
                      class="call-record-item"
                    >
                      <div class="record-header">
                        <div class="record-type-status">
                          <el-tag :type="getCallStatusTagType(record.status)" size="small">
                            {{ getCallStatusLabel(record.status) }}
                          </el-tag>
                          <el-tag :type="getCallTypeTagType(record.type)" size="small">
                            {{ getCallTypeLabel(record.type) }}
                          </el-tag>
                        </div>
                        <div class="record-time">{{ formatCallTime(record.call_time) }}</div>
                      </div>
                      
                      <div class="record-content">
                        <div class="record-info-line">
                          <span class="info-label">发起人：</span>
                          <span class="info-value">{{ record.caller_name || '本人' }}</span>
                          <span v-if="record.caller_id && record.caller_id !== 'self'" class="info-value-secondary">
                            ({{ record.caller_id }})
                          </span>
                          <span v-if="record.duration" class="info-separator">|</span>
                          <span v-if="record.duration" class="info-item">
                            <span class="info-label">通话时长：</span>
                            <span class="info-value">{{ formatDuration(record.duration) }}</span>
                          </span>
                        </div>
                        <div v-if="record.next_plan" class="record-info-line">
                          <span class="info-item">
                            <span class="info-label">计划下一轮：</span>
                            <span class="info-value next-plan-value">{{ formatNextPlan(record.next_plan) }}</span>
                          </span>
                          <el-button 
                            v-if="record.next_plan && record.next_plan.status === 'scheduled'"
                            text 
                            size="small" 
                            type="danger"
                            class="cancel-plan-btn"
                            @click="cancelNextPlan(record.id)"
                          >
                            终止计划
                          </el-button>
                        </div>
                      </div>
                    </div>
                  </div>
                </el-scrollbar>
              </div>

              <!-- 浏览器权限检查区域（底部） -->
              <div class="call-permissions">
                <div class="permissions-title">浏览器权限状态</div>
                <div class="permissions-list">
                  <!-- 麦克风权限 -->
                  <div class="permission-item" :class="{ 'permission-denied': !microphonePermission.granted }">
                    <div class="permission-info">
                      <el-icon class="permission-icon" :class="{ 'permission-error': !microphonePermission.granted }">
                        <Microphone v-if="microphonePermission.granted" />
                        <CircleCloseFilled v-else />
                      </el-icon>
                      <span class="permission-name">麦克风权限</span>
                      <el-tag 
                        :type="microphonePermission.granted ? 'success' : 'danger'" 
                        size="small"
                        effect="plain"
                        round
                      >
                        {{ microphonePermission.granted ? '已授权' : '未授权' }}
                      </el-tag>
                    </div>
                    <el-button 
                      v-if="!microphonePermission.granted" 
                      type="primary" 
                      size="small" 
                      :icon="Microphone"
                      @click="requestMicrophonePermission"
                      :loading="microphonePermission.requesting"
                    >
                      点击授权
                    </el-button>
                  </div>
                  
                  <!-- 音频输出权限 -->
                  <div class="permission-item" :class="{ 'permission-denied': !audioPermission.granted }">
                    <div class="permission-info">
                      <el-icon class="permission-icon" :class="{ 'permission-error': !audioPermission.granted }">
                        <VideoPlay v-if="audioPermission.granted" />
                        <CircleCloseFilled v-else />
                      </el-icon>
                      <span class="permission-name">音频输出权限</span>
                      <el-tag 
                        :type="audioPermission.granted ? 'success' : 'danger'" 
                        size="small"
                        effect="plain"
                        round
                      >
                        {{ audioPermission.granted ? '已授权' : '未授权' }}
                      </el-tag>
                    </div>
                    <el-button 
                      v-if="!audioPermission.granted" 
                      type="primary" 
                      size="small" 
                      :icon="VideoPlay"
                      @click="requestAudioPermission"
                      :loading="audioPermission.requesting"
                    >
                      点击授权
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <!-- SMS渠道 -->
          <el-tab-pane name="sms">
            <template #label>
              <span class="tab-label">
                <el-icon><Message /></el-icon>
                <span>SMS</span>
                <span v-if="channelHasUnread('sms')" class="channel-unread-dot"></span>
              </span>
            </template>
            <div class="messages-container" ref="smsContainer">
              <div v-for="message in smsMessages" :key="message.id" class="message-wrapper">
                <div class="sms-message-item">
                  <!-- 发送时间 -->
                  <div class="sms-date-divider" v-if="message.showDateDivider">
                    {{ formatMessageDate(message.sent_at) }}
                  </div>
                  
                  <div class="sms-message-card">
                    <!-- 消息内容 -->
                    <div class="sms-content">
                      {{ message.content }}
                    </div>
                    
                    <!-- 消息元信息 -->
                    <div class="sms-meta">
                      <div class="sms-meta-row">
                        <span class="sms-time">{{ formatMessageTime(message.sent_at) }}</span>
                        <span class="sms-sender">
                          <el-icon v-if="message.sender_type === 'ai'"><Connection /></el-icon>
                          <span>{{ message.sender_name }}</span>
                        </span>
                        <el-tag v-if="message.from_template" size="small" type="info" effect="plain">
                          来自模板
                        </el-tag>
                        <el-tag 
                          :type="getSmsStatusType(message.status)" 
                          size="small"
                          effect="plain"
                        >
                          {{ getSmsStatusLabel(message.status) }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <el-empty v-if="smsMessages.length === 0" description="暂无SMS消息" />
            </div>
          </el-tab-pane>

          <!-- RCS渠道 -->
          <el-tab-pane name="rcs">
            <template #label>
              <span class="tab-label">
                <el-icon><Message /></el-icon>
                <span>RCS</span>
                <span v-if="channelHasUnread('rcs')" class="channel-unread-dot"></span>
              </span>
            </template>
            <div class="messages-container" ref="rcsContainer">
              <div v-for="message in rcsMessages" :key="message.id" class="message-wrapper">
                <div class="sms-message-item">
                  <!-- 发送时间 -->
                  <div class="sms-date-divider" v-if="message.showDateDivider">
                    {{ formatMessageDate(message.sent_at) }}
                  </div>
                  
                  <div class="sms-message-card">
                    <!-- 消息内容 -->
                    <div class="sms-content">
                      {{ message.content }}
                    </div>
                    
                    <!-- 消息元信息 -->
                    <div class="sms-meta">
                      <div class="sms-meta-row">
                        <span class="sms-time">{{ formatMessageTime(message.sent_at) }}</span>
                        <span class="sms-sender">
                          <el-icon v-if="message.sender_type === 'ai'"><Connection /></el-icon>
                          <span>{{ message.sender_name }}</span>
                        </span>
                        <el-tag v-if="message.from_template" size="small" type="info" effect="plain">
                          来自模板
                        </el-tag>
                        <el-tag 
                          :type="getSmsStatusType(message.status)" 
                          size="small"
                          effect="plain"
                        >
                          {{ getSmsStatusLabel(message.status) }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <el-empty v-if="rcsMessages.length === 0" description="暂无RCS消息" />
            </div>
          </el-tab-pane>
        </el-tabs>

        <!-- WA账号选择区域（仅在WhatsApp tab下显示） -->
        <div v-if="activeChannel === 'whatsapp' && selectedContact" class="wa-account-selector">
          <div class="wa-account-row">
            <!-- 平台WA -->
            <div class="wa-account-group">
              <div class="wa-group-header">
                <span class="wa-group-title">公司WA</span>
                <span class="wa-group-count">{{ platformWAAccounts.available }}/{{ platformWAAccounts.total }}</span>
                <el-tooltip 
                  content="前面的数字是当前可用的，后面的是本自然月已分配wa的总数字"
                  placement="top"
                >
                  <el-icon class="wa-help-icon"><InfoFilled /></el-icon>
                </el-tooltip>
              </div>
              <div class="wa-avatars">
                <div 
                  v-for="account in platformWAAccounts.accounts" 
                  :key="account.id"
                  class="wa-avatar-item"
                  :class="{ active: selectedWAAccount?.id === account.id && selectedWAAccount?.type === 'platform' }"
                  @click="selectWAAccount(account, 'platform')"
                >
                  <div class="wa-avatar-icon">
                    <el-icon><OfficeBuildingIcon /></el-icon>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 个人WA -->
            <div class="wa-account-group">
              <div class="wa-group-header">
                <span class="wa-group-title">个人WA</span>
                <span class="wa-group-count">{{ personalWAAccounts.available }}/{{ personalWAAccounts.total }}</span>
                <el-tooltip 
                  content="前面的数字是当前可用的，后面的是今日绑定的wa的总数字"
                  placement="top"
                >
                  <el-icon class="wa-help-icon"><InfoFilled /></el-icon>
                </el-tooltip>
              </div>
              <div class="wa-avatars">
                <div 
                  v-for="account in personalWAAccounts.accounts" 
                  :key="account.id"
                  class="wa-avatar-item"
                  :class="{ active: selectedWAAccount?.id === account.id && selectedWAAccount?.type === 'personal' }"
                  @click="selectWAAccount(account, 'personal')"
                >
                  <el-avatar :size="36" :src="account.avatar">
                    <el-icon><User /></el-icon>
                  </el-avatar>
                </div>
                <!-- 添加按钮 -->
                <div class="wa-avatar-item wa-add-btn" @click="showQRCodeDialog">
                  <el-icon><Plus /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div v-if="activeChannel !== 'call'" class="input-area">
          <div class="input-box">
            <el-input
              v-model="messageInput"
              type="textarea"
              :rows="2"
              placeholder="输入消息..."
              @keydown.enter.ctrl="sendMessage"
            />
          </div>
          
          <div class="input-toolbar-bottom">
            <div class="toolbar-left">
              <el-button @click="showTemplateDialog" :icon="Document" text title="消息模板">
                模板
              </el-button>
              <el-upload 
                v-if="activeChannel !== 'sms' && activeChannel !== 'rcs'"
                :auto-upload="false" 
                :show-file-list="false"
                accept="image/*"
                @change="handleImageSelect"
              >
                <el-button :icon="Picture" text title="发送图片">
                  图片
                </el-button>
              </el-upload>
              <el-popover 
                v-if="activeChannel !== 'sms' && activeChannel !== 'rcs'"
                placement="top" 
                :width="350" 
                trigger="click"
              >
                <template #reference>
                  <el-button :icon="Orange" text title="Emoji">
                    表情
                  </el-button>
                </template>
                <div class="emoji-picker">
                  <span v-for="emoji in commonEmojis" :key="emoji" class="emoji-item" @click="insertEmoji(emoji)">
                    {{ emoji }}
                  </span>
                </div>
              </el-popover>
            </div>
            
            <div class="toolbar-right">
              <!-- 当前渠道限制信息 -->
              <div v-if="currentChannelLimit" class="channel-limit-info">
                <span class="limit-count-text">
                  {{ currentChannelLimit.sentCount }} / {{ currentChannelLimit.maxCount }}
                </span>
                <span v-if="currentChannelLimit.nextSendTime" class="limit-time-text">
                  {{ formatNextSendTime(currentChannelLimit.nextSendTime) }}
                </span>
              </div>
              
              <el-button type="success" @click="sendMessage" :icon="Promotion">
                发送 (Ctrl+Enter)
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="no-contact-selected">
        <el-icon :size="80" color="#d1d7db"><ChatDotRound /></el-icon>
        <p>请选择联系人开始对话</p>
      </div>
    </div>

    <!-- 右侧：催记区域 -->
    <div class="case-note-area">
      <div v-if="selectedContact" class="case-note-container">
        <div class="note-header">
          <h3>新增催记 (Case Note)</h3>
          <el-button 
            text 
            type="primary" 
            @click="showHistoryNotesDialog = true"
            class="history-notes-btn"
          >
            <el-icon><Clock /></el-icon>
            历史催记
          </el-button>
        </div>
        
          <el-scrollbar class="note-form-scroll">
          <el-form :model="caseNoteForm" label-width="80px" label-position="top" class="note-form">
            <!-- 自动填充信息（紧凑显示） -->
            <div class="note-auto-info">
              <span class="auto-info-item">
                <label>联系方式：</label>
                <span>{{ caseNoteForm.contact_method }}</span>
              </span>
              <span class="auto-info-item">
                <label>联系人：</label>
                <span>{{ caseNoteForm.contact_name }}</span>
              </span>
              <span class="auto-info-item">
                <label>关系：</label>
                <span>{{ caseNoteForm.relation }}</span>
              </span>
            </div>

            <!-- 关联度（仅非本人时显示） -->
            <el-form-item v-if="!isMainContact" label="关联度" class="relation-level-item">
              <div class="relation-level-row">
                <el-rate 
                  v-model="caseNoteForm.relation_level" 
                  :max="5"
                  show-text
                  :texts="['很低', '较低', '中等', '较高', '很高']"
                  :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                />
                <span class="relation-level-hint">标记该联系人与客户本人的关系紧密程度</span>
              </div>
            </el-form-item>

            <!-- 沟通状态 -->
            <el-form-item label="沟通状态" required>
              <el-radio-group v-model="caseNoteForm.communication_status" class="status-radio-group">
                <el-radio value="reachable">可联</el-radio>
                <el-radio value="not_exist">不存在</el-radio>
                <el-radio value="no_response">未响应</el-radio>
              </el-radio-group>
            </el-form-item>

            <!-- 沟通结果（根据沟通状态动态显示） -->
            <el-form-item label="沟通结果" required>
              <el-radio-group v-model="caseNoteForm.communication_result" class="result-radio-group">
                <!-- 可联 - 本人 -->
                <template v-if="caseNoteForm.communication_status === 'reachable' && isMainContact">
                  <el-radio value="promise_repay">承诺还款</el-radio>
                  <el-radio value="refuse_repay">拒绝还款</el-radio>
                  <el-radio value="not_related">与借款人不相关</el-radio>
                  <el-radio value="other">其它</el-radio>
                </template>
                <!-- 可联 - 非本人 -->
                <template v-if="caseNoteForm.communication_status === 'reachable' && !isMainContact">
                  <el-radio value="not_related">与借款人不相关</el-radio>
                  <el-radio value="related">与借款人相关</el-radio>
                  <el-radio value="promise_repay_on_behalf">承诺代还</el-radio>
                  <el-radio value="promise_inform">承诺转告</el-radio>
                  <el-radio value="other">其他</el-radio>
                </template>
                <!-- 不存在 -->
                <template v-if="caseNoteForm.communication_status === 'not_exist'">
                  <el-radio value="lost_contact">失联</el-radio>
                </template>
                <!-- 未响应 -->
                <template v-if="caseNoteForm.communication_status === 'no_response'">
                  <el-radio value="continuous_follow_up">持续跟进</el-radio>
                </template>
              </el-radio-group>
            </el-form-item>

            <!-- 备注 -->
            <el-form-item label="备注">
              <el-input 
                v-model="caseNoteForm.remark" 
                type="textarea" 
                :rows="4" 
                placeholder="请输入备注信息"
              />
            </el-form-item>

            <!-- 下次跟进时间 -->
            <el-form-item label="下次跟进时间">
              <el-radio-group v-model="caseNoteForm.follow_up_type" style="margin-bottom: 12px;">
                <el-radio value="one_hour">一小时后</el-radio>
                <el-radio value="specific_time">具体时间</el-radio>
              </el-radio-group>
              
              <div v-if="caseNoteForm.follow_up_type === 'specific_time'" class="time-picker-group">
                <el-date-picker
                  v-model="caseNoteForm.follow_up_date"
                  type="date"
                  placeholder="选择日期"
                  :disabled-date="disabledDate"
                  style="width: 100%; margin-bottom: 8px;"
                />
                <div class="time-selectors">
                  <el-select v-model="caseNoteForm.follow_up_hour" placeholder="小时" style="width: 48%;">
                    <el-option v-for="h in 24" :key="h-1" :label="`${h-1}时`" :value="h-1" />
                  </el-select>
                  <el-select v-model="caseNoteForm.follow_up_minute" placeholder="分钟" style="width: 48%;">
                    <el-option label="00分" :value="0" />
                    <el-option label="30分" :value="30" />
                  </el-select>
                </div>
              </div>
            </el-form-item>

            <!-- 提交按钮 -->
            <el-form-item>
              <el-button type="success" @click="submitCaseNote" style="width: 100%;">
                提交催记
              </el-button>
            </el-form-item>
          </el-form>
        </el-scrollbar>
      </div>
      
      <div v-else class="no-contact-for-note">
        <el-icon :size="60" color="#d1d7db"><Document /></el-icon>
        <p>请选择联系人以记录催记</p>
      </div>
    </div>

    <!-- 新增联系人对话框 -->
    <el-dialog v-model="addContactDialogVisible" title="新增联系人" width="500px">
      <el-form :model="newContactForm" label-width="100px">
        <el-form-item label="手机号码" required>
          <el-input v-model="newContactForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="newContactForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="关系" required>
          <el-select v-model="newContactForm.relation" placeholder="请选择关系">
            <el-option label="本人" value="本人" />
            <el-option label="配偶" value="配偶" />
            <el-option label="父亲" value="父亲" />
            <el-option label="母亲" value="母亲" />
            <el-option label="朋友" value="朋友" />
            <el-option label="同事" value="同事" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="支持渠道" required>
          <el-checkbox-group v-model="newContactForm.channels">
            <el-checkbox label="whatsapp">WhatsApp</el-checkbox>
            <el-checkbox label="sms">SMS</el-checkbox>
            <el-checkbox label="call">电话</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addContactDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNewContact">确定</el-button>
      </template>
    </el-dialog>

    <!-- 消息模板对话框 -->
    <el-dialog v-model="templateDialogVisible" title="选择消息模板" width="900px" top="5vh">
      <div class="template-picker">
        <!-- 筛选器 -->
        <div class="template-filters">
          <div class="filter-row">
            <div class="filter-item">
              <label>案件阶段：</label>
              <el-select v-model="templateFilters.stage" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="C（催收前）" value="C" />
                <el-option label="S0（首次联系）" value="S0" />
                <el-option label="S1-3（初期）" value="S1-3" />
                <el-option label="S3+（后期）" value="S3+" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label>组织/个人：</label>
              <el-select v-model="templateFilters.type" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="组织模板" value="organization" />
                <el-option label="个人模板" value="personal" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label>场景：</label>
              <el-select v-model="templateFilters.scene" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="问候" value="greeting" />
                <el-option label="提醒" value="reminder" />
                <el-option label="强度" value="strong" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label>时间点：</label>
              <el-select v-model="templateFilters.timeSlot" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="上午" value="morning" />
                <el-option label="下午" value="afternoon" />
                <el-option label="晚上" value="evening" />
              </el-select>
            </div>
            
            <el-button size="small" @click="resetTemplateFilters">重置</el-button>
          </div>
        </div>
        
        <!-- 模板列表 -->
        <div class="template-list-container">
          <el-scrollbar height="450px">
            <div class="template-list">
              <div 
                v-for="template in filteredTemplates" 
                :key="template.id"
                class="template-card"
                @click="selectTemplate(template)"
              >
                <div class="template-header">
                  <span class="template-title">{{ template.title }}</span>
                  <div class="template-tags">
                    <el-tag size="small" type="info">{{ getStageLabel(template.stage) }}</el-tag>
                    <el-tag size="small" :type="template.type === 'organization' ? 'success' : 'warning'">
                      {{ template.type === 'organization' ? '组织' : '个人' }}
                    </el-tag>
                    <el-tag size="small">{{ getSceneLabel(template.scene) }}</el-tag>
                    <el-tag size="small" type="info">{{ getTimeSlotLabel(template.timeSlot) }}</el-tag>
                  </div>
                </div>
                
                <div class="template-content">
                  {{ renderTemplatePreview(template.content) }}
                </div>
                
                <div v-if="template.variables && template.variables.length > 0" class="template-variables">
                  <span class="variables-label">变量：</span>
                  <el-tag 
                    v-for="variable in template.variables" 
                    :key="variable"
                    size="small"
                    effect="plain"
                  >
                    {{ variable }}
                  </el-tag>
                </div>
              </div>
              
              <el-empty v-if="filteredTemplates.length === 0" description="没有符合条件的模板" />
            </div>
          </el-scrollbar>
        </div>
      </div>
    </el-dialog>

    <!-- AI机器人外呼协助对话框 -->
    <el-dialog v-model="aiCallDialogVisible" title="请求AI机器人外呼协助" width="900px" top="5vh">
      <div class="template-picker">
        <!-- 筛选器 -->
        <div class="template-filters">
          <div class="filter-row">
            <div class="filter-item">
              <label>案件阶段：</label>
              <el-select v-model="aiCallTemplateFilters.stage" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="C（催收前）" value="C" />
                <el-option label="S0（首次联系）" value="S0" />
                <el-option label="S1-3（初期）" value="S1-3" />
                <el-option label="S3+（后期）" value="S3+" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label>组织/个人：</label>
              <el-select v-model="aiCallTemplateFilters.type" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="组织模板" value="organization" />
                <el-option label="个人模板" value="personal" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label>场景：</label>
              <el-select v-model="aiCallTemplateFilters.scene" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="问候" value="greeting" />
                <el-option label="提醒" value="reminder" />
                <el-option label="强度" value="strong" />
              </el-select>
            </div>
            
            <div class="filter-item">
              <label>时间点：</label>
              <el-select v-model="aiCallTemplateFilters.timeSlot" placeholder="全部" size="small" style="width: 120px;">
                <el-option label="全部" value="" />
                <el-option label="上午" value="morning" />
                <el-option label="下午" value="afternoon" />
                <el-option label="晚上" value="evening" />
              </el-select>
            </div>
            
            <el-button size="small" @click="resetAICallTemplateFilters">重置</el-button>
          </div>
        </div>
        
        <!-- 模板列表 -->
        <div class="template-list-container">
          <el-scrollbar height="450px">
            <div class="template-list">
              <div 
                v-for="template in filteredAICallTemplates" 
                :key="template.id"
                class="template-card"
                @click="selectAICallTemplate(template)"
              >
                <div class="template-header">
                  <span class="template-title">{{ template.title }}</span>
                  <div class="template-tags">
                    <el-tag size="small" type="info">{{ getStageLabel(template.stage) }}</el-tag>
                    <el-tag size="small" :type="template.type === 'organization' ? 'success' : 'warning'">
                      {{ template.type === 'organization' ? '组织' : '个人' }}
                    </el-tag>
                    <el-tag size="small">{{ getSceneLabel(template.scene) }}</el-tag>
                    <el-tag size="small" type="info">{{ getTimeSlotLabel(template.timeSlot) }}</el-tag>
                  </div>
                </div>
                
                <div class="template-content">
                  {{ renderTemplatePreview(template.content) }}
                </div>
                
                <div v-if="template.variables && template.variables.length > 0" class="template-variables">
                  <span class="variables-label">变量：</span>
                  <el-tag 
                    v-for="variable in template.variables" 
                    :key="variable"
                    size="small"
                    effect="plain"
                  >
                    {{ variable }}
                  </el-tag>
                </div>
              </div>
              
              <el-empty v-if="filteredAICallTemplates.length === 0" description="没有符合条件的模板" />
            </div>
          </el-scrollbar>
        </div>
      </div>
    </el-dialog>

    <!-- 扫码绑定WA对话框 -->
    <el-dialog 
      v-model="qrCodeDialogVisible" 
      title="扫码绑定WhatsApp账号" 
      width="400px"
      center
    >
      <div class="qr-code-content">
        <div class="qr-code-placeholder">
          <div class="qr-code-icon-placeholder">
            <div class="qr-code-grid">
              <div v-for="(filled, index) in qrCodePattern" :key="index" class="qr-code-cell" :class="{ filled }"></div>
            </div>
          </div>
          <p>请使用WhatsApp扫描二维码</p>
          <p class="qr-tip">打开WhatsApp → 设置 → 已连接的设备 → 连接设备</p>
        </div>
        <el-button type="primary" style="width: 100%; margin-top: 20px;" @click="refreshQRCode">
          <el-icon><Refresh /></el-icon>
          刷新二维码
        </el-button>
      </div>
    </el-dialog>

    <!-- 历史催记对话框 -->
    <el-dialog 
      v-model="showHistoryNotesDialog" 
      title="历史催记" 
      width="1200px" 
      top="5vh"
      class="history-notes-dialog"
    >
      <div class="history-notes-content">
        <!-- 搜索框 -->
        <div class="history-search">
          <el-input
            v-model="historySearchKeyword"
            placeholder="搜索案件ID"
            clearable
            @clear="handleHistorySearch"
            @keyup.enter="handleHistorySearch"
            style="width: 300px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 筛选器 -->
        <div class="history-filters">
          <el-select 
            v-model="historyFilters.collector" 
            placeholder="触达人" 
            clearable 
            style="width: 150px;"
            @change="handleHistoryFilter"
          >
            <el-option 
              v-for="collector in collectorList" 
              :key="collector" 
              :label="collector" 
              :value="collector" 
            />
          </el-select>
          
          <el-select 
            v-model="historyFilters.channel" 
            placeholder="触达渠道" 
            clearable 
            style="width: 150px;"
            @change="handleHistoryFilter"
          >
            <el-option label="WhatsApp" value="WhatsApp" />
            <el-option label="SMS" value="SMS" />
            <el-option label="RCS" value="RCS" />
            <el-option label="电话外呼" value="电话外呼" />
          </el-select>
          
          <el-select 
            v-model="historyFilters.status" 
            placeholder="状态" 
            clearable 
            style="width: 120px;"
            @change="handleHistoryFilter"
          >
            <el-option label="可联" value="reachable" />
            <el-option label="不存在" value="not_exist" />
            <el-option label="未响应" value="no_response" />
          </el-select>
          
          <el-select 
            v-model="historyFilters.result" 
            placeholder="结果" 
            clearable 
            style="width: 150px;"
            @change="handleHistoryFilter"
          >
            <el-option label="承诺还款" value="promise_repay" />
            <el-option label="拒绝还款" value="refuse_repay" />
            <el-option label="失联" value="lost_contact" />
            <el-option label="持续跟进" value="continuous_follow_up" />
          </el-select>
          
          <el-date-picker
            v-model="historyFilters.dateRange"
            type="daterange"
            range-separator="-"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px;"
            @change="handleHistoryFilter"
          />
        </div>

        <!-- 历史催记列表 -->
        <div class="history-notes-list">
          <el-table 
            :data="filteredHistoryNotes" 
            stripe
            style="width: 100%"
            :empty-text="'暂无历史催记'"
            max-height="500"
          >
            <el-table-column prop="register_time" label="登记时间" width="150" />
            <el-table-column prop="case_id" label="案件ID" width="120" />
            <el-table-column prop="collector" label="触达人" width="100" />
            <el-table-column prop="channel" label="触达渠道" width="100" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusTagType(row.status)">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="result" label="结果" width="100">
              <template #default="{ row }">
                <span>{{ getResultLabel(row.result) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
            <el-table-column prop="next_follow_up" label="下次跟进时间" width="150" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { makeCall } from '@/api/infinity'
import { getContactPhoneStatus } from '@/api/case'
import { useUserStore } from '@/stores/user'
import { 
  User, Plus, Document, Picture, Orange, Promotion, ChatDotRound, 
  Microphone, Select, CircleCheck, Clock, Connection, ChatLineRound, Message, Phone,
  Search, InfoFilled, OfficeBuilding as OfficeBuildingIcon, Refresh, VideoPlay, CircleCloseFilled, StarFilled
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

// Props
const props = defineProps<{
  caseData: any
}>()

// 联系人列表
const contacts = ref([
  {
    id: 1,
    name: props.caseData?.user_name || '客户本人',
    phone: props.caseData?.mobile_number || '',
    phone_last4: (props.caseData?.mobile_number || '').slice(-4),
    relation: '本人',
    channels: ['whatsapp', 'sms', 'call'],
    relation_level: 0, // 关联度
    phoneStatus: null as string | null // 电话状态：never_called, never_connected, connected, invalid_number
  },
  // Mock联系人数据 - BTSK案件
  {
    id: 2,
    name: '张三',
    phone: '+91 98 7654 3210',
    phone_last4: '3210',
    relation: '配偶',
    channels: ['whatsapp', 'call'],
    relation_level: 0, // 关联度
    phoneStatus: null as string | null
  },
  {
    id: 3,
    name: '李四',
    phone: '+91 99 8765 4321',
    phone_last4: '4321',
    relation: '朋友',
    channels: ['whatsapp', 'sms'],
    relation_level: 0, // 关联度
    phoneStatus: null as string | null
  },
  // Mock联系人数据 - BTQ案件（用于模拟不同案件）
  {
    id: 4,
    name: 'Carlos Rodriguez',
    phone: '+52 55 1234 5678',
    phone_last4: '5678',
    relation: '本人',
    channels: ['whatsapp', 'sms', 'call'],
    relation_level: 0, // 关联度
    phoneStatus: null as string | null
  },
  {
    id: 5,
    name: 'Maria Garcia',
    phone: '+52 55 2345 6789',
    phone_last4: '6789',
    relation: '配偶',
    channels: ['whatsapp', 'sms'],
    relation_level: 0, // 关联度
    phoneStatus: null as string | null
  },
  {
    id: 6,
    name: 'Juan Lopez',
    phone: '+52 55 3456 7890',
    phone_last4: '7890',
    relation: '朋友',
    channels: ['whatsapp', 'call'],
    relation_level: 0, // 关联度
    phoneStatus: null as string | null
  }
])

const selectedContactId = ref<number | null>(null)
const selectedContact = computed(() => contacts.value.find(c => c.id === selectedContactId.value))

// 渠道Tab
const activeChannel = ref('aggregated')

// 渠道限制信息
const channelLimits = ref([
  {
    channel: 'whatsapp',
    channelName: 'WhatsApp',
    sentCount: 45,
    maxCount: 100,
    nextSendTime: null as Date | null
  },
  {
    channel: 'sms',
    channelName: 'SMS',
    sentCount: 120,
    maxCount: 200,
    nextSendTime: new Date(Date.now() + 5 * 60 * 1000) // 5分钟后
  },
  {
    channel: 'rcs',
    channelName: 'RCS',
    sentCount: 80,
    maxCount: 150,
    nextSendTime: new Date(Date.now() + 2 * 60 * 1000) // 2分钟后
  },
  {
    channel: 'call',
    channelName: '电话外呼',
    sentCount: 30,
    maxCount: 50,
    nextSendTime: null as Date | null
  }
])

// 格式化下一条发送时间
const formatNextSendTime = (time: Date | null) => {
  if (!time) return ''
  const now = new Date()
  const diff = time.getTime() - now.getTime()
  if (diff <= 0) return '可立即发送'
  
  const minutes = Math.floor(diff / 60000)
  const seconds = Math.floor((diff % 60000) / 1000)
  
  if (minutes > 0) {
    return `${minutes}分${seconds}秒后`
  } else {
    return `${seconds}秒后`
  }
}

// 当前渠道的限制信息
const currentChannelLimit = computed(() => {
  // 根据当前激活的渠道获取限制信息
  let channel = activeChannel.value
  // 如果是会话聚合，默认使用WhatsApp的限制
  if (channel === 'aggregated') {
    channel = 'whatsapp'
  }
  
  return channelLimits.value.find(limit => limit.channel === channel) || null
})

// Mock消息数据
const mockMessages = ref([
  {
    id: 1,
    contact_id: 1,
    type: 'text',
    content: '您好，请问您什么时候可以还款？',
    sender_type: 'collector',
    sender_name: '催员小王',
    sender_id: 'collector001',
    tool: '公司WA',
    channel: 'whatsapp',
    status: 'read',
    sent_at: '2025-11-06 10:30:25'
  },
  {
    id: 2,
    contact_id: 1,
    type: 'text',
    content: '我这两天就还',
    sender_type: 'customer',
    sender_name: '客户',
    channel: 'whatsapp',
    status: 'read',
    sent_at: '2025-11-06 10:35:12'
  },
  {
    id: 3,
    contact_id: 1,
    type: 'text',
    content: '好的，请您尽快还款，避免影响信用记录',
    sender_type: 'collector',
    sender_name: '催员小王',
    sender_id: 'collector001',
    tool: '公司WA',
    channel: 'whatsapp',
    status: 'delivered',
    sent_at: '2025-11-06 10:36:45'
  },
  {
    id: 4,
    contact_id: 2,
    type: 'text',
    content: '请问您认识Sanjay Patel吗？',
    sender_type: 'collector',
    sender_name: '催员小王',
    sender_id: 'collector001',
    tool: '个人WA（edisonzh）',
    channel: 'whatsapp',
    status: 'read',
    sent_at: '2025-11-06 11:20:00'
  },
  // SMS消息
  {
    id: 5,
    contact_id: 1,
    type: 'text',
    content: '您好Sanjay Patel，您的贷款BTSK-200100已逾期23天，应还金额10,529。请尽快安排还款，避免影响信用记录。',
    sender_type: 'collector',
    sender_name: '催员小王',
    sender_id: 'collector001',
    channel: 'sms',
    status: 'delivered',
    from_template: true,
    sent_at: '2025-11-06 09:15:30'
  },
  {
    id: 6,
    contact_id: 1,
    type: 'text',
    content: '【BTSK】您的贷款已严重逾期，如不尽快还款将影响您的征信记录。请立即联系我们：+91 96 4870 7764',
    sender_type: 'ai',
    sender_name: 'AI机器人',
    channel: 'sms',
    status: 'read',
    from_template: true,
    sent_at: '2025-11-05 18:30:00'
  },
  {
    id: 7,
    contact_id: 1,
    type: 'text',
    content: '感谢您的还款！我们已收到您的款项5,000。',
    sender_type: 'collector',
    sender_name: '催员小李',
    sender_id: 'collector002',
    channel: 'sms',
    status: 'delivered',
    from_template: false,
    sent_at: '2025-11-04 14:20:15'
  },
  // RCS消息
  {
    id: 8,
    contact_id: 1,
    type: 'text',
    content: '【BTSK】您好，您的贷款BTSK-200100已逾期23天，未还金额10,529。请尽快安排还款，避免影响信用记录。',
    sender_type: 'collector',
    sender_name: '催员小王',
    sender_id: 'collector001',
    channel: 'rcs',
    status: 'delivered',
    from_template: true,
    sent_at: '2025-11-06 11:30:00'
  },
  {
    id: 9,
    contact_id: 1,
    type: 'text',
    content: '感谢您的配合！如已还款，请忽略此消息。',
    sender_type: 'collector',
    sender_name: '催员小李',
    sender_id: 'collector002',
    channel: 'rcs',
    status: 'read',
    from_template: false,
    sent_at: '2025-11-05 15:45:20'
  },
  // ========== 未读消息模拟数据 ==========
  // BTSK案件 - 本人联系人未读消息
  {
    id: 10,
    contact_id: 1,
    type: 'text',
    content: '我需要延期还款，可以吗？',
    sender_type: 'customer',
    sender_name: '客户',
    channel: 'whatsapp',
    status: 'read',
    sent_at: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss')
  },
  // BTSK案件 - 配偶联系人未读消息
  {
    id: 11,
    contact_id: 2,
    type: 'text',
    content: '他最近不在家，等他回来我会转告他的',
    sender_type: 'customer',
    sender_name: '客户',
    channel: 'whatsapp',
    status: 'read',
    sent_at: dayjs().subtract(1, 'hour').format('YYYY-MM-DD HH:mm:ss')
  },
  // BTSK案件 - 朋友联系人未读消息
  {
    id: 12,
    contact_id: 3,
    type: 'text',
    content: '我已经联系他了，他说会尽快处理',
    sender_type: 'customer',
    sender_name: '客户',
    channel: 'sms',
    status: 'read',
    sent_at: dayjs().subtract(30, 'minute').format('YYYY-MM-DD HH:mm:ss')
  },
  // BTSK案件 - 本人SMS渠道未读消息
  {
    id: 13,
    contact_id: 1,
    type: 'text',
    content: '我明天会还款，请不要再催了',
    sender_type: 'customer',
    sender_name: '客户',
    channel: 'sms',
    status: 'read',
    sent_at: dayjs().subtract(15, 'minute').format('YYYY-MM-DD HH:mm:ss')
  },
  // BTSK案件 - 本人RCS渠道未读消息
  {
    id: 14,
    contact_id: 1,
    type: 'text',
    content: '我已经在银行排队了，今天会还',
    sender_type: 'customer',
    sender_name: '客户',
    channel: 'rcs',
    status: 'read',
    sent_at: dayjs().subtract(10, 'minute').format('YYYY-MM-DD HH:mm:ss')
  },
  // BTQ案件模拟 - 需要添加更多联系人ID来模拟不同案件
  // 假设contact_id 4, 5, 6是BTQ案件的联系人
  {
    id: 15,
    contact_id: 4,
    type: 'text',
    content: 'Hola, necesito más tiempo para pagar',
    sender_type: 'customer',
    sender_name: 'Cliente',
    channel: 'whatsapp',
    status: 'read',
    sent_at: dayjs().subtract(45, 'minute').format('YYYY-MM-DD HH:mm:ss')
  },
  {
    id: 16,
    contact_id: 5,
    type: 'text',
    content: 'Voy a pagar mañana, por favor no me molesten más',
    sender_type: 'customer',
    sender_name: 'Cliente',
    channel: 'sms',
    status: 'read',
    sent_at: dayjs().subtract(20, 'minute').format('YYYY-MM-DD HH:mm:ss')
  },
  {
    id: 17,
    contact_id: 6,
    type: 'text',
    content: 'Ya transferí el dinero, debería llegar hoy',
    sender_type: 'customer',
    sender_name: 'Cliente',
    channel: 'whatsapp',
    status: 'read',
    sent_at: dayjs().subtract(5, 'minute').format('YYYY-MM-DD HH:mm:ss')
  }
])

// 聚合消息
const aggregatedMessages = computed(() => {
  if (!selectedContact.value) return []
  return mockMessages.value.filter(m => m.contact_id === selectedContact.value?.id)
})

// WhatsApp消息
const whatsappMessages = computed(() => {
  if (!selectedContact.value) return []
  return mockMessages.value.filter(m => m.contact_id === selectedContact.value?.id && m.channel === 'whatsapp')
})

// SMS消息
const smsMessages = computed(() => {
  if (!selectedContact.value) return []
  const messages = mockMessages.value.filter(m => m.contact_id === selectedContact.value?.id && m.channel === 'sms')
  // 添加日期分隔符
  return messages.map((msg, index) => {
    const prevMsg = index > 0 ? messages[index - 1] : null
    const showDivider = !prevMsg || dayjs(msg.sent_at).format('YYYY-MM-DD') !== dayjs(prevMsg.sent_at).format('YYYY-MM-DD')
    return { ...msg, showDateDivider: showDivider }
  })
})

// RCS消息
const rcsMessages = computed(() => {
  if (!selectedContact.value) return []
  const messages = mockMessages.value.filter(m => m.contact_id === selectedContact.value?.id && m.channel === 'rcs')
  // 添加日期分隔符
  return messages.map((msg, index) => {
    const prevMsg = index > 0 ? messages[index - 1] : null
    const showDivider = !prevMsg || dayjs(msg.sent_at).format('YYYY-MM-DD') !== dayjs(prevMsg.sent_at).format('YYYY-MM-DD')
    return { ...msg, showDateDivider: showDivider }
  })
})

// 检查联系人是否有未读消息（客户消息后没有催员回复）
const hasUnreadMessagesForContact = (contactId: number) => {
  const contactMessages = mockMessages.value
    .filter(m => m.contact_id === contactId)
    .sort((a, b) => dayjs(a.sent_at).valueOf() - dayjs(b.sent_at).valueOf())
  
  // 找到最后一条客户消息
  let lastCustomerMessageIndex = -1
  for (let i = contactMessages.length - 1; i >= 0; i--) {
    if (contactMessages[i].sender_type === 'customer') {
      lastCustomerMessageIndex = i
      break
    }
  }
  
  // 如果没有客户消息，返回false
  if (lastCustomerMessageIndex === -1) return false
  
  // 检查最后一条客户消息之后是否有催员回复
  for (let i = lastCustomerMessageIndex + 1; i < contactMessages.length; i++) {
    if (contactMessages[i].sender_type === 'collector' || contactMessages[i].sender_type === 'ai') {
      return false // 有催员回复，不是未读
    }
  }
  
  return true // 最后一条是客户消息且之后没有催员回复
}

// 检查渠道是否有未读消息
const hasUnreadMessagesForChannel = (contactId: number, channel: string) => {
  const channelMessages = mockMessages.value
    .filter(m => m.contact_id === contactId && m.channel === channel)
    .sort((a, b) => dayjs(a.sent_at).valueOf() - dayjs(b.sent_at).valueOf())
  
  // 找到最后一条客户消息
  let lastCustomerMessageIndex = -1
  for (let i = channelMessages.length - 1; i >= 0; i--) {
    if (channelMessages[i].sender_type === 'customer') {
      lastCustomerMessageIndex = i
      break
    }
  }
  
  // 如果没有客户消息，返回false
  if (lastCustomerMessageIndex === -1) return false
  
  // 检查最后一条客户消息之后是否有催员回复
  for (let i = lastCustomerMessageIndex + 1; i < channelMessages.length; i++) {
    if (channelMessages[i].sender_type === 'collector' || channelMessages[i].sender_type === 'ai') {
      return false // 有催员回复，不是未读
    }
  }
  
  return true // 最后一条是客户消息且之后没有催员回复
}

// 检查案件是否有未读消息（所有联系人中是否有未读）
const hasUnreadMessagesForCase = computed(() => {
  return contacts.value.some(contact => hasUnreadMessagesForContact(contact.id))
})

// 为联系人添加未读标记
// 联系人筛选和搜索
const contactSearchKeyword = ref('')
const contactTypeFilter = ref('all') // all, self, emergency
const communicationFilter = ref('all') // all, no_sent, sent_no_reply, has_reply

// 检查联系人是否有发送的消息
const contactHasSentMessages = (contactId: number) => {
  return mockMessages.value.some(msg => 
    msg.contact_id === contactId && (msg.sender_type === 'collector' || msg.sender_type === 'ai')
  )
}

// 检查联系人是否有回复
const contactHasReply = (contactId: number) => {
  return mockMessages.value.some(msg => 
    msg.contact_id === contactId && msg.sender_type === 'customer'
  )
}

const contactsWithUnread = computed(() => {
  let filtered = contacts.value.map(contact => ({
    ...contact,
    hasUnread: hasUnreadMessagesForContact(contact.id)
  }))
  
  // 联系人类型筛选
  if (contactTypeFilter.value === 'self') {
    filtered = filtered.filter(c => c.relation === '本人')
  } else if (contactTypeFilter.value === 'emergency') {
    filtered = filtered.filter(c => c.relation !== '本人')
  }
  
  // 沟通筛选
  if (communicationFilter.value === 'no_sent') {
    filtered = filtered.filter(c => !contactHasSentMessages(c.id))
  } else if (communicationFilter.value === 'sent_no_reply') {
    filtered = filtered.filter(c => contactHasSentMessages(c.id) && !contactHasReply(c.id))
  } else if (communicationFilter.value === 'has_reply') {
    filtered = filtered.filter(c => contactHasReply(c.id))
  }
  
  // 搜索筛选
  if (contactSearchKeyword.value.trim()) {
    const keyword = contactSearchKeyword.value.trim().toLowerCase()
    filtered = filtered.filter(c => 
      c.name.toLowerCase().includes(keyword)
    )
  }
  
  return filtered
})

// 检查当前选中联系人的渠道是否有未读
const channelHasUnread = (channel: string) => {
  if (!selectedContact.value) return false
  return hasUnreadMessagesForChannel(selectedContact.value.id, channel)
}

// 输入框
const messageInput = ref('')

// 对话框
const addContactDialogVisible = ref(false)
const templateDialogVisible = ref(false)
const aiCallDialogVisible = ref(false)
const qrCodeDialogVisible = ref(false)

// WA账号相关
const platformWAAccounts = ref({
  available: 3,
  total: 20,
  accounts: [
    { id: 'platform_1', name: '平台WA', avatar: null }
  ]
})

const personalWAAccounts = ref({
  available: 2,
  total: 3,
  accounts: [
    { id: 'personal_1', name: '个人WA1', avatar: 'https://via.placeholder.com/32' },
    { id: 'personal_2', name: '个人WA2', avatar: 'https://via.placeholder.com/32' }
  ]
})

// 当前选中的WA账号
const selectedWAAccount = ref<{ id: string, type: 'platform' | 'personal' } | null>({
  id: 'platform_1',
  type: 'platform'
})

// 新增联系人表单
const newContactForm = ref({
  phone: '',
  name: '',
  relation: '',
  channels: []
})

// 消息模板筛选器
const templateFilters = ref({
  stage: '',
  type: '',
  scene: '',
  timeSlot: ''
})

// 消息模板（结构化数据）
const messageTemplates = ref([
  {
    id: 1,
    title: '早安问候 + 还款提醒',
    content: '您好【客户名】，早上好！这里是【机构名】。您在我司的贷款【贷款编号】已逾期【逾期天数】天，应还金额【应还金额】。请您尽快安排还款，避免影响信用记录。',
    stage: 'S0',
    type: 'organization',
    scene: 'greeting',
    timeSlot: 'morning',
    variables: ['客户名', '机构名', '贷款编号', '逾期天数', '应还金额']
  },
  {
    id: 2,
    title: '下午催款提醒',
    content: '【客户名】您好，您的贷款【贷款编号】逾期【逾期天数】天，未还金额【应还金额】，请今日内完成还款。如有困难请联系我们。',
    stage: 'S1-3',
    type: 'organization',
    scene: 'reminder',
    timeSlot: 'afternoon',
    variables: ['客户名', '贷款编号', '逾期天数', '应还金额']
  },
  {
    id: 3,
    title: '晚间强度提醒',
    content: '【客户名】，您的贷款已严重逾期【逾期天数】天，如不尽快还款将影响您的征信记录，并可能采取法律措施。请立即联系我们：【联系电话】',
    stage: 'S3+',
    type: 'organization',
    scene: 'strong',
    timeSlot: 'evening',
    variables: ['客户名', '逾期天数', '联系电话']
  },
  {
    id: 4,
    title: '个人问候（上午）',
    content: '您好，早上好！我是【催员名】，关于您的还款事宜想和您沟通一下，现在方便吗？',
    stage: 'S0',
    type: 'personal',
    scene: 'greeting',
    timeSlot: 'morning',
    variables: ['催员名']
  },
  {
    id: 5,
    title: '还款确认',
    content: '【客户名】您好，我们已收到您的还款【还款金额】，感谢您的配合！',
    stage: 'C',
    type: 'organization',
    scene: 'greeting',
    timeSlot: 'afternoon',
    variables: ['客户名', '还款金额']
  },
  {
    id: 6,
    title: '承诺还款跟进',
    content: '【客户名】您好，您昨天承诺今天还款【承诺金额】，请问现在方便安排吗？',
    stage: 'S1-3',
    type: 'personal',
    scene: 'reminder',
    timeSlot: 'afternoon',
    variables: ['客户名', '承诺金额']
  },
  {
    id: 7,
    title: '首次联系',
    content: '您好【客户名】，我是【机构名】的【催员名】，关于您的贷款【贷款编号】想和您确认一下还款安排，请问现在方便通话吗？',
    stage: 'S0',
    type: 'personal',
    scene: 'greeting',
    timeSlot: 'morning',
    variables: ['客户名', '机构名', '催员名', '贷款编号']
  },
  {
    id: 8,
    title: '最终通知',
    content: '【客户名】，这是最后通知。您的贷款已逾期【逾期天数】天，如24小时内不还款，我们将启动法律程序。请立即联系：【联系电话】',
    stage: 'S3+',
    type: 'organization',
    scene: 'strong',
    timeSlot: 'evening',
    variables: ['客户名', '逾期天数', '联系电话']
  }
])

// 筛选后的模板
const filteredTemplates = computed(() => {
  return messageTemplates.value.filter(template => {
    if (templateFilters.value.stage && template.stage !== templateFilters.value.stage) return false
    if (templateFilters.value.type && template.type !== templateFilters.value.type) return false
    if (templateFilters.value.scene && template.scene !== templateFilters.value.scene) return false
    if (templateFilters.value.timeSlot && template.timeSlot !== templateFilters.value.timeSlot) return false
    return true
  })
})

// 外呼记录
const callRecords = ref<any[]>([])

// 浏览器权限状态
const microphonePermission = ref({
  granted: false,
  requesting: false
})

const audioPermission = ref({
  granted: false,
  requesting: false
})

// 检查麦克风权限
const checkMicrophonePermission = async () => {
  try {
    // 使用 Permissions API 检查权限状态
    if ('permissions' in navigator) {
      const result = await navigator.permissions.query({ name: 'microphone' as PermissionName })
      microphonePermission.value.granted = result.state === 'granted'
      
      // 监听权限变化
      result.onchange = () => {
        microphonePermission.value.granted = result.state === 'granted'
      }
    } else {
      // 如果不支持 Permissions API，尝试直接请求权限
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        microphonePermission.value.granted = true
        // 立即停止流，我们只是检查权限
        stream.getTracks().forEach(track => track.stop())
      } catch (error: any) {
        microphonePermission.value.granted = false
      }
    }
  } catch (error) {
    console.error('检查麦克风权限失败:', error)
    microphonePermission.value.granted = false
  }
}

// 请求麦克风权限
const requestMicrophonePermission = async () => {
  microphonePermission.value.requesting = true
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    microphonePermission.value.granted = true
    ElMessage.success('麦克风权限已授权')
    // 立即停止流，我们只是请求权限
    stream.getTracks().forEach(track => track.stop())
    
    // 重新检查权限状态
    await checkMicrophonePermission()
  } catch (error: any) {
    microphonePermission.value.granted = false
    if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
      ElMessage.error('用户拒绝了麦克风权限请求')
    } else if (error.name === 'NotFoundError') {
      ElMessage.error('未找到麦克风设备')
    } else {
      ElMessage.error('请求麦克风权限失败：' + error.message)
    }
  } finally {
    microphonePermission.value.requesting = false
  }
}

// 检查音频输出权限（实际上浏览器没有单独的音频输出权限，这里检查是否可以播放音频）
const checkAudioPermission = async () => {
  try {
    // 创建一个临时的 AudioContext 来测试音频输出
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    audioPermission.value.granted = audioContext.state === 'running' || audioContext.state === 'suspended'
    await audioContext.close()
  } catch (error) {
    console.error('检查音频输出权限失败:', error)
    audioPermission.value.granted = false
  }
}

// 请求音频输出权限（通过播放测试音频）
const requestAudioPermission = async () => {
  audioPermission.value.requesting = true
  try {
    // 创建一个临时的 AudioContext
    const audioContext = new (window.AudioContext || (window as any).webkitAudioContext)()
    
    // 创建一个短暂的静音音频来触发用户交互
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    gainNode.gain.value = 0 // 静音
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    oscillator.start()
    oscillator.stop(audioContext.currentTime + 0.01)
    
    await audioContext.close()
    audioPermission.value.granted = true
    ElMessage.success('音频输出权限已授权')
    
    // 重新检查权限状态
    await checkAudioPermission()
  } catch (error: any) {
    audioPermission.value.granted = false
    ElMessage.error('请求音频输出权限失败：' + error.message)
  } finally {
    audioPermission.value.requesting = false
  }
}

// 初始化权限检查（在组件挂载时执行）

// AI外呼模板筛选器
const aiCallTemplateFilters = ref({
  stage: '',
  type: '',
  scene: '',
  timeSlot: ''
})

// AI外呼模板（复用消息模板）
const aiCallTemplates = computed(() => messageTemplates.value)

// 筛选后的AI外呼模板
const filteredAICallTemplates = computed(() => {
  return aiCallTemplates.value.filter(template => {
    if (aiCallTemplateFilters.value.stage && template.stage !== aiCallTemplateFilters.value.stage) return false
    if (aiCallTemplateFilters.value.type && template.type !== aiCallTemplateFilters.value.type) return false
    if (aiCallTemplateFilters.value.scene && template.scene !== aiCallTemplateFilters.value.scene) return false
    if (aiCallTemplateFilters.value.timeSlot && template.timeSlot !== aiCallTemplateFilters.value.timeSlot) return false
    return true
  })
})

// 重置AI外呼模板筛选器
const resetAICallTemplateFilters = () => {
  aiCallTemplateFilters.value = {
    stage: '',
    type: '',
    scene: '',
    timeSlot: ''
  }
}

// 重置筛选器
const resetTemplateFilters = () => {
  templateFilters.value = {
    stage: '',
    type: '',
    scene: '',
    timeSlot: ''
  }
}

// 获取阶段标签
const getStageLabel = (stage: string) => {
  const labels: Record<string, string> = {
    'C': 'C',
    'S0': 'S0',
    'S1-3': 'S1-3',
    'S3+': 'S3+'
  }
  return labels[stage] || stage
}

// 获取场景标签
const getSceneLabel = (scene: string) => {
  const labels: Record<string, string> = {
    'greeting': '问候',
    'reminder': '提醒',
    'strong': '强度'
  }
  return labels[scene] || scene
}

// 获取时间段标签
const getTimeSlotLabel = (timeSlot: string) => {
  const labels: Record<string, string> = {
    'morning': '上午',
    'afternoon': '下午',
    'evening': '晚上'
  }
  return labels[timeSlot] || timeSlot
}

// 渲染模板预览（替换变量为示例值）
const renderTemplatePreview = (content: string) => {
  const variables: Record<string, string> = {
    '客户名': props.caseData?.user_name || 'Sanjay Patel',
    '机构名': 'BTSK',
    '催员名': '小王',
    '贷款编号': props.caseData?.loan_id || 'BTSK-200100',
    '逾期天数': props.caseData?.overdue_days?.toString() || '23',
    '应还金额': props.caseData?.outstanding_amount?.toString() || '10,529',
    '还款金额': '5,000',
    '承诺金额': '10,000',
    '联系电话': '+91 96 4870 7764'
  }
  
  let preview = content
  Object.keys(variables).forEach(key => {
    preview = preview.replace(new RegExp(`【${key}】`, 'g'), variables[key])
  })
  
  return preview
}

// 常用Emoji
const commonEmojis = ['😊', '👍', '🙏', '💰', '📱', '✅', '❌', '⏰', '📅', '💳', '🏦', '📞', '✨', '👋', '🤝']

// 催记表单
const caseNoteForm = ref({
  contact_method: '',
  contact_name: '',
  relation: '',
  relation_level: 0, // 关联度（1-5星）
  communication_status: '',
  communication_result: '',
  remark: '',
  follow_up_type: 'one_hour',
  follow_up_date: null as Date | null,
  follow_up_hour: dayjs().hour(),
  follow_up_minute: 0
})

// 是否为本人联系
const isMainContact = computed(() => {
  return selectedContact.value?.relation === '本人'
})

// 历史催记相关
const showHistoryNotesDialog = ref(false)
const historySearchKeyword = ref('')
const historyFilters = ref({
  collector: '',
  channel: '',
  status: '',
  result: '',
  dateRange: null as [Date, Date] | null
})

// 触达人列表（mock数据）
const collectorList = ref(['张三', '李四', '王五', '当前用户'])

// 历史催记列表（mock数据）
const historyNotes = ref([
  {
    id: 1,
    register_time: '2025-01-15 10:30:25',
    case_id: props.caseData?.loan_id || 'BTSK-200100',
    collector: '张三',
    channel: 'WhatsApp',
    status: 'reachable',
    result: 'promise_repay',
    remark: '客户承诺今天下午还款',
    next_follow_up: '2025-01-15 14:00:00'
  },
  {
    id: 2,
    register_time: '2025-01-14 15:20:10',
    case_id: props.caseData?.loan_id || 'BTSK-200100',
    collector: '李四',
    channel: 'SMS',
    status: 'no_response',
    result: 'continuous_follow_up',
    remark: '发送催收短信，未收到回复',
    next_follow_up: '2025-01-15 09:00:00'
  },
  {
    id: 3,
    register_time: '2025-01-13 11:15:30',
    case_id: props.caseData?.loan_id || 'BTSK-200100',
    collector: '王五',
    channel: '电话外呼',
    status: 'reachable',
    result: 'refuse_repay',
    remark: '电话接通，客户拒绝还款',
    next_follow_up: '2025-01-14 10:00:00'
  }
])

// 监听案件变化，更新搜索关键词
watch(() => props.caseData?.loan_id, (newLoanId) => {
  if (newLoanId) {
    historySearchKeyword.value = newLoanId
  }
}, { immediate: true })

// 筛选后的历史催记列表
const filteredHistoryNotes = computed(() => {
  let result = historyNotes.value

  // 搜索案件ID
  if (historySearchKeyword.value) {
    const keyword = historySearchKeyword.value.toLowerCase()
    result = result.filter(note => 
      note.case_id?.toLowerCase().includes(keyword)
    )
  }

  // 筛选触达人
  if (historyFilters.value.collector) {
    result = result.filter(note => 
      note.collector === historyFilters.value.collector
    )
  }

  // 筛选触达渠道
  if (historyFilters.value.channel) {
    result = result.filter(note => 
      note.channel === historyFilters.value.channel
    )
  }

  // 筛选状态
  if (historyFilters.value.status) {
    result = result.filter(note => 
      note.status === historyFilters.value.status
    )
  }

  // 筛选结果
  if (historyFilters.value.result) {
    result = result.filter(note => 
      note.result === historyFilters.value.result
    )
  }

  // 筛选时间范围
  if (historyFilters.value.dateRange && historyFilters.value.dateRange.length === 2) {
    const [startDate, endDate] = historyFilters.value.dateRange
    result = result.filter(note => {
      const noteDate = dayjs(note.register_time)
      return noteDate.isAfter(dayjs(startDate).startOf('day')) && 
             noteDate.isBefore(dayjs(endDate).endOf('day'))
    })
  }

  return result
})

// 处理历史催记搜索
const handleHistorySearch = () => {
  // 搜索逻辑已在 computed 中实现
}

// 处理历史催记筛选
const handleHistoryFilter = () => {
  // 筛选逻辑已在 computed 中实现
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'reachable': '可联',
    'not_exist': '不存在',
    'no_response': '未响应'
  }
  return labels[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const types: Record<string, string> = {
    'reachable': 'success',
    'not_exist': 'danger',
    'no_response': 'warning'
  }
  return types[status] || ''
}

// 获取结果标签
const getResultLabel = (result: string) => {
  const labels: Record<string, string> = {
    'promise_repay': '承诺还款',
    'refuse_repay': '拒绝还款',
    'not_related': '与借款人不相关',
    'related': '与借款人相关',
    'promise_repay_on_behalf': '承诺代还',
    'promise_inform': '承诺转告',
    'lost_contact': '失联',
    'continuous_follow_up': '持续跟进',
    'other': '其它'
  }
  return labels[result] || result
}

// 更新催记表单的联系方式
const updateContactMethod = () => {
  if (activeChannel.value === 'whatsapp') {
    caseNoteForm.value.contact_method = 'WhatsApp'
  } else if (activeChannel.value === 'sms') {
    caseNoteForm.value.contact_method = 'SMS'
  } else if (activeChannel.value === 'rcs') {
    caseNoteForm.value.contact_method = 'RCS'
  } else if (activeChannel.value === 'call') {
    caseNoteForm.value.contact_method = '电话外呼'
  } else {
    caseNoteForm.value.contact_method = 'WhatsApp'
  }
}

// 更新催记表单的联系人和关系
const updateContactInfo = () => {
  if (selectedContact.value) {
    caseNoteForm.value.contact_name = selectedContact.value.name
    caseNoteForm.value.relation = selectedContact.value.relation
  }
}

// 选择联系人
const selectContact = (contact: any) => {
  selectedContactId.value = contact.id
  // 自动填充催记表单
  updateContactMethod()
  updateContactInfo()
  // 重置其他字段
  caseNoteForm.value.relation_level = 0
  caseNoteForm.value.communication_status = ''
  caseNoteForm.value.communication_result = ''
  caseNoteForm.value.remark = ''
}

// 监听渠道变化，自动更新联系方式
watch(activeChannel, () => {
  if (selectedContact.value) {
    updateContactMethod()
  }
})

// 监听联系人变化，自动更新联系人和关系
watch(selectedContact, () => {
  if (selectedContact.value) {
    updateContactInfo()
    updateContactMethod()
  }
}, { immediate: true })

// 显示新增联系人对话框
const showAddContactDialog = () => {
  addContactDialogVisible.value = true
}

// 提交新增联系人
const submitNewContact = () => {
  if (!newContactForm.value.phone || !newContactForm.value.name || !newContactForm.value.relation) {
    ElMessage.warning('请填写完整信息')
    return
  }
  
  if (newContactForm.value.channels.length === 0) {
    ElMessage.warning('请至少选择一个支持渠道')
    return
  }
  
  const newContact = {
    id: contacts.value.length + 1,
    name: newContactForm.value.name,
    phone: newContactForm.value.phone,
    phone_last4: newContactForm.value.phone.slice(-4),
    relation: newContactForm.value.relation,
    channels: newContactForm.value.channels
  }
  
  contacts.value.push(newContact)
  addContactDialogVisible.value = false
  
  // 重置表单
  newContactForm.value = {
    phone: '',
    name: '',
    relation: '',
    channels: []
  }
  
  ElMessage.success('联系人添加成功')
}

// 显示模板对话框
const showTemplateDialog = () => {
  templateDialogVisible.value = true
}

// 选择模板（填充变量到输入框）
const selectTemplate = (template: any) => {
  messageInput.value = renderTemplatePreview(template.content)
  templateDialogVisible.value = false
  ElMessage.success(`已填充模板：${template.title}`)
}

// 插入Emoji
const insertEmoji = (emoji: string) => {
  messageInput.value += emoji
}

// 处理图片选择
const handleImageSelect = (file: any) => {
  ElMessage.info('图片发送功能开发中...')
  console.log('Selected image:', file)
}

// 发送消息
const sendMessage = () => {
  if (!messageInput.value.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  if (!selectedContact.value) {
    ElMessage.warning('请选择联系人')
    return
  }
  
  const channel = activeChannel.value === 'whatsapp' ? 'whatsapp' : 
                  activeChannel.value === 'sms' ? 'sms' :
                  activeChannel.value === 'rcs' ? 'rcs' : 'whatsapp'
  
  // 判断是否来自模板（检查是否包含变量替换后的内容）
  const isFromTemplate = messageTemplates.value.some(template => {
    const preview = renderTemplatePreview(template.content)
    return messageInput.value.trim() === preview.trim()
  })
  
  const newMessage: any = {
    id: mockMessages.value.length + 1,
    contact_id: selectedContact.value.id,
    type: 'text',
    content: messageInput.value,
    sender_type: 'collector',
    sender_name: '当前催员',
    sender_id: 'collector001',
    channel: channel,
    status: 'sent',
    sent_at: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  
  // SMS和RCS特有字段
  if (channel === 'sms' || channel === 'rcs') {
    newMessage.from_template = isFromTemplate
  } else {
    // WhatsApp消息：根据选中的WA账号设置tool字段
    if (selectedWAAccount.value?.type === 'platform') {
      newMessage.tool = '公司WA'
    } else if (selectedWAAccount.value?.type === 'personal') {
      const account = personalWAAccounts.value.accounts.find(a => a.id === selectedWAAccount.value?.id)
      newMessage.tool = account ? `个人WA（${account.name}）` : '个人WA'
    } else {
      newMessage.tool = '公司WA'
    }
  }
  
  mockMessages.value.push(newMessage)
  messageInput.value = ''
  
  const channelNames: Record<string, string> = {
    whatsapp: 'WhatsApp',
    sms: 'SMS',
    rcs: 'RCS'
  }
  ElMessage.success(`${channelNames[channel] || 'WhatsApp'}消息发送成功`)
  
  // 滚动到底部
  nextTick(() => {
    scrollToBottom()
  })
}

// 滚动到底部
const messagesContainer = ref<HTMLElement>()
const whatsappContainer = ref<HTMLElement>()
const smsContainer = ref<HTMLElement>()
const rcsContainer = ref<HTMLElement>()

const scrollToBottom = () => {
  let container: HTMLElement | undefined
  if (activeChannel.value === 'whatsapp') {
    container = whatsappContainer.value
  } else if (activeChannel.value === 'sms') {
    container = smsContainer.value
  } else if (activeChannel.value === 'rcs') {
    container = rcsContainer.value
  } else {
    container = messagesContainer.value
  }
  
  if (container) {
    container.scrollTop = container.scrollHeight
  }
}

// 格式化消息日期
const formatMessageDate = (dateStr: string) => {
  return dayjs(dateStr).format('YYYY年MM月DD日')
}

// 格式化消息时间
const formatMessageTime = (dateStr: string) => {
  return dayjs(dateStr).format('HH:mm:ss')
}

// 获取渠道标签
const getChannelLabel = (channel: string) => {
  const labels: Record<string, string> = {
    whatsapp: 'WhatsApp',
    sms: 'SMS',
    rcs: 'RCS',
    call: '电话'
  }
  return labels[channel] || channel
}

// 获取SMS状态类型
const getSmsStatusType = (status: string) => {
  const typeMap: Record<string, string> = {
    'delivered': 'success',
    'read': 'success',
    'sent': 'info',
    'failed': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取SMS状态标签
const getSmsStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'delivered': '送达',
    'read': '已读',
    'sent': '已发送',
    'failed': '发送失败'
  }
  return labels[status] || status
}

// 禁用日期（只能选择今天到未来30天）
const disabledDate = (time: Date) => {
  const today = dayjs().startOf('day')
  const maxDate = today.add(30, 'day')
  const targetDate = dayjs(time)
  return targetDate.isBefore(today) || targetDate.isAfter(maxDate)
}

// 提交催记
const submitCaseNote = () => {
  // 验证必填项
  if (!caseNoteForm.value.communication_status) {
    ElMessage.warning('请选择沟通状态')
    return
  }
  
  if (!caseNoteForm.value.communication_result) {
    ElMessage.warning('请选择沟通结果')
    return
  }
  
  // 计算下次跟进时间
  let followUpTime = ''
  if (caseNoteForm.value.follow_up_type === 'one_hour') {
    followUpTime = dayjs().add(1, 'hour').format('YYYY-MM-DD HH:mm:ss')
  } else if (caseNoteForm.value.follow_up_type === 'specific_time' && caseNoteForm.value.follow_up_date) {
    const date = dayjs(caseNoteForm.value.follow_up_date)
    followUpTime = date
      .hour(caseNoteForm.value.follow_up_hour)
      .minute(caseNoteForm.value.follow_up_minute)
      .format('YYYY-MM-DD HH:mm:ss')
  }
  
  // 构建催记数据
  const noteData = {
    case_id: props.caseData?.case_id,
    contact_id: selectedContact.value?.id,
    contact_method: caseNoteForm.value.contact_method,
    contact_name: caseNoteForm.value.contact_name,
    relation: caseNoteForm.value.relation,
    relation_level: caseNoteForm.value.relation_level, // 关联度
    communication_status: caseNoteForm.value.communication_status,
    communication_result: caseNoteForm.value.communication_result,
    remark: caseNoteForm.value.remark,
    follow_up_time: followUpTime,
    created_at: dayjs().format('YYYY-MM-DD HH:mm:ss')
  }
  
  console.log('提交催记:', noteData)
  
  // 这里可以调用API保存催记
  // await saveCaseNote(noteData)
  
  // 如果标记了关联度且非本人，更新联系人的关联度（取最新的）
  if (selectedContact.value && selectedContact.value.relation !== '本人' && caseNoteForm.value.relation_level > 0) {
    const contactIndex = contacts.value.findIndex(c => c.id === selectedContact.value?.id)
    if (contactIndex !== -1) {
      contacts.value[contactIndex].relation_level = caseNoteForm.value.relation_level
    }
  }
  
  ElMessage.success('催记提交成功')
  
  // 添加到历史催记列表
  historyNotes.value.unshift({
    id: historyNotes.value.length + 1,
    register_time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    case_id: props.caseData?.loan_id || props.caseData?.case_id || '',
    collector: '当前用户', // 这里应该从用户store获取
    channel: caseNoteForm.value.contact_method,
    status: caseNoteForm.value.communication_status,
    result: caseNoteForm.value.communication_result,
    remark: caseNoteForm.value.remark,
    next_follow_up: followUpTime
  })
  
  // 重置表单（除了自动填充的字段）
  caseNoteForm.value.relation_level = 0
  caseNoteForm.value.communication_status = ''
  caseNoteForm.value.communication_result = ''
  caseNoteForm.value.remark = ''
  caseNoteForm.value.follow_up_type = 'one_hour'
  caseNoteForm.value.follow_up_date = null
}

// ========== 外呼相关方法 ==========

// 立即呼叫1次
const handleCallOnce = async () => {
  if (!selectedContact.value) {
    ElMessage.warning('请选择联系人')
    return
  }
  
  // 调用真实的Infinity API发起外呼
  try {
    const userStore = useUserStore()
    const collectorId = userStore.userInfo?.id
    
    if (!collectorId) {
      ElMessage.error('无法获取当前催员信息')
      return
    }
    
    const loadingMsg = ElMessage.loading('正在发起外呼...')
    
    const response = await makeCall({
      case_id: props.caseId,
      collector_id: collectorId,
      contact_number: selectedContact.value.phone,
      custom_params: {
        contact_person_id: selectedContact.value.id,
        contact_type: selectedContact.value.type
      }
    })
    
    loadingMsg.close()
    
    if (response.success) {
      ElMessage.success(response.message)
      
      // 添加到通话记录列表
      const newRecord = {
        id: response.call_id || callRecords.value.length + 1,
        type: 'single',
        status: 'calling',
        caller_name: '本人',
        caller_id: 'self',
        call_time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
        duration: null,
        next_plan: null,
        call_uuid: response.call_uuid,
        extension_number: response.extension_number
      }
      
      callRecords.value.unshift(newRecord)
    } else {
      ElMessage.error(response.message || '发起外呼失败')
    }
  } catch (error: any) {
    console.error('外呼失败:', error)
    ElMessage.error(error.response?.data?.detail || '发起外呼失败，请检查配置')
  }
}

// 立即呼叫5次直到接通
const handleCallUntilAnswer = () => {
  if (!selectedContact.value) {
    ElMessage.warning('请选择联系人')
    return
  }
  
  const newRecord = {
    id: callRecords.value.length + 1,
    type: 'polling', // 轮询N次
    status: 'calling', // 呼叫中
    caller_name: '本人',
    caller_id: 'self',
    call_time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    duration: null,
    next_plan: {
      status: 'scheduled',
      retry_count: 1,
      max_retries: 5,
      delay_seconds: 10,
      description: '结束后10s后，重试拨打4/5轮'
    }
  }
  
  callRecords.value.unshift(newRecord)
  ElMessage.success('已发起轮询呼叫，最多尝试5次')
  
  // 模拟呼叫过程
  setTimeout(() => {
    const record = callRecords.value.find(r => r.id === newRecord.id)
    if (record && record.next_plan) {
      record.status = 'ended_no_response'
    }
  }, 1500)
}

// 显示AI外呼对话框
const showAICallDialog = () => {
  if (!selectedContact.value) {
    ElMessage.warning('请选择联系人')
    return
  }
  aiCallDialogVisible.value = true
}

// 选择AI外呼模板
const selectAICallTemplate = (template: any) => {
  const newRecord = {
    id: callRecords.value.length + 1,
    type: 'ai', // AI
    status: 'scheduled', // 已计划
    caller_name: 'AI机器人',
    caller_id: 'ai_bot',
    call_time: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    duration: null,
    template_id: template.id,
    template_title: template.title,
    next_plan: {
      status: 'scheduled',
      description: 'AI自动外呼计划'
    }
  }
  
  callRecords.value.unshift(newRecord)
  aiCallDialogVisible.value = false
  ElMessage.success(`已创建AI外呼计划：${template.title}`)
  
  // 模拟AI外呼开始
  setTimeout(() => {
    const record = callRecords.value.find(r => r.id === newRecord.id)
    if (record) {
      record.status = 'calling'
    }
  }, 1000)
}

// 终止计划
const cancelNextPlan = (recordId: number) => {
  const record = callRecords.value.find(r => r.id === recordId)
  if (record && record.next_plan) {
    record.next_plan.status = 'cancelled'
    ElMessage.success('已终止计划')
  }
}

// 获取外呼类型标签
const getCallTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    'single': '单次',
    'polling': '轮询N次',
    'ai': 'AI'
  }
  return labels[type] || type
}

// 获取外呼类型标签类型
const getCallTypeTagType = (type: string) => {
  const types: Record<string, string> = {
    'single': 'primary',
    'polling': 'success',
    'ai': 'warning'
  }
  return types[type] || 'info'
}

// 获取外呼状态标签
const getCallStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'calling': '呼叫中',
    'in_call': '通话中',
    'ended_not_exist': '结束：不存在',
    'ended_no_response': '结束：未响应',
    'ended_success': '结束：成功',
    'scheduled': '已计划'
  }
  return labels[status] || status
}

// 获取外呼状态标签类型
const getCallStatusTagType = (status: string) => {
  const types: Record<string, string> = {
    'calling': 'warning',
    'in_call': 'success',
    'ended_not_exist': 'danger',
    'ended_no_response': 'info',
    'ended_success': 'success',
    'scheduled': 'info'
  }
  return types[status] || 'info'
}

// 格式化呼叫时间
const formatCallTime = (timeStr: string) => {
  return dayjs(timeStr).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化通话时长
const formatDuration = (seconds: number) => {
  if (!seconds) return '-'
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}分${secs}秒`
}

// 格式化下一轮计划
const formatNextPlan = (plan: any) => {
  if (!plan) return '无'
  if (plan.status === 'cancelled') return '已终止'
  if (plan.description) return plan.description
  if (plan.retry_count && plan.max_retries) {
    return `结束后${plan.delay_seconds}s后，重试拨打${plan.max_retries - plan.retry_count}/${plan.max_retries}轮`
  }
  return plan.description || '无'
}

// 定时更新下一条发送时间倒计时
let limitTimer: number | null = null
onMounted(async () => {
  // 初始化权限检查
  await checkMicrophonePermission()
  await checkAudioPermission()
  
  // 设置定时器
  limitTimer = window.setInterval(() => {
    // 触发响应式更新
    channelLimits.value.forEach(limit => {
      if (limit.nextSendTime) {
        // 重新创建Date对象以触发响应式更新
        limit.nextSendTime = new Date(limit.nextSendTime.getTime())
      }
    })
  }, 1000)
})

onUnmounted(() => {
  if (limitTimer) {
    clearInterval(limitTimer)
  }
})

// 获取联系人电话状态
const fetchContactPhoneStatuses = async () => {
  if (!props.caseData?.id) {
    return
  }
  
  const caseId = props.caseData.id
  
  // 为每个联系人获取电话状态
  for (const contact of contacts.value) {
    try {
      // 只获取有电话渠道的联系人状态
      if (contact.channels && contact.channels.includes('call')) {
        // 确保传递有效的参数
        const contactId = contact.id
        const phoneNumber = contact.phone || ''
        
        // 调试日志
        console.log(`[电话状态] 联系人: ${contact.name}, ID: ${contactId}, Phone: ${phoneNumber}`)
        
        if (!contactId && !phoneNumber) {
          console.warn(`联系人 ${contact.name} 缺少ID和电话号码，跳过获取电话状态`)
          continue
        }
        
        const statusData = await getContactPhoneStatus(
          caseId,
          contactId,
          phoneNumber
        )
        
        if (statusData && statusData.status) {
          contact.phoneStatus = statusData.status
        } else {
          // 默认状态：未拨打
          contact.phoneStatus = 'never_called'
        }
      } else {
        // 没有电话渠道的联系人，不显示电话状态
        contact.phoneStatus = null
      }
    } catch (error) {
      console.error(`获取联系人 ${contact.id} 的电话状态失败:`, error)
      // 出错时默认显示未拨打状态
      contact.phoneStatus = 'never_called'
    }
  }
}

// 监听案件变化
watch(() => props.caseData?.id, () => {
  // 更新本人联系人信息
  if (contacts.value.length > 0) {
    contacts.value[0] = {
      id: 1,
      name: props.caseData?.user_name || '客户本人',
      phone: props.caseData?.mobile_number || '',
      phone_last4: (props.caseData?.mobile_number || '').slice(-4),
      relation: '本人',
      channels: ['whatsapp', 'sms', 'call'],
      phoneStatus: null // 初始状态，稍后获取
    }
  }
  // 自动选中本人联系人
  selectedContactId.value = 1
  // 自动填充催记表单
  if (selectedContact.value) {
    updateContactMethod()
    updateContactInfo()
  }
  // 获取所有联系人的电话状态
  fetchContactPhoneStatuses()
}, { immediate: true })

// 切换联系人和渠道的方法（供父组件调用）
const switchToContactAndChannel = (contactId: number, channel: string) => {
  selectedContactId.value = contactId
  activeChannel.value = channel
}

// 选择WA账号
const selectWAAccount = async (account: any, type: 'platform' | 'personal') => {
  // 如果选择的是当前账号，不需要切换
  if (selectedWAAccount.value?.id === account.id && selectedWAAccount.value?.type === type) {
    return
  }
  
  // 确认切换
  try {
    await ElMessageBox.confirm(
      '是否确认更换WhatsApp账号？',
      '提示',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 切换账号
    selectedWAAccount.value = { id: account.id, type }
    ElMessage.success(`已切换到${type === 'platform' ? '公司WA' : '个人WA'}`)
    
    // 更新消息中的tool字段
    // 这里可以根据需要更新当前会话的发送账号标识
  } catch {
    // 用户取消
  }
}

// 二维码图案（5x5网格，模拟二维码样式）
const qrCodePattern = ref([
  true, true, true, true, true,
  true, false, false, false, true,
  true, false, true, false, true,
  true, false, false, false, true,
  true, true, true, true, true
])

// 显示扫码对话框
const showQRCodeDialog = () => {
  qrCodeDialogVisible.value = true
}

// 刷新二维码
const refreshQRCode = () => {
  // 随机生成新的二维码图案
  qrCodePattern.value = Array.from({ length: 25 }, () => Math.random() > 0.5)
  ElMessage.success('二维码已刷新')
  // 这里可以调用API重新生成二维码
}

// 暴露给父组件
defineExpose({
  hasUnreadMessagesForCase,
  switchToContactAndChannel
})
</script>

<style scoped>
.im-panel {
  flex: 1;
  display: flex;
  min-height: 0;
  background: #fff;
  overflow: hidden;
}

/* 联系人侧边栏 */
.contacts-sidebar {
  width: 200px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  height: 100%;
}

/* 联系人筛选器和搜索框 */
.contacts-filters {
  padding: 12px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

.contact-search {
  width: 100%;
}

.filter-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.filter-select {
  flex: 1;
  min-width: 0;
}

.contacts-list {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
  gap: 10px;
  position: relative;
}

.contact-item:hover {
  background: #f5f5f5;
}

.contact-item.active {
  background: #e8f5e9;
}

.contact-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #25D366 0%, #128C7E 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(37, 211, 102, 0.2);
  position: relative;
}

.unread-dot {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 10px;
  height: 10px;
  background: #F56C6C;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(245, 108, 108, 0.3);
}

.contact-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-relation-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.contact-relation {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.contact-stars {
  display: inline-flex;
  align-items: center;
  gap: 1px;
}

.contact-stars .star-icon {
  color: #FF9900;
  flex-shrink: 0;
}

.contact-details {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
}

.contact-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.contact-phone {
  color: #909399;
  flex-shrink: 0;
}

/* 电话状态图标 */
.contact-phone-status {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: auto;
}

.phone-status-icon {
  cursor: pointer;
  transition: all 0.2s;
}

/* 未拨打：绿色电话 */
.phone-status-never-called {
  color: #67C23A;
}

.phone-status-never-called:hover {
  color: #85ce61;
  transform: scale(1.1);
}

/* 从未接通：空心电话（灰色） */
.phone-status-never-connected {
  color: #909399;
  opacity: 0.6;
}

.phone-status-never-connected:hover {
  opacity: 1;
  transform: scale(1.1);
}

/* 播过且接通：对号（绿色） */
.phone-status-connected {
  color: #67C23A;
}

.phone-status-connected:hover {
  color: #85ce61;
  transform: scale(1.1);
}

/* 号码不存在：电话上打x（红色） */
.phone-status-invalid {
  color: #F56C6C;
}

.phone-status-invalid:hover {
  color: #f78989;
  transform: scale(1.1);
}

.add-contact-btn {
  padding: 12px 10px;
  border-top: 1px solid #e4e7ed;
}

.add-contact-button {
  background-color: #ffffff !important;
  border: 1px solid #25D366 !important;
  color: #25D366 !important;
}

.add-contact-button:hover {
  background-color: #f0f9ff !important;
  border-color: #20b858 !important;
  color: #20b858 !important;
}

.add-contact-button:active {
  background-color: #e8f5e9 !important;
  border-color: #1da850 !important;
  color: #1da850 !important;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
  min-width: 0;
  border-right: 1px solid #e4e7ed;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.channel-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding-left: 4px;
  position: relative;
}

.tab-label .el-icon {
  font-size: 16px;
}

.channel-unread-dot {
  width: 8px;
  height: 8px;
  background: #F56C6C;
  border-radius: 50%;
  margin-left: 2px;
  flex-shrink: 0;
}

.channel-tabs :deep(.el-tabs__header) {
  padding: 0 16px;
}

.channel-tabs :deep(.el-tabs__nav-wrap) {
  padding-left: 0;
}

.channel-tabs :deep(.el-tabs__item) {
  padding: 0 20px;
}

.channel-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: hidden;
}

.channel-tabs :deep(.el-tab-pane) {
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
  min-height: 0;
}

/* WhatsApp tab下的消息容器需要底部padding，为WA账号选择器留出空间 */
.channel-tabs :deep(.el-tab-pane[name="whatsapp"]) .messages-container {
  padding-bottom: 120px;
}

.message-wrapper {
  margin-bottom: 12px;
}

.message-date-divider {
  text-align: center;
  margin: 16px 0;
  font-size: 12px;
  color: #667781;
}

.message-item {
  display: flex;
  margin-bottom: 8px;
}

.message-sent {
  justify-content: flex-end;
}

.message-received {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 65%;
  padding: 8px 12px;
  border-radius: 8px;
  box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
}

.message-sent .message-bubble {
  background: #d9fdd3;
  border-top-right-radius: 0;
}

.message-received .message-bubble {
  background: #ffffff;
  border-top-left-radius: 0;
}

.message-sender-info {
  font-size: 11px;
  color: #667781;
  margin-bottom: 4px;
  font-weight: 600;
}

.message-content {
  font-size: 14px;
  color: #111b21;
  line-height: 1.5;
  word-wrap: break-word;
}

.message-image {
  margin: 4px 0;
}

.message-voice {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #111b21;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  font-size: 11px;
  color: #667781;
  justify-content: flex-end;
}

.message-channel,
.message-tool {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 4px;
}

.message-status {
  font-size: 14px;
}

/* 输入区域 */
.input-area {
  border-top: 1px solid #e4e7ed;
  background: #f8f9fa;
  padding: 10px 12px;
}

.input-box {
  margin-bottom: 8px;
}

.input-box :deep(.el-textarea__inner) {
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  background: #ffffff;
  resize: none;
  transition: all 0.2s;
}

.input-box :deep(.el-textarea__inner):focus {
  border-color: #25D366;
  box-shadow: 0 0 0 3px rgba(37, 211, 102, 0.1);
}

.input-box :deep(.el-textarea__inner):hover {
  border-color: #b3b3b3;
}

.input-toolbar-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.channel-limit-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  font-size: 11px;
}

.limit-count-text {
  color: #606266;
  font-weight: 500;
}

.limit-time-text {
  color: #909399;
  font-size: 10px;
}

.toolbar-left .el-button {
  color: #606266;
  font-size: 13px;
  padding: 6px 12px;
  height: 32px;
}

.toolbar-left .el-button:hover {
  color: #25D366;
  background: rgba(37, 211, 102, 0.1);
}

.toolbar-right > .el-button {
  height: 32px;
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
}

.toolbar-left :deep(.el-button) {
  color: #606266;
  padding: 8px 12px;
}

.toolbar-left :deep(.el-button):hover {
  color: #25D366;
  background: rgba(37, 211, 102, 0.1);
}

/* Emoji选择器 */
.emoji-picker {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 8px;
}

.emoji-item {
  font-size: 24px;
  cursor: pointer;
  text-align: center;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.emoji-item:hover {
  background: #e8f5e9;
}

/* 模板选择器 */
.template-picker {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.template-filters {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
  white-space: nowrap;
}

/* 模板列表容器 */
.template-list-container {
  flex: 1;
}

.template-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 12px;
  padding: 4px;
}

/* 模板卡片 */
.template-card {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.template-card:hover {
  border-color: #25D366;
  background: #f0fdf4;
  box-shadow: 0 2px 12px rgba(37, 211, 102, 0.15);
  transform: translateY(-2px);
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.template-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  flex: 1;
}

.template-tags {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.template-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  line-clamp: 4;
  -webkit-box-orient: vertical;
}

.template-variables {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  padding-top: 8px;
  border-top: 1px dashed #e4e7ed;
}

.variables-label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

/* 无选中联系人 */
.no-contact-selected {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.no-contact-selected p {
  margin-top: 16px;
  font-size: 16px;
}

/* SMS消息样式 */
.sms-message-item {
  margin-bottom: 16px;
}

.sms-date-divider {
  text-align: center;
  margin: 16px 0;
  font-size: 12px;
  color: #667781;
  font-weight: 500;
}

.sms-message-card {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 0.2s;
}

.sms-message-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.sms-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
  word-wrap: break-word;
}

.sms-meta {
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.sms-meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  flex-wrap: wrap;
}

.sms-time {
  color: #909399;
}

.sms-sender {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-weight: 500;
}

.sms-sender .el-icon {
  color: #25D366;
}

.coming-soon {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 16px;
  color: #909399;
}

/* 催记区域 */
.case-note-area {
  width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
  overflow: hidden;
  height: 100%;
  border-left: 1px solid #e4e7ed;
}

.case-note-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.note-header {
  padding: 14px 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.note-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

.history-notes-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.history-notes-btn .el-icon {
  font-size: 14px;
}

/* 历史催记对话框 */
.history-notes-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.history-notes-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-search {
  display: flex;
  justify-content: flex-start;
}

.history-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.history-notes-list {
  margin-top: 8px;
}

.history-notes-list :deep(.el-table) {
  font-size: 13px;
}

.history-notes-list :deep(.el-table th) {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  padding: 12px 0;
}

.history-notes-list :deep(.el-table td) {
  padding: 12px 0;
}

.history-notes-list :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: #fafafa;
}

.note-form-scroll {
  flex: 1;
  min-height: 0;
}

.note-form {
  padding: 12px;
}

/* 关联度样式 */
.relation-level-item {
  margin-bottom: 18px;
}

.relation-level-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.relation-level-row :deep(.el-rate) {
  height: auto;
  flex-shrink: 0;
}

.relation-level-row :deep(.el-rate__icon) {
  font-size: 22px;
  margin-right: 4px;
}

.relation-level-row :deep(.el-rate__text) {
  margin-left: 6px;
  font-size: 14px;
}

.relation-level-hint {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  flex-shrink: 1;
}

.status-radio-group,
.result-radio-group {
  display: flex;
  flex-direction: row;
  gap: 8px;
  width: 100%;
  flex-wrap: wrap;
}

.status-radio-group :deep(.el-radio),
.result-radio-group :deep(.el-radio) {
  margin-right: 0;
  margin-bottom: 0;
  height: auto;
  line-height: 1.5;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  padding: 6px 12px;
  transition: all 0.2s;
  cursor: pointer;
  background: #ffffff;
}

.status-radio-group :deep(.el-radio:hover),
.result-radio-group :deep(.el-radio:hover) {
  border-color: #25D366;
  background: #f0fdf4;
}

.status-radio-group :deep(.el-radio.is-checked),
.result-radio-group :deep(.el-radio.is-checked) {
  border-color: #25D366;
  background: #e8f5e9;
  border-style: solid;
}

.status-radio-group :deep(.el-radio__input),
.result-radio-group :deep(.el-radio__input) {
  display: none;
}

.status-radio-group :deep(.el-radio__label),
.result-radio-group :deep(.el-radio__label) {
  padding-left: 0;
  font-size: 13px;
  color: #606266;
}

.status-radio-group :deep(.el-radio.is-checked .el-radio__label),
.result-radio-group :deep(.el-radio.is-checked .el-radio__label) {
  color: #25D366;
  font-weight: 500;
}

.note-auto-info {
  background: #f0f2f5;
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: #606266;
}

.auto-info-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.auto-info-item label {
  color: #909399;
  font-weight: 500;
}

.auto-info-item span {
  color: #303133;
}

.time-picker-group {
  width: 100%;
}

.time-selectors {
  display: flex;
  justify-content: space-between;
  gap: 4%;
}

.no-contact-for-note {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.no-contact-for-note p {
  margin-top: 16px;
  font-size: 14px;
}

/* 外呼面板样式 */
.call-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #ffffff;
}

.call-actions {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  background: #f8f9fa;
}

.call-actions .el-button {
  flex: 1;
}

.call-records-container {
  flex: 1;
  overflow: hidden;
  padding: 16px;
  min-height: 0;
  margin-bottom: 0;
}

.call-records-scrollbar {
  height: 100%;
}

.call-records-scrollbar :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

.empty-records {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
}

.call-records-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 浏览器权限检查区域样式（底部） */
.call-permissions {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin: 0 16px 16px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  flex-shrink: 0;
}

.permissions-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.permissions-title::before {
  content: '';
  width: 3px;
  height: 14px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 2px;
}

.permissions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.permission-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.permission-item:hover {
  border-color: #c0c4cc;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.permission-item.permission-denied {
  background: #fef0f0;
  border-color: #fbc4c4;
}

.permission-item.permission-denied:hover {
  border-color: #f89898;
}

.permission-info {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
}

.permission-icon {
  font-size: 20px;
  color: #67c23a;
  flex-shrink: 0;
}

.permission-icon.permission-error {
  color: #f56c6c;
}

.permission-name {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  min-width: 100px;
}

.permission-item :deep(.el-tag) {
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 4px;
}

.permission-item :deep(.el-button) {
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
}

.permission-item :deep(.el-button):hover {
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.call-record-item {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 12px;
  transition: all 0.2s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.call-record-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border-color: #25D366;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-type-status {
  display: flex;
  gap: 8px;
  align-items: center;
}

.record-time {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  white-space: nowrap;
}

.record-actions {
  display: flex;
  gap: 8px;
}

.record-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  line-height: 1.4;
}

.record-info-line {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  flex-wrap: wrap;
}

.record-info-line:first-child {
  margin-bottom: 2px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-label {
  color: #909399;
  font-weight: 500;
  white-space: nowrap;
}

.info-value {
  color: #303133;
  white-space: nowrap;
}

.info-value-secondary {
  color: #909399;
  font-size: 11px;
}

.info-separator {
  color: #dcdfe6;
  margin: 0 4px;
}

.next-plan-value {
  color: #E6A23C;
  font-weight: 500;
}

.cancel-plan-btn {
  margin-left: 8px;
  padding: 0 4px;
  height: auto;
  font-size: 12px;
}

.cancel-plan-btn :deep(.el-button__text) {
  line-height: 1.4;
}

/* WA账号选择器样式 */
.wa-account-selector {
  background: #f8f9fa;
  border-top: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  padding: 8px 16px;
  display: flex;
  flex-direction: column;
  gap: 0;
  flex-shrink: 0;
  margin-top: 8px;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.05);
}

.wa-account-row {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: nowrap;
}

.wa-account-group {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
}

.wa-group-header {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
  flex-shrink: 0;
}

.wa-group-title {
  font-weight: 600;
  color: #303133;
  font-size: 12px;
  line-height: 1;
}

.wa-group-count {
  color: #606266;
  font-size: 11px;
  font-weight: 500;
}

.wa-help-icon {
  color: #909399;
  cursor: help;
  font-size: 12px;
  flex-shrink: 0;
}

.wa-help-icon:hover {
  color: #25D366;
}

.wa-avatars {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: nowrap;
}

.wa-avatar-item {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid transparent;
  position: relative;
  flex-shrink: 0;
}

.wa-avatar-item:hover {
  transform: scale(1.1);
}

.wa-avatar-item.active {
  border-color: #25D366;
  box-shadow: 0 0 0 2px rgba(37, 211, 102, 0.2);
}

.wa-avatar-icon {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #25D366 0%, #128C7E 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 18px;
  box-shadow: 0 2px 4px rgba(37, 211, 102, 0.3);
}

.wa-add-btn {
  background: #f5f7fa;
  border: 2px dashed #dcdfe6;
  color: #909399;
  font-size: 16px;
}

.wa-add-btn:hover {
  background: #e8f5e9;
  border-color: #25D366;
  color: #25D366;
  border-style: solid;
}

/* 二维码对话框样式 */
.qr-code-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.qr-code-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  background: #f5f7fa;
  border-radius: 8px;
  width: 100%;
}

.qr-code-placeholder p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.qr-tip {
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.6;
}

/* 二维码占位符样式 */
.qr-code-icon-placeholder {
  width: 200px;
  height: 200px;
  background: #ffffff;
  border: 2px solid #25D366;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
}

.qr-code-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  grid-template-rows: repeat(5, 1fr);
  gap: 2px;
  width: 100%;
  height: 100%;
}

.qr-code-cell {
  background: #ffffff;
  border-radius: 2px;
}

.qr-code-cell.filled {
  background: #25D366;
}
</style>

