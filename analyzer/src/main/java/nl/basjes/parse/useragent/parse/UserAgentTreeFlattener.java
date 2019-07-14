/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2019 Niels Basjes
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

package nl.basjes.parse.useragent.parse;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.analyze.Analyzer;
import nl.basjes.parse.useragent.analyze.MatcherAction;
import nl.basjes.parse.useragent.analyze.treewalker.steps.walk.stepdown.UserAgentGetChildrenVisitor;
import nl.basjes.parse.useragent.parser.UserAgentLexer;
import nl.basjes.parse.useragent.parser.UserAgentParser;
import nl.basjes.parse.useragent.parser.UserAgentParser.Base64Context;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentBlockContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentEntryContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.CommentProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.EmailAddressContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.EmptyWordContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueProductVersionNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyValueVersionNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.KeyWithoutValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.MultipleWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameEmailContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameKeyValueContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameNoVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameUuidContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductNameWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionSingleWordContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.ProductVersionWordsContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.RootTextContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SingleVersionWithCommasContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.SiteUrlContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UserAgentContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.UuIdContext;
import nl.basjes.parse.useragent.parser.UserAgentParser.VersionWordsContext;
import nl.basjes.parse.useragent.utils.AntlrUtils;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.basjes.parse.useragent.UserAgent.SYNTAX_ERROR;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.AGENT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.BASE64;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.COMMENTS;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.EMAIL;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.ENTRY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.KEY;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.KEYVALUE;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.NAME;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.PRODUCT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.TEXT;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.URL;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.UUID;
import static nl.basjes.parse.useragent.parse.AgentPathFragment.VERSION;

// FIXME: Checkstyle cleanup
// CHECKSTYLE.OFF: LineLength

public class UserAgentTreeFlattener implements Serializable {

    private final Analyzer analyzer;

    @FunctionalInterface
    public interface FindMatch {
        /**
         * Traverse the two trees in sync and send info to any matcher that is found.
         * @param matcherTree       The current node in the matcher tree.
         * @param useragentTree     The current node in the parseTree
         */
        void findMatch(MatcherTree matcherTree, ParseTree useragentTree);
    }

    private static final Map<Class, FindMatch> MATCH_FINDERS = new HashMap<>();

    static {
        // In case of a parse error the 'parsed' version of agent can be incomplete
// FIXME (perhaps)       MATCH_FINDERS.put(UserAgentContext.class,                  (mTree, uaTree) -> match(TEXT,      mTree, uaTree, ((ParseTree)uaTree).start.getTokenSource().getInputStream().toString()));

        MATCH_FINDERS.put(RootTextContext.class,                   (mTree, uaTree) -> match(TEXT,      mTree, uaTree));
        MATCH_FINDERS.put(ProductContext.class,                    (mTree, uaTree) -> match(PRODUCT,   mTree, uaTree));
        MATCH_FINDERS.put(CommentProductContext.class,             (mTree, uaTree) -> match(PRODUCT,   mTree, uaTree));
        MATCH_FINDERS.put(ProductNameNoVersionContext.class,       (mTree, uaTree) -> match(PRODUCT,   mTree, uaTree));
        MATCH_FINDERS.put(ProductNameEmailContext.class,           (mTree, uaTree) -> match(NAME,      mTree, uaTree));
        MATCH_FINDERS.put(ProductNameUrlContext.class,             (mTree, uaTree) -> match(NAME,      mTree, uaTree));
        MATCH_FINDERS.put(ProductNameWordsContext.class,           (mTree, uaTree) -> match(NAME,      mTree, uaTree));
        MATCH_FINDERS.put(ProductNameVersionContext.class,         (mTree, uaTree) -> match(NAME,      mTree, uaTree));
        MATCH_FINDERS.put(ProductNameUuidContext.class,            (mTree, uaTree) -> match(NAME,      mTree, uaTree));
        MATCH_FINDERS.put(ProductVersionSingleWordContext.class,   (mTree, uaTree) -> match(VERSION,   mTree, uaTree));
        MATCH_FINDERS.put(SingleVersionContext.class,              (mTree, uaTree) -> match(VERSION,   mTree, uaTree));
        MATCH_FINDERS.put(SingleVersionWithCommasContext.class,    (mTree, uaTree) -> match(VERSION,   mTree, uaTree));
        MATCH_FINDERS.put(ProductVersionWordsContext.class,        (mTree, uaTree) -> match(VERSION,   mTree, uaTree));
        MATCH_FINDERS.put(KeyValueProductVersionNameContext.class, (mTree, uaTree) -> match(VERSION,   mTree, uaTree));
        MATCH_FINDERS.put(CommentBlockContext.class,               (mTree, uaTree) -> match(COMMENTS,  mTree, uaTree));
        MATCH_FINDERS.put(CommentEntryContext.class,               (mTree, uaTree) -> match(ENTRY,     mTree, uaTree));

        MATCH_FINDERS.put(MultipleWordsContext.class,              (mTree, uaTree) -> match(TEXT,      mTree, uaTree));
        MATCH_FINDERS.put(KeyValueContext.class,                   (mTree, uaTree) -> match(KEYVALUE,  mTree, uaTree));
        MATCH_FINDERS.put(KeyWithoutValueContext.class,            (mTree, uaTree) -> match(KEYVALUE,  mTree, uaTree));
        MATCH_FINDERS.put(KeyNameContext.class,                    (mTree, uaTree) -> match(KEY,       mTree, uaTree));
        MATCH_FINDERS.put(KeyValueVersionNameContext.class,        (mTree, uaTree) -> match(VERSION,   mTree, uaTree));
        MATCH_FINDERS.put(VersionWordsContext.class,               (mTree, uaTree) -> match(TEXT,      mTree, uaTree));
        MATCH_FINDERS.put(SiteUrlContext.class,                    (mTree, uaTree) -> match(URL,       mTree, uaTree, ((SiteUrlContext)uaTree).url.getText()));
        MATCH_FINDERS.put(UuIdContext.class,                       (mTree, uaTree) -> match(UUID,      mTree, uaTree, ((UuIdContext)uaTree).uuid.getText()));
        MATCH_FINDERS.put(EmailAddressContext.class,               (mTree, uaTree) -> match(EMAIL,     mTree, uaTree, ((EmailAddressContext)uaTree).email.getText()));
        MATCH_FINDERS.put(Base64Context.class,                     (mTree, uaTree) -> match(BASE64,    mTree, uaTree, ((Base64Context)uaTree).value.getText()));
        MATCH_FINDERS.put(EmptyWordContext.class,                  (mTree, uaTree) -> match(TEXT,      mTree, uaTree, ""));

        MATCH_FINDERS.put(ProductNameKeyValueContext.class,        (mTree, uaTree) ->
            // FIXME: Fakechild = false ...  inform(ctx, "name.(1)keyvalue", ctx.getText(), false);
            match(NAME, mTree, uaTree, "") // FIXME: Fakechild = true....
        );

        MATCH_FINDERS.put(ProductVersionContext.class,              UserAgentTreeFlattener::verifyMatchProductVersion);
        MATCH_FINDERS.put(ProductVersionWithCommasContext.class,    UserAgentTreeFlattener::verifyMatchProductVersion);

        MATCH_FINDERS.put(ProductNameContext.class,                 (mTree, uaTree) -> match(NAME,      mTree, uaTree));

    }


    private static void match(AgentPathFragment fragment, MatcherTree mTree, ParseTree uaTree) {
        match(fragment, mTree, uaTree, null);
    }

    private static final Logger LOG = LoggerFactory.getLogger(UserAgentTreeFlattener.class);


    private static void match(AgentPathFragment fragment, MatcherTree mTree, ParseTree uaTree, String value) {
//        LOG.warn("[match] F:{} | MT:{} | PT:{} | V:{} |", fragment, mTree, AntlrUtils.getSourceText(uaTree), value);

        if (mTree == null) {// || uaTree == null) {
            return; // Nothing can be here.
        }

        // Inform the actions at THIS level that need to be informed.
        Set<MatcherAction> matcherActions = mTree.getActions();
        if (!matcherActions.isEmpty()) {
            String informValue = value == null ? AntlrUtils.getSourceText(uaTree) : value;
            matcherActions.forEach(action ->
                // LOG.info("[Inform] A:{} | MT:{} | V:{} |", action, mTree, informValue);
                action.inform(mTree, uaTree, informValue)
            );
        }

        // For each of the possible child fragments
        for (Map.Entry<AgentPathFragment, Pair<List<MatcherTree>, UserAgentGetChildrenVisitor>> agentPathFragment: mTree.getChildren().entrySet()) {

            // Find the subnodes for which we actually patterns
            List<MatcherTree>               relevantMatcherSubTrees     = agentPathFragment.getValue().getKey();
            Iterator<? extends ParseTree>   children                    = agentPathFragment.getValue().getValue().visit(uaTree);

            for (MatcherTree matcherSubTree: relevantMatcherSubTrees) {
                if (!children.hasNext()) {
                    break;
                }
                ParseTree parseSubTree = children.next();
                if (matcherSubTree == null) {
                    continue;
                }
                if (parseSubTree == null) {
                    continue;
                }
                FindMatch matchFinder = MATCH_FINDERS.get(parseSubTree.getClass());
                if (matchFinder != null) {
                    matchFinder.findMatch(matcherSubTree, parseSubTree);
                } else {
                    LOG.error("No matchFinder for class {}", parseSubTree.getClass().getCanonicalName());
                }
            }
        }
    }

    private static void verifyMatchProductVersion(MatcherTree mTree, ParseTree uaTree) {
        match(VERSION, mTree, uaTree);
//        if (uaTree.getChildCount() != 1) {
//            // These are the specials with multiple children like keyvalue, etc.
//            match(VERSION, mTree, uaTree);
//            return;
//        }
//
//        ParserRuleContext child = (ParserRuleContext)uaTree.getChild(0);
//        // Only for the SingleVersion edition we want to have splits of the version.
//        if (child instanceof SingleVersionContext || child instanceof SingleVersionWithCommasContext) {
//            return;
//        }
//
//        match(VERSION, mTree, child);
    }





































    // FIXME: Later ... private constructor for serialization systems ONLY (like Kyro)
//    private UserAgentTreeFlattener() {
//        analyzer = null;
//    }

    public UserAgentTreeFlattener(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    private boolean verbose = false;

    public void setVerbose(boolean newVerbose) {
        this.verbose = newVerbose;
    }

    public UserAgent parse(String userAgentString) {
        UserAgent userAgent = new UserAgent(userAgentString);
        return parseIntoCleanUserAgent(userAgent);
    }

    public UserAgent parse(UserAgent userAgent) {
        userAgent.reset();
        return parseIntoCleanUserAgent(userAgent);
    }

    /**
     * Parse the useragent and return every part that was found.
     *
     * @param userAgent The useragent instance that needs to be parsed
     * @return If the parse was valid (i.e. were there any parser errors: true=valid; false=has errors
     */
    private UserAgent parseIntoCleanUserAgent(UserAgent userAgent) {
        if (userAgent.getUserAgentString() == null) {
            userAgent.set(SYNTAX_ERROR, "true", 1);
            return userAgent; // Cannot parse this
        }

        // Parse the userAgent into tree
        UserAgentContext userAgentTree = parseUserAgent(userAgent);

        // Match the agent against the
        match(AGENT, analyzer.getMatcherTreeRoot(), userAgentTree, userAgent.getUserAgentString());

//        if (userAgent.hasSyntaxError()) {
//            inform(null, SYNTAX_ERROR, "true");
//        } else {
//            inform(null, SYNTAX_ERROR, "false");
//        }

        return userAgent;
    }

    // =================================================================================

//    private String inform(ParseTree ctx, AgentPathFragment path) {
//        return inform(ctx, path, getSourceText((ParserRuleContext)ctx));
//    }

//    private String inform(ParseTree ctx, AgentPathFragment name, String value) {
//        return inform(ctx, ctx, name, value, false);
//    }

//    private String inform(ParseTree ctx, AgentPathFragment name, String value, boolean fakeChild) {
//        return inform(ctx, ctx, name, value, fakeChild);
//    }

//    private String inform(ParseTree stateCtx, ParseTree ctx, AgentPathFragment name, String value, boolean fakeChild) {
//        AgentPathFragment path = name;
//        if (stateCtx == null) {
//            analyzer.inform(path, value, ctx);
//        } else {
//            State myState = new State(stateCtx, name);
//
//            if (!fakeChild) {
//                state.put(stateCtx, myState);
//            }
//
//            PathType childType;
//            switch (name) {
//                case COMMENTS:
//                    childType = PathType.COMMENT;
//                    break;
//                case VERSION:
//                    childType = PathType.VERSION;
//                    break;
//                default:
//                    childType = PathType.CHILD;
//            }
//
//            path = myState.calculatePath(childType, fakeChild);
//            analyzer.inform(path, value, ctx);
//        }
//        return path.toString();
//    }

//  =================================================================================

    private UserAgentContext parseUserAgent(UserAgent userAgent) {
        String userAgentString = EvilManualUseragentStringHacks.fixIt(userAgent.getUserAgentString());

        CodePointCharStream input = CharStreams.fromString(userAgentString);
        UserAgentLexer lexer = new UserAgentLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        UserAgentParser parser = new UserAgentParser(tokens);

        if (!verbose) {
            lexer.removeErrorListeners();
            parser.removeErrorListeners();
        }
        lexer.addErrorListener(userAgent);
        parser.addErrorListener(userAgent);

        return parser.userAgent();
    }

    //  =================================================================================


//    private void informSubstrings(ParserRuleContext ctx, AgentPathFragment name) {
//        informSubstrings(ctx, name, false);
//    }

//    private void informSubstrings(ParserRuleContext ctx, AgentPathFragment name, boolean fakeChild) {
//        informSubstrings(ctx, name, fakeChild, WordSplitter.getInstance());
//    }

//    private void informSubVersions(ParserRuleContext ctx, AgentPathFragment name) {
//        informSubstrings(ctx, name, false, VersionSplitter.getInstance());
//    }

//    private void informSubstrings(ParserRuleContext ctx, AgentPathFragment name, boolean fakeChild, Splitter splitter) {
//        String text = getSourceText(ctx);
//        String path = inform(ctx, name, text, fakeChild);
//        Set<Range> ranges = analyzer.getRequiredInformRanges(path);
//
//        if (ranges.size() > 4) { // Benchmarks showed this to be the breakeven point. (see below)
//            List<Pair<Integer, Integer>> splitList = splitter.createSplitList(text);
//            for (Range range : ranges) {
//                String value = splitter.getSplitRange(text, splitList, range);
//                if (value != null) {
//                    inform(ctx, ctx, name + range, value, true);
//                }
//            }
//        } else {
//            for (Range range : ranges) {
//                String value = splitter.getSplitRange(text, range);
//                if (value != null) {
//                    inform(ctx, ctx, name + range, value, true);
//                }
//            }
//        }
//    }

    // # Ranges | Direct                   |  SplitList
    // 1        |    1.664 ± 0.010  ns/op  |    99.378 ± 1.548  ns/op
    // 2        |   38.103 ± 0.479  ns/op  |   115.808 ± 1.055  ns/op
    // 3        |  109.023 ± 0.849  ns/op  |   141.473 ± 6.702  ns/op
    // 4        |  162.917 ± 1.842  ns/op  |   166.120 ± 7.166  ns/op  <-- Break even
    // 5        |  264.877 ± 6.264  ns/op  |   176.334 ± 3.999  ns/op
    // 6        |  356.914 ± 2.573  ns/op  |   196.640 ± 1.306  ns/op
    // 7        |  446.930 ± 3.329  ns/op  |   215.499 ± 3.410  ns/op
    // 8        |  533.153 ± 2.250  ns/op  |   233.241 ± 5.311  ns/op
    // 9        |  519.130 ± 3.495  ns/op  |   250.921 ± 6.107  ns/op

}
