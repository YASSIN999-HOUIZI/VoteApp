
cmake_minimum_required(VERSION 3.22.1)

project(“tfhe”)

# node library
##add_library (tfhe SHARED IMPORTED)
#set_target_properties (tfhe PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/lib/libtfhe-spqlios-portable.so)
# android logging
#find_library (log-lib log)
# nodejsmobiledemo library
#add_library (mytest2 SHARED native-lib.cpp)
#target_link_libraries (mytest2 tfhe ${log-lib})

include_directories(${CMAKE_SOURCE_DIR}/include/
        ${CMAKE_SOURCE_DIR}/include/boost/
        ${CMAKE_SOURCE_DIR}/include/boost/random/
        ${CMAKE_SOURCE_DIR}/include/boost/random/detail/)


add_library(tfhe SHARED IMPORTED)
set_target_properties(tfhe PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libtfhe-fftw.so)

add_library(tfhemain SHARED KeysGeneration.cpp DecryptSys.cpp)# EncryptSys.cpp SendData.cpp DecryptSys.cpp KeysGeneration.cpp VerifyCircuit.cpp ReadData.cpp)
find_library (log-lib log)
target_link_libraries( tfhemain tfhe ${log-lib} )
