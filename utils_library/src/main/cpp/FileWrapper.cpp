//
// Created by DonkeyTomy on 2019/5/20.
//

#include "include/FileWrapper.h"

FileWrapper::FileWrapper() {

}


bool FileWrapper::open(const char *filePath, const char *mode) {
    mFile = fopen(filePath, mode);
    return mFile != NULL;
}

int FileWrapper::write(const char *buffer) {
    if (mFile != NULL) {
        size_t size = fwrite(buffer, strlen(buffer), 1, mFile);
        return static_cast<int>(size);
    }
    return -1;
}

int FileWrapper::write(const char *filePath, const char *buffer) {
    close();
    int size = -1;
    if (open(filePath, "r+")) {
        size = write(buffer);
    }
    close();
    return size;
}

bool FileWrapper::close() {
    if (mFile != NULL) {
        bool success =  fclose(mFile) == 0;
        mFile = NULL;
        return success;
    }
    return true;
}

FileWrapper::~FileWrapper() {
    close();
}