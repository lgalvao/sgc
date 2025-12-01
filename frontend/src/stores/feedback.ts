import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface FeedbackMessage {
  title: string;
  message: string;
  variant: 'success' | 'danger' | 'warning' | 'info';
  show: boolean;
  autoHideDelay?: number;
}

export const useFeedbackStore = defineStore('feedback', () => {
  const currentFeedback = ref<FeedbackMessage>({
    title: '',
    message: '',
    variant: 'info',
    show: false
  });

  let timeoutId: number | undefined;

  function show(title: string, message: string, variant: 'success' | 'danger' | 'warning' | 'info' = 'info', autoHideDelay = 5000) {
    currentFeedback.value = {
      title,
      message,
      variant,
      show: true,
      autoHideDelay
    };

    if (timeoutId) clearTimeout(timeoutId);
    
    if (autoHideDelay > 0) {
      timeoutId = window.setTimeout(() => {
        close();
      }, autoHideDelay);
    }
  }

  function close() {
    currentFeedback.value.show = false;
    if (timeoutId) clearTimeout(timeoutId);
  }

  return {
    currentFeedback,
    show,
    close
  };
});
