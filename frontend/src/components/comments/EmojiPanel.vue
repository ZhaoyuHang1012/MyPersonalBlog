<template>
  <div class="emoji-panel">
    <div class="emoji-trigger" title="插入表情" @click="open = !open">😊</div>
    <div v-if="open" class="emoji-pop">
      <span v-for="e in emojis" :key="e" class="emoji-item" @click="pick(e)">{{ e }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['pick'])
const open = ref(false)

const emojis = [
  '😀', '😁', '😂', '🤣', '😊', '😍', '😘', '😜', '🤔', '😎',
  '🥳', '😭', '😡', '😅', '😳', '🥰', '😇', '🤗', '🤭', '😱',
  '😴', '🤯', '🙄', '😷', '🤡', '👍', '👎', '🙏', '👏', '💪',
  '🤝', '✌️', '👌', '❤️', '💔', '💯', '🎉', '✨', '🔥', '⭐',
  '🌈', '☀️', '🌙', '❄️', '🌹', '🌸', '🌻', '☕', '🍺', '🎂',
  '🐱', '🐶', '🐰', '🦊', '🚀', '🎈', '🎁', '📚', '💡', '🍀'
]

const pick = (e) => {
  emit('pick', e)
  open.value = false
}
</script>
