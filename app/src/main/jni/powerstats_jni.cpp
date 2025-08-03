#include <jni.h>
#include <string>
#include <android/hardware/power/stats/1.0/IPowerStats.h>

using namespace android::hardware::power::stats::V1_0;

extern "C"
JNIEXPORT jstring JNICALL
java_com_example_aimodelbench_MainActivity_getRailInfo(JNIEnv *env, jobject /*thiz*/) {
    android::sp<IPowerStats> service = IPowerStats::getService();
    if (service == nullptr) {
        return env->NewStringUTF("No IPowerStats service found");
    }

    std::string result;
    service->getRailInfo({}, [&](auto status, const auto &infos) {
        if (status == Status::SUCCESS) {
            for (const auto &info : infos) {
                result += "Rail: ";
                result += info.railName.c_str();
                result += "\n";
            }
        } else {
            result += "Failed to get Rail Info\n";
        }
    });

    return env->NewStringUTF(result.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
java_com_example_aimodelbench_MainActivity_getEnergyData(JNIEnv *env, jobject /*thiz*/) {
    android::sp<IPowerStats> service = IPowerStats::getService();
    if (service == nullptr) {
        return env->NewStringUTF("No IPowerStats service found");
    }

    std::string result;
    service->getEnergyData({}, [&](auto status, const auto &dataList) {
        if (status == Status::SUCCESS) {
            for (const auto &data : dataList) {
                result += "Rail ID: " + std::to_string(data.index) + " Energy: " +
                          std::to_string(data.energy) + "\n";
            }
        } else {
            result += "Failed to get Energy Data\n";
        }
    });

    return env->NewStringUTF(result.c_str());
}
