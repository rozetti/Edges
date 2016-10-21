#include <jni.h>

#include "processor.h"
#include "face_detector.h"
#include "processors.h"
#include "processor_factory.h"

//static rz::processor processor;
rz::face_detector _face_detector;

static rz::processor_factory processor_factory;
static rz::processors processors;

extern "C"
{

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_FaceDetector_SetCascadeDataNative(JNIEnv *env, jclass type,
                                                                jstring data_)
{
    const char *data = env->GetStringUTFChars(data_, 0);

    _face_detector.load_cascade(data);

    env->ReleaseStringUTFChars(data_, data);
}

JNIEXPORT jint JNICALL
Java_uk_co_wideopentech_edges_EdgesConfig_GetParameterNative(JNIEnv *env, jclass type, jint id, jint parameter)
{
    int value = 0;

    auto p = processors.get(id);
    if (p)
    {
        p->get_parameter(parameter, value);
    }

    return value;
}

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_EdgesConfig_SetParameterNative(JNIEnv *env, jobject instance,
                                                        jint id, jint parameter, jint value)
{
    auto p = processors.get(id);
    if (p)
    {
        p->set_parameter(parameter, value);
    }
}

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_SetFrameDataNative(JNIEnv *env, jobject instance,
                                                               jint id, jbyteArray data_) {
    jbyte *data = env->GetByteArrayElements(data_, 0);

    auto p = processors.get(id);
    if (p)
    {
        p->set_frame_data((unsigned char *) data);
    }

    env->ReleaseByteArrayElements(data_, data, 0);
}

JNIEXPORT jint JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_CreateProcessorNative(JNIEnv *env, jclass type_,
                                                                  jint type) {

    auto t = static_cast<enum filter_type_t>(type);

    int id = processors.add(processor_factory.create_processor(t));

    return id;
}

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_SetupProcessorsNative(JNIEnv *env, jobject instance,
                                                                  jint id, jint width, jint height)
{
    auto p = processors.get(id);
    if (p)
    {
        p->setup_processors(width, height);
    }
}

JNIEXPORT jboolean JNICALL
Java_uk_co_wideopentech_edges_EdgeProcessor_ProcessNative(
        JNIEnv *env, jobject thiz,
        jint id,
        jintArray outPixels)
{
    jboolean rv;

    jint *pixels = env->GetIntArrayElements(outPixels, 0);

    auto p = processors.get(id);
    if (p)
    {
        p->process(p->get_type(), (unsigned char *)pixels);
    }

    env->ReleaseIntArrayElements(outPixels, pixels, 0);

    return true;
}

}
