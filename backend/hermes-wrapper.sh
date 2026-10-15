#!/bin/sh
# Runs the local Hermes CLI through its own virtualenv, regardless of PATH.
exec /home/userland/.hermes/hermes-agent/.venv/bin/python /home/userland/.hermes/hermes-agent/hermes "$@"
