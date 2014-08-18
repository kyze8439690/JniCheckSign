LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := check-sign
LOCAL_SRC_FILES := check-sign.c md5.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)