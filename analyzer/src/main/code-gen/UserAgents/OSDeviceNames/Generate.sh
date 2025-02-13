#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2023 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
TARGETDIR=$(cd "${SCRIPTDIR}/../../../resources/UserAgents" || exit 1; pwd)

INPUT="${SCRIPTDIR}/OperatingSystemDeviceNames.csv"
OUTPUT="${TARGETDIR}/OperatingSystemDeviceNames.yaml"

[ "$1" = "--force" ] && rm "${OUTPUT}"

if [ "Generate.sh" -ot "${OUTPUT}" ]; then
    if [ "${INPUT}" -ot "${OUTPUT}" ]; then
        echo "Up to date: ${OUTPUT}";
        exit;
    fi
fi

echo "Generating: ${OUTPUT}";

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "#"
echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2023 Niels Basjes"
echo "#"
echo "# Licensed under the Apache License, Version 2.0 (the \"License\");"
echo "# you may not use this file except in compliance with the License."
echo "# You may obtain a copy of the License at"
echo "#"
echo "# https://www.apache.org/licenses/LICENSE-2.0"
echo "#"
echo "# Unless required by applicable law or agreed to in writing, software"
echo "# distributed under the License is distributed on an \"AS IS\" BASIS,"
echo "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied."
echo "# See the License for the specific language governing permissions and"
echo "# limitations under the License."
echo "#"
echo "config:"

echo "- set:"
echo "    name: 'UnwantedVersionValues'"
echo "    merge:"
echo "    - 'CPUArchitectures'"
echo "    values:"
echo "    - 'sun4u'"
echo "    # Tags indicating the SGI IRIX Platform used."
echo "    - 'IP4'"
echo "    - 'IP6'"
echo "    - 'IP10'"
echo "    - 'IP12'"
echo "    - 'IP14'"
echo "    - 'IP17'"
echo "    - 'IP19'"
echo "    - 'IP20'"
echo "    - 'IP21'"
echo "    - 'IP22'"
echo "    - 'IP24'"
echo "    - 'IP25'"
echo "    - 'IP26'"
echo "    - 'IP27'"
echo "    - 'IP28'"
echo "    - 'IP30'"
echo "    - 'IP31'"
echo "    - 'IP32'"
echo "    - 'IP34'"
echo "    - 'IP35'"
echo "    - 'IP53'"
echo ""
echo "- set:"
echo "    name: 'OSPatterns'"
echo "    values:"
grep -F -v '#' "${INPUT}" | grep . | sed 's/ *|/|/g;s/| */|/g' | while read -r line
do
    ospattern=$(echo "${line}" | cut -d'|' -f1)
    echo "    - '${ospattern}'"
done
echo ""

grep -F -v '#' "${INPUT}" | grep . | sed 's/ *|/|/g;s/| */|/g' | while read -r line
do
    ospattern=$(echo "${line}" | cut -d'|' -f1)
    osname=$(   echo "${line}" | cut -d'|' -f2)
    devclass=$( echo "${line}" | cut -d'|' -f3)
    devname=$(  echo "${line}" | cut -d'|' -f4)
    devbrand=$( echo "${line}" | cut -d'|' -f5)
    osclass=$(  echo "${line}" | cut -d'|' -f6)

    ospatternWords=$(echo "${ospattern}" | wc -w)
echo "
# =======================================================================
# ${ospattern}

- matcher:
    variable:
    - 'OS                                  :agent.(1)product.(1-2)comments.entry.(1-2)product.name=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      112 :\"${devclass}\"'
    - 'DeviceName                          :      112 :\"${devname}\"'
    - 'DeviceBrand                         :      112 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      150 :CleanVersion[@OS^.(1)version!?UnwantedVersionValues]'

- matcher:
    variable:
    - 'OS                                  :agent.(1)product.(1-2)comments.entry.(1-2)product.name[2]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      111 :\"${devclass}\"'
    - 'DeviceName                          :      111 :\"${devname}\"'
    - 'DeviceBrand                         :      111 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      150 :CleanVersion[@OS^.(1)version!?UnwantedVersionValues]'

- matcher:
    variable:
    - 'OS                                  :agent.(1)product.(1-2)comments.entry.(1-2)product.name=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      112 :\"${devclass}\"'
    - 'DeviceName                          :      112 :\"${devname}\"'
    - 'DeviceBrand                         :      112 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      151 :CleanVersion[@OS^.(2)version!?UnwantedVersionValues]'

# Exact match
- matcher:
    require:
    - 'agent.product.(1-2)comments.entry=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      111 :\"${devclass}\"'
    - 'DeviceName                          :      111 :\"${devname}\"'
    - 'DeviceBrand                         :      111 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      151 :\"${osclass}\"'
    - 'OperatingSystemName                 :      151 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'
"

case ${ospatternWords} in
1)
echo "
# One word
- matcher:
    require:
    - 'agent.product.(1-2)comments.entry[1-1]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      108 :\"${devclass}\"'
    - 'DeviceName                          :      108 :\"${devname}\"'
    - 'DeviceBrand                         :      108 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      148 :\"${osclass}\"'
    - 'OperatingSystemName                 :      148 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'

- matcher:
    require:
    - 'agent.product.(1-2)comments.entry[2-2]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      107 :\"${devclass}\"'
    - 'DeviceName                          :      107 :\"${devname}\"'
    - 'DeviceBrand                         :      107 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      147 :\"${osclass}\"'
    - 'OperatingSystemName                 :      147 :\"${osname}\"'
    - 'OperatingSystemVersion              :      148 :\"<<<null>>>\"'
"
;;

2)
echo "
# Two words
- matcher:
    require:
    - 'agent.product.(1-2)comments.entry[1-2]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      109 :\"${devclass}\"'
    - 'DeviceName                          :      109 :\"${devname}\"'
    - 'DeviceBrand                         :      109 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      149 :\"${osclass}\"'
    - 'OperatingSystemName                 :      149 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'
- matcher:
    require:
    - 'agent.product.(1-2)comments.entry[2-3]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      109 :\"${devclass}\"'
    - 'DeviceName                          :      109 :\"${devname}\"'
    - 'DeviceBrand                         :      109 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      149 :\"${osclass}\"'
    - 'OperatingSystemName                 :      149 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'
"
;;

3)
echo "
# Three words
- matcher:
    require:
    - 'agent.product.(1-2)comments.entry[1-3]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      110 :\"${devclass}\"'
    - 'DeviceName                          :      110 :\"${devname}\"'
    - 'DeviceBrand                         :      110 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'

- matcher:
    require:
    - 'agent.product.(1-2)comments.entry[2-4]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      110 :\"${devclass}\"'
    - 'DeviceName                          :      110 :\"${devname}\"'
    - 'DeviceBrand                         :      110 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'
"
;;
esac

echo "
- matcher:
    variable:
    - 'OS                                  :agent.product.name=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      111 :\"${devclass}\"'
    - 'DeviceName                          :      111 :\"${devname}\"'
    - 'DeviceBrand                         :      111 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      150 :CleanVersion[@OS^.(1)version!?UnwantedVersionValues]'

- matcher:
    variable:
    - 'OS                                  :agent.product.name=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      111 :\"${devclass}\"'
    - 'DeviceName                          :      111 :\"${devname}\"'
    - 'DeviceBrand                         :      111 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      150 :\"${osclass}\"'
    - 'OperatingSystemName                 :      150 :\"${osname}\"'
    - 'OperatingSystemVersion              :      151 :CleanVersion[@OS^.(2)version!?UnwantedVersionValues]'

- matcher:
    require:
    - 'agent.product.name[-1]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      111 :\"${devclass}\"'
    - 'DeviceName                          :      111 :\"${devname}\"'
    - 'DeviceBrand                         :      111 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      152 :\"${osclass}\"'
    - 'OperatingSystemName                 :      152 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'

- matcher:
    require:
    - 'agent.product.name[-2]=\"${ospattern}\"'
    extract:
    - 'DeviceClass                         :      112 :\"${devclass}\"'
    - 'DeviceName                          :      112 :\"${devname}\"'
    - 'DeviceBrand                         :      112 :\"${devbrand}\"'
    - 'OperatingSystemClass                :      153 :\"${osclass}\"'
    - 'OperatingSystemName                 :      153 :\"${osname}\"'
    - 'OperatingSystemVersion              :      149 :\"<<<null>>>\"'

"
done

) >"${OUTPUT}"
