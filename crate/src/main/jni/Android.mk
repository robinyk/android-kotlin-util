LOCAL_PATH := $(call my-dir)

# Build library
include $(CLEAR_VARS)

LOCAL_MODULE    := forger_ndk
LOCAL_SRC_FILES += hidingutil.c
LOCAL_CFLAGS    := -funwind-tables -Wl,--no-merge-exidx-entries
# LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)