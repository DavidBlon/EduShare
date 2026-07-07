<template>
  <div class="about-page">
    <div class="page-container about-content" style="padding-top:40px;">
      <section class="about-section">
        <h2>平台简介</h2>
        <p>
          小初学习资料圈 是一个专注于教育资源共享的平台，致力于为广大师生提供优质、便捷的教育资源获取渠道。
          我们汇聚了涵盖小学、初中、高中、中考、高考等多个阶段的精品教育资源，包括课件教案、同步练习、
          真题试卷、知识点总结等丰富类型。
        </p>
      </section>

      <section class="about-section">
        <h2>我们的使命</h2>
        <p>
          让每一份优质教育资源都能被更多需要的人获取。我们相信，教育资源的开放共享能够有效促进教育公平，
          帮助每一位学习者获得更好的学习体验和成长机会。
        </p>
      </section>

      <section class="about-section">
        <h2>资源范围</h2>
        <div class="scope-grid">
          <div class="scope-card" v-for="item in scopeItems" :key="item.name">
            <div class="scope-icon" :style="{ background: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <h3>{{ item.name }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </section>

      <section v-if="contactLoading || hasContact" class="about-section">
        <h2>联系我们</h2>
        <div v-if="contactLoading" class="contact-info">
          <el-skeleton :rows="3" animated />
        </div>
        <div v-else class="contact-info">
          <div v-if="contact.showEmail === 1 && contact.email" class="contact-item">
            <el-icon><Message /></el-icon>
            <span>邮箱：{{ contact.email }}</span>
          </div>
          <div v-if="contact.showPhone === 1 && contact.phone" class="contact-item">
            <el-icon><Iphone /></el-icon>
            <span>电话：{{ contact.phone }}</span>
          </div>
          <div v-if="contact.showAddress === 1 && contact.address" class="contact-item">
            <el-icon><Location /></el-icon>
            <span>地址：{{ contact.address }}</span>
          </div>
        </div>
      </section>

      <!-- ====== 免责声明（从 API 动态获取） ====== -->
      <section v-if="!disclaimerLoading && disclaimer" id="disclaimer" class="about-section disclaimer-section">
        <h2>免责声明及侵权处理</h2>

        <div v-for="(section, index) in disclaimerSections" :key="index" class="disclaimer-item">
          <h3>{{ section.title }}</h3>
          <p v-html="section.content"></p>
        </div>

        <div class="disclaimer-contact">
          <el-icon><Message /></el-icon>
          <span>📧 侵权投诉/联系邮箱：<a :href="'mailto:' + disclaimer.contactEmail" class="disclaimer-email">{{ disclaimer.contactEmail || '请填写联系邮箱' }}</a></span>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPublicContact } from '@/api/contact'
import { getPublicDisclaimer } from '@/api/disclaimer'

const contact = ref({
  email: '',
  phone: '',
  address: '',
  showEmail: 1,
  showPhone: 1,
  showAddress: 1
})
const contactLoading = ref(true)

const hasContact = computed(() =>
  (contact.value.showEmail === 1 && contact.value.email) ||
  (contact.value.showPhone === 1 && contact.value.phone) ||
  (contact.value.showAddress === 1 && contact.value.address)
)

// ====== 免责声明 ======
const disclaimer = ref(null)
const disclaimerLoading = ref(true)

const disclaimerSections = computed(() => {
  if (!disclaimer.value) return []
  return [
    { title: disclaimer.value.section1Title, content: disclaimer.value.section1Content },
    { title: disclaimer.value.section2Title, content: disclaimer.value.section2Content },
    { title: disclaimer.value.section3Title, content: disclaimer.value.section3Content },
    { title: disclaimer.value.section4Title, content: disclaimer.value.section4Content },
    { title: disclaimer.value.section5Title, content: disclaimer.value.section5Content }
  ]
})

onMounted(async () => {
  try {
    const res = await getPublicContact()
    if (res.data) {
      contact.value = res.data
    }
  } catch {
    // 使用默认值
  } finally {
    contactLoading.value = false
  }

  try {
    const res = await getPublicDisclaimer()
    disclaimer.value = res.data
  } catch {
    // 使用默认值
  } finally {
    disclaimerLoading.value = false
  }
})

const scopeItems = [
  { name: '小学教育', desc: '语文、数学、英语、科学等学科资源', icon: 'Reading', color: '#409eff' },
  { name: '初中教育', desc: '语文、数学、英语、理化等学科资源', icon: 'School', color: '#67c23a' },
  { name: '高中教育', desc: '全学科覆盖，助力高考备考', icon: 'Trophy', color: '#e6a23c' },
  { name: '中考备考', desc: '中考各科真题、模拟题与复习资料', icon: 'TrendCharts', color: '#f56c6c' },
  { name: '高考备考', desc: '高考全科真题、专题训练与冲刺资料', icon: 'Lightning', color: '#909399' }
]
</script>

<style scoped>
.about-content {
  padding: 40px 0;
}

.about-section {
  background: white;
  border-radius: var(--radius-lg);
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: var(--shadow);
}

.about-section h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--bg);
}

.about-section p {
  font-size: 15px;
  color: var(--text-regular);
  line-height: 1.8;
  margin: 0;
}

.scope-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 16px;
}

.scope-card {
  text-align: center;
  padding: 24px 16px;
  background: var(--bg);
  border-radius: var(--radius);
  transition: var(--transition);
}
.scope-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-hover);
}

.scope-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  color: white;
}

.scope-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.scope-card p {
  font-size: 13px;
  color: var(--text-secondary);
}

.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  color: var(--text-regular);
  padding: 12px 16px;
  background: var(--bg);
  border-radius: 8px;
}

/* ====== 免责声明 ====== */
.disclaimer-section {
  border-left: 4px solid var(--primary);
}

.disclaimer-item {
  margin-bottom: 20px;
}
.disclaimer-item:last-of-type {
  margin-bottom: 24px;
}

.disclaimer-item h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.disclaimer-item p {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.8;
  margin: 0;
}

.disclaimer-contact {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: var(--primary-bg);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-regular);
}

.disclaimer-email {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}
.disclaimer-email:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .about-content {
    padding: 16px 0 !important;
  }
  .about-section {
    padding: 20px;
    margin-bottom: 16px;
  }
  .about-section h2 {
    font-size: 18px;
    margin-bottom: 12px;
  }
  .about-section p {
    font-size: 14px;
  }
  .scope-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .scope-card {
    padding: 16px 12px;
  }
  .scope-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
  }
  .scope-card h3 {
    font-size: 14px;
  }
}
</style>
