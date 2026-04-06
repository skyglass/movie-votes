#!/bin/bash

skaffold delete
sh ./scripts/local/prepare-k8s.sh
skaffold dev