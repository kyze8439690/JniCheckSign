#include <jni.h>
#include <android/log.h>
#include <string.h>
#include <md5.h>
#include <stdio.h>

#define LOG(args...)  __android_log_print(ANDROID_LOG_DEBUG, "checkSignature" , ## args)

char* jstringTostring(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "utf-8");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode);
    jsize alen = (*env)->GetArrayLength(env, barr);
    jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, barr, ba, 0);
    return rtn;
}

char* loadSignature(JNIEnv* env, jobject obj)
{
    jclass cls = (*env)->FindClass(env, "android/content/ContextWrapper");
    //this.getPackageManager();
    jmethodID mid = (*env)->GetMethodID(env, cls, "getPackageManager",
            "()Landroid/content/pm/PackageManager;");
    if (mid == NULL) {
        return "";
    }

    jobject pm = (*env)->CallObjectMethod(env, obj, mid);
    if (pm == NULL) {
        return "";
    }

    //this.getPackageName();
    mid = (*env)->GetMethodID(env, cls, "getPackageName", "()Ljava/lang/String;");
    if (mid == NULL) {
        return "";
    }

    jstring packageName = (jstring)(*env)->CallObjectMethod(env, obj, mid);

    // packageManager->getPackageInfo(packageName, GET_SIGNATURES);
    cls = (*env)->GetObjectClass(env, pm);
    mid  = (*env)->GetMethodID(env, cls, "getPackageInfo", "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");

    jobject packageInfo = (*env)->CallObjectMethod(env, pm, mid, packageName, 0x40); //GET_SIGNATURES = 64;

    cls = (*env)->GetObjectClass(env, packageInfo);
    jfieldID fid = (*env)->GetFieldID(env, cls, "signatures", "[Landroid/content/pm/Signature;");
    jobjectArray signatures = (jobjectArray)(*env)->GetObjectField(env, packageInfo, fid);
    jobject sign = (*env)->GetObjectArrayElement(env, signatures, 0);

    cls = (*env)->GetObjectClass(env, sign);
    mid = (*env)->GetMethodID(env, cls, "toCharsString", "()Ljava/lang/String;");
    if(mid == NULL){
        return "";
    }

    jstring signString = (jstring)(*env)->CallObjectMethod(env, sign, mid);

    return jstringTostring(env, signString);
}

char* getSignatureMd5(JNIEnv* env, jobject obj)
{
    char* sign = loadSignature(env, obj);

    MD5_CTX context = { 0 };
    MD5Init(&context);
    MD5Update(&context, sign, strlen(sign));
    unsigned char dest[16] = { 0 };
    MD5Final(dest, &context);

    int i;
    static char destination[32]={0};
    for (i = 0; i < 16; i++) {
        sprintf(destination, "%s%02x", destination, dest[i]);
    }
//    LOG("%s", destination);
    return destination;
}

jstring Java_me_yugy_example_jnichecksign_MyActivity_getToken(JNIEnv* env, jobject obj){
    char* correctSign = "72eed157ba97751805fc2a3dfacea102";
    char* signMd5 = getSignatureMd5(env, obj);
    if(strcmp(signMd5, correctSign) == 0){
        return (*env)->NewStringUTF(env, "Here is the token.");
    }else{
        return (*env)->NewStringUTF(env, "Don't stole token from me!");
    }
}