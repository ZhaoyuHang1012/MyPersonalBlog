<template>
  <div class="video-thumb">
    <template v-if="!playing">
      <!-- 隐藏的视频源：仅用于捕获第一帧作为封面 -->
      <video
        ref="captureRef"
        class="video-thumb-source"
        :src="src"
        preload="metadata"
        muted
        playsinline
        @loadeddata="seekFrame"
        @seeked="drawFrame"
      ></video>
      <img v-if="poster" :src="poster" class="video-thumb-poster" alt="" />
      <div v-else class="video-thumb-default"><span>🎬 视频</span></div>
      <div class="video-thumb-mask" title="点击播放" @click="playing = true">
        <span class="video-thumb-play">▶</span>
      </div>
    </template>
    <video v-else :src="src" controls autoplay class="video-thumb-player"></video>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  src: { type: String, required: true }
})

const playing = ref(false)
const poster = ref('')
const captureRef = ref(null)

/** 跳到视频前段，触发 seeked 后抓帧 */
const seekFrame = () => {
  const v = captureRef.value
  if (!v) return
  try {
    v.currentTime = Math.min(0.1, (v.duration || 1) / 2)
  } catch (e) {
    /* ignore */
  }
}

/** 抓取当前帧作为封面（失败则保留默认封面） */
const drawFrame = () => {
  const v = captureRef.value
  if (!v || poster.value) return
  try {
    const w = v.videoWidth || 320
    const h = v.videoHeight || 180
    const canvas = document.createElement('canvas')
    canvas.width = w
    canvas.height = h
    canvas.getContext('2d').drawImage(v, 0, 0, w, h)
    poster.value = canvas.toDataURL('image/jpeg', 0.7)
  } catch (e) {
    /* ignore */
  }
}

watch(() => props.src, () => {
  playing.value = false
  poster.value = ''
})
</script>
