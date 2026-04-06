#!/bin/bash

skaffold delete
sh ./scripts/production/prepare-k8s.sh
skaffold dev