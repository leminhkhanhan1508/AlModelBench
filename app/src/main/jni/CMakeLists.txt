cmake_minimum_required(VERSION 3.10.2)
project("powerstats")

add_library(powerstats_jni SHARED powerstats_jni.cpp)

find_library(log-lib log)
target_link_libraries(powerstats_jni ${log-lib})
