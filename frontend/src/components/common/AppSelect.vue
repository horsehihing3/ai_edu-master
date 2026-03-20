<template>
  <div class="form-group">
    <label v-if="label">
      {{ label }}
      <span v-if="required" class="required">*</span>
    </label>
    <select
      :class="['form-control', { 'is-error': error }]"
      :value="modelValue"
      :disabled="disabled"
      v-bind="$attrs"
      @change="$emit('update:modelValue', $event.target.value)"
    >
      <option v-if="placeholder" value="" disabled>{{ placeholder }}</option>
      <option
        v-for="opt in options"
        :key="opt.value"
        :value="opt.value"
      >
        {{ opt.label }}
      </option>
    </select>
    <span v-if="error" class="form-error">{{ error }}</span>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  label: { type: String, default: '' },
  options: { type: Array, default: () => [] },
  error: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  required: Boolean,
  disabled: Boolean
})

defineEmits(['update:modelValue'])
defineOptions({ inheritAttrs: false })
</script>

<style scoped lang="scss">
</style>
