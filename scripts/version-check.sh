#!/bin/bash

echoerr() { echo "$@" 1>&2; }

if [[ $(cat VERSION.md | md5sum) == $(lein project-version | md5sum) ]];then
    echoerr "You need to bump the project version!"
    exit 255
fi
