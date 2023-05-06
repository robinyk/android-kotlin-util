#ifndef __HIDINGUTIL_H__
#define __HIDINGUTIL_H__
#include <jni.h>
#include "../../../../../../../AppData/Local/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/jni.h"

// underscores are reserved characters in JNI referring to package boundaries.
extern "C"
jstring Java_com_ownapp_blacksmith_Forger_mold(JNIEnv* env, jobject obj, jstring _receiver);
extern "C"
jstring Java_com_ownapp_blacksmith_Forger_unmold(JNIEnv* env, jobject obj, jstring _receiver);
extern "C"
jstring Java_com_ownapp_blacksmith_Forger_forge(JNIEnv* env, jobject obj, jstring _receiver);
extern "C"
jstring Java_com_ownapp_blacksmith_Forger_unforge(JNIEnv* env, jobject obj, jstring _receiver);
#endif //__HIDINGUTIL_H__