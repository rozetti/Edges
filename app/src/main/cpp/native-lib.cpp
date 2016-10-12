#include <jni.h>

#include "processor.h"

static rz::processor processor;

extern "C"
{

JNIEXPORT jint JNICALL
Java_uk_co_wideopentech_edges_EdgesConfig_GetParameterNative(JNIEnv *env, jclass type, jint parameter)
{
    int value;

    processor.get_parameter(parameter, value);

    return value;
}

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_EdgesConfig_SetParameterNative(JNIEnv *env, jobject instance,
                                                        jint parameter, jint value)
{
    processor.set_parameter(parameter, value);
}

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_SetupProcessorsNative(JNIEnv *env, jobject instance,
                                                                 jint width, jint height)
{
    processor.setup_processors(width, height);
}

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_SetFrameDataNative(JNIEnv *env, jobject instance,
                                                               jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, 0);
    processor.set_frame_data((unsigned char *)data);
    env->ReleaseByteArrayElements(data_, data, 0);
}

JNIEXPORT jboolean JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_ProcessNative(
        JNIEnv *env, jobject thiz,
        jint type,
        jintArray outPixels)
{
    jboolean rv;

    jint *pixels = env->GetIntArrayElements(outPixels, 0);
    processor.process(type, (unsigned char *)pixels);
    env->ReleaseIntArrayElements(outPixels, pixels, 0);

    return true;
}

}
