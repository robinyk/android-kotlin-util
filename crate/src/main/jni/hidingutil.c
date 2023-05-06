#include <string.h>
#include <jni.h>
#include "Base64Util.h"
#include "Base64Util.c"
//#include <android/log.h>

//#define Log(...) __android_log_print(ANDROID_LOG_DEBUG, "NDK", __VA_ARGS__)

/* Macro definitions */
#define CHARSIZE      1024

static unsigned char passwordKey[] = "K76NhYbhtxuRvaNSXqHQ2JcWnC6jKdyye57QG4KaRrHpJkGBkYmCjCFTbhtEGSUhY5FxhjfKupzrEyJntb8gzPqa8hGSQxcF5bBKuwZpyyvvnWJg8RpTBqrtFZ9xnjr3";

void xor_value_with_key(const char* value, char* xorOutput)
{
    int i = 0;
    while(value[i] != '\0')
    {
        int offset = i % sizeof(passwordKey);
        xorOutput[i] = value[i] ^ passwordKey[offset];
        i++;
    }
}

jstring hide(JNIEnv* env, jstring javaString)
{
    const char *nativeString = (*env)->GetStringUTFChars(env, javaString, 0);

    char xorOutput[BUFFFERLEN + 1] = "";
    xor_value_with_key(nativeString, xorOutput);

    char encodedOutput[BUFFFERLEN + 1] = "";

    Base64Encode(xorOutput, encodedOutput, BUFFFERLEN);

    (*env)->ReleaseStringUTFChars(env, javaString, nativeString);

    jstring text = (*env)->NewStringUTF(env, encodedOutput);

    if(text == NULL)
        return "";

    return text;
}

jstring unhide(JNIEnv* env, jstring javaString)
{
    const char *nativeString = (*env)->GetStringUTFChars(env, javaString, 0);

    char decodedOutput[BUFFFERLEN + 1] = "";

    Base64Decode(nativeString, decodedOutput, BUFFFERLEN);

    char xorOutput[BUFFFERLEN + 1] = "";
    xor_value_with_key(decodedOutput, xorOutput);

    (*env)->ReleaseStringUTFChars(env, javaString, nativeString);

    jstring text = (*env)->NewStringUTF(env, xorOutput);

    if(text == NULL)
        return "";

    return text;
}

jstring Java_com_ownapp_blacksmith_Forger_mold(JNIEnv *env, jobject obj, jstring text)
{
    if(text == NULL) text = "";
    text = hide(env, text);
    if(text == NULL) text = "";
    return text;
}

jstring Java_com_ownapp_blacksmith_Forger_unmold(JNIEnv *env, jobject obj, jstring text)
{
    if(text == NULL) text = "";
    text = unhide(env, text);
    if(text == NULL) text = "";
    return text;
}

jstring Java_com_ownapp_blacksmith_Forger_forge(JNIEnv *env, jobject obj, jstring text)
{
    if(text == NULL)
        return "";

    jstring hiddenString = hide(env, text);

    if(hiddenString == Java_com_ownapp_blacksmith_Forger_unmold(env, obj, hiddenString))
        return hiddenString;

    const char *textCharPtr = (*env)->GetStringUTFChars(env, text, 0);
    char textChar[CHARSIZE + 1] = "";
    char resultChar[CHARSIZE + 1] = "";

    strcpy(textChar, textCharPtr);

    while(strlen(textChar) != 0)
    {
        jstring hidden = Java_com_ownapp_blacksmith_Forger_mold(env, obj, (*env)->NewStringUTF(env, textChar));
        const char *hiddenCharPtr = (*env)->GetStringUTFChars(env, hidden, 0);

        strcat(resultChar, hiddenCharPtr);
        strcat(resultChar, ",");

        jstring unhidden = Java_com_ownapp_blacksmith_Forger_unmold(env, obj, hidden);
        const char *unhiddenCharPtr = (*env)->GetStringUTFChars(env, unhidden, 0);
        char unhiddenChar[CHARSIZE + 1] = "";

        strcpy(unhiddenChar, unhiddenCharPtr);

        int i = 0;

        while(strlen(textChar) != 0 && textChar[i] == unhiddenChar[i])
        {
            memmove(&textChar[i], &textChar[i + 1], strlen(textChar) - i);
            memmove(&unhiddenChar[i], &unhiddenChar[i + 1], (strlen(unhiddenChar) - i) + 1);
        }

        (*env)->ReleaseStringUTFChars(env, hidden, hiddenCharPtr);
        (*env)->ReleaseStringUTFChars(env, unhidden, unhiddenCharPtr);
    }

    (*env)->ReleaseStringUTFChars(env, text, textCharPtr);

    return (*env)->NewStringUTF(env, resultChar);
}

jstring Java_com_ownapp_blacksmith_Forger_unforge(JNIEnv *env, jobject obj, jstring text)
{
    if(text == NULL)
        return "";

    const char *textCharPtr = (*env)->GetStringUTFChars(env, text, 0);
    char textChar[CHARSIZE + 1] = "";
    char resultChar[CHARSIZE + 1] = "";

    strcpy(textChar, textCharPtr);

    char *tokenCharPtr = strtok(textChar, ",");

    while(tokenCharPtr != NULL)
    {
        jstring unhidden = Java_com_ownapp_blacksmith_Forger_unmold(env, obj, (*env)->NewStringUTF(env, tokenCharPtr));
        const char *unhiddenCharPtr = (*env)->GetStringUTFChars(env, unhidden, 0);

        strcat(resultChar, unhiddenCharPtr);

        (*env)->ReleaseStringUTFChars(env, unhidden, unhiddenCharPtr);

        tokenCharPtr = strtok(NULL, ",");
    }

    (*env)->ReleaseStringUTFChars(env, text, textCharPtr);

    return (*env)->NewStringUTF(env, resultChar);
}