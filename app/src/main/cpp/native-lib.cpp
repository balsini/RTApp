#include <jni.h>
#include <string>
#include <sstream>
#include <ctime>
#include <sched.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/resource.h>
#include <unistd.h>
#include <sys/syscall.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_it_sssup_retis_alessiobalsini_rtapp_MainActivity_stringFromJNI(JNIEnv *env, jobject)
{
    std::string hello = "Threads / Processors";

    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_it_sssup_retis_alessiobalsini_rtapp_TimerTaskWorker_getSchedulingInfo(JNIEnv *env, jobject)
{
    sched_param sp;
    int tid;
    int sn;
    int snice;
    cpu_set_t cs;
    std::stringstream ss;

    tid = syscall(SYS_gettid);
    sched_getparam(0, &sp);
    sn = sched_getscheduler(0);
    sched_getaffinity(0, sizeof(cs), &cs);
    snice = getpriority(PRIO_PROCESS, 0);

    ss << "Scheduling info: "
       << "process (" << tid << ") "
       << "scheduler (" << sn << ") "
       << "priority (" << sp.sched_priority << ") "
       << "niceness (" << snice << ") "
       << "affinity (";

    for (unsigned int i=0; i<sizeof(cs); ++i) {
        if (CPU_ISSET(i, &cs))
            ss << " " << i << " ";
    }

    ss << ")";

    return env->NewStringUTF(ss.str().c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_it_sssup_retis_alessiobalsini_rtapp_MainActivity_getSchedulingInfo(JNIEnv *env, jobject)
{
    sched_param sp;
    int tid;
    int sn;
    int snice;
    cpu_set_t cs;
    std::stringstream ss;

    tid = syscall(SYS_gettid);
    sched_getparam(0, &sp);
    sn = sched_getscheduler(0);
    sched_getaffinity(0, sizeof(cs), &cs);
    snice = getpriority(PRIO_PROCESS, 0);

    ss << "Scheduling info: "
       << "process (" << tid << ") "
       << "scheduler (" << sn << ") "
       << "priority (" << sp.sched_priority << ") "
       << "niceness (" << snice << ") "
       << "affinity (";

    for (unsigned int i=0; i<sizeof(cs); ++i) {
        if (CPU_ISSET(i, &cs))
            ss << " " << i << " ";
    }

    ss << ")";

    return env->NewStringUTF(ss.str().c_str());
}

#if 0
extern "C"
JNIEXPORT jstring JNICALL
void Java_it_sssup_retis_alessiobalsini_rtapp_TimerTaskWorker_waitAbsolute(long long absTime_ms)
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
#endif