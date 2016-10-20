#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <RenderScript.h>

//#include "ScriptC_filter.h"

#define  LOG_TAG    "RS"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace android::RSC;

static char cache_dir[100];

JNIEXPORT void JNICALL
Java_uk_co_wideopentech_edges_MainActivity_InitRenderScriptNative(JNIEnv *env, jclass type,
                                                                  jstring cacheDir_)
{
    const char *cacheDir = env->GetStringUTFChars(cacheDir_, 0);
    strcpy(cache_dir, cacheDir);
    env->ReleaseStringUTFChars(cacheDir_, cacheDir);
}

void filter(void *input, void *output, int width, int height)
{
    sp<RS> rs = new RS();
    rs->init(cache_dir);

    sp<const Element> e = Element::RGBA_8888(rs);
    sp<const Type> t = Type::create(rs, e, width, height, 0);

    sp<Allocation> inputAlloc = Allocation::createTyped(rs, t, RS_ALLOCATION_MIPMAP_NONE,
                                                        RS_ALLOCATION_USAGE_SHARED | RS_ALLOCATION_USAGE_SCRIPT,
                                                        input);
    sp<Allocation> outputAlloc = Allocation::createTyped(rs, t, RS_ALLOCATION_MIPMAP_NONE,
                                                         RS_ALLOCATION_USAGE_SHARED | RS_ALLOCATION_USAGE_SCRIPT,
                                                         output);

    inputAlloc->copy2DRangeFrom(0, 0, width, height, input);
    ScriptC_mono* sc = new ScriptC_harsh_mono(rs);
    sc->forEach_root(inputAlloc, outputAlloc);
    outputAlloc->copy2DRangeTo(0, 0, width, height, output);
}
