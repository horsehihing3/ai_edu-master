<template>
  <template v-for="(seg, i) in segments" :key="i">
    <div v-if="seg.type === 'block'" class="katex-block" v-html="seg.html" />
    <span v-else-if="seg.type === 'inline'" v-html="seg.html" />
    <template v-else>{{ seg.content }}</template>
  </template>
</template>

<script setup>
import { computed } from 'vue'
import katex from 'katex'

const props = defineProps({
  text: { type: String, default: '' }
})

// $$...$$ 또는 \[...\] → block, $...$ 또는 \(...\) → inline
// 순서: block 먼저 ($$가 $보다 먼저 매칭되어야 함)
const MATH_PATTERN = /(\$\$[\s\S]*?\$\$|\\\[[\s\S]*?\\\]|\\\([\s\S]*?\\\)|\$[^$\n]+?\$)/g

function renderKatex(inner, displayMode) {
  return katex.renderToString(inner, { throwOnError: false, displayMode })
}

function extractInner(raw) {
  if (raw.startsWith('$$'))  return { inner: raw.slice(2, -2), block: true }
  if (raw.startsWith('\\[')) return { inner: raw.slice(2, -2), block: true }
  if (raw.startsWith('\\(')) return { inner: raw.slice(2, -2), block: false }
  return { inner: raw.slice(1, -1), block: false }
}

const segments = computed(() => {
  const text = props.text || ''
  if (!text) return []

  const result = []
  let lastIndex = 0
  let match

  MATH_PATTERN.lastIndex = 0
  while ((match = MATH_PATTERN.exec(text)) !== null) {
    if (match.index > lastIndex) {
      result.push({ type: 'text', content: text.slice(lastIndex, match.index) })
    }
    const { inner, block } = extractInner(match[0])
    try {
      result.push({ type: block ? 'block' : 'inline', html: renderKatex(inner, block) })
    } catch {
      result.push({ type: 'text', content: match[0] })
    }
    lastIndex = match.index + match[0].length
  }

  if (lastIndex < text.length) {
    result.push({ type: 'text', content: text.slice(lastIndex) })
  }

  return result
})
</script>

<style scoped>
.katex-block {
  display: block;
  text-align: center;
  margin: 0.75em 0;
  overflow-x: auto;
}
</style>
