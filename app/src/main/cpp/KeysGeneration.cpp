#include <cstdio>
#include <cstdlib>
#include <unistd.h>
#include <cstring>
#include <arpa/inet.h>
#include <jni.h>
#include <strings.h>
#include "tfhe.h"
#include "tfhe_io.h"
#include "tfhe_core.h"
#include <sys/sendfile.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <fstream>
#include <iostream>
#include <cmath>
#include <ctime>
#include <android/log.h>
#include <cstdlib>
#define  LOG_TAG    "TFHE init:"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)



extern "C"
JNIEXPORT jint JNICALL
Java_com_example_m1_MainActivity_init(JNIEnv *env, jobject thiz, jstring SK_PATH,
                                                 jstring PK_PATH, jstring CK_PATH, jstring CD_PATH,
                                                 jint x1,
                                                 jint y1,
                                                 jint z1) {

    const char *Sk_path = env->GetStringUTFChars(SK_PATH, nullptr);
    const char *Pk_path = env->GetStringUTFChars(PK_PATH, nullptr);
    const char *Ck_path = env->GetStringUTFChars(CK_PATH, nullptr);
    const char *CD_path = env->GetStringUTFChars(CD_PATH, nullptr);

    LOGD(" SK path : %s",Sk_path);
    LOGD(" PK path : %s",Pk_path);
    LOGD(" CK path : %s",Ck_path);
    LOGD(" CD path : %s",CD_path);


    LOGD("[+] key generation");

    //generate a keyset
    const int minimum_lambda = 80;
    TFheGateBootstrappingParameterSet* params = new_default_gate_bootstrapping_parameters(minimum_lambda);
    //generate a random key
    uint32_t seed[] = { 314, 1592, 411 };
    tfhe_random_generator_setSeed(seed,3);
    TFheGateBootstrappingSecretKeySet* key = new_random_gate_bootstrapping_secret_keyset(params);

    LweSample* enc_x1 = new_gate_bootstrapping_ciphertext_array(4, params);
    LweSample* enc_y1 = new_gate_bootstrapping_ciphertext_array(4, params);
    LweSample* enc_z1 = new_gate_bootstrapping_ciphertext_array(4, params);
    LweSample* ONE = new_gate_bootstrapping_ciphertext_array(1, params);
    LweSample* ZERO = new_gate_bootstrapping_ciphertext_array(1, params);

    FILE* pk_data = fopen(Pk_path,"wb");
    bootsSymEncrypt(ONE,1, key);
    export_gate_bootstrapping_ciphertext_toFile(pk_data, ONE, params);
    bootsSymEncrypt(ZERO,0, key);
    export_gate_bootstrapping_ciphertext_toFile(pk_data, ZERO, params);
    fclose(pk_data);

    for (int i=0; i<4; i++) {
        bootsSymEncrypt(&enc_x1[i], (x1>>i)&1, key);
    }
    for (int i=0; i<4; i++) {
        bootsSymEncrypt(&enc_y1[i], (y1>>i)&1, key);
    }
    for (int i=0; i<4; i++) {
        bootsSymEncrypt(&enc_z1[i], (z1>>i)&1, key);
    }

    FILE* cloud_data = fopen(CD_path,"wb");
    for (int i=0; i<4; i++)
        export_gate_bootstrapping_ciphertext_toFile(cloud_data, &enc_x1[i], params);
    for (int i=0; i<4; i++)
        export_gate_bootstrapping_ciphertext_toFile(cloud_data, &enc_y1[i], params);
    for (int i=0; i<4; i++)
        export_gate_bootstrapping_ciphertext_toFile(cloud_data, &enc_z1[i], params);
    fclose(cloud_data);

    FILE* cloud_key = fopen(Ck_path,"wb");
    export_tfheGateBootstrappingCloudKeySet_toFile(cloud_key, &key->cloud);
    fclose(cloud_key);

    FILE* SK_pt = fopen(Sk_path,"wb");
    export_tfheGateBootstrappingSecretKeySet_toFile(SK_pt,key);
    fclose(SK_pt);

    return 1;
}


