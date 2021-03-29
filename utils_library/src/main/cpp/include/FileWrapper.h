//
// Created by DonkeyTomy on 2019/5/20.
//

#ifndef FILE_WRAPPER_H
#define FILE_WRAPPER_H
extern "C"
{
#include <jni.h>
#include "jni_util.h"
};
#include <string>
#include <stdio.h>
#include <sys/types.h>

typedef unsigned char BYTE;

class FileWrapper
{
public:
    FileWrapper(void);
    virtual ~FileWrapper(void);

public:
    bool open(const char *filePath, const char *mode);
    int write(const char *buffer);
    int write(const char * filePath, const char *buffer);
    bool close();

private:
    FILE *mFile;
};

#endif //FILE_WRAPPER_H
