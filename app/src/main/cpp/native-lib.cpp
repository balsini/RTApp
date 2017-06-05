#include <jni.h>
#include <string>
#include <ctime>

extern "C"
JNIEXPORT jstring JNICALL
Java_it_sssup_retis_alessiobalsini_rtapp_MainActivity_stringFromJNI(JNIEnv *env, jobject)
{
    std::string hello = "Threads / Processors";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
void Java_it_sssup_retis_alessiobalsini_rtapp_BusyWait_waitAbsolute(long long absTime_ms)
{
    /*
    timespec now, wakeup;

    clock_gettime(CLOCK_MONOTONIC, &wakeup);

    wakeup.tv_sec += s;
    wakeup.tv_nsec += ns;

    do {
        clock_gettime(CLOCK_MONOTONIC, &now);
    } while (clock_compare(now, wakeup) < 0) ;
*/

    timespec wakeup;

    //clock_gettime(CLOCK_MONOTONIC, &wakeup);

    wakeup.tv_sec = absTime_ms / 1000;
    //wakeup.tv_nsec = (absTime_ms % 1000) * 1000000;

    clock_nanosleep(CLOCK_MONOTONIC, TIMER_ABSTIME, &wakeup, NULL);
}