#include "config.h"

#ifdef ANDROID_V

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "com_github_teocci_opuslib_OpusTool.h"
#include "opusaudio.h"
#include "log.h"
#include <errno.h>

#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    jstrToChar
 * Signature:
 * convert jstring to char*, return the lenth of that string
 */
int jstrToChar(JNIEnv *env, jstring inStr, char *outBuffer) {
    if (inStr == NULL)
        return 0;
    int len = 0;
    len = (*env)->GetStringLength(env, inStr);
    (*env)->GetStringUTFRegion(env, inStr, 0, len, outBuffer);
    LOGD("length of Jstring -- Char*= %d\t-- %s", len, outBuffer);
    //  perror("If String Converting Char error ?:");
    return len;
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    nativeGetString
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_github_teocci_opuslib_OpusTool_nativeGetString
        (JNIEnv *env, jobject obj) {

    char buf[] = "Hello from OpusTool!";
    return (*env)->NewStringUTF(env, buf);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    encode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_encode
        (JNIEnv *env, jobject obj, jstring fileIn, jstring fileOut, jstring option) {

    char bufFileIn[256] = {0}, bufFileOut[256] = {0}, bufFileOp[MAX_CMD_BUFFER] = {0};
    int rst = 1;
    jstrToChar(env, fileIn, bufFileIn);
    jstrToChar(env, fileOut, bufFileOut);
    jstrToChar(env, option, bufFileOp);
    rst = encode(bufFileIn, bufFileOut, bufFileOp);
    return rst;
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    decode
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_decode
        (JNIEnv *env, jobject obj, jstring fileIn, jstring fileOut, jstring option) {

    char bufFileIn[256] = {0}, bufFileOut[256] = {0}, bufFileOp[MAX_CMD_BUFFER] = {0};
    int rst = 1;
    jstrToChar(env, fileIn, bufFileIn);
    jstrToChar(env, fileOut, bufFileOut);
    jstrToChar(env, option, bufFileOp);
    rst = decode(bufFileIn, bufFileOut, bufFileOp);
    return rst;
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    startRecording
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_startRecording
        (JNIEnv *env, jobject obj, jstring fileIn) {
    char bufFileIn[256] = {0};
    jstrToChar(env, fileIn, bufFileIn);
    return startRecording(bufFileIn);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    stopRecording
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_teocci_opuslib_OpusTool_stopRecording
        (JNIEnv *env, jobject obj) {
    return stopRecording();
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    play
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_play
        (JNIEnv *env, jobject obj, jstring fileIn) {
    char bufFileIn[256] = {0};
    jstrToChar(env, fileIn, bufFileIn);

    //return play(bufFileIn);
    //to do:  no need to
    return 0;
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    stopPlaying
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_teocci_opuslib_OpusTool_stopPlaying
        (JNIEnv *env, jobject obj) {
    //	stopPlaying();
    //to do:
    return;
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    writeFrame
 * Signature: (Ljava/nio/ByteBuffer;I)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_writeFrame
        (JNIEnv *env, jobject obj, jobject buffer, jint len) {
    jbyte *bufferBytes = (*env)->GetDirectBufferAddress(env, buffer);
    return writeFrame(bufferBytes, len);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    isOpusFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_isOpusFile
        (JNIEnv *env, jobject obj, jstring fileIn) {
    char bufFileIn[256] = {0};
    jstrToChar(env, fileIn, bufFileIn);

    return isOpusFile(bufFileIn);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    openOpusFile
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_openOpusFile
        (JNIEnv *env, jobject obj, jstring fileIn) {
    char bufFileIn[256] = {0};
    jstrToChar(env, fileIn, bufFileIn);

    return openOpusFile(bufFileIn);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    seekOpusFile
 * Signature: (F)I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_seekOpusFile
        (JNIEnv *env, jobject obj, jfloat position) {
    return seekOpusFile(position);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    closeOpusFile
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_github_teocci_opuslib_OpusTool_closeOpusFile
        (JNIEnv *env, jobject obj) {
    return closeOpusFile();
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    readOpusFile
 * Signature: (Ljava/nio/ByteBuffer;I)V
 */
JNIEXPORT void JNICALL Java_com_github_teocci_opuslib_OpusTool_readOpusFile
        (JNIEnv *env, jobject obj, jobject buffer, jint capacity) {
    jbyte *bufferBytes = (*env)->GetDirectBufferAddress(env, buffer);
    return readOpusFile(bufferBytes, capacity);
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    getFinished
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_getFinished
        (JNIEnv *env, jobject obj) {
    return getFinished();
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    getSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_getSize
        (JNIEnv *env, jobject obj) {
    return getSize();
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    getChannelCount
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_github_teocci_opuslib_OpusTool_getChannelCount
        (JNIEnv *env, jobject obj) {
    return getChannelCount();
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    getPcmOffset
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_github_teocci_opuslib_OpusTool_getPcmOffset
        (JNIEnv *env, jobject obj) {
    return getPcmOffset();
}

/*
 * Class:     com_github_teocci_opuslib_OpusTool
 * Method:    getTotalPcmDuration
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_github_teocci_opuslib_OpusTool_getTotalPcmDuration
        (JNIEnv *env, jobject obj) {
    return getTotalPcmDuration();
}

#ifdef __cplusplus
}
#endif

#endif
