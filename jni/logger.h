#ifndef LOGGER_H_
#define LOGGER_H_

#include <android/log.h>

#define  LOG_INFO    "intelligence_info"
#define  LOG_DEBUG    "intelligence_debug"
#define  LOG_ERROR    "intelligence_error"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,  LOG_INFO, __VA_ARGS__);
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_DEBUG, __VA_ARGS__);
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_ERROR, __VA_ARGS__);
#endif
