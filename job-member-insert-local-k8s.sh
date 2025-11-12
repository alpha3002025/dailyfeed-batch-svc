#!/bin/bash

# Member Insert (local-k8s) Batch Job Runner
# This script executes the jwtKeyInitJob batch job with specific Spring Boot configuration
#
# Usage: ./job-member-insert-local-k8s.sh [profile]
#   profile: Spring profile to use (local-k8s)
#   If not specified, defaults to 'local-k8s'

set -e

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Member Insert (local-k8s) Batch Job${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check and set Spring profile
PROFILE="${1:-local-k8s}"

if [ -z "$1" ]; then
    echo -e "${BLUE}ℹ No profile specified. Using default profile: 'local-k8s'${NC}"
    echo ""
fi

echo -e "${BLUE}Active Profile: ${PROFILE}${NC}"
echo ""

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}Error: gradlew not found in current directory${NC}"
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

# Run the batch job with specific arguments
echo -e "${YELLOW}Starting memberInsertAtLocalK8sUsingNodePortJob...${NC}"
echo ""

# Generate unique job parameter with current timestamp (nanoseconds)
TIMESTAMP=$(date +"%Y-%m-%dT%H:%M:%S.%N")

# Check if the job succeeded
if ./gradlew :dailyfeed-batch:bootRun --args="\
--spring.profiles.active=${PROFILE} \
--spring.batch.job.name=memberInsertAtLocalK8sUsingNodePortJob \
--spring.task.scheduling.enabled=false \
--spring.main.web-application-type=none \
requestedAt=${TIMESTAMP}"; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}Member Insert (local-k8s) Batch Job Completed Successfully!${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}Member Insert (local-k8s) Batch Job Failed!${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi