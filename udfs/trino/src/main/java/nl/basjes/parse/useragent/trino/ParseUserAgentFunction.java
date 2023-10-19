/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2023 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.parse.useragent.trino;

import io.airlift.slice.Slice;
import io.trino.spi.block.Block;
import io.trino.spi.block.MapBlockBuilder;
import io.trino.spi.function.Description;
import io.trino.spi.function.ScalarFunction;
import io.trino.spi.function.SqlType;
import io.trino.spi.type.MapType;
import io.trino.spi.type.StandardTypes;
import io.trino.spi.type.TypeOperators;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import nl.basjes.parse.useragent.Version;

import java.util.Map;

import static io.trino.spi.type.VarcharType.VARCHAR;

public final class ParseUserAgentFunction {

    private ParseUserAgentFunction() {
    }

    // NOTE: We currently cannot make an instance with only the wanted fields.
    //       We only know the required parameters the moment the call is done.
    //       At that point it is too late to create an optimized instance.
    private static ThreadLocal<UserAgentAnalyzer> threadLocalUserAgentAnalyzer =
        ThreadLocal.withInitial(() ->
            UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(10000)
                .immediateInitialization()
                .build());

    @ScalarFunction("parse_user_agent")
    @Description("Tries to parse and analyze the provided useragent string and extract as many attributes " +
        "as possible. Uses Yauaa (Yet Another UserAgent Analyzer) version " + Version.PROJECT_VERSION + ". " +
        "See https://yauaa.basjes.nl/udf/trino/ for documentation.")
    @SqlType("map(varchar, varchar)")
    public static Block parseUserAgent(@SqlType(StandardTypes.VARCHAR) Slice userAgentSlice) throws IllegalArgumentException {
        String userAgentStringToParse = null;
        if (userAgentSlice != null) {
            userAgentStringToParse = userAgentSlice.toStringUtf8();
        }

        UserAgentAnalyzer userAgentAnalyzer = threadLocalUserAgentAnalyzer.get();

        UserAgent userAgent = userAgentAnalyzer.parse(userAgentStringToParse);
        Map<String, String> resultMap = userAgent.toMap(userAgentAnalyzer.getAllPossibleFieldNamesSorted());

        MapType mapType = new MapType(VARCHAR, VARCHAR, new TypeOperators());

        MapBlockBuilder blockBuilder = mapType.createBlockBuilder(null, resultMap.size());
        blockBuilder.buildEntry((keyBuilder, valueBuilder) ->
            resultMap.forEach((key, value) -> {
                VARCHAR.writeString(keyBuilder, key);
                VARCHAR.writeString(valueBuilder, value);
            })
        );

        return mapType.getObject(blockBuilder.build(), blockBuilder.getPositionCount() - 1);
    }
}

