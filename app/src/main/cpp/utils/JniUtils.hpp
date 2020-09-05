#ifndef SNPE_JNI_TEST_JNIUTILS_H
#define SNPE_JNI_TEST_JNIUTILS_H

#include <jni.h>
#include <android/log.h>

#include <string>
#include <iostream>

#define LOG_TAG "daguerre::"

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  ASSERT(cond, fmt, ...)                               \
  if (!(cond)) {                                              \
    __android_log_assert(#cond, LOG_TAG, fmt, ##__VA_ARGS__); \
  }


#endif //SNPE_JNI_TEST_JNIUTILS_H
