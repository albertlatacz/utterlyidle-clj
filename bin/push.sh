#!/bin/bash

PROJECT_NAME=utterlyidle-clj
PROJECT_VERSION=${UTTERLYIDLE_CLJ_BUILD_NUMBER}
ARTEFACT=${PROJECT_NAME}-${UTTERLYIDLE_CLJ_BUILD_NUMBER}

S3_CONFIG=~/.s3cfg
echo "[default] \
         access_key = ${AWS_KEY} \
         secret_key = ${AWS_SECRET}" > ${S3_CONFIG}

s3cmd -c ${S3_CONFIG} put \
    target/${ARTEFACT}.jar \
    s3://albertlatacz.published/repo/${PROJECT_NAME}/${PROJECT_NAME}/${PROJECT_VERSION}/${ARTEFACT}.jar

rm ${S3_CONFIG}