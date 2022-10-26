#include "cn_ffcs_is_sa_flink_scenes_xss_xssUtil_XSSInjectionUtil.h"
#include <stdio.h>
#include <strings.h>
#include <errno.h>


JNIEXPORT jint JNICALL Java_cn_ffcs_is_sa_flink_scenes_xss_xssUtil_XSSInjectionUtil_checkXss(JNIEnv *env, jobject thisObj, jstring string)
{

    const char* str = (*env)->GetStringUTFChars(env, string, 0);
    char line[10000];
    strcpy(line, str);
//    printf(line);
//    printf("\n");
    size_t slen = strlen(line);

    int state = 0;

    return libinjection_is_xss(line, slen, state);

}
