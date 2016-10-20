cmake_minimum_required(VERSION 3.4.1)

include_directories(${OPENCV_INCLUDE_PATH})
link_directories(${OPENCV_LIB_PATH}/${ANDROID_NDK_ABI_NAME})

add_library(
native-lib SHARED
src/main/cpp/native-lib.cpp
src/main/cpp/processor.cpp
src/main/cpp/face_detector.cpp
#src/main/cpp/renderscript_filters.cpp # crz: RS from NDK is not supported on my API 15 tablet
)

find_library(log-lib log)

target_link_libraries(
native-lib
opencv_core
opencv_imgproc
opencv_java3
opencv_calib3d
opencv_features2d
opencv_flann
opencv_highgui
opencv_imgcodecs
opencv_ml
opencv_objdetect
opencv_photo
opencv_shape
opencv_stitching
opencv_superres
opencv_video
opencv_videoio
opencv_videostab
z
${log-lib}
)
