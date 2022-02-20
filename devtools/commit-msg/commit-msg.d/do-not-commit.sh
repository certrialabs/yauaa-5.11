#!/bin/bash
#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2022 Niels Basjes
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
#


# Fail if there is a DONOTCOMMIT in one of the files to be committed
git-diff-index -p -M --cached HEAD -- | grep '^+' | grep -F DONOTCOMMIT && die "Blocking commit because string DONOTCOMMIT detected in patch: \n$(git diff --cached | fgrep -B2 -A2 DONOTCOMMIT)\n"
