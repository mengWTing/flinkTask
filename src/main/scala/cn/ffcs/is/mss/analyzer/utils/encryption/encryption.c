#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <stdlib.h>
#include <openssl/aes.h>
#include "cn_ffcs_is_mss_analyzer_utils_encryption_EncryptionUtil.h"

// gcc -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include -I /usr/local/Cellar/openssl/1.0.2r/include -I /Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home/include/darwin -L /usr/local/Cellar/openssl/1.0.2r/lib/ -l ssl encryption.c  -fPIC -shared -o libencryption.so

int main(int argn, char *argv[]){
    return 0;
}

// 字符串转16进制
unsigned char *str2hex_m(char *str)
{
    unsigned char *ret = NULL;
    int str_len = strlen(str);
    int i = 0;
    assert((str_len % 2) == 0);
    ret = (unsigned char *)malloc(str_len / 2);
    for (i = 0; i < str_len; i = i + 2)
    {
        sscanf(str + i, "%2hhx", &ret[i / 2]);
    }
    return ret;
}

char *padding_buf_m(char *buf, int size, int *final_size)
{
    char *ret = NULL;
    int pidding_size = AES_BLOCK_SIZE - (size % AES_BLOCK_SIZE);
    int i;
    *final_size = size + pidding_size;
    ret = (char *)malloc(size + pidding_size);
    memcpy(ret, buf, size);
    if (pidding_size != 0)
    {
        for (i = size; i < (size + pidding_size); i++)
        {
            ret[i] = 0;
        }
    }
    return ret;
}

//加密字符串
void encrpyt_buf_m(char *raw_buf, char **encrpy_buf, int len, unsigned char *key, unsigned char *iv)
{
    AES_KEY aes;
    unsigned char *key_hex = str2hex_m(key);
    unsigned char *iv_hex = str2hex_m(iv);

    AES_set_encrypt_key(key_hex, 128, &aes);
    AES_cbc_encrypt(raw_buf, *encrpy_buf, len, &aes, iv_hex, AES_ENCRYPT);

    free(key_hex);
    free(iv_hex);
}

//解密字符串
void decrpyt_buf_m(char *raw_buf, char **encrpy_buf, int len, char *key, char *iv)
{

    AES_KEY aes;
    unsigned char *key_hex = str2hex_m(key);
    unsigned char *iv_hex = str2hex_m(iv);

    AES_set_decrypt_key(key_hex, 128, &aes);
    
    AES_cbc_encrypt(raw_buf, *encrpy_buf, len, &aes, iv_hex, AES_DECRYPT);
    free(key_hex);
    free(iv_hex);
}

char *key = "e9f288ed8705b0309965f090fba5e510";
char *iv = "e9f288ed8705b0309965f090fba5e510";

/*
 * Class:     cn_ffcs_is_mss_analyzer_utils_encryption_EncryptionUtil
 * Method:    encryption
 * Signature: (Ljava/lang/String;)[I
 */
JNIEXPORT jintArray JNICALL Java_cn_ffcs_is_mss_analyzer_utils_encryption_EncryptionUtil_encryption
  (JNIEnv *env, jobject thisObj, jstring string)
  {
    const char* str = (*env)->GetStringUTFChars(env, string, 0);
    int date_len = strlen(str);

    char *after_padding_buf = NULL;
    int padding_size = 0;
    char *encrypt_buf = NULL;

    // 2
    after_padding_buf = padding_buf_m(str, date_len, &padding_size);

    // 3
    encrypt_buf = (char *)malloc(padding_size);
    encrpyt_buf_m(after_padding_buf, &encrypt_buf, padding_size, key, iv);
    
    jintArray jarr = (*env)->NewIntArray(env, padding_size);
    jint array[padding_size];
    for (size_t i = 0; i < padding_size; i++)
    {
        array[i] = (int) encrypt_buf[i];
    }

    (*env)->SetIntArrayRegion(env, jarr, 0, padding_size, array);
    (*env)->ReleaseStringUTFChars(env, string, str);

    return jarr;
  }

/*
 * Class:     cn_ffcs_is_mss_analyzer_utils_EncryptionUtil
 * Method:    decryption
 * Signature: ([I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cn_ffcs_is_mss_analyzer_utils_encryption_EncryptionUtil_decryption
  (JNIEnv *env, jobject thisObj, jintArray jarr)
{
    jint *arr;
    arr = (*env)->GetIntArrayElements(env, jarr, 0);
    int padding_size = (*env)->GetArrayLength(env, jarr);
    
    char *decrypt_buf = NULL;
    unsigned char encrypt_buf[padding_size];

    for (size_t i = 0; i < padding_size; i++)
    {
        encrypt_buf[i] = arr[i];
    }
    
    decrypt_buf = (char *)malloc(padding_size);
    decrpyt_buf_m(encrypt_buf, &decrypt_buf, padding_size, key, iv);

    (*env)->ReleaseIntArrayElements(env, jarr, arr, 0);
    return (*env)->NewStringUTF(env, decrypt_buf);
}