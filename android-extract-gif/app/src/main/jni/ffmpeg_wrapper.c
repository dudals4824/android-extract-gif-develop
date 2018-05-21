//
// Created by 한승범 on 2018. 5. 16..
//
#include <jni.h>
#include <android/log.h>
#define LOG_TAG "veaver"
#include <dlfcn.h>
#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);
typedef int (*func)(int,char**);


JNIEXPORT jint JNICALL Java_com_naver_hackday_android_1extract_1gif_FFmpegUtils_runFFmpeg
(JNIEnv * env, jobject jobject, jstring fps,jstring filename,jstring outputPath)
{

    //ffmpeg -f image2 -framerate 9 -i frame%04d.jpg -vf scale=531x299,transpose=1 out.gif
    char* a0 = "ffmpeg";
    char* a1 = "-f";
    char* a2 = "image2";
    char* a3 = "-framerate";
    char* a4 = (*env)->GetStringUTFChars(env, fps, 0);
    char* a5 = "-i";
    char* a6 = (*env)->GetStringUTFChars(env, filename, 0);
    char* a7 = (*env)->GetStringUTFChars(env, outputPath, 0);

    char* argv[8];
    void* handle;
    func myFunc;

    argv[0] = a0;
    argv[1] = a1;
    argv[2] = a2;
    argv[3] = a3;
    argv[4] = a4;
    argv[5] = a5;
    argv[6] = a6;
    argv[7] = a7;

    LOGI("call run_ffmpeg");

    handle = dlopen("libffmpeg.so",RTLD_LAZY);

    myFunc = (func)dlsym(handle,"run_ffmpeg");
    myFunc(8,&argv);

    dlclose(handle);
}
