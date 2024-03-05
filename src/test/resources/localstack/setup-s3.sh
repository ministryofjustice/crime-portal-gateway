#!/usr/bin/env bash
set -e
export TERM=ansi
export AWS_ACCESS_KEY_ID=key
export AWS_SECRET_ACCESS_KEY=secret
export AWS_DEFAULT_REGION=eu-west-2
export PAGER=

aws --endpoint-url http://localhost:4566 sqs create-queue --queue-name crime-portal-gateway-queue --region eu-west-2
aws --endpoint-url=http://localhost:4566 s3api create-bucket --bucket cpg-bucket --region eu-west-2 --create-bucket-configuration LocationConstraint="eu-west-2"

echo "S3 Configured"
