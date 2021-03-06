#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")"; pwd)"
source "${SCRIPT_DIR}/common.sh"
TACHYON_WORKER_JAVA_OPTS="${TACHYON_WORKER_JAVA_OPTS:-${TACHYON_JAVA_OPTS}}"

# ${TACHYON_WORKER_MEMORY_SIZE} needs to be set to mount ramdisk
echo "Mounting ramdisk of ${TACHYON_WORKER_MEMORY_SIZE} MB on Worker"
${TACHYON_HOME}/bin/tachyon-mount.sh SudoMount

# Yarn will set LOG_DIRS to point to the Yarn application log directory
YARN_LOG_DIR="$LOG_DIRS"

echo "Formatting Tachyon Worker"

"${JAVA}" -cp "${CLASSPATH}" \
  ${TACHYON_WORKER_JAVA_OPTS} \
  -Dtachyon.home="${TACHYON_HOME}" \
  -Dtachyon.logger.type="WORKER_LOGGER" \
  -Dtachyon.logs.dir="${YARN_LOG_DIR}" \
  tachyon.Format WORKER > "${YARN_LOG_DIR}"/worker.out 2>&1

echo "Starting Tachyon Worker"

"${JAVA}" -cp "${CLASSPATH}" \
  ${TACHYON_WORKER_JAVA_OPTS} \
  -Dtachyon.accesslogger.type="WORKER_ACCESS_LOGGER" \
  -Dtachyon.home="${TACHYON_HOME}" \
  -Dtachyon.logger.type="WORKER_LOGGER" \
  -Dtachyon.logs.dir="${YARN_LOG_DIR}" \
  -Dtachyon.master.hostname="${TACHYON_MASTER_ADDRESS}" \
  tachyon.worker.TachyonWorker >> "${YARN_LOG_DIR}"/worker.out 2>&1
