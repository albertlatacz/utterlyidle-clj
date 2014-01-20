#!/bin/bash

PROJECT_NAME=utterlyidle-clj
PROJECT_VERSION=${UTTERLYIDLE_CLJ_BUILD_NUMBER}
ARTEFACT=${PROJECT_NAME}-${PROJECT_VERSION}

S3_CONFIG=~/.s3cfg
printf "[default]
     access_key = ${AWS_KEY}
     secret_key = ${AWS_SECRET}" > ${S3_CONFIG}

lein2 test
lein2 uberjar
lein2 pom
mv pom.xml target/pom.xml

function s3-deploy-maven {
    s3cmd -c ${S3_CONFIG} put \
        target/${1} \
        s3://albertlatacz.published/repo/${PROJECT_NAME}/${PROJECT_NAME}/${PROJECT_VERSION}/${1}

    md5sum target/${1} | cut -d" " -f1,2 > target/${1}.md5
    s3cmd -c ${S3_CONFIG} put \
        target/${1}.md5 \
        s3://albertlatacz.published/repo/${PROJECT_NAME}/${PROJECT_NAME}/${PROJECT_VERSION}/${1}.md5

    sha1sum target/${1} | cut -d" " -f1,2 > target/${1}.sha1
    s3cmd -c ${S3_CONFIG} put \
        target/${1}.sha1 \
        s3://albertlatacz.published/repo/${PROJECT_NAME}/${PROJECT_NAME}/${PROJECT_VERSION}/${1}.sha1
}

s3-deploy-maven ${ARTEFACT}.jar
s3-deploy-maven pom.xml

rm ${S3_CONFIG}