#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=foobar
export AWS_SECRET_ACCESS_KEY=foobar
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

aws --endpoint-url http://localhost:4566 sqs create-queue --queue-name crime-portal-gateway-queue
aws --endpoint-url=http://localhost:4566 s3 mb s3://cpg-bucket

echo "S3 Configured"
