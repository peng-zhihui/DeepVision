cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/gen_jniLibs/${ANDROID_ABI})
set(opencv_lib_dir ${CMAKE_SOURCE_DIR}/../lib_opencv_348/src/main/jniLibs)
set(nn_lib_dir ${CMAKE_SOURCE_DIR}/../lib_neural_engine/src/main/gen_jniLibs)


if (ANDROID_ABI MATCHES "^armeabi-v7a$")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfloat-abi=softfp -mfpu=neon ")
elseif (ANDROID_ABI MATCHES "^arm64-v8a")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2 -ftree-vectorize  ")
endif ()


include_directories(
        src/main/cpp
        src/main/cpp/library
        src/main/cpp/utils

)
