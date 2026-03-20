<template>
  <div class="form-group">
    <label v-if="label">
      {{ label }}
      <span v-if="required" class="required">*</span>
    </label>
    <input
      :class="['form-control', { 'is-error': error }]"
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :value="modelValue"
      v-bind="$attrs"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <span v-if="error" class="form-error">{{ error }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  label: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  type: { type: String, default: 'text' },
  error: { type: String, default: '' },
  required: Boolean,
  disabled: Boolean
})

defineEmits(['update:modelValue'])
defineOptions({ inheritAttrs: false })
</script>

<style scoped lang="scss">
</style>
