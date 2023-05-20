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
#define  LOG_TAG    "Tfhe:"
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jint JNICALL
Java_com_loginid_cryptodid_protocols_TFHE_EncryptPrivate(JNIEnv *env, jobject thiz, jint claim, jstring SK_PATH, jstring DataPath) {

    const char *Sk_path = env->GetStringUTFChars(SK_PATH, NULL);
    const char *data_path = env->GetStringUTFChars(DataPath, NULL);

    LOGD(" SK path : %s",Sk_path);
    LOGD(" PK path : %s",data_path);

    LOGD("[+]reading the key...\n");
    //reads the cloud key from file
    FILE* secret_key = fopen(Sk_path,"rb");
    TFheGateBootstrappingSecretKeySet* key = new_tfheGateBootstrappingSecretKeySet_fromFile(secret_key);
    fclose(secret_key);

    //if necessary, the params are inside the key
    const TFheGateBootstrappingParameterSet* params = key->params;
    printf("[+]reading the input...\n");
    // Encrypt the number

    int16_t plaintext = claim;
    LweSample* ciphertext = new_gate_bootstrapping_ciphertext_array(16, params);
    for (int i=0; i<16; i++) {
        bootsSymEncrypt(&ciphertext[i], (plaintext>>i)&1, key);
    }

    FILE* cloud_data = fopen(data_path,"wb");
    for (int i=0; i<16; i++)
        export_gate_bootstrapping_ciphertext_toFile(cloud_data, &ciphertext[i], params);
    fclose(cloud_data);

    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_loginid_cryptodid_protocols_TFHE_Encrypt(JNIEnv *env, jobject thiz, jint claim,
                                                  jstring OnePath, jstring ZeroPath,
                                                  jstring PkPath, jstring dataPath) {
    const char *One_path = env->GetStringUTFChars(OnePath, NULL);
    const char *Zero_path = env->GetStringUTFChars(ZeroPath, NULL);
    const char *data_path = env->GetStringUTFChars(dataPath, NULL);
    const char *PK_path = env->GetStringUTFChars(PkPath, NULL);

    LOGD(" One path : %s",One_path);
    LOGD(" Zero path : %s",Zero_path);
    LOGD(" Claim path : %s",data_path);
    LOGD(" Claim path : %s",PK_path);

    FILE* cloud_key = fopen(PK_path,"rb");
    TFheGateBootstrappingCloudKeySet* bk = new_tfheGateBootstrappingCloudKeySet_fromFile(cloud_key);
    fclose(cloud_key);

    //if necessary, the params are inside the key
    const TFheGateBootstrappingParameterSet* params = bk->params;
    /*read the data (01) from the file*/
    LweSample* ZERO = new_gate_bootstrapping_ciphertext(params);
    LweSample* ONE = new_gate_bootstrapping_ciphertext(params);


    FILE* data_0 = fopen(Zero_path,"rb");
    import_gate_bootstrapping_ciphertext_fromFile(data_0, ZERO, params);
    fclose(data_0);
    FILE* data_1 = fopen(One_path,"rb");
    import_gate_bootstrapping_ciphertext_fromFile(data_1, ONE, params);
    fclose(data_1);

    int k[16]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    /*convert n to binary and store the bits in k*/
    int n = claim;
    for(int i=0;n>0;i++)
    {
        k[i]=n%2;
        n=n/2;
    }

    /* encrypt k*/
    LweSample* claim_cipher = new_gate_bootstrapping_ciphertext_array(16,params);
    const LweParams *in_out_params = params->in_out_params;
    for (int i=0;i<16;i++){
        if (k[i] == 0){
            lweCopy(&claim_cipher[i],ZERO,in_out_params);
        }
        else if (k[i] == 1)
        {
            lweCopy(&claim_cipher[i],ONE,in_out_params);
        }
        else printf("[-] error bits");
    }

    FILE* cloud_data = fopen(data_path,"wb");
    for (int i=0; i<16; i++)
        export_gate_bootstrapping_ciphertext_toFile(cloud_data, &claim_cipher[i], params);
    fclose(cloud_data);

    return 0;
}